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

package io.nosqlbench.adapter.jmx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SecureUtils {
    private final static Logger logger = LogManager.getLogger(SecureUtils.class);

    public static String readSecret(String description, String source) {
        if (source==null) {
            return null;
        }

        if (source.startsWith("file:")) {
            String sourceFile = source.substring("file:".length());
            try {
                return Files.readString(Path.of(sourceFile), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (source.startsWith("console:")||source.equals("")) {
            System.out.print("Enter " + description+":");

            char[] chars = System.console().readPassword("%s:",description);
            return new String(chars);
        } else {
            logger.warn("Parameter for '" + description + "' was passed directly. This is less secure." +
                    " Consider using 'file:<file>' or 'console:' for this value instead");
            return source;
        }
    }
}
