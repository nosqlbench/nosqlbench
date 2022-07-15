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

import java.io.IOException;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * PathContent provides the Path-centric way of accessing
 * resolved content from the URIs API.
 */
public class PathContent implements Content<Path> {

    private final Path path;

    public PathContent(Path path) {
        this.path = path;
    }

    @Override
    public Path getLocation() {
        return path;
    }


    @Override
    public URI getURI() {
        return path.toUri();
    }

    @Override
    public CharBuffer getCharBuffer() {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return CharBuffer.wrap(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path asPath() {
        return this.path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathContent that = (PathContent) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public String toString() {
        return "PathContent{" + getURI().toString() + "}";
    }

}
