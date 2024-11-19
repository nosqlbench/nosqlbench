package io.nosqlbench.nb.api.advisor;

/*
 * Copyright (c) nosqlbench
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
import org.apache.logging.log4j.Level;

public class NBAdvisorOutput {
    private final static Logger logger = LogManager.getLogger("ADVISOR");

    public static void render(Level level,String message) {
        if (NBAdvisorLevel.get() == NBAdvisorLevel.validate) {
            output(level, message);
        }
    }
    
    public static void test(String message) {
        output(Level.WARN, message);
        if (NBAdvisorLevel.get() == NBAdvisorLevel.enforce) {
            throw new NBAdvisorException(message, 2);
        }
    }
    
    public static void output(Level level,String message) {
        if (level ==  Level.INFO) {
            NBAdvisorOutput.logger.info(message);
        } else if (level == Level.WARN) {
            NBAdvisorOutput.logger.warn(message);
        } else if (level == Level.ERROR) {
            NBAdvisorOutput.logger.error(message);
        }
    }

}
