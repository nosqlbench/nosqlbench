package io.nosqlbench.driver.pulsar.exception;

public class PulsarMsgDuplicateException extends RuntimeException {

    public PulsarMsgDuplicateException(boolean asyncPulsarOp, long nbCycleNum, long curMsgSeqId, long prevMsgSeqId) {
        super("" + (asyncPulsarOp ? "[AsyncAPI]" : "[SyncAPI]") +
            " Detected duplicate message when message deduplication is enabled (curCycleNum=" + nbCycleNum +
            ", curMsgSeqId=" + curMsgSeqId + ", prevMsgSeqId=" + prevMsgSeqId + ").");
    }
}
