package io.nosqlbench.engine.rest.services;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class WorkspaceFinder {
    private final static Logger logger = LogManager.getLogger(WorkspaceFinder.class);

    public static String DEFAULT = "default";
    public static final String WORKSPACE_ROOT = "workspaces_root";

    private final Path root;

    public WorkspaceFinder(Configuration config) {
        Object root = config.getProperties().get(WORKSPACE_ROOT);
        if (root instanceof Path) {
            this.root = (Path) root;
        } else if (root instanceof CharSequence) {
            this.root = Paths.get(((CharSequence) root).toString());
        } else if (root == null) {
            this.root = Paths.get(
                System.getProperty("user.dir"),
                "workspaces"
            );
            try {
                Files.createDirectories(this.root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Unable to use workspaces root " +
                "path of type " + root.getClass().getCanonicalName());
        }
        createDefaultIfNotExist();
    }

    public WorkspaceFinder(Path root) {
        this.root = root;
        createDefaultIfNotExist();
    }

    private void createDefaultIfNotExist() {
        getWorkspaceView(DEFAULT);
    }

    public List<WorkspaceView> getWorkspaceViews() {
        List<WorkspaceView> views = new ArrayList<>();
        try (DirectoryStream<Path> wsrEntries = Files.newDirectoryStream(root)) {
            for (Path entry : wsrEntries) {
                views.add(new WorkspaceView(entry));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return views;
    }

    public WorkSpace getWorkspace(String workspaceName) {
        assertLegalWorkspaceName(workspaceName);
        return new WorkSpace(this.root, workspaceName);
    }

    public static void assertLegalWorkspaceName(String workspaceName) {
        if (!workspaceName.matches("[a-zA-Z0-9]+")) {
            throw new RuntimeException("Workspace names must contain only letters and numbers.");
        }
    }

    public WorkspaceView getWorkspaceView(String workspaceName) {
        return getWorkspace(workspaceName).getWorkspaceView();
    }

    public void putFile(String workspaceName, String filename, ByteBuffer content) {
        Path toWrite = root.resolve(workspaceName).resolve(filename);
        try {
            Files.write(toWrite, content.array(),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the bytes of the named file in the named workspace.
     *
     * @param workspaceName The workspace name to look in for the file
     * @param filename      The filename within the workspace to read
     * @return null if the file is not found
     * @throws RuntimeException if the file was found but could not be
     *                          read.
     */
    public FileInfo readFile(String workspaceName, String filename) {
        Path filePath = workspacePath(workspaceName).resolve(filename);
        if (Files.exists(filePath)) {
            return new FileInfo(filePath);
        } else {
            return null;
        }
    }

    private Path workspacePath(String workspaceName) {
        return root.resolve(workspaceName);
    }

    public void purgeWorkspace(String workspaceName, int deleteCount) {
        assertLegalWorkspaceName(workspaceName);
        Path path = workspacePath(workspaceName);
        if (Files.exists(path)) {
            try (Stream<Path> counter = Files.walk(path)) {
                long foundFiles = counter.count();
                if (foundFiles > 100 && deleteCount != foundFiles) {
                    throw new RuntimeException(
                        "To delete " + foundFiles + " files, you must provide a deleteCount=<count> " +
                            "parameter that matches. This is a safety mechanism."
                    );
                }
                logger.debug("found " + foundFiles + " to delete.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Path relativize = root.relativize(path);
            if (relativize.toString().contains("..")) {
                throw new RuntimeException("Illegal path to delete: " + path);
            }

            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
//                    .peek(System.out::println)
                    .forEach(f -> {
                        logger.debug("deleting '" + f + "'");
                        if (!f.delete()) {
                            throw new RuntimeException("Unable to delete " + f);
                        }
                    });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public final static class FileInfo {
        private final Path path;

        public FileInfo(Path path) {
            this.path = path;
        }

        public MediaType getMediaType() {
            try {
                String contentType = Files.probeContentType(path);
                MediaType mediaType = MediaType.valueOf(contentType);
                return mediaType;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public ByteBuffer getContent() {
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ByteBuffer.wrap(bytes);
        }

        public Path getPath() {
            return path;
        }
    }

}
