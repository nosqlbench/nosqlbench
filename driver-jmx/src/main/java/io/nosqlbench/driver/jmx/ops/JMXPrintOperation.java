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

package io.nosqlbench.driver.jmx.ops;

import io.nosqlbench.driver.jmx.ValueConverter;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.util.Map;

public class JMXPrintOperation extends JMXReadOperation {
    public static final String PRINTVAR = "printvar";

    public JMXPrintOperation(JMXConnector connector, ObjectName objectName, String attribute, Map<String, String> cfg) {
        super(connector, objectName, attribute, cfg);
    }

    @Override
    public void execute() {
        Object value = readObject(attribute);
        System.out.println("# read JMX attribute '" + attribute + "' as " + value.getClass() +
                ((asType != null) ? " as_type=" + asType : "") +
                ((asName != null) ? " as_name=" + asName : ""));

        if (asType != null) {
            value = ValueConverter.convert(asType, value);
        }
        String storedName = (asName == null) ? attribute : asName;

        System.out.println(storedName + "=" + value + "\n");
    }

}
