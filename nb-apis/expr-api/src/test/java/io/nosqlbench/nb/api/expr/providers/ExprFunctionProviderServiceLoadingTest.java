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


import io.nosqlbench.nb.api.expr.ExprFunctionProvider;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExprFunctionProviderServiceLoadingTest {

    @Test
    void shouldDiscoverProvidersViaServiceAnnotation() {
        ServiceLoader<ExprFunctionProvider> loader = ServiceLoader.load(ExprFunctionProvider.class);
        Set<Class<?>> implementations = new HashSet<>();
        loader.forEach(provider -> implementations.add(provider.getClass()));

        assertTrue(implementations.contains(CoreExprFunctionsProvider.class));
        assertTrue(implementations.contains(UnifiedParameterProvider.class));
    }
}
