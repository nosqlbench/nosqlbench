package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.Namespaces;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.admin.Tenants;
import org.apache.pulsar.common.policies.data.TenantInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
