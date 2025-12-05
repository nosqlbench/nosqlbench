/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.docsys.apps.docinventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocInventoryAppTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void docsInventorySnapshotIsCurrent() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path expected = repoRoot.resolve("docs/docs_inventory.json");
        assertTrue(
            Files.exists(expected),
            "Missing docs/docs_inventory.json. Run `nb app docs-inventory --root " + repoRoot + "` and commit the result."
        );

        Path actual = Files.createTempFile("docs-inventory-", ".json");
        try {
            DocInventoryApp app = new DocInventoryApp();
            int exit = app.applyAsInt(new String[] {
                "--root", repoRoot.toString(),
                "--output", actual.toString()
            });
            assertEquals(0, exit, "docs-inventory app exited non-zero.");

            JsonNode expectedNode = loadNormalized(expected);
            JsonNode actualNode = loadNormalized(actual);
            JsonNode fullExpectedNode = MAPPER.readTree(expected.toFile());

            assertEquals(
                expectedNode,
                actualNode,
                () -> "docs/docs_inventory.json is stale. Run `nb app docs-inventory --root " + repoRoot + "` and commit the update."
            );

            Path schemaPath = repoRoot.resolve("docs/docs_inventory.schema.json");
            validateAgainstSchema(schemaPath, fullExpectedNode);
            publishInventoryArtifact(expected);
        } finally {
            Files.deleteIfExists(actual);
        }
    }

    private static Path locateRepoRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to locate repository root (missing .git directory).");
    }

    private static JsonNode loadNormalized(Path file) throws IOException {
        JsonNode node = MAPPER.readTree(file.toFile());
        if (node instanceof ObjectNode object) {
            object.remove("generated_at");
            object.remove("root");
        }
        return node;
    }

    private static void validateAgainstSchema(Path schemaPath, JsonNode document) throws IOException {
        assertTrue(Files.exists(schemaPath), "Missing schema file: " + schemaPath);
        JsonNode schemaNode = MAPPER.readTree(schemaPath.toFile());
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaNode);
        Set<ValidationMessage> errors = schema.validate(document);
        assertTrue(errors.isEmpty(), () -> "docs_inventory.json schema violations:\n" +
            errors.stream()
                .map(ValidationMessage::toString)
                .sorted()
                .reduce((a, b) -> a + "\n" + b)
                .orElse("<unknown>"));
    }

    private static void publishInventoryArtifact(Path source) throws IOException {
        Path moduleRoot = Paths.get("").toAbsolutePath().normalize();
        Path artifact = moduleRoot.resolve("target/docs_inventory.json");
        Files.createDirectories(artifact.getParent());
        Files.copy(source, artifact, StandardCopyOption.REPLACE_EXISTING);
    }
}
