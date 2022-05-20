package io.nosqlbench.driver.pulsar.ops;

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


import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import java.util.*;
import org.apache.commons.lang3.RandomUtils;

/**
 * Handles adding a monotonic sequence number to message properties of sent messages
 */
class MessageSequenceNumberSendingHandler {
    static final int SIMULATED_ERROR_PROBABILITY_PERCENTAGE = 10;
    long number = 1;
    Queue<Long> outOfOrderNumbers;

    public long getNextSequenceNumber(Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> simulatedErrorTypes) {
        return getNextSequenceNumber(simulatedErrorTypes, SIMULATED_ERROR_PROBABILITY_PERCENTAGE);
    }

    long getNextSequenceNumber(Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> simulatedErrorTypes, int errorProbabilityPercentage) {
        simulateError(simulatedErrorTypes, errorProbabilityPercentage);
        return nextNumber();
    }

    private void simulateError(Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> simulatedErrorTypes, int errorProbabilityPercentage) {
        if (!simulatedErrorTypes.isEmpty() && shouldSimulateError(errorProbabilityPercentage)) {
            int selectIndex = 0;
            int numberOfErrorTypes = simulatedErrorTypes.size();
            if (numberOfErrorTypes > 1) {
                // pick one of the simulated error type randomly
                selectIndex = RandomUtils.nextInt(0, numberOfErrorTypes);
            }
            PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE errorType = simulatedErrorTypes.stream()
                .skip(selectIndex)
                .findFirst()
                .get();
            switch (errorType) {
                case OutOfOrder:
                    // simulate message out of order
                    injectMessagesOutOfOrder();
                    break;
                case MsgDup:
                    // simulate message duplication
                    injectMessageDuplication();
                    break;
                case MsgLoss:
                    // simulate message loss
                    injectMessageLoss();
                    break;
            }
        }
    }

    private boolean shouldSimulateError(int errorProbabilityPercentage) {
        // Simulate error with the specified probability
        return RandomUtils.nextInt(0, 100) < errorProbabilityPercentage;
    }

    long nextNumber() {
        if (outOfOrderNumbers != null) {
            long nextNumber = outOfOrderNumbers.poll();
            if (outOfOrderNumbers.isEmpty()) {
                outOfOrderNumbers = null;
            }
            return nextNumber;
        }
        return number++;
    }

    void injectMessagesOutOfOrder() {
        if (outOfOrderNumbers == null) {
            outOfOrderNumbers = new ArrayDeque<>(Arrays.asList(number + 2, number, number + 1));
            number += 3;
        }
    }

    void injectMessageDuplication() {
        if (outOfOrderNumbers == null) {
            number--;
        }
    }

    void injectMessageLoss() {
        if (outOfOrderNumbers == null) {
            number++;
        }
    }
}
