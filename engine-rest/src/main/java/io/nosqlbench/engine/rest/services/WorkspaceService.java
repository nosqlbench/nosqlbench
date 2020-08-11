package io.nosqlbench.engine.rest.services;

import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceService {
    private final Path root;
    public static String DEFAULT = "default";

    public WorkspaceService(Object root) {
        if (root instanceof Path) {
            this.root = (Path) root;
        } else if (root instanceof CharSequence) {
            this.root = Paths.get(((CharSequence) root).toString());
        } else if (root == null) {
            this.root = Paths.get(System.getProperty("user.dir"),
                "workspaces");
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

    private void createDefaultIfNotExist() {
        getWorkspaceView(DEFAULT);
    }

    public List<WorkspaceView> getWorkspaceViews() {
        List<WorkspaceView> views = new ArrayList<>();
        DirectoryStream<Path> wsrEntries = null;
        try {
            wsrEntries = Files.newDirectoryStream(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Path entry : wsrEntries) {
            views.add(new WorkspaceView(entry));
        }
        return views;
    }

    public WorkspaceView getWorkspaceView(String workspace) {
        if (!workspace.matches("[a-zA-Z][a-zA-Z0-9]+")) {
            throw new RuntimeException("Workspaces must start with an alphabetic" +
                " character, and contain only letters and numbers.");
        }

        Path wspath = root.resolve(Paths.get(workspace));
        if (!Files.exists(wspath)) {
            try {
                Files.createDirectories(wspath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new WorkspaceView(wspath);
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
    }
}
