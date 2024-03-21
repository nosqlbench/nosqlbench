/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.regex.Pattern;

/**
 * <P>Read each line in each matching file in a directory structure, providing one
 * line for each time this function is called. The files are sorted at the time
 * the function is initialized, and each line is read in order.
 * </P>
 * <P>This function accepts long input values, but they are used as ints, modulo the total number of lines known.
 * This is due to historic limitations in the Java file APIs and file size support.</P>
 *
 * <P>This is a variant of {@link DirectoryLines}. This version keeps a map of files and their respective cardinality,
 * computed at initialization time. The content is assumed to be static during the lifetime of this function.
 * </P>
 * <p>
 * The value returned for a given cycle is stable, so long as the underlying data is stable.</P>
 * <HR/>
 * <P><EM>This caches all
 * data at initialization time. If you need to buffer the data in stream mode, use {@link DirectoryLines} instead,
 * which is not order-stable.</EM>
 * </P>
 */
@ThreadSafeMapper
@Categories({Category.general})
public class DirectoryLinesStable implements LongFunction<String> {

    private final static Logger logger = LogManager.getLogger(DirectoryLinesStable.class);

    private final Pattern namePattern;
    private final String basepath;
    private final List<Path> allFiles;
    private int[] sizes;
    private final int totalSize;
    private List<IntFunction<String>> fileFunctions = new ArrayList<>();

    @Example({"DirectoryLines('/var/tmp/bardata', '.*')", "load every line from every file in /var/tmp/bardata"})
    public DirectoryLinesStable(String basepath, String namePattern) {
        this.basepath = basepath;
        this.namePattern = Pattern.compile(namePattern);
        allFiles = getAllFiles();
        if (allFiles.isEmpty()) {
            throw new RuntimeException("Loaded zero files from " + basepath + ", full path:" + Paths.get(basepath).getFileName());
        }
        sizes = new int[allFiles.size()];
        int accumulator = 0;
        for (int i = 0; i < allFiles.size(); i++) {
            try {
                List<String> values = Files.readAllLines(allFiles.get(i));
                fileFunctions.add(values::get);
                int lineCount = values.size();
                if (((long) lineCount + (long) accumulator) > Integer.MAX_VALUE) {
                    throw new RuntimeException("Total number of lines is beyond addressible values for int type. " +
                        "Reduce total lines to fix this.");
                }
                accumulator += lineCount;
                sizes[i] = accumulator;
            } catch (IOException e) {
                throw new RuntimeException("Error while reading filepath '" + allFiles.get(i).toString(), e);
            }
        }
        this.totalSize = accumulator;
    }

    private String getLine(int index, int offset) {
        try {
            IntFunction<String> func = fileFunctions.get(index);
            return func.apply(offset);
        } catch (Exception e)  {
            throw new RuntimeException("Error while binding index=" + index + " offset=" + offset + " for " + this);
        }
    }
    @Override
    public synchronized String apply(long cycle) {
        int value = (int) (cycle % totalSize);
        int index = 0;
        while (value >= sizes[index]) {
            value -= sizes[index];
            index++;
        }

        return this.getLine(index,value);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DirectoryLinesStable{");
        sb.append("namePattern=").append(namePattern);
        sb.append(", basepath='").append(basepath).append('\'');
        sb.append(", allFiles=").append(allFiles);
        sb.append(", sizes=");
        if (sizes == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < sizes.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(sizes[i]);
            sb.append(']');
        }
        sb.append(", fileFunctions=").append(fileFunctions.size());
        sb.append('}');
        return sb.toString();
    }

    private List<Path> getAllFiles() {
        logger.debug(() -> "Loading file paths from " + basepath);
        Set<FileVisitOption> options = new HashSet<>();
        options.add(FileVisitOption.FOLLOW_LINKS);
        FileList fileList = new FileList(namePattern);

        try {
            Files.walkFileTree(Paths.get(basepath), options, 10, fileList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.debug(() -> "File reader: " + fileList + " in path: " + Paths.get(basepath).getFileName());
        fileList.paths.sort(Path::compareTo);
        return fileList.paths;
    }

    private static class FileList implements FileVisitor<Path> {
        public final Pattern namePattern;
        public int seen;
        public int kept;
        public List<Path> paths = new ArrayList<>();

        private FileList(Pattern namePattern) {
            this.namePattern = namePattern;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            seen++;
            if (file.toString().matches(namePattern.pattern())) {
                paths.add(file);
                kept++;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            logger.warn("Error traversing file: " + file + ":" + exc);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        public String toString() {
            return "" + kept + "/" + seen + " files with pattern '" + namePattern + "'";
        }

    }

}

