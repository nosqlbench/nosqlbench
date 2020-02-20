/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.numbers.fraction;

import java.text.MessageFormat;
import java.text.ParseException;

/**
 * Error thrown when a string cannot be parsed into a fraction.
 */
class FractionParseException extends ParseException {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 201701181879L;

    /**
     * Constructs an exception with specified formatted detail message.
     * Message formatting is delegated to {@link MessageFormat}.
     * @param source string being parsed
     * @param position position of error
     * @param type type of target object
     */
    FractionParseException(String source, int position, Class<?> type) {
        super(MessageFormat.format("string \"{0}\" unparseable (from position {1}) as an object of type {2}",
                                   source, position, type),
              position);
    }
}
