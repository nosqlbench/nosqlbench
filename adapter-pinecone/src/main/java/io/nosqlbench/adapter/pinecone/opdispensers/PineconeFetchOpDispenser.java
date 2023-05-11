package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeFetchOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.FetchRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

public class PineconeFetchOpDispenser extends PineconeOpDispenser {
    private final LongFunction<FetchRequest> fetchRequestFunc;

    public PineconeFetchOpDispenser(PineconeDriverAdapter adapter,
                                    ParsedOp op,
                                    LongFunction<PineconeSpace> pcFunction,
                                    LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        indexNameFunc = op.getAsRequiredFunction("fetch", String.class);
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
        return new PineconeFetchOp(pcFunction.apply(value).getConnection(indexNameFunc.apply(value)),
            fetchRequestFunc.apply(value));
    }
}
