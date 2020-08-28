package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WorkspaceItemView {

    private String type;
    private String perms;
    private String owner;
    private String group;
    private long size;
    private long mtime;

    private final static List<String> fields = List.of(
        "type",
        "perms",
        "owner",
        "group",
        "size",
        "mtime",
        "name"
    );

    public WorkspaceItemView(Path wspath, Path path) {
        try {
            PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            PosixFileAttributes attrs = posix.readAttributes();

            setPerms(fromPerms(attrs.permissions()));
            setType(typeOf(path));
            setOwner(attrs.owner().getName());
            setGroup(attrs.group().getName());
            setSize(attrs.size());
            setMtimeMillis(attrs.lastModifiedTime().to(TimeUnit.MILLISECONDS));
            setName(wspath.relativize(path).toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setOwner(FileOwnerAttributeView fileAttributeView) {
        try {
            this.setOwner(fileAttributeView.getOwner().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String typeOf(Path path) {
        if (Files.isRegularFile(path)) {
            return "F";
        }
        if (Files.isDirectory(path)) {
            return "D";
        }
        return "U";
    }

    @JsonProperty("fields")
    public List<List<String>> getAsFields() {
        return List.of(
            fields,
            List.of(
                this.type,
                this.perms,
                this.owner,
                this.group,
                String.valueOf(this.size),
                "mtime",
                this.name
            )
        );

    }
    private String fromPerms(Set<PosixFilePermission> perms) {
        StringBuilder sb = new StringBuilder();
        String img = "rwxrwxrwx";
        String not = "---------";
        int step = 0;
        for (PosixFilePermission perm : PosixFilePermission.values()) {
            String src = perms.contains(perm) ? img : not;
            sb.append(src.charAt(step));
            step++;
        }
        return sb.toString();
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getMtimeMillis() {
        return mtime;
    }

    public void setMtimeMillis(long mtime) {
        this.mtime = mtime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

}
