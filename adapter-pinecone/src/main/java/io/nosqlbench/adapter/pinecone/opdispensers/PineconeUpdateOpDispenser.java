package io.nosqlbench.adapter.pinecone.opdispensers;

import com.google.protobuf.Struct;
import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpdateOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.SparseValues;
import io.pinecone.proto.UpdateRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.LongFunction;

public class PineconeUpdateOpDispenser extends PineconeOpDispenser {
    private static final Logger LOGGER = LogManager.getLogger(PineconeUpdateOpDispenser.class);
    private final LongFunction<UpdateRequest.Builder> updateRequestFunc;
    private final LongFunction<Struct> updateMetadataFunc;
    private final LongFunction<SparseValues> sparseValuesFunc;

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
        updateMetadataFunc = createUpdateMetadataFunction(op);
        sparseValuesFunc = createSparseValuesFunction(op);
    }

    private LongFunction<SparseValues> createSparseValuesFunction(ParsedOp op) {
        return null;
    }

    private LongFunction<Struct> createUpdateMetadataFunction(ParsedOp op) {
        //new Struct.newBuilder(
        //    UpdateRequest.newBuilder().getSetMetadataBuilder().putAllFields(Map<String,Value>)))
        return null;
    }

    /*
      update-example:
    type: update
    index: update_index
    id: string_id
    values: list_of_floats
    namespace: update_namespace
    metadata:
      - key1: val1
      - key2: val2
      - key3: val3
    sparse_values:
      indices: list_of_ints
      values: list_of_floats
     */
    private LongFunction<UpdateRequest.Builder> createUpdateRequestFunction(ParsedOp op) {
        LongFunction<UpdateRequest.Builder> rFunc = l -> UpdateRequest.newBuilder();

        Optional<LongFunction<String>> nFunc = op.getAsOptionalFunction("namespace", String.class);
        if (nFunc.isPresent()) {
            LongFunction<UpdateRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = nFunc.get();
            rFunc = l -> finalFunc.apply(l).setNamespace(af.apply(l));
        }

        Optional<LongFunction<String>> iFunc = op.getAsOptionalFunction("id", String.class);
        if (iFunc.isPresent()) {
            LongFunction<UpdateRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = iFunc.get();
            rFunc = l -> finalFunc.apply(l).setId(af.apply(l));
        }

        Optional<LongFunction<String>> vFunc = op.getAsOptionalFunction("values", String.class);
        if (vFunc.isPresent()) {
            LongFunction<UpdateRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = vFunc.get();
            LongFunction<ArrayList<Float>> alf = l -> {
                String[] vals = af.apply(l).split(",");
                ArrayList<Float> fVals = new ArrayList<>();
                for (String val : vals) {
                    fVals.add(Float.valueOf(val));
                }
                return fVals;
            };
            rFunc = l -> finalFunc.apply(l).addAllValues(alf.apply(l));
        }

        return rFunc;
    }

    @Override
    public PineconeOp apply(long value) {
        UpdateRequest.Builder urb = updateRequestFunc.apply(value);
        if (updateMetadataFunc != null) {
            urb.setSetMetadata(updateMetadataFunc.apply(value));
        }
        if (sparseValuesFunc != null) {
            urb.setSparseValues(sparseValuesFunc.apply(value));
        }
        return new PineconeUpdateOp(pcFunction.apply(value).getConnection(targetFunction.apply(value)), urb.build());
    }
}
