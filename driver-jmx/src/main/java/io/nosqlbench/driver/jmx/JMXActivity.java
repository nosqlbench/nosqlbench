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

package io.nosqlbench.driver.jmx;

import io.nosqlbench.driver.jmx.ops.JmxOp;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import javax.net.ssl.SSLContext;

public class JMXActivity extends SimpleActivity implements Activity {

    private OpSequence<OpDispenser<? extends JmxOp>> sequence;
    private SSLContext sslContext;

    public JMXActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        this.sequence = createOpSequenceFromCommands(ReadyJmxOp::new, false);
        setDefaultsFromOpSequence(sequence);
        NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(activityDef.getParams());
        this.sslContext= SSLKsFactory.get().getContext(sslCfg);

        // TODO: Require qualified default with an op sequence as the input
    }

    /**
     * If this is null, then no SSL is requested.
     * @return The SSLContext for this activity
     */
    public SSLContext getSslContext() {
        return sslContext;
    }

    public OpSequence<OpDispenser<? extends JmxOp>> getSequencer() {
        return sequence;
    }
}
