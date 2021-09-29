package io.nosqlbench.driver.pulsar.exception;

public class PulsarMsgOutOfOrderException extends RuntimeException {

    public PulsarMsgOutOfOrderException(boolean asyncPulsarOp, long nbCycleNum, long curMsgSeqId, long prevMsgSeqId) {
        super("" + (asyncPulsarOp ? "[AsyncAPI]" : "[SyncAPI]" ) +
            " Detected message ordering is not guaranteed (curCycleNum=" + nbCycleNum +
            ", curMsgSeqId=" + curMsgSeqId + ", prevMsgSeqId=" + prevMsgSeqId + "). " +
            "Older messages are received earlier!");
    }
}
