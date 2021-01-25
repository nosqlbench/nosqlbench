package com.datastax.ebdrivers.kafkaproducer;

import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class KafkaAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(KafkaAction.class);

    private final KafkaProducerActivity activity;
    private final int slot;

    private OpSequence<KafkaStatement> sequencer;

    public KafkaAction(KafkaProducerActivity activity, int slot) {
        this.activity = activity;
        this.slot = slot;
    }

    @Override
    public void init() {
        this.sequencer = activity.getOpSequencer();
    }

    @Override
    public int runCycle(long cycle) {
        sequencer.get(cycle).write(cycle);
        return 1;
    }

}
