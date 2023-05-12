package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpsertOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.UpsertRequest;
import jakarta.ws.rs.NotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class PineconeUpsertOpDispenser extends PineconeOpDispenser {
    private static final Logger LOGGER = LogManager.getLogger(PineconeUpsertOpDispenser.class);
    private final LongFunction<UpsertRequest> upsertRequestFunc;

    /**
     * Create a new PineconeUpsertOpDispenser subclassed from {@link PineconeOpDispenser}.
     *
     * @param adapter           The associated {@link PineconeDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param pcFunction        A function to return the associated context of this activity (see {@link PineconeSpace})
     * @param targetFunction    A LongFunction that returns the specified Pinecone Index for this Op
     */
    public PineconeUpsertOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        upsertRequestFunc = createUpsertRequestFunc(op);
    }

    /*
     *float[][] upsertData = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};
     *List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
     *List<Vector> upsertVectors = new ArrayList<>();
     *
     *for (int i = 0; i < upsertData.length; i++) {
     *upsertVectors.add(Vector.newBuilder()
     *.addAllValues(Floats.asList(upsertData[i]))
     *.setMetadata(Struct.newBuilder()
     *.putFields("some_field", Value.newBuilder().setNumberValue(i).build())
     *.build())
     *.setId(upsertIds.get(i))
     *.build());
     * }
     *
     *return UpsertRequest.newBuilder()
     *.addAllVectors(upsertVectors)
     *.setNamespace("default-namespace")
     *.build();
     */
    private LongFunction<UpsertRequest> createUpsertRequestFunc(ParsedOp op) {
        throw new NotSupportedException("Pinecone Upsert Request Op not yet supported");
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeUpsertOp(pcFunction.apply(value).getConnection(targetFunction.apply(value)),
            upsertRequestFunc.apply(value));
    }
}
