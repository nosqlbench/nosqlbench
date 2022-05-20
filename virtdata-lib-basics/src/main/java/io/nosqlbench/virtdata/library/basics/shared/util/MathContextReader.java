package io.nosqlbench.virtdata.library.basics.shared.util;

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


import io.nosqlbench.nb.api.errors.BasicError;

import java.math.MathContext;

public class MathContextReader {

    public static MathContext getMathContext(String name) {
        try {

            switch (name.toLowerCase()) {
                case "unlimited":
                    return MathContext.UNLIMITED;
                case "decimal32":
                    return MathContext.DECIMAL32;
                case "decimal64":
                    return MathContext.DECIMAL64;
                case "decimal128":
                    return MathContext.DECIMAL128;
                default:
                    return new MathContext(name);
            }
        } catch (IllegalArgumentException iae) {
            throw new BasicError("'" + name + "' was not a valid format for a new MathContext(String), try something " +
                "like 'precision=17 roundingMode=UP");
        }

    }

}
