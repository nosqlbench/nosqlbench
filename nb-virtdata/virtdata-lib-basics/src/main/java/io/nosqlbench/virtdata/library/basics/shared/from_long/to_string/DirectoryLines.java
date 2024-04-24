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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.LongFunction;
import java.util.regex.Pattern;

/**
 * Read each line in each matching file in a directory structure, providing one
 * line for each time this function is called. The files are sorted at the time
 * the function is initialized, and each line is read in order.
 *
 * This function does not produce the same result per cycle value. It is possible
 * that different cycle inputs will return different inputs if the cycles are not
 * applied in strict order. Still, this function is useful for consuming input
 * from a set of files as input to a test or simulation.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class DirectoryLines implements LongFunction<String> {

    private final static Logger logger  = LogManager.getLogger(DirectoryLines.class);

    private final Pattern namePattern;
    private final String basepath;
    private final List<Path> allFiles;
    private Iterator<String> stringIterator;
    private Iterator<Path> pathIterator;

    @Example({"DirectoryLines('/var/tmp/bardata', '.*')","load every line from every file in /var/tmp/bardata"})
    public DirectoryLines(String basepath, String namePattern) {
        this.basepath = basepath;
        this.namePattern = Pattern.compile(namePattern);
        allFiles = getAllFiles();
        if (allFiles.isEmpty()) {
            throw new RuntimeException("Loaded zero files from " + basepath + ", full path:" + Paths.get(basepath).getFileName());
        }
        pathIterator = allFiles.iterator();
        try {
            stringIterator = Files.readAllLines(pathIterator.next()).iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String apply(long value) {
        while (!stringIterator.hasNext()) {
            if (pathIterator.hasNext()) {
                Path nextPath = pathIterator.next();
                try {
                    stringIterator = Files.readAllLines(nextPath).iterator();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.debug("Resetting path iterator after exhausting input.");
                pathIterator = allFiles.iterator();
            }
        }
        return stringIterator.next();
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

