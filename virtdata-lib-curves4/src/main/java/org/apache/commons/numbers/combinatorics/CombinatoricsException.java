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
package org.apache.commons.numbers.combinatorics;

import java.text.MessageFormat;

/**
 * Package private exception class with constants for frequently used messages.
 */
class CombinatoricsException extends IllegalArgumentException {
    /** Error message for "out of range" condition. */
    static final String OUT_OF_RANGE = "Number {0} is out of range [{1}, {2}]";
    /** Error message for "out of range" condition. */
    static final String NEGATIVE = "Number {0} is negative";
    /** Error message for "mismatch" condition. */
    static final String MISMATCH = "Expected {1} but was {0}";

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20170515L;

    /** Arguments for formatting the message. */
    protected Object[] formatArguments;

    /**
     * Create an exception where the message is constructed by applying
     * the {@code format()} method from {@code java.text.MessageFormat}.
     *
     * @param message  the exception message with replaceable parameters
     * @param formatArguments the arguments for formatting the message
     */
    CombinatoricsException(String message, Object... formatArguments) {
        super(message);
        this.formatArguments = formatArguments;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return MessageFormat.format(super.getMessage(), formatArguments);
    }
}
