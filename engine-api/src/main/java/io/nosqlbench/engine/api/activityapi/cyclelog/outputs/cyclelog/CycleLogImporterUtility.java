/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleSpanResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CycleLogImporterUtility {

    private final static Pattern linePattern = Pattern.compile("\\[?(?<start>\\d+)(,(?<end>\\d+)\\))?->(?<result>\\d+)");

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("USAGE: CyclesCLI <input-textfile>, <output-cyclelog>");
        }
        String infile = args[0];
        String outfile = args[1];
        try {
            new CycleLogImporterUtility().convert(infile, outfile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void convert(String infile, String outfile) throws Exception {
        CycleLogOutput output = new CycleLogOutput(new File(outfile), 1024);
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line = reader.readLine();
        while (line != null) {
            Matcher matcher = linePattern.matcher(line);
            if (matcher.matches()) {
                long start = Long.valueOf(matcher.group("start"));
                int result = Integer.valueOf(matcher.group("result"));
                String endMatched = matcher.group("end");
                if (endMatched == null) {
                    output.onCycleResult(start, result);
                } else {
                    long end = Long.valueOf(endMatched);
                    output.onCycleResultSegment(new CycleSpanResults(start, end, result));
                }
            } else {
                throw new RuntimeException("Unrecognized line format on import: " + line);
            }
            line = reader.readLine();
        }
        output.close();
    }

}
