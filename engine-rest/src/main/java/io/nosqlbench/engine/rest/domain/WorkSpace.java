package io.nosqlbench.engine.rest.domain;

import java.nio.file.Path;
import java.util.Optional;

public class WorkSpace {
    private Path workspacesRoot;
    private String workspace;

    public WorkSpace(Path workspacesRoot, String workspace) {
        this.workspacesRoot = workspacesRoot;
        this.workspace = workspace;
    }

    public Optional<Path> get(String filename) {
        return Optional.empty();
    }

    public Optional<javax.ws.rs.Path> put(WorkSpace workspace) {
        return Optional.empty();
    }
}
