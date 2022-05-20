package io.nosqlbench.nb.api.content;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.function.Supplier;

/**
 * A generic content wrapper for anything that can be given to a NoSQLBench runtime
 * using a specific type of locator.
 *
 * @param <T>
 */
public interface Content<T> extends Supplier<CharSequence>, Comparable<Content<?>> {

    T getLocation();

    URI getURI();

    default URL getURL() {
        try {
            return getURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    Path asPath();

    public default String asString() {
        return getCharBuffer().toString();
    }

    CharBuffer getCharBuffer();

    @Override
    default CharSequence get() {
        return getCharBuffer();
    }

    default int compareTo(Content<?> other) {
        return getURI().compareTo(other.getURI());
    }

    default Reader getReader() {
        InputStream inputStream = getInputStream();
        return new InputStreamReader(inputStream);
    }

    default InputStream getInputStream() {
        try {
            Path path = asPath();
            FileSystem fileSystem = path.getFileSystem();
            FileSystemProvider provider = fileSystem.provider();
            InputStream stream = provider.newInputStream(path, StandardOpenOption.READ);
            return stream;
        } catch (IOException ignored) {
        }

        String stringdata = getCharBuffer().toString();
        return new ByteArrayInputStream(stringdata.getBytes());
    }
}
