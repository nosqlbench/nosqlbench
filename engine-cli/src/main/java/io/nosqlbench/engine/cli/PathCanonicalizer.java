/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.cli;

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.file.FileSystems;
import java.util.Optional;

public class PathCanonicalizer {
    private final static Logger logger = LogManager.getLogger(Cmd.class);

    private final String[] includes;

    public PathCanonicalizer(String... includes) {
        this.includes = includes;
    }

    public String canonicalizePath(String path) {

        Optional<Content<?>> found = NBIO.local().searchPrefixes("activities")
            .searchPrefixes(includes)
            .pathname(path)
            .first();

        if (found.isPresent()) {
            String rewriteTo = found.get().asPath().toString();
            String separator = FileSystems.getDefault().getSeparator();
            rewriteTo=(rewriteTo.startsWith(separator) ? rewriteTo.substring(1) : rewriteTo);

            if (!rewriteTo.equals(path)) {
                if (NBIO.local().searchPrefixes("activities").searchPrefixes(includes).pathname(rewriteTo).first().isPresent()) {
                    logger.info("rewrote path for " + path + " as " + rewriteTo);
                    return rewriteTo;
                } else {
                    logger.trace(() -> "kept path for " + path + " as " + found.get().asPath().toString());
                    return path;
                }
            } else {
                logger.trace(() -> "kept path for " + path + " as " + found.get().asPath().toString());
            }
        } else {
            logger.trace(() -> "unable to find " + path + " for path qualification, either it is remote or missing.");
        }
        return path;
    }
}
