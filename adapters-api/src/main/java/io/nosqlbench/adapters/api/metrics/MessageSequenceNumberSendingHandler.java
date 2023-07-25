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
 */

package io.nosqlbench.adapters.api.metrics;

import io.nosqlbench.adapters.api.metrics.EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

/**
 * Handles adding a monotonic sequence number to message properties of sent messages
 */
public class MessageSequenceNumberSendingHandler {
    static final int SIMULATED_ERROR_PROBABILITY_PERCENTAGE = 10;
    long number = 1;
    Queue<Long> outOfOrderNumbers;

    public long getNextSequenceNumber(final Set<MSG_SEQ_ERROR_SIMU_TYPE> simulatedErrorTypes) {
        return this.getNextSequenceNumber(simulatedErrorTypes, MessageSequenceNumberSendingHandler.SIMULATED_ERROR_PROBABILITY_PERCENTAGE);
    }

    long getNextSequenceNumber(final Set<MSG_SEQ_ERROR_SIMU_TYPE> simulatedErrorTypes, final int errorProbabilityPercentage) {
        this.simulateError(simulatedErrorTypes, errorProbabilityPercentage);
        return this.nextNumber();
    }

    private void simulateError(final Set<MSG_SEQ_ERROR_SIMU_TYPE> simulatedErrorTypes, final int errorProbabilityPercentage) {
        if (!simulatedErrorTypes.isEmpty() && this.shouldSimulateError(errorProbabilityPercentage)) {
            int selectIndex = 0;
            final int numberOfErrorTypes = simulatedErrorTypes.size();
            // pick one of the simulated error type randomly
            if (1 < numberOfErrorTypes) selectIndex = RandomUtils.nextInt(0, numberOfErrorTypes);
            final MSG_SEQ_ERROR_SIMU_TYPE errorType = simulatedErrorTypes.stream()
                .skip(selectIndex)
                .findFirst()
                .get();
            switch (errorType) {
                case OutOfOrder:
                    // simulate message out of order
                    this.injectMessagesOutOfOrder();
                    break;
                case MsgDup:
                    // simulate message duplication
                    this.injectMessageDuplication();
                    break;
                case MsgLoss:
                    // simulate message loss
                    this.injectMessageLoss();
                    break;
            }
        }
    }

    private boolean shouldSimulateError(final int errorProbabilityPercentage) {
        // Simulate error with the specified probability
        return RandomUtils.nextInt(0, 100) < errorProbabilityPercentage;
    }

    long nextNumber() {
        if (null != outOfOrderNumbers) {
            final long nextNumber = this.outOfOrderNumbers.poll();
            if (this.outOfOrderNumbers.isEmpty()) this.outOfOrderNumbers = null;
            return nextNumber;
        }
        long l = this.number;
        this.number++;
        return l;
    }

    void injectMessagesOutOfOrder() {
        if (null == outOfOrderNumbers) {
            this.outOfOrderNumbers = new ArrayDeque<>(Arrays.asList(this.number + 2, this.number, this.number + 1));
            this.number += 3;
        }
    }

    void injectMessageDuplication() {
        if (null == outOfOrderNumbers) this.number--;
    }

    void injectMessageLoss() {
        if (null == outOfOrderNumbers) this.number++;
    }
}
