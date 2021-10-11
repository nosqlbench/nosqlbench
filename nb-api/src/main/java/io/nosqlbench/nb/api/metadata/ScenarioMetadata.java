package io.nosqlbench.nb.api.metadata;

import java.util.Map;

/**
 * If an object is ScenarioMetadata, then they will be updated with a map of
 * scenario metadata. Supported types are:
 * <UL>
 *     <LI>ScriptingPluginInfo</LI>
 * </UL>
 */
public class ScenarioMetadata {
    private final long startedAt;
    private final String sessionName;
    private final String systemId;
    private final String systemFingerprint;

    public ScenarioMetadata(long startedAt, String sessionName, String systemId, String systemFingerprint) {
        this.startedAt = startedAt;
        this.sessionName = sessionName;
        this.systemId = systemId;
        this.systemFingerprint = systemFingerprint;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getSystemFingerprint() {
        return systemFingerprint;
    }

    public Map<String,String> asMap() {
        return Map.of("STARTED_AT",String.valueOf(startedAt),
            "SESSION_NAME",sessionName,
            "SYSTEM_ID",systemId,
            "SYSTEM_FINGERPRINT", systemFingerprint);
    }
}
