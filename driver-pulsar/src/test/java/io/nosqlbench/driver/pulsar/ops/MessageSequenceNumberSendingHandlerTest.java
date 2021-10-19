package io.nosqlbench.driver.pulsar.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MessageSequenceNumberSendingHandlerTest {
    MessageSequenceNumberSendingHandler sequenceNumberSendingHandler = new MessageSequenceNumberSendingHandler();

    @Test
    void shouldAddMonotonicSequence() {
        for (long l = 1; l <= 100; l++) {
            assertEquals(l, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        }
    }

    @Test
    void shouldInjectMessageLoss() {
        assertEquals(1L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(3L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.singleton(PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE.MsgLoss), 100));
    }

    @Test
    void shouldInjectMessageDuplication() {
        assertEquals(1L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(1L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.singleton(PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE.MsgDup), 100));
    }

    @Test
    void shouldInjectMessageOutOfOrder() {
        assertEquals(1L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(4L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.singleton(PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE.OutOfOrder), 100));
        assertEquals(2L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(3L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(5L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(6, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
    }

    @Test
    void shouldInjectOneOfTheSimulatedErrorsRandomly() {
        Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> allErrorTypes = new HashSet<>(Arrays.asList(PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE.values()));

        assertEquals(1L, sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        long previousSequenceNumber = 1L;
        int outOfSequenceInjectionCounter = 0;
        int messageDupCounter = 0;
        int messageLossCounter = 0;
        int successCounter = 0;
        for (int i = 0; i < 1000; i++) {
            long nextSequenceNumber = sequenceNumberSendingHandler.getNextSequenceNumber(allErrorTypes);
            if (nextSequenceNumber >= previousSequenceNumber + 3) {
                outOfSequenceInjectionCounter++;
            } else if (nextSequenceNumber <= previousSequenceNumber) {
                messageDupCounter++;
            } else if (nextSequenceNumber >= previousSequenceNumber + 2) {
                messageLossCounter++;
            } else if (nextSequenceNumber == previousSequenceNumber + 1) {
                successCounter++;
            }
            previousSequenceNumber = nextSequenceNumber;
        }
        assertTrue(outOfSequenceInjectionCounter > 0);
        assertTrue(messageDupCounter > 0);
        assertTrue(messageLossCounter > 0);
        assertEquals(1000, outOfSequenceInjectionCounter + messageDupCounter + messageLossCounter + successCounter);
    }

}
