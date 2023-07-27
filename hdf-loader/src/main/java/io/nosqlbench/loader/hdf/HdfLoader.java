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

package io.nosqlbench.loader.hdf;

import io.nosqlbench.loader.hdf.config.LoaderConfig;
import io.nosqlbench.loader.hdf.readers.HdfReaders;
import io.nosqlbench.loader.hdf.readers.Hdf5Reader;
import io.nosqlbench.loader.hdf.readers.HdfReader;
import io.nosqlbench.loader.hdf.writers.AstraVectorWriter;
import io.nosqlbench.loader.hdf.writers.FileVectorWriter;
import io.nosqlbench.loader.hdf.writers.VectorWriter;
import io.nosqlbench.loader.hdf.writers.VectorWriters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class HdfLoader {
    private static final Logger logger = LogManager.getLogger(HdfLoader.class);

    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: hdf-loader <filename>");
            System.exit(1);
        }
        try {
            LoaderConfig config = new LoaderConfig(args[0]);
            HdfReader reader = null;
            VectorWriter writer = null;

            String format = config.getFormat();
            switch (HdfReaders.valueOf(format)) {
                case HDF4 -> {
                    logger.info("HDF4 format not yet supported");
                    System.exit(1);
                }
                case HDF5 -> {
                    reader = new Hdf5Reader(config);
                }
                default -> {
                    logger.info("Unknown format: " + format);
                    System.exit(1);
                }
            }

            String writerType = config.getWriter();
            switch (VectorWriters.valueOf(writerType)) {
                case filewriter -> {
                    writer = new FileVectorWriter(config);
                }
                case astra -> {
                    writer = new AstraVectorWriter(config);
                }
                default -> {
                    logger.info("Unknown writer type: " + writerType);
                    System.exit(1);
                }
            }
            reader.setWriter(writer);
            reader.read();
        } catch (Exception e) {
            logger.error(e);
            System.exit(1);
        }

    }
}
