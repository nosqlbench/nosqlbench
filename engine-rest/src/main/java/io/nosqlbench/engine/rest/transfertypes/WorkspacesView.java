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

package io.nosqlbench.engine.rest.transfertypes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorkspacesView {

    private final Path workspacesRoot;
    public WorkspacesView(Path workspacesRoot) {
        this.workspacesRoot = workspacesRoot;
    }

    public List<String> getWorkspaces() {
        List<String> workspaces = new ArrayList<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(workspacesRoot)) {
            for (Path path : paths) {
                workspaces.add(path.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return workspaces;
    }
}
