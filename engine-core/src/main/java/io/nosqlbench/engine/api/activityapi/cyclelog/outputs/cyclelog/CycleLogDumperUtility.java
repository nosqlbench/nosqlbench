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

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class CycleLogDumperUtility {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("USAGE: CyclesCLI <filename>");
        }
        String filename = args[0];

        DisplayType displayType = DisplayType.spans;
        if (args.length >= 2) {
            displayType = DisplayType.valueOf(args[1]);
        }
        new CycleLogDumperUtility().dumpData(filename, displayType);
    }

    private void dumpData(String filename, DisplayType displayType) {
        File filepath = new File(filename);
        MappedByteBuffer mbb = null;
        if (!filepath.exists()) {
            if (!filepath.getPath().endsWith(".cyclelog")) {
                filepath = new File(filename+".cyclelog");
                if (!filepath.exists()) {
                    throw new RuntimeException("neither '" + filename + "' nor  '" + filename + ".cyclelog' exists!");
                }
            }
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(filepath, "rw");
            mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int readsize = 100;

        if (mbb.remaining() > 0) {
            CycleResultsRLEBufferReadable readable = null;
            while (mbb.remaining() > 0) {
                readable = new CycleResultsRLEBufferReadable(readsize, mbb);

                for (CycleResultsSegment segment : readable) {
                    switch (displayType) {
                        case cycles:
                            for (CycleResult cycleResult : segment) {
                                System.out.println(cycleResult);
                            }
                            break;
                        case spans:
                            System.out.println(segment.toString());
                            break;

                    }

                }
            }

        }

    }

    enum DisplayType {
        cycles,
        spans
    }
}
