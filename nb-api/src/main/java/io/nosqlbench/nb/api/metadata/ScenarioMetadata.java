/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
