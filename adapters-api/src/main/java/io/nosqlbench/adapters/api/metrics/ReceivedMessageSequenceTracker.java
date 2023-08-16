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
public class ReceivedMessageSequenceTracker implements AutoCloseable {
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

    public ReceivedMessageSequenceTracker(final Counter msgErrOutOfSeqCounter, final Counter msgErrDuplicateCounter, final Counter msgErrLossCounter) {
        this(msgErrOutOfSeqCounter, msgErrDuplicateCounter, msgErrLossCounter,
            ReceivedMessageSequenceTracker.DEFAULT_MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS, ReceivedMessageSequenceTracker.DEFAULT_MAX_TRACK_SKIPPED_SEQUENCE_NUMBERS);
    }

    public ReceivedMessageSequenceTracker(final Counter msgErrOutOfSeqCounter, final Counter msgErrDuplicateCounter, final Counter msgErrLossCounter,
                                          final int maxTrackOutOfOrderSequenceNumbers, final int maxTrackSkippedSequenceNumbers) {
        this.msgErrOutOfSeqCounter = msgErrOutOfSeqCounter;
        this.msgErrDuplicateCounter = msgErrDuplicateCounter;
        this.msgErrLossCounter = msgErrLossCounter;
        this.maxTrackOutOfOrderSequenceNumbers = maxTrackOutOfOrderSequenceNumbers;
        this.maxTrackSkippedSequenceNumbers = maxTrackSkippedSequenceNumbers;
        pendingOutOfSeqNumbers = new TreeSet<>();
        skippedSeqNumbers = new TreeSet<>();
    }

    /**
     * Notifies the tracker about a received sequence number
     *
     * @param sequenceNumber the sequence number of the received message
     */
    public void sequenceNumberReceived(final long sequenceNumber) {
        if (-1 == expectedNumber) {
            this.expectedNumber = sequenceNumber + 1;
            return;
        }

        if (sequenceNumber < this.expectedNumber) {
            if (this.skippedSeqNumbers.remove(sequenceNumber)) {
                // late out-of-order delivery was detected
                // decrease the loss counter
                this.msgErrLossCounter.dec();
                // increment the out-of-order counter
                this.msgErrOutOfSeqCounter.inc();
            } else this.msgErrDuplicateCounter.inc();
            return;
        }

        boolean messagesSkipped = false;
        // sequenceNumber == expectedNumber
        if (sequenceNumber > this.expectedNumber) {
            if (this.pendingOutOfSeqNumbers.size() == this.maxTrackOutOfOrderSequenceNumbers)
                messagesSkipped = this.processLowestPendingOutOfSequenceNumber();
            if (!this.pendingOutOfSeqNumbers.add(sequenceNumber)) this.msgErrDuplicateCounter.inc();
        } else this.expectedNumber++;
        this.processPendingOutOfSequenceNumbers(messagesSkipped);
        this.cleanUpTooFarBehindOutOfSequenceNumbers();
    }

    private boolean processLowestPendingOutOfSequenceNumber() {
        // remove the lowest pending out of sequence number
        final Long lowestOutOfSeqNumber = this.pendingOutOfSeqNumbers.first();
        this.pendingOutOfSeqNumbers.remove(lowestOutOfSeqNumber);
        if (lowestOutOfSeqNumber > this.expectedNumber) {
            // skip the expected number ahead to the number after the lowest sequence number
            // increment the counter with the amount of sequence numbers that got skipped
            // keep track of the skipped sequence numbers to detect late out-of-order message delivery
            for (long l = this.expectedNumber; l < lowestOutOfSeqNumber; l++) {
                this.msgErrLossCounter.inc();
                this.skippedSeqNumbers.add(l);
                if (this.skippedSeqNumbers.size() > this.maxTrackSkippedSequenceNumbers)
                    this.skippedSeqNumbers.remove(this.skippedSeqNumbers.first());
            }
            this.expectedNumber = lowestOutOfSeqNumber + 1;
            return true;
        }
        this.msgErrLossCounter.inc();
        return false;
    }

    private void processPendingOutOfSequenceNumbers(final boolean messagesSkipped) {
        // check if there are previously received out-of-order sequence number that have been received
        while (this.pendingOutOfSeqNumbers.remove(this.expectedNumber)) {
            this.expectedNumber++;
            if (!messagesSkipped) this.msgErrOutOfSeqCounter.inc();
        }
    }

    private void cleanUpTooFarBehindOutOfSequenceNumbers() {
        // remove sequence numbers that are too far behind
        for (final Iterator<Long> iterator = this.pendingOutOfSeqNumbers.iterator(); iterator.hasNext(); ) {
            final Long number = iterator.next();
            if (number < (this.expectedNumber - this.maxTrackOutOfOrderSequenceNumbers)) {
                this.msgErrLossCounter.inc();
                iterator.remove();
            } else break;
        }
    }

    /**
     * Handles the possible pending out of sequence numbers. Mainly needed in unit tests to assert the
     * counter values.
     */
    @Override
    public void close() {
        while (!this.pendingOutOfSeqNumbers.isEmpty())
            this.processPendingOutOfSequenceNumbers(this.processLowestPendingOutOfSequenceNumber());
    }

    public int getMaxTrackOutOfOrderSequenceNumbers() {
        return this.maxTrackOutOfOrderSequenceNumbers;
    }

    public int getMaxTrackSkippedSequenceNumbers() {
        return this.maxTrackSkippedSequenceNumbers;
    }
}
