package io.nosqlbench.engine.rest.transfertypes;

import java.nio.file.Path;

public class WorkspaceView {

    private final Path workspaceRoot;

    public WorkspaceView(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String getName() {
        return workspaceRoot.getFileName().toString();
    }
}
