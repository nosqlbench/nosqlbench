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

package io.nosqlbench.adapter.jmx.operations;

import io.nosqlbench.adapter.jmx.JMXSpace;
import io.nosqlbench.adapter.jmx.ValueConverter;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import javax.management.ObjectName;

public class JMXReadOperation extends JmxOp {

    protected final String readvar;
    protected final String asType;
    protected final String asName;
    protected final SharedState.Scope scope;

    public JMXReadOperation(
        JMXSpace space,
        ObjectName oname,
        String readvar,
        String asType,
        String asName,
        SharedState.Scope scope
    ) {
        super(space.getConnector(), oname);
        this.readvar = readvar;
        this.asType = asType;
        this.asName = asName;
        this.scope = scope;
    }

    @Override
    public void execute() {
        Object value = readObject(readvar);
        value = ValueConverter.convert(asType, value);
        String storedName = asName;
        SharedState.put(scope, storedName, value);
    }


}
