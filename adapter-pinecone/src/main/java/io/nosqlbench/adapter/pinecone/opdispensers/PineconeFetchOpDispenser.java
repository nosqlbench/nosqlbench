package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeFetchOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.FetchRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

public class PineconeFetchOpDispenser extends PineconeOpDispenser {
    private static final Logger LOGGER = LogManager.getLogger(PineconeFetchOpDispenser.class);
    private final LongFunction<FetchRequest> fetchRequestFunc;

    /**
     * Create a new PineconeFetchOpDispenser subclassed from {@link PineconeOpDispenser}.
     *
     * @param adapter           The associated {@link PineconeDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param pcFunction        A function to return the associated context of this activity (see {@link PineconeSpace})
     * @param targetFunction    A LongFunction that returns the specified Pinecone Index for this Op
     */
    public PineconeFetchOpDispenser(PineconeDriverAdapter adapter,
                                    ParsedOp op,
                                    LongFunction<PineconeSpace> pcFunction,
                                    LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        fetchRequestFunc = createFetchRequestFunction(op);
    }

    private LongFunction<FetchRequest> createFetchRequestFunction(ParsedOp op) {
        LongFunction<FetchRequest.Builder> rFunc = l -> FetchRequest.newBuilder();

        Optional<LongFunction<String>> nFunc = op.getAsOptionalFunction("namespace", String.class);
        if (nFunc.isPresent()) {
            LongFunction<FetchRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = nFunc.get();
            rFunc = l -> finalFunc.apply(l).setNamespace(af.apply(l));
        }

        Optional<LongFunction<String>> iFunc = op.getAsOptionalFunction("ids", String.class);
        if (iFunc.isPresent()) {
            LongFunction<FetchRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = iFunc.get();
            LongFunction<List<String>> alf = l -> {
                String[] vals = af.apply(l).split(",");
                return Arrays.asList(vals);
            };
            rFunc = l -> finalFunc.apply(l).addAllIds(alf.apply(l));
        }

        LongFunction<FetchRequest.Builder> finalRFunc = rFunc;
        return l -> finalRFunc.apply(l).build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeFetchOp(pcFunction.apply(value).getConnection(targetFunction.apply(value)),
            fetchRequestFunc.apply(value));
    }
}
