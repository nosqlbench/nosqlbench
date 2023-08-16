/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapters.api.activityconfig.yaml;

import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDocList;

import java.net.URI;
import java.nio.file.Path;

/**
 * The formats which are recognized as source data for the workload. Any serialization may be supported
 * which can be converted from a character string to an {@link RawOpsDocList} structure.
 *
 * Those which are derived from paths may be determined by their filename extension. Others, which are provided from internal
 * NoSQLBench sources, may only be invoked explicitly.
 */
public enum OpTemplateFormat {
    /**
     * The source is in YAML format
     */
    yaml("yaml", "yml"),

    /**
     * The source is in JSON format
     */
    json("json", "json5"),

    /**
     * The source is in jsonnet format, suitable for pre-processing with context data
     */
    jsonnet("jsonnet", "jsonnet5"),

    /**
     * The source is inline, meaning {@link io.nosqlbench.api.config.params.ParamsParser} format.
     * This is similar to the {@link #stmt} format except that it is parsed for internal op structure.
     * This format is not detectable by source path, and thus can only be used when provided directly
     * from the caller.
     */
    inline(),

    /**
     * The source is in single-statement form, meaning that it is known to be the value of the 'stmt'
     * field of an op template. This format is not detectable by source path, and thus can only be used when provided directly
     * from the caller.
     */
    stmt();

    private final String[] pathExtensions;

    OpTemplateFormat(String... pathExtensions) {
        this.pathExtensions = pathExtensions;
    }

    public static OpTemplateFormat valueOfURI(URI uri) {
        var fullName = uri.toString();
        String extension = fullName.substring(fullName.lastIndexOf('.')+1).toLowerCase();

        for (OpTemplateFormat value : values()) {
            for (String pathExtension : value.pathExtensions) {
                if (pathExtension.equals(extension)) {
                    return value;
                }
            }
        }
        throw new RuntimeException("Unable to determine source format for " + uri);

    }

    public static OpTemplateFormat valueOfPath(Path path) {
        var fullName = path.toString();
        String extension = fullName.substring(fullName.lastIndexOf('.')+1).toLowerCase();

        for (OpTemplateFormat value : values()) {
            for (String pathExtension : value.pathExtensions) {
                if (pathExtension.equals(extension)) {
                    return value;
                }
            }
        }
        throw new RuntimeException("Unable to determine source format for " + path);
    }
}
