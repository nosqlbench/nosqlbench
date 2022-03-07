package io.nosqlbench.docapi;

import java.nio.file.Path;
import java.util.*;

public class DocsNameSpaceImpl implements DocsNameSpace {

    private final Set<Path> paths = new HashSet<>();
    private String namespace;
    private boolean enabledByDefault = false;

    public DocsNameSpaceImpl() {}

    public static DocsNameSpaceImpl of(String descriptiveName, Path path) {
        return new DocsNameSpaceImpl().setNameSpace(descriptiveName).addPath(path);
    }

    private DocsNameSpaceImpl setNameSpace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DocsNameSpaceImpl(String name) {
        this.namespace = name;
    }

    public String getName() {
        return namespace;
    }

    @Override
    public List<Path> getPaths() {
        return new ArrayList<>(this.paths);
    }

    @Override
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    @Override
    public String toString() {
        return "DocPath{" +
                "namespace='" + namespace + '\'' +
                ",paths=" + paths +
                '}';
    }

    public DocsNameSpaceImpl addPath(Path path) {
        this.paths.add(path);
        return this;
    }

    public DocsNameSpaceImpl enabledByDefault() {
        this.enabledByDefault=true;
        return this;
    }

    @Override
    public Iterator<Path> iterator() {
        return this.paths.iterator();
    }

    public DocsNameSpaceImpl setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault=enabledByDefault;
        return this;
    }
}
