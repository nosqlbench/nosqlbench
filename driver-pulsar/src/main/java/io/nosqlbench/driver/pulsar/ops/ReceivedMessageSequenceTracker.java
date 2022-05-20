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


import com.codahale.metrics.Counter;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Detects message loss, message duplication and out-of-order message delivery
 * based on a monotonic sequence number that each received message contains.
 * <p>
 * Out-of-order messages are detected with a maximum look behind of 1000 sequence number entries.
 * This is currently defined as a constant, {@link ReceivedMessageSequenceTracker#DEFAULT_MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS}.
 */
class ReceivedMessageSequenceTracker implements AutoCloseable {
    private static final int DEFAULT_MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS = 1000;
    private static final int DEFAULT_MAX_TRACK_SKIPPED_SEQUENCE_NUMBERS = 1000;
    // message out-of-sequence error counter
    private final Counter msgErrOutOfSeqCounter;
    // duplicate message error counter
    private final Counter msgErrDuplicateCounter;
    // message loss error counter
    private final Counter msgErrLossCounter;
    private final SortedSet<Long> pendingOutOfSeqNumbers;
    private final int maxTrackOutOfOrderSequenceNumbers;
    private final SortedSet<Long> skippedSeqNumbers;
    private final int maxTrackSkippedSequenceNumbers;
    private long expectedNumber = -1;

    public ReceivedMessageSequenceTracker(Counter msgErrOutOfSeqCounter, Counter msgErrDuplicateCounter, Counter msgErrLossCounter) {
        this(msgErrOutOfSeqCounter, msgErrDuplicateCounter, msgErrLossCounter,
            DEFAULT_MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS, DEFAULT_MAX_TRACK_SKIPPED_SEQUENCE_NUMBERS);
    }

    public ReceivedMessageSequenceTracker(Counter msgErrOutOfSeqCounter, Counter msgErrDuplicateCounter, Counter msgErrLossCounter,
                                          int maxTrackOutOfOrderSequenceNumbers, int maxTrackSkippedSequenceNumbers) {
        this.msgErrOutOfSeqCounter = msgErrOutOfSeqCounter;
        this.msgErrDuplicateCounter = msgErrDuplicateCounter;
        this.msgErrLossCounter = msgErrLossCounter;
        this.maxTrackOutOfOrderSequenceNumbers = maxTrackOutOfOrderSequenceNumbers;
        this.maxTrackSkippedSequenceNumbers = maxTrackSkippedSequenceNumbers;
        this.pendingOutOfSeqNumbers = new TreeSet<>();
        this.skippedSeqNumbers = new TreeSet<>();
    }

    /**
     * Notifies the tracker about a received sequence number
     *
     * @param sequenceNumber the sequence number of the received message
     */
    public void sequenceNumberReceived(long sequenceNumber) {
        if (expectedNumber == -1) {
            expectedNumber = sequenceNumber + 1;
            return;
        }

        if (sequenceNumber < expectedNumber) {
            if (skippedSeqNumbers.remove(sequenceNumber)) {
                // late out-of-order delivery was detected
                // decrease the loss counter
                msgErrLossCounter.dec();
                // increment the out-of-order counter
                msgErrOutOfSeqCounter.inc();
            } else {
                msgErrDuplicateCounter.inc();
            }
            return;
        }

        boolean messagesSkipped = false;
        if (sequenceNumber > expectedNumber) {
            if (pendingOutOfSeqNumbers.size() == maxTrackOutOfOrderSequenceNumbers) {
                messagesSkipped = processLowestPendingOutOfSequenceNumber();
            }
            if (!pendingOutOfSeqNumbers.add(sequenceNumber)) {
                msgErrDuplicateCounter.inc();
            }
        } else {
            // sequenceNumber == expectedNumber
            expectedNumber++;
        }
        processPendingOutOfSequenceNumbers(messagesSkipped);
        cleanUpTooFarBehindOutOfSequenceNumbers();
    }

    private boolean processLowestPendingOutOfSequenceNumber() {
        // remove the lowest pending out of sequence number
        Long lowestOutOfSeqNumber = pendingOutOfSeqNumbers.first();
        pendingOutOfSeqNumbers.remove(lowestOutOfSeqNumber);
        if (lowestOutOfSeqNumber > expectedNumber) {
            // skip the expected number ahead to the number after the lowest sequence number
            // increment the counter with the amount of sequence numbers that got skipped
            // keep track of the skipped sequence numbers to detect late out-of-order message delivery
            for (long l = expectedNumber; l < lowestOutOfSeqNumber; l++) {
                msgErrLossCounter.inc();
                skippedSeqNumbers.add(l);
                if (skippedSeqNumbers.size() > maxTrackSkippedSequenceNumbers) {
                    skippedSeqNumbers.remove(skippedSeqNumbers.first());
                }
            }
            expectedNumber = lowestOutOfSeqNumber + 1;
            return true;
        } else {
            msgErrLossCounter.inc();
        }
        return false;
    }

    private void processPendingOutOfSequenceNumbers(boolean messagesSkipped) {
        // check if there are previously received out-of-order sequence number that have been received
        while (pendingOutOfSeqNumbers.remove(expectedNumber)) {
            expectedNumber++;
            if (!messagesSkipped) {
                msgErrOutOfSeqCounter.inc();
            }
        }
    }

    private void cleanUpTooFarBehindOutOfSequenceNumbers() {
        // remove sequence numbers that are too far behind
        for (Iterator<Long> iterator = pendingOutOfSeqNumbers.iterator(); iterator.hasNext(); ) {
            Long number = iterator.next();
            if (number < expectedNumber - maxTrackOutOfOrderSequenceNumbers) {
                msgErrLossCounter.inc();
                iterator.remove();
            } else {
                break;
            }
        }
    }

    /**
     * Handles the possible pending out of sequence numbers. Mainly needed in unit tests to assert the
     * counter values.
     */
    @Override
    public void close() {
        while (!pendingOutOfSeqNumbers.isEmpty()) {
            processPendingOutOfSequenceNumbers(processLowestPendingOutOfSequenceNumber());
        }
    }

    public int getMaxTrackOutOfOrderSequenceNumbers() {
        return maxTrackOutOfOrderSequenceNumbers;
    }

    public int getMaxTrackSkippedSequenceNumbers() {
        return maxTrackSkippedSequenceNumbers;
    }
}
