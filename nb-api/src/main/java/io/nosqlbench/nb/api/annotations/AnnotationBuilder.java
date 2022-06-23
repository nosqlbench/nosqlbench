/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.annotations;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.TimeZone;

public class AnnotationBuilder implements AnnotationBuilderFacets.All {
    private String session;
    private long start;
    private long end;
    private final LinkedHashMap<String, String> labels = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> details = new LinkedHashMap<>();
    private Layer layer;
    private final TimeZone timezone = TimeZone.getTimeZone(ZoneId.of("GMT"));

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
    public AnnotationBuilderFacets.WantsMoreDetailsOrBuild detail(String name, String value) {
        this.details.put(name, value);
        return this;
    }

    @Override
    public Annotation build() {
        return new MutableAnnotation(timezone, session, layer, start, end, labels, details).asReadOnly();

    }

    @Override
    public AnnotationBuilderFacets.WantsInterval session(String session) {
        this.session = session;
        return this;
    }

}
