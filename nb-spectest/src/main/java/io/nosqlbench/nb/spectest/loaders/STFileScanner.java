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

package io.nosqlbench.nb.spectest.loaders;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class STFileScanner {
    public static List<Path> findMatching(Path... paths) {
        return findMatching(".*\\.md",paths);
    }
    public static List<Path> findMatching(String regex, Path... paths) {
        List<Path> found = new ArrayList<>();
        return findMatching(found, Pattern.compile(regex), paths);
    }

    private static List<Path> findMatching(List<Path> accumulator, Pattern regex, Path... paths) {

        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                DirectoryStream<Path> dirdata;
                try {
                    dirdata = Files.newDirectoryStream(path);
                } catch (IOException e) {
                    throw new RuntimeException("Error while scanning for specifications: " + e,e);
                }
                for (Path dirdatum : dirdata) {
                    findMatching(accumulator,regex,dirdatum);
                }
            } else if (Files.isReadable(path)) {
                if (regex.matcher(path.getFileName().toString()).matches()) {
                    accumulator.add(path);
                }
            } else {
                throw new RuntimeException("unreadable path: '" + path + "'");
            }
        }
        return accumulator;
    }
}
