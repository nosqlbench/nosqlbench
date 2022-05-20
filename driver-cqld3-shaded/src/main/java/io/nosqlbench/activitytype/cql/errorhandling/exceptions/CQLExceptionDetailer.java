package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

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


import com.datastax.driver.core.exceptions.ReadTimeoutException;
import com.datastax.driver.core.exceptions.WriteTimeoutException;

public class CQLExceptionDetailer {

    public static String messageFor(long cycle, Throwable e) {

        if (e instanceof ReadTimeoutException) {
            ReadTimeoutException rte = (ReadTimeoutException) e;
            return rte.getMessage() +
                    ", coordinator: " + rte.getHost() +
                    ", wasDataRetrieved: " + rte.wasDataRetrieved();
        }

        if (e instanceof WriteTimeoutException) {
            WriteTimeoutException wte = (WriteTimeoutException) e;
            return wte.getMessage() +
                    ", coordinator: " + wte.getHost();
        }

        return e.getMessage();
    }
}
