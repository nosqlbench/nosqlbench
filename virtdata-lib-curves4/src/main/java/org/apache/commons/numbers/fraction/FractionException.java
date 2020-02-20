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

/**
 * Package private exception class with constants for frequently used messages.
 */
class FractionException extends ArithmeticException {

    /** Error message for overflow during conversion. */
    static final String ERROR_CONVERSION_OVERFLOW = "Overflow trying to convert {0} to fraction ({1}/{2})";
    /** Error message when iterative conversion fails. */
    static final String ERROR_CONVERSION = "Unable to convert {0} to fraction after {1} iterations";
    /** Error message for overflow by negation. */
    static final String ERROR_NEGATION_OVERFLOW = "overflow in fraction {0}/{1}, cannot negate";
    /** Error message for zero-valued denominator. */
    static final String ERROR_ZERO_DENOMINATOR = "denominator must be different from 0";

    /** Serializable version identifier. */
    private static final long serialVersionUID = 201701191744L;

    /** Arguments for formatting the message. */
    protected Object[] formatArguments;

    /**
     * Create an exception where the message is constructed by applying
     * the {@code format()} method from {@code java.text.MessageFormat}.
     *
     * @param message  the exception message with replaceable parameters
     * @param formatArguments the arguments for formatting the message
     */
    FractionException(String message, Object... formatArguments) {
        super(message);
        this.formatArguments = formatArguments;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return MessageFormat.format(super.getMessage(), formatArguments);
    }


}
