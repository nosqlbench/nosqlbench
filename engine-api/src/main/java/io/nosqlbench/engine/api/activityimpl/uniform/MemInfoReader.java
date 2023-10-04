package io.nosqlbench.engine.api.activityimpl.uniform;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemInfoReader {
    private static final String filePath = "/proc/meminfo";
    private static final Set<String> relevantKeys = Set.of(
        "MemTotal", "MemFree", "MemAvailable", "Cached", "Buffers", "SwapTotal", "SwapFree"
    );
    private static final Map<String,Double> metricsMap = new HashMap<>();

    public static Double getMemTotalkB() {
        return getValue("MemTotal");
    }

    public static Double getMemFreekB() {
        return getValue("MemFree");
    }

    public static Double getMemAvailablekB() {
        return getValue("MemAvailable");
    }

    public static Double getMemUsedkB() {
        return getValue("MemUsed");
    }

    public static Double getMemCachedkB() {
        return getValue("Cached");
    }

    public static Double getMemBufferskB() {
        return getValue("Buffers");
    }

    public static Double getSwapTotalkB() {
        return getValue("SwapTotal");
    }

    public static Double getSwapFreekB() {
        return getValue("SwapFree");
    }

    public static Double getSwapUsedkB() {
        return getValue("SwapUsed");
    }

    private static Double getValue(String key) {
        parseFile();
        return metricsMap.get(key);
    }

    private static void parseFile() {
        /*
        References:
        - https://docs.kernel.org/filesystems/proc.html#meminfo
        - https://stackoverflow.com/questions/41224738/how-to-calculate-system-memory-usage-from-proc-meminfo-like-htop
         */
        metricsMap.clear();
        try {
            FileReader file = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(file);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length != 2)
                    continue;
                String key = parts[0].trim();
                Double value = Double.parseDouble(parts[1].trim().split(" ")[0]);
                if (relevantKeys.contains(key)) {
                    metricsMap.put(key, value);
                }
            }
            if (metricsMap.get("MemTotal") != null && metricsMap.get("MemFree") != null) {
                metricsMap.put("MemUsed", metricsMap.get("MemTotal") - metricsMap.get("MemFree"));
            }
            if (metricsMap.get("SwapTotal") != null && metricsMap.get("SwapFree") != null) {
                metricsMap.put("SwapUsed", metricsMap.get("SwapTotal") - metricsMap.get("SwapFree"));
            }
        } catch (FileNotFoundException e) {
            return;
        }
        catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
    }
}
