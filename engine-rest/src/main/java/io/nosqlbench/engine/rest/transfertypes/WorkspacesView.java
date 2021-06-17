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
