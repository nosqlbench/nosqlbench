/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.nb.api.system;

import io.nosqlbench.nb.api.errors.BasicError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NBStatePath {

    public static final String NB_STATEDIR_PATHS = "$NBSTATEDIR:$PWD/.nosqlbench:$HOME/.nosqlbench";

    private static final List<String> statePathAccesses = new ArrayList<>();
    private static Path statepath;

    public static Path initialize() {
        return initialize(NB_STATEDIR_PATHS);
    }
    public static Path initialize(String statedirs) {
        if (statedirs==null || statedirs.isBlank()) {
            statedirs = NB_STATEDIR_PATHS;
        }

        if (statepath!=null) {
            return statepath;
        }

        if (0 < statePathAccesses.size())
            throw new BasicError("The state dir must be set before it is used by other\n" +
                " options. If you want to change the statedir, be sure you do it before\n" +
                " dependent options. These parameters were called before this --statedir:\n" +
                statePathAccesses.stream().map(s -> "> " + s).collect(Collectors.joining("\n")));
        if (null != statepath) return statepath;

        final List<String> paths = NBEnvironment.INSTANCE.interpolateEach(":", statedirs);
        Path selected = null;

        for (final String pathName : paths) {
            final Path path = Path.of(pathName);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    selected = path;
                    break;
                }
                System.err.println("ERROR: possible state dir path is not a directory: '" + path + '\'');
            }
        }
        if (null == selected) selected = Path.of(paths.get(paths.size() - 1));

        if (!Files.exists(selected)) try {
            Files.createDirectories(
                selected,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---"))
            );
        } catch (final IOException e) {
            throw new BasicError("Could not create state directory at '" + selected + "': " + e.getMessage());
        }

        NBEnvironment.INSTANCE.put(NBEnvironment.NBSTATEDIR, selected.toString());

        return selected;
    }

}
