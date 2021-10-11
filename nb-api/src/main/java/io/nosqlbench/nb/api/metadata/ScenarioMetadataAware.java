package io.nosqlbench.nb.api.metadata;

/**
 * Where supported, the following named fields are injected into object which
 * implement this interface:
 * <UL>
 *     <LI>SCENARIO_NAME - The full scenario name, used for logging, metrics, etc</LI>
 *     <LI>STARTED_AT_MILLIS - The millisecond timestamp used to create the scenario name</LI>
 *     <LI>SYSTEM_ID - A stable identifier based on the available ip addresses</LI></LK>
 *     <LI>SYSTEM_FINGERPRINT - a stable and pseudonymous identifier based on SYSTEM_ID</LI>
 * </UL>
 */
public interface ScenarioMetadataAware {
    void setScenarioMetadata(ScenarioMetadata metadata);

    static void apply(Object target, ScenarioMetadata metadata) {
        if (target instanceof ScenarioMetadataAware) {
            ((ScenarioMetadataAware)target).setScenarioMetadata(metadata);
        }
    }
}
