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
 *
 */

package io.nosqlbench.loader.hdf.writers;

import io.nosqlbench.loader.hdf.config.LoaderConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class FileVectorWriter extends AbstractVectorWriter {
    private static final Logger logger = LogManager.getLogger(FileVectorWriter.class);
    private final BufferedWriter targetFile;
    public FileVectorWriter(LoaderConfig config) throws IOException {
        String targetFileName = config.getTargetFile();
        targetFile = new BufferedWriter(new FileWriter(targetFileName));
    }

    @Override
    protected void writeVector(float[] vector) {
        try {
            targetFile.write("[");
            for (int i = 0; i < vector.length; i++) {
                targetFile.write(String.valueOf(vector[i]));
                if (i < vector.length - 1) {
                    targetFile.write(",");
                }
            }
            targetFile.write("]");
            targetFile.write("\n");
            targetFile.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }
}
