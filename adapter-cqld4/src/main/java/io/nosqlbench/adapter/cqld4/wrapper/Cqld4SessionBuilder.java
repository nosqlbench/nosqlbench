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
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.api.core.session.SessionBuilder;
import org.jetbrains.annotations.NotNull;


public class Cqld4SessionBuilder extends CqlSessionBuilder {
    private NodeSummary summarizer;

    @Override
    protected CqlSession wrap(@NotNull CqlSession defaultSession) {
        return new Cqld4ObserverSession(defaultSession);
    }

    @Override
    protected DriverContext buildContext(DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
        return new Cqld4DriverContext(configLoader,programmaticArguments).setSummarizer(summarizer);
    }

    public void setNodeSummarizer(NodeSummary summarizer) {
        this.summarizer = summarizer;
    }
}
