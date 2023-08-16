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
import io.nosqlbench.loader.hdf.readers.Hdf5Reader;
import io.nosqlbench.loader.hdf.readers.HdfReader;
import io.nosqlbench.loader.hdf.writers.AstraVectorWriter;
import io.nosqlbench.loader.hdf.writers.FileVectorWriter;
import io.nosqlbench.loader.hdf.writers.NoopVectorWriter;
import io.nosqlbench.loader.hdf.writers.VectorWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HdfLoader {
    private static final Logger logger = LogManager.getLogger(HdfLoader.class);
    public static final String FILEWRITER = "filewriter";
    public static final String ASTRA = "astra";
    public static final String NOOP = "noop";
    public static final String HDF5 = "hdf5";
    public static final String HDF4 = "hdf4";

    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: hdf-loader <filename>");
            System.exit(1);
        }
        try {
            LoaderConfig config = new LoaderConfig(args[0]);
            logger.info("Starting loader with config: " + config);
            HdfReader reader = null;
            VectorWriter writer = null;

            String format = config.getFormat();
            switch (format.toLowerCase()) {
                case HDF4 -> {
                    logger.info("HDF4 format not yet supported");
                    System.exit(1);
                }
                case HDF5 -> {
                    logger.info("HDF5 format selected");
                    reader = new Hdf5Reader(config);
                }
                default -> {
                    logger.info("Unknown format: " + format);
                    System.exit(1);
                }
            }

            String writerType = config.getWriter();
            logger.info("Using writer type: " + writerType);
            switch (writerType.toLowerCase()) {
                case FILEWRITER -> writer = new FileVectorWriter(config);
                case ASTRA -> writer = new AstraVectorWriter(config);
                case NOOP -> writer = new NoopVectorWriter();
                default -> {
                    logger.info("Unknown writer type: " + writerType);
                    System.exit(1);
                }
            }
            reader.setWriter(writer);
            logger.info("Starting main read loop");
            reader.read();
        } catch (Exception e) {
            logger.error(e);
            System.exit(1);
        }
    }
}
