package io.nosqlbench.nb.api.annotations;

import io.nosqlbench.nb.api.Layer;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.TimeZone;

public class AnnotationBuilder implements BuilderFacets.All {
    private String session;
    private long start;
    private long end;
    private final LinkedHashMap<String, String> labels = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> details = new LinkedHashMap<>();
    private Layer layer;
    private TimeZone timezone = TimeZone.getTimeZone(ZoneId.of("GMT"));

    @Override
    public AnnotationBuilder layer(Layer layer) {
        this.layer = layer;
        this.label("layer", layer.toString());
        return this;
    }

    @Override
    public AnnotationBuilder interval(long start, long end) {
        start(start);
        end(end);
        return this;
    }

    @Override
    public AnnotationBuilder now() {
        start(System.currentTimeMillis());
        end(this.start);
        return this;
    }

    private AnnotationBuilder start(long start) {
        this.start = start;
        return this;
    }

    private AnnotationBuilder end(long end) {
        this.end = end;
        return this;
    }

    @Override
    public AnnotationBuilder at(long at) {
        this.start(at);
        this.end(at);
        return this;
    }


    @Override
    public AnnotationBuilder label(String name, String value) {
        this.labels.put(name, value);
        return this;
    }

    @Override
    public BuilderFacets.WantsMoreDetailsOrBuild detail(String name, String value) {
        this.details.put(name, value);
        return this;
    }

    @Override
    public Annotation build() {
        return new MutableAnnotation(timezone, session, layer, start, end, labels, details).asReadOnly();

    }

    @Override
    public BuilderFacets.WantsInterval session(String session) {
        this.session = session;
        return this;
    }

}
