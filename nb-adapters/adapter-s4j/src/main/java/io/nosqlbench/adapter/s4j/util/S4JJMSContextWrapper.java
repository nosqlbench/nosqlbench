package io.nosqlbench.adapter.s4j.util;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.jms.JMSContext;
import javax.jms.Session;

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
public class S4JJMSContextWrapper {
    private final String jmsContextIdentifier;
    private final JMSContext jmsContext;
    private final int jmsSessionMode;

    public S4JJMSContextWrapper(String identifer, JMSContext jmsContext) {
        this.jmsContextIdentifier = identifer;
        this.jmsContext = jmsContext;
        this.jmsSessionMode = jmsContext.getSessionMode();
    }

    public int getJmsSessionMode() { return jmsSessionMode; }
    public boolean isTransactedMode() { return Session.SESSION_TRANSACTED == this.getJmsSessionMode(); }
    public String getJmsContextIdentifier() { return jmsContextIdentifier; }
    public JMSContext getJmsContext() { return jmsContext; }

    public void close() {
        if (jmsContext != null) {
            jmsContext.close();
        }
    }

    public String toString() {
        return new ToStringBuilder(this).
            append("jmsContextIdentifier", jmsContextIdentifier).
            append("jmsContext", jmsContext.toString()).
            toString();
    }
}
