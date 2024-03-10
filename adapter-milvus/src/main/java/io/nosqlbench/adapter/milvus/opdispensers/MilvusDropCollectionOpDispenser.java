package io.nosqlbench.adapter.milvus.opdispensers;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.DropCollectionParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusDropCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class MilvusDropCollectionOpDispenser extends MilvusOpDispenser {
    private static final Logger logger = LogManager.getLogger(MilvusDropCollectionOpDispenser.class);

    /**
     * Create a new MilvusDeleteOpDispenser subclassed from {@link MilvusOpDispenser}.
     *
     * @param adapter           The associated {@link MilvusDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction    A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusDropCollectionOpDispenser(MilvusDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    // https://milvus.io/docs/drop_collection.md
    @Override
    public LongFunction<MilvusDropCollectionOp> createOpFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        DropCollectionParam.Builder eb = DropCollectionParam.newBuilder();
        LongFunction<DropCollectionParam.Builder> f =
            l -> DropCollectionParam.newBuilder().withCollectionName(targetF.apply(l));
        return l -> new MilvusDropCollectionOp(clientF.apply(l), f.apply(1).build());
    }
}
