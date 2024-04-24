/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.cqld4.wrapper;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.loadbalancing.LoadBalancingPolicy;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.internal.core.context.DefaultDriverContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cqld4DriverContext extends DefaultDriverContext {

    private DriverContext delegate;
    ConcurrentHashMap<String,LoadBalancingPolicy> wrappedPolicies = new ConcurrentHashMap<>();
    private NodeSummary summarizer;

    public Cqld4DriverContext(DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
        super(configLoader, programmaticArguments);
    }

    @NotNull
    @Override
    public Map<String, LoadBalancingPolicy> getLoadBalancingPolicies() {
        Map<String, LoadBalancingPolicy> loadBalancingPolicies = super.getLoadBalancingPolicies();
        for (String profileName : loadBalancingPolicies.keySet()) {
            wrappedPolicies.computeIfAbsent(profileName,s -> wrapPolicy(loadBalancingPolicies.get(profileName),this.summarizer));
        }
        return Collections.unmodifiableMap(wrappedPolicies);
    }

    private LoadBalancingPolicy wrapPolicy(LoadBalancingPolicy wrapped, NodeSummary summarizer) {
        return new Cqld4LoadBalancerObserver(wrapped, summarizer);
    }

    public DriverContext setSummarizer(NodeSummary summarizer) {
        this.summarizer = summarizer;
        return this;
    }
}
