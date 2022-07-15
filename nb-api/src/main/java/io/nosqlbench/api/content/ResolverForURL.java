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

package io.nosqlbench.api.content;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ResolverForURL implements ContentResolver {

    public static final ContentResolver INSTANCE = new ResolverForURL();
    private final static Logger logger = LogManager.getLogger(ResolverForURL.class);

    @Override
    public List<Content<?>> resolve(URI uri) {
        URLContent urlContent = resolveURI(uri);
        if (urlContent!=null) {
            return List.of(urlContent);
        } else {
            return List.of();
        }
    }

    private URLContent resolveURI(URI uri) {
        if (uri.getScheme() == null) {
            return null;
        }
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            try {
                URL url = uri.toURL();
                InputStream inputStream = url.openStream();
                logger.debug("Found accessible remote file at " + url);
                return new URLContent(url, inputStream);
            } catch (IOException e) {
                logger.warn("Unable to find content at URI '" + uri + "', this often indicates a configuration error.");
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        return Collections.emptyList();
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

}
