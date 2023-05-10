package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeQueryOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryVector;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.LongFunction;

/**
 float[] rawVector = {1.0F, 2.0F, 3.0F};
 QueryVector queryVector = QueryVector.newBuilder()
 .addAllValues(Floats.asList(rawVector))
 .setFilter(Struct.newBuilder()
 .putFields("some_field", Value.newBuilder()
 .setStructValue(Struct.newBuilder()
 .putFields("$lt", Value.newBuilder()
 .setNumberValue(3)
 .build()))
 .build())
 .build())
 .setNamespace("default-namespace")
 .build();

 QueryRequest queryRequest = QueryRequest.newBuilder()
 .addQueries(queryVector)
 .setNamespace("default-namespace")
 .setTopK(2)
 .setIncludeMetadata(true)
 .build();
 }
 **/

public class PineconeQueryOpDispenser extends PineconeOpDispenser {
    private final LongFunction<QueryRequest> queryRequestFunc;
    private final String indexName;

    public PineconeQueryOpDispenser(PineconeDriverAdapter adapter,
                                    ParsedOp op,
                                    LongFunction<PineconeSpace> pcFunction,
                                    LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        indexName = op.getAsRequiredFunction("query", String.class).apply(0);
        queryRequestFunc = createQueryRequestFunc(op, createQueryVectorFunc(op));
    }

    private LongFunction<QueryRequest> createQueryRequestFunc(ParsedOp op, LongFunction<QueryVector> queryVectorFunc) {
        LongFunction<QueryRequest.Builder> rFunc = l -> QueryRequest.newBuilder();

        Optional<LongFunction<String>> nFunc = op.getAsOptionalFunction("namespace", String.class);
        if (nFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = nFunc.get();
            rFunc = l -> finalFunc.apply(l).setNamespace(af.apply(l));
        }

        Optional<LongFunction<Integer>> tFunc = op.getAsOptionalFunction("topk", Integer.class);
        if (tFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Integer> af = tFunc.get();
            rFunc = l -> finalFunc.apply(l).setTopK(af.apply(l));
        }

        Optional<LongFunction<Boolean>> mFunc = op.getAsOptionalFunction("includemetadata", Boolean.class);
        if (mFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Boolean> af = mFunc.get();
            rFunc = l -> finalFunc.apply(l).setIncludeMetadata(af.apply(l));
        }

        LongFunction<QueryRequest.Builder> returnFunc = rFunc;
        rFunc = l -> returnFunc.apply(l).addQueries(queryVectorFunc.apply(l));
        LongFunction<QueryRequest.Builder> finalRFunc = rFunc;
        return l -> finalRFunc.apply(l).build();
    }

    private LongFunction<QueryVector> createQueryVectorFunc(ParsedOp op) {
        LongFunction<QueryVector.Builder> vFunc = l -> QueryVector.newBuilder();

        Optional<LongFunction<String>> qFunc = op.getAsOptionalFunction("query", String.class);
        if (qFunc.isPresent()) {
            LongFunction<QueryVector.Builder> finalFunc = vFunc;
            LongFunction<String> af = qFunc.get();
            LongFunction<ArrayList<Float>> alf = l -> {
                String[] vals = af.apply(l).split(",");
                ArrayList<Float> fVals = new ArrayList<Float>();
                for (int i = 0; i < vals.length; i++) {
                    fVals.add(Float.valueOf(vals[i]));
                }
                return fVals;
            };
            vFunc = l -> finalFunc.apply(l).addAllValues(alf.apply(l));
        }

        Optional<LongFunction<String>> nFunc = op.getAsOptionalFunction("namespace", String.class);
        if (nFunc.isPresent()) {
            LongFunction<QueryVector.Builder> finalFunc = vFunc;
            LongFunction<String> af = nFunc.get();
            vFunc = l -> finalFunc.apply(l).setNamespace(af.apply(l));
        }
        //TODO: Add in optional filters

        LongFunction<QueryVector.Builder> finalVFunc = vFunc;
        return l -> finalVFunc.apply(l).build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeQueryOp(pcFunction.apply(value).getConnection(indexName), queryRequestFunc.apply(value));
    }
}
