package io.nosqlbench.driver.pulsar.exception;

public class PulsarMsgLossException extends RuntimeException {

    public PulsarMsgLossException(boolean asyncPulsarOp, long nbCycleNum, long curMsgSeqId, long prevMsgSeqId) {
        super("" + (asyncPulsarOp ? "[AsyncAPI]" : "[SyncAPI]") +
            " Detected message sequence id gap (curCycleNum=" + nbCycleNum +
            ", curMsgSeqId=" + curMsgSeqId + ", prevMsgSeqId=" + prevMsgSeqId + "). " +
            "Some published messages are not received!");
    }
}
