/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api;

import io.nosqlbench.nb.api.metadata.SystemId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemIdTest {
    private static final Logger logger = LogManager.getLogger(SystemIdTest.class);

    @Test
    public void testHostInfo() {
        String hostSummary = SystemId.getHostSummary();
        logger.info("host summary: " + hostSummary);
    }

    @Test
    public void testNostId() {
        String nodeId = SystemId.getNodeId();
        assertThat(nodeId).matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
        logger.info("node id: " + nodeId);
    }

    @Test
    public void testNodeFingerprint() {
        String nodeFingerprint = SystemId.getNodeFingerprint();
        assertThat(nodeFingerprint).matches("[A-Z0-9]+");
        logger.info("node fingerprint: " + nodeFingerprint);
    }

    @Test
    public void testBrailleNodeId() {
        String brailleNodeId = SystemId.getBrailleNodeId();
        assertThat(brailleNodeId).matches("[⠀-⣿]{4}"); // note, that is not a space. It is the starting braille value of empty
        logger.info("braille node id: " + brailleNodeId);
    }

    @Test
    public void testPackedNodeId() {
        String packedNodeId = SystemId.getPackedNodeId();
        assertThat(packedNodeId).matches("[0-9A-Za-z_-]+");
        logger.info("packed node id: " + packedNodeId);
    }

    @Test
    public void testGenSessionCode() {
        String sessionCode=SystemId.genSessionCode(234L);
        assertThat(sessionCode).matches("[0-9a-zA-Z~-]+");
        logger.info("session code: " + sessionCode);
    }

    @Test
    public void testGenSessionBits() {
        String sessionBits = SystemId.genSessionBits();
        assertThat(sessionBits).matches("[⠀-⣿]+:[⠀-⣿]+");
        logger.info("session bits: " + sessionBits);
    }

    @Test
    public void testRadixExpansion() {
        long base=64L;
        long value=1L;
        String image="1";
        for (int i = 0; i < 11; i++) {
            value= (long) Math.pow(base,i);
            image = "1"+"0".repeat(i);
            String rendered = SystemId.packLong(value);
            assertThat(rendered).isEqualTo(image);
        }
    }

}
