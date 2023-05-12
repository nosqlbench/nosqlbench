package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpdateOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.UpdateRequest;
import jakarta.ws.rs.NotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class PineconeUpdateOpDispenser extends PineconeOpDispenser {
    private static final Logger LOGGER = LogManager.getLogger(PineconeUpdateOpDispenser.class);
    private final LongFunction<UpdateRequest> updateRequestFunc;

    /**
     * Create a new PineconeUpdateOpDispenser subclassed from {@link PineconeOpDispenser}.
     *
     * @param adapter           The associated {@link PineconeDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param pcFunction        A function to return the associated context of this activity (see {@link PineconeSpace})
     * @param targetFunction    A LongFunction that returns the specified Pinecone Index for this Op
     */
    public PineconeUpdateOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        updateRequestFunc = createUpdateRequestFunction(op);
    }

    private LongFunction<UpdateRequest> createUpdateRequestFunction(ParsedOp op) {
        throw new NotSupportedException("Pinecone Update Request Op not yet supported");
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeUpdateOp(pcFunction.apply(value).getConnection(targetFunction.apply(value)),
            updateRequestFunc.apply(value));
    }
}
