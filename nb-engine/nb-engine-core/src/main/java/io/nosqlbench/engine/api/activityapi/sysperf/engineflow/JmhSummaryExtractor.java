/*
 * Copyright (c) 2025 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.activityapi.sysperf.engineflow;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extracts and summarizes JMH benchmark results from large log files.
 * <p>
 * Efficiently reads only the summary table from the end of JMH output files,
 * avoiding memory issues with multi-million line logs.
 */
@Command(
    name = "JmhSummaryExtractor",
    mixinStandardHelpOptions = true,
    description = "Extract and summarize JMH benchmark results from log files.",
    version = "1.0"
)
public class JmhSummaryExtractor implements Callable<Integer> {

    @Parameters(index = "0", description = "JMH output log file to process.")
    private Path logFile;

    @Option(
        names = {"-g", "--group-by"},
        description = "Group results by these parameters (comma-separated). Example: benchmark,fieldCount",
        split = ","
    )
    private List<String> groupBy = List.of("benchmark");

    @Option(
        names = {"-c", "--compare"},
        description = "Compare results across this parameter. Shows side-by-side comparison."
    )
    private String compareBy;

    @Option(
        names = {"-f", "--filter"},
        description = "Filter results by parameter value. Format: param=value (can repeat)",
        split = ","
    )
    private List<String> filters = new ArrayList<>();

    @Option(
        names = {"-o", "--output"},
        description = "Output format: table, csv, markdown",
        defaultValue = "table"
    )
    private String outputFormat;

    @Option(
        names = {"--tail-bytes"},
        description = "Bytes to read from end of file to find summary table",
        defaultValue = "1000000"
    )
    private int tailBytes;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmhSummaryExtractor()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        List<BenchmarkResult> results = extractResults(logFile);

        if (results.isEmpty()) {
            System.err.println("No benchmark results found in " + logFile);
            return 1;
        }

        System.err.println("Extracted " + results.size() + " benchmark results");

        // Apply filters
        results = applyFilters(results);
        if (results.isEmpty()) {
            System.err.println("No results match the specified filters");
            return 1;
        }

        // Output based on mode
        if (compareBy != null && !compareBy.isBlank()) {
            outputComparison(results);
        } else {
            outputGrouped(results);
        }

        return 0;
    }

    private List<BenchmarkResult> extractResults(Path file) throws IOException {
        String summaryText = readTailForSummary(file);
        return parseSummaryTable(summaryText);
    }

    /// Read the tail of the file to find the summary table.
    /// JMH outputs the summary table at the very end, so we read backwards.
    private String readTailForSummary(Path file) throws IOException {
        long fileSize = Files.size(file);
        long readFrom = Math.max(0, fileSize - tailBytes);

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            raf.seek(readFrom);
            byte[] buffer = new byte[(int) (fileSize - readFrom)];
            raf.readFully(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }

    /// Parse the JMH summary table format.
    private List<BenchmarkResult> parseSummaryTable(String text) {
        List<BenchmarkResult> results = new ArrayList<>();

        // Find the header line that starts with "Benchmark"
        String[] lines = text.split("\n");
        int headerIndex = -1;
        List<String> paramNames = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("Benchmark") && line.contains("Mode") && line.contains("Score")) {
                headerIndex = i;
                // Parse parameter names from header
                paramNames = parseHeaderParams(line);
                break;
            }
        }

        if (headerIndex < 0) {
            return results;
        }

        // Parse data lines after header
        Pattern dataPattern = Pattern.compile(
            "^(\\S+)\\s+" +                    // benchmark name
            "(.+?)\\s+" +                      // parameters (N/A or values)
            "(avgt|thrpt|sample|ss)\\s+" +     // mode
            "(\\d+)\\s+" +                     // count
            "([\\d.]+)\\s*±?\\s*" +            // score
            "([\\d.]+)?\\s*" +                 // error (optional)
            "(\\S+)\\s*$"                      // units
        );

        for (int i = headerIndex + 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("NOTE:") || line.startsWith("REMEMBER:")) {
                continue;
            }

            BenchmarkResult result = parseResultLine(line, paramNames);
            if (result != null) {
                results.add(result);
            }
        }

        return results;
    }

    private List<String> parseHeaderParams(String headerLine) {
        List<String> params = new ArrayList<>();
        // Extract parameter names between Benchmark and Mode
        Pattern paramPattern = Pattern.compile("\\((\\w+)\\)");
        Matcher matcher = paramPattern.matcher(headerLine);
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }

    private BenchmarkResult parseResultLine(String line, List<String> paramNames) {
        // Split carefully - benchmark name, then param values, then mode/cnt/score/error/units
        String[] parts = line.split("\\s+");
        if (parts.length < 5) return null;

        try {
            String fullName = parts[0];
            String shortName = extractShortName(fullName);

            // Find mode position (avgt, thrpt, sample, ss)
            int modeIndex = -1;
            for (int i = 1; i < parts.length; i++) {
                if (parts[i].matches("avgt|thrpt|sample|ss")) {
                    modeIndex = i;
                    break;
                }
            }
            if (modeIndex < 0 || modeIndex + 3 >= parts.length) return null;

            // Extract parameters (between name and mode)
            Map<String, String> params = new LinkedHashMap<>();
            params.put("benchmark", shortName);
            int paramCount = Math.min(paramNames.size(), modeIndex - 1);
            for (int i = 0; i < paramCount; i++) {
                String value = parts[1 + i];
                if (!"N/A".equals(value)) {
                    params.put(paramNames.get(i), value);
                }
            }

            String mode = parts[modeIndex];
            int cnt = Integer.parseInt(parts[modeIndex + 1]);
            double score = Double.parseDouble(parts[modeIndex + 2]);

            // Error might have ± prefix or be separate
            double error = 0.0;
            String units;
            int unitsIndex = modeIndex + 3;

            if (parts[unitsIndex].equals("±")) {
                error = Double.parseDouble(parts[unitsIndex + 1]);
                units = parts[unitsIndex + 2];
            } else if (parts[unitsIndex].startsWith("±")) {
                error = Double.parseDouble(parts[unitsIndex].substring(1));
                units = parts[unitsIndex + 1];
            } else {
                // No error, this is units
                units = parts[unitsIndex];
            }

            return new BenchmarkResult(fullName, shortName, params, mode, cnt, score, error, units);
        } catch (Exception e) {
            // Skip malformed lines
            return null;
        }
    }

    private String extractShortName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }

    private List<BenchmarkResult> applyFilters(List<BenchmarkResult> results) {
        if (filters.isEmpty()) return results;

        return results.stream()
            .filter(r -> {
                for (String filter : filters) {
                    String[] kv = filter.split("=", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String value = kv[1].trim();
                        String actual = r.params.get(key);
                        if (actual == null || !actual.equals(value)) {
                            return false;
                        }
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    private void outputGrouped(List<BenchmarkResult> results) {
        // Group by specified parameters
        Map<String, List<BenchmarkResult>> grouped = results.stream()
            .collect(Collectors.groupingBy(
                r -> groupBy.stream()
                    .map(p -> r.params.getOrDefault(p, "N/A"))
                    .collect(Collectors.joining("|")),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        switch (outputFormat.toLowerCase()) {
            case "csv" -> outputGroupedCsv(grouped);
            case "markdown" -> outputGroupedMarkdown(grouped);
            default -> outputGroupedTable(grouped);
        }
    }

    private void outputGroupedTable(Map<String, List<BenchmarkResult>> grouped) {
        // Determine column widths
        int keyWidth = Math.max(20, grouped.keySet().stream().mapToInt(String::length).max().orElse(20));

        System.out.printf("%-" + keyWidth + "s  %12s  %12s  %s%n", "Group", "Score", "Error", "Units");
        System.out.println("-".repeat(keyWidth + 42));

        for (var entry : grouped.entrySet()) {
            // Aggregate scores for group
            DoubleSummaryStatistics stats = entry.getValue().stream()
                .mapToDouble(r -> r.score)
                .summaryStatistics();
            double avgError = entry.getValue().stream()
                .mapToDouble(r -> r.error)
                .average().orElse(0);
            String units = entry.getValue().get(0).units;

            System.out.printf("%-" + keyWidth + "s  %12.3f  %12.3f  %s%n",
                entry.getKey(), stats.getAverage(), avgError, units);
        }
    }

    private void outputGroupedCsv(Map<String, List<BenchmarkResult>> grouped) {
        System.out.println("Group,Score,Error,Units,Count");
        for (var entry : grouped.entrySet()) {
            DoubleSummaryStatistics stats = entry.getValue().stream()
                .mapToDouble(r -> r.score).summaryStatistics();
            double avgError = entry.getValue().stream()
                .mapToDouble(r -> r.error).average().orElse(0);
            String units = entry.getValue().get(0).units;
            System.out.printf("%s,%.3f,%.3f,%s,%d%n",
                entry.getKey(), stats.getAverage(), avgError, units, entry.getValue().size());
        }
    }

    private void outputGroupedMarkdown(Map<String, List<BenchmarkResult>> grouped) {
        System.out.println("| Group | Score | Error | Units |");
        System.out.println("|-------|------:|------:|-------|");
        for (var entry : grouped.entrySet()) {
            DoubleSummaryStatistics stats = entry.getValue().stream()
                .mapToDouble(r -> r.score).summaryStatistics();
            double avgError = entry.getValue().stream()
                .mapToDouble(r -> r.error).average().orElse(0);
            String units = entry.getValue().get(0).units;
            System.out.printf("| %s | %.3f | %.3f | %s |%n",
                entry.getKey(), stats.getAverage(), avgError, units);
        }
    }

    private void outputComparison(List<BenchmarkResult> results) {
        // Get unique values for comparison parameter
        Set<String> compareValues = results.stream()
            .map(r -> r.params.getOrDefault(compareBy, "N/A"))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // Build comparison key (all params except compareBy)
        List<String> keyParams = groupBy.stream()
            .filter(p -> !p.equals(compareBy))
            .toList();

        // Group by comparison key
        Map<String, Map<String, BenchmarkResult>> comparison = new LinkedHashMap<>();
        for (BenchmarkResult r : results) {
            String key = keyParams.stream()
                .map(p -> r.params.getOrDefault(p, "N/A"))
                .collect(Collectors.joining("|"));
            String compareVal = r.params.getOrDefault(compareBy, "N/A");
            comparison.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(compareVal, r);
        }

        // Output comparison table
        int keyWidth = Math.max(30, comparison.keySet().stream().mapToInt(String::length).max().orElse(30));
        int valWidth = 15;

        // Header
        System.out.printf("%-" + keyWidth + "s", "Benchmark");
        for (String val : compareValues) {
            System.out.printf("  %" + valWidth + "s", compareBy + "=" + val);
        }
        System.out.println("  Ratio");
        System.out.println("-".repeat(keyWidth + (compareValues.size() + 1) * (valWidth + 2)));

        // Data rows
        for (var entry : comparison.entrySet()) {
            System.out.printf("%-" + keyWidth + "s", entry.getKey());
            List<Double> scores = new ArrayList<>();
            for (String val : compareValues) {
                BenchmarkResult r = entry.getValue().get(val);
                if (r != null) {
                    System.out.printf("  %12.3f±%s", r.score, formatError(r.error));
                    scores.add(r.score);
                } else {
                    System.out.printf("  %" + valWidth + "s", "N/A");
                }
            }
            // Show ratio if we have exactly 2 values to compare
            if (scores.size() == 2 && scores.get(1) != 0) {
                double ratio = scores.get(0) / scores.get(1);
                System.out.printf("  %.2fx", ratio);
            }
            System.out.println();
        }
    }

    private String formatError(double error) {
        if (error >= 1000) return String.format("%.0f", error);
        if (error >= 100) return String.format("%.1f", error);
        if (error >= 10) return String.format("%.2f", error);
        return String.format("%.3f", error);
    }

    /// Represents a single JMH benchmark result.
    record BenchmarkResult(
        String fullName,
        String shortName,
        Map<String, String> params,
        String mode,
        int count,
        double score,
        double error,
        String units
    ) {}
}
