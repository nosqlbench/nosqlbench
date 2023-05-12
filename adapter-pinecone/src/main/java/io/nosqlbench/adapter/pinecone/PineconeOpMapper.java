package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.pinecone.opdispensers.*;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeOpTypes;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class PineconeOpMapper implements OpMapper<PineconeOp> {
    private static final Logger LOGGER = LogManager.getLogger(PineconeOpMapper.class);
    private final PineconeDriverAdapter adapter;
    private final DriverSpaceCache<? extends PineconeSpace> spaceCache;
    private final NBConfiguration cfg;

    /**
     * Create a new PineconeOpMapper implementing the {@link OpMapper} interface.
     *
     * @param adapter       The associated {@link PineconeDriverAdapter}
     * @param spaceCache    A cached context Object of thpe {@link PineconeSpace})
     * @param cfg           The configuration ({@link NBConfiguration}) for this nb run
     */
    public PineconeOpMapper(PineconeDriverAdapter adapter,
                            DriverSpaceCache<? extends PineconeSpace> spaceCache,
                            NBConfiguration cfg) {
        this.adapter = adapter;
        this.spaceCache = spaceCache;
        this.cfg = cfg;
    }

    /**
     * Given an instance of a {@link ParsedOp} returns the appropriate {@link PineconeOpDispenser} subclass
     *
     * @param op    The ParsedOp to be evaluated
     * @return      The correct PineconeOpDispenser subclass based on the op type
     */
    @Override
    public OpDispenser<? extends PineconeOp> apply(ParsedOp op) {
        LongFunction<String> spaceFunction = op.getAsFunctionOr("space", "default");
        LongFunction<PineconeSpace> pcFunction = l -> spaceCache.get(spaceFunction.apply(l));

        TypeAndTarget<PineconeOpTypes, String> opType = op.getTypeAndTarget(PineconeOpTypes.class, String.class, "type", "index");

        LOGGER.info(() -> "Using " + opType.enumId + " statement form for '" + op.getName());

        return switch (opType.enumId) {
            case query ->
                new PineconeQueryOpDispenser(adapter, op, pcFunction, opType.targetFunction);
            case update ->
                new PineconeUpdateOpDispenser(adapter, op, pcFunction, opType.targetFunction);
            case upsert ->
                new PineconeUpsertOpDispenser(adapter, op, pcFunction, opType.targetFunction);
            case delete ->
                new PineconeDeleteOpDispenser(adapter, op, pcFunction, opType.targetFunction);
            case describeindexstats ->
                new PineconeDescribeIndexStatsOpDispenser(adapter, op, pcFunction, opType.targetFunction);
            case fetch ->
                new PineconeFetchOpDispenser(adapter, op, pcFunction, opType.targetFunction);
        };
    }
}

