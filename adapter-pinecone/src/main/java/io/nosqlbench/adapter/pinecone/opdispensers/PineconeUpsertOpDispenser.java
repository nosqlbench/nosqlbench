package io.nosqlbench.adapter.pinecone.opdispensers;

import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpsertOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;
/**
 *     float[][] upsertData = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};
 *         List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
 *         List<Vector> upsertVectors = new ArrayList<>();
 *
 *         for (int i = 0; i < upsertData.length; i++) {
 *             upsertVectors.add(Vector.newBuilder()
 *                 .addAllValues(Floats.asList(upsertData[i]))
 *                 .setMetadata(Struct.newBuilder()
 *                     .putFields("some_field", Value.newBuilder().setNumberValue(i).build())
 *                     .build())
 *                 .setId(upsertIds.get(i))
 *                 .build());
 *         }
 *
 *         return UpsertRequest.newBuilder()
 *             .addAllVectors(upsertVectors)
 *             .setNamespace("default-namespace")
 *             .build();
 */


public class PineconeUpsertOpDispenser extends PineconeOpDispenser {
    private final LongFunction<UpsertRequest> upsertRequestFunc;

    public PineconeUpsertOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        indexNameFunc = op.getAsRequiredFunction("upsert", String.class);
        upsertRequestFunc = createUpsertRequestFunc(op);
    }

    private LongFunction<UpsertRequest> createUpsertRequestFunc(ParsedOp op) {
        return null;
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeUpsertOp(pcFunction.apply(value).getConnection(indexNameFunc.apply(value)),
            upsertRequestFunc.apply(value));
    }
}
