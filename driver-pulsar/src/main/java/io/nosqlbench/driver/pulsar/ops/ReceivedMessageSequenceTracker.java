package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Detects message loss, message duplication and out-of-order message delivery
 * based on a monotonic sequence number that each received message contains.
 *
 * Out-of-order messages are detected with a maximum look behind of 20 sequence number entries.
 * This is currently defined as a constant, {@link ReceivedMessageSequenceTracker#MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS}.
 */
class ReceivedMessageSequenceTracker implements AutoCloseable{
    public static final int MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS = 20;
    // message out-of-sequence error counter
    private final Counter msgErrOutOfSeqCounter;
    // message out-of-sequence error counter
    private final Counter msgErrDuplicateCounter;
    // message loss error counter
    private final Counter msgErrLossCounter;
    long expectedNumber = -1;

    SortedSet<Long> pendingOutOfSeqNumbers = new TreeSet<>();


    ReceivedMessageSequenceTracker(Counter msgErrOutOfSeqCounter, Counter msgErrDuplicateCounter, Counter msgErrLossCounter) {
        this.msgErrOutOfSeqCounter = msgErrOutOfSeqCounter;
        this.msgErrDuplicateCounter = msgErrDuplicateCounter;
        this.msgErrLossCounter = msgErrLossCounter;
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
            msgErrDuplicateCounter.inc();
            return;
        }

        boolean messagesSkipped = false;
        if (sequenceNumber > expectedNumber) {
            if (pendingOutOfSeqNumbers.size() == MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS) {
                messagesSkipped = processEarliestPendingOutOfSequenceNumber();
            }
            pendingOutOfSeqNumbers.add(sequenceNumber);
        } else {
            // sequenceNumber == expectedNumber
            expectedNumber++;
        }
        processPendingOutOfSequenceNumbers(messagesSkipped);
        cleanUpTooFarBehindOutOfSequenceNumbers();
    }

    private boolean processEarliestPendingOutOfSequenceNumber() {
        // remove the earliest pending out of sequence number
        Long earliestOutOfSeqNumber = pendingOutOfSeqNumbers.first();
        pendingOutOfSeqNumbers.remove(earliestOutOfSeqNumber);
        if (earliestOutOfSeqNumber > expectedNumber) {
            // skip the expected number ahead to the number after the earliest sequence number
            // increment the counter with the amount of sequence numbers that got skipped
            msgErrLossCounter.inc(earliestOutOfSeqNumber - expectedNumber);
            expectedNumber = earliestOutOfSeqNumber + 1;
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
            if (number < expectedNumber - MAX_TRACK_OUT_OF_ORDER_SEQUENCE_NUMBERS) {
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
            processPendingOutOfSequenceNumbers(processEarliestPendingOutOfSequenceNumber());
        }
    }
}
