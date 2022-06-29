package io.nosqlbench.driver.jms.util;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.jms.JMSContext;

/*
 * Copyright (c) 2022 nosqlbench
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
    private final String jmsContextIdentifer;
    private final JMSContext jmsContext;

    public S4JJMSContextWrapper(String identifer, JMSContext jmsContext) {
        this.jmsContextIdentifer = identifer;
        this.jmsContext = jmsContext;
    }

    public String getJmsContextIdentifer() { return jmsContextIdentifer; }
    public JMSContext getJmsContext() { return jmsContext; }

    public String toString() {
        return new ToStringBuilder(this).
            append("jmsContextIdentifer", jmsContextIdentifer).
            append("jmsContext", jmsContext.toString()).
            toString();
    }
}
