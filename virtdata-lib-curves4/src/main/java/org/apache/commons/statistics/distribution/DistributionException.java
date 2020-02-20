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
package org.apache.commons.statistics.distribution;

import java.text.MessageFormat;

/**
 * Package private exception class with constants for frequently used messages.
 */
class DistributionException extends IllegalArgumentException {
    /** Error message for "too large" condition. */
    static final String TOO_LARGE = "{0} > {1}";
    /** Error message for "too small" condition. */
    static final String TOO_SMALL = "{0} < {1}";
    /** Error message for "out of range" condition. */
    static final String OUT_OF_RANGE = "Number {0} is out of range [{1}, {2}]";
    /** Error message for "out of range" condition. */
    static final String NEGATIVE = "Number {0} is negative";
    /** Error message for "mismatch" condition. */
    static final String MISMATCH = "Expected {1} but was {0}";
    /** Error message for "failed bracketing" condition. */
    static final String BRACKETING = "No bracketing: f({0})={1}, f({2})={3}";

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20180119L;

    /** Arguments for formatting the message. */
    private Object[] formatArguments;

    /**
     * Create an exception where the message is constructed by applying
     * the {@code format()} method from {@code java.text.MessageFormat}.
     *
     * @param message  the exception message with replaceable parameters
     * @param formatArguments the arguments for formatting the message
     */
    DistributionException(String message, Object... formatArguments) {
        super(message);
        this.formatArguments = formatArguments;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return MessageFormat.format(super.getMessage(), formatArguments);
    }
}
