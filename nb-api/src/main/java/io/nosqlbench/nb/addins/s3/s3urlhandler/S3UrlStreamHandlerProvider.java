package io.nosqlbench.nb.addins.s3.s3urlhandler;

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


import io.nosqlbench.nb.annotations.Service;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

@Service(value = URLStreamHandlerProvider.class, selector = "s3")
public class S3UrlStreamHandlerProvider extends URLStreamHandlerProvider {

    private final S3ClientCache clientCache = new S3ClientCache();

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("s3".equals(protocol)) {
            return new S3UrlStreamHandler(clientCache, protocol);
        }
        return null;
    }

}
