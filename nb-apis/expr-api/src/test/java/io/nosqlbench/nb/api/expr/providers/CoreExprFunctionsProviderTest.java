package io.nosqlbench.nb.api.expr.providers;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.expr.ExprFunction;
import io.nosqlbench.nb.api.expr.ExprFunctionMetadata;
import io.nosqlbench.nb.api.expr.ExprFunctionAnnotations;
import io.nosqlbench.nb.api.expr.TestExprRuntimeContext;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreExprFunctionsProviderTest {

    @Test
    void shouldExposeCoreHelpers() {
        TestExprRuntimeContext context = new TestExprRuntimeContext(Map.of(), Optional.of(URI.create("nb://source")));

        CoreExprFunctionsProvider provider = new CoreExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction env = context.function("env");
        ExprFunction prop = context.function("prop");
        ExprFunction uuid = context.function("uuid");
        ExprFunction now = context.function("now");
        ExprFunction upper = context.function("upper");
        ExprFunction lower = context.function("lower");
        ExprFunction source = context.function("source");

        assertEquals("fallback", env.apply("NB_MISSING_ENV", "fallback"));
        RuntimeException envNoArgs = assertThrows(RuntimeException.class, env::apply);
        assertTrue(envNoArgs.getCause() instanceof IllegalArgumentException);
        Assumptions.assumeTrue(!System.getenv().isEmpty());
        String envName = System.getenv().keySet().iterator().next();
        assertEquals(System.getenv(envName), env.apply(envName));

        System.setProperty("nb.expr.test", "value");
        try {
            assertEquals("value", prop.apply("nb.expr.test"));
        } finally {
            System.clearProperty("nb.expr.test");
        }
        assertEquals("default", prop.apply("nb.expr.test2", "default"));
        RuntimeException propNoArgs = assertThrows(RuntimeException.class, prop::apply);
        assertTrue(propNoArgs.getCause() instanceof IllegalArgumentException);

        String firstUuid = (String) uuid.apply();
        String secondUuid = (String) uuid.apply();
        assertNotNull(UUID.fromString(firstUuid));
        assertNotNull(UUID.fromString(secondUuid));
        assertNotEquals(firstUuid, secondUuid);

        String timestamp = (String) now.apply();
        Instant parsed = Instant.parse(timestamp);
        assertNotNull(parsed);

        assertEquals("VALUE", upper.apply("value"));
        assertEquals("value", lower.apply("VALUE"));
        RuntimeException upperNoArgs = assertThrows(RuntimeException.class, upper::apply);
        assertTrue(upperNoArgs.getCause() instanceof IllegalArgumentException);
        RuntimeException lowerTooMany = assertThrows(RuntimeException.class, () -> lower.apply("a", "b"));
        assertTrue(lowerTooMany.getCause() instanceof IllegalArgumentException);

        assertEquals("nb://source", source.apply());

        Map<String, ExprFunctionMetadata> metadata = context.getRegisteredMetadata();
        assertTrue(metadata.keySet().containsAll(
            Set.of("env", "prop", "uuid", "now", "upper", "lower", "source")
        ));
    }
}
