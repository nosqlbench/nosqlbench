package io.nosqlbench.engine.rest.services;

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsLoader;
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;
import io.nosqlbench.engine.rest.transfertypes.WorkspaceItemView;
import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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

    public List<WorkloadDesc> getWorkloadsWithScenarioScripts() {
        List<Content<?>> candidates = NBIO.fs().prefix(this.getWorkspacePath().toString()).extension(RawStmtsLoader.YAML_EXTENSIONS).list();
        List<WorkloadDesc> workloads = NBCLIScenarioParser.filterForScenarios(candidates);
        List<WorkloadDesc> relativized = new ArrayList<>();
        for (WorkloadDesc workload : workloads) {
            WorkloadDesc relative = workload.relativize(getWorkspacePath());
            relativized.add(relative);
        }
        return relativized;
    }

    public Path storeFile(String filespec, String encoded, Map<String,String> replacements) {

        String encoding = "raw";
        String filename = filespec;

        String[] parts = filespec.split(":", 2);
        if (parts.length == 2) {
            filename = parts[0];
            encoding = parts[1].toLowerCase();
        }


        List<OpenOption> openOptions = new ArrayList<>();

        if (filename.startsWith(">>")) {
            filename = filename.substring(">>".length());
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.APPEND);
        } else if (filename.startsWith(">")) {
            filename = filename.substring(">".length());
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
            openOptions.add(StandardOpenOption.CREATE);
        }

        Path targetPath = Paths.get(filename);
        assertLegalWorkspacePath(targetPath);

        if (targetPath.isAbsolute()) {
            throw new RuntimeException("You may not use absolute paths in workspaces: '" + targetPath + "'");
        }
        targetPath = this.workspacePath.resolve(targetPath);

        ByteBuffer content;

        switch (encoding) {
            case "raw":
                content = ByteBuffer.wrap(encoded.getBytes(StandardCharsets.UTF_8));
                break;
            case "base64":
                byte[] bytes = Base64.getDecoder().decode(encoded);
                content = ByteBuffer.wrap(bytes);
                break;
            default:
                throw new RuntimeException("Unrecognized encoding of file data '" + encoding + "'");
        }

        try {
            Files.createDirectories(
                targetPath.getParent(),
                PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwxr-x---")
                ));
            Files.write(targetPath, content.array(), openOptions.toArray(new OpenOption[0]));
            replacements.put(filename,targetPath.toString());
            return targetPath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public List<WorkspaceItemView> getWorkspaceListingView(String filepath) {

        Path target = this.workspacePath.resolve(filepath);
        assertLegalWorkspacePath(target);

        List<WorkspaceItemView> items = new ArrayList<>();

        try {
            DirectoryStream<Path> elementPaths = Files.newDirectoryStream(target);
            for (Path elementPath : elementPaths) {
                items.add(new WorkspaceItemView(this.workspacePath,elementPath));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return items;
    }

    private void assertLegalWorkspacePath(Path target) {
        if (target.toString().contains("..")) {
            throw new RuntimeException("Possible path injection:" + target);
        }
    }

    @Override
    public String toString() {
        return this.workspaceName;
    }

    public String[] asIncludes() {
        Path relativePath =
            this.workspacesRoot.toAbsolutePath().getParent().relativize(this.workspacePath.toAbsolutePath());
        return new String[]{ relativePath.toString() };
    }
}
