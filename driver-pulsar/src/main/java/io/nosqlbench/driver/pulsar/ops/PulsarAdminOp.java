package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PulsarAdminOp extends SyncPulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminOp.class);

    protected final PulsarSpace clientSpace;
    protected final boolean asyncApi;
    protected final boolean adminDelOp;

    protected PulsarAdminOp(PulsarSpace clientSpace,
                         boolean asyncApi,
                         boolean adminDelOp)
    {
        this.clientSpace = clientSpace;
        this.asyncApi = asyncApi;
        this.adminDelOp = adminDelOp;
    }
}
