package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.Set;
import java.util.function.LongFunction;

/**
 * This maps a set of specifier functions to a pulsar operation. The pulsar operation contains
 * enough state to define a pulsar operation such that it can be executed, measured, and possibly
 * retried if needed.
 *
 * This function doesn't act *as* the operation. It merely maps the construction logic into
 * a simple functional type, given the component functions.
 *
 * For additional parameterization, the command template is also provided.
 */
public class PulsarAdminCrtTennamMapper extends PulsarOpMapper {
    private final LongFunction<Set<String>> adminRolesFunc;
    private final LongFunction<Set<String>> allowedClustersFunc;
    private final LongFunction<String> tenantFunc;
    private final LongFunction<String> namespaceFunc;

    public PulsarAdminCrtTennamMapper(CommandTemplate cmdTpl,
                                      PulsarSpace clientSpace,
                                      LongFunction<Set<String>> adminRolesFunc,
                                      LongFunction<Set<String>> allowedClustersFunc,
                                      LongFunction<String> tenantFunc,
                                      LongFunction<String> namespaceFunc) {
        super(cmdTpl, clientSpace);
        this.adminRolesFunc = adminRolesFunc;
        this.allowedClustersFunc = allowedClustersFunc;
        this.tenantFunc = tenantFunc;
        this.namespaceFunc = namespaceFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Set<String> adminRoleSet = adminRolesFunc.apply(value);
        Set<String> allowedClusterSet = allowedClustersFunc.apply(value);
        String tenant = tenantFunc.apply(value);
        String namespace = namespaceFunc.apply(value);

        return new PulsarAdminCrtTennamOp(
            clientSpace,
            adminRoleSet,
            allowedClusterSet,
            tenant,
            namespace);
    }
}
