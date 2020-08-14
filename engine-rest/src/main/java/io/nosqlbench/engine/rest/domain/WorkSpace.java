package io.nosqlbench.engine.rest.domain;

import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorkSpace {
    private final Path workspacesRoot;
    private final Path workspacePath;
    private final String workspaceName;

    public WorkSpace(Path workspacesRoot, String workspaceName) {
        this.workspacesRoot = workspacesRoot;
        this.workspaceName = workspaceName;
        this.workspacePath = workspacesRoot.resolve(workspaceName);
        if (!Files.exists(workspacePath)) {
            try {
                Files.createDirectories(workspacePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public WorkspaceView getWorkspaceView() {
        return new WorkspaceView(workspacesRoot.resolve(workspaceName));
    }

    public Path getWorkspacePath() {
        return workspacePath;
    }
}
