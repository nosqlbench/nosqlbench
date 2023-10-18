package io.nosqlbench.adapter.jdbc.utils;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JDBCAdapterUtil {

    private final static Logger logger = LogManager.getLogger(JDBCAdapterUtil.class);
    public static void pauseCurThreadExec(int pauseInSec) {
        if (pauseInSec > 0) {
            try {
                Thread.sleep(pauseInSec * 1000L);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}

