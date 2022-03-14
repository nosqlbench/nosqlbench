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

import io.nosqlbench.driver.jmx.formats.MBeanInfoConsoleFormat;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;

public class JMXExplainOperation extends JmxOp {
    public final static String EXPLAIN = "explain";

    public JMXExplainOperation(JMXConnector connector, ObjectName objectName) {
        super(connector,objectName);
    }

    @Override
    public void execute() {
        MBeanServerConnection bean = getMBeanConnection();
        try {
            MBeanInfo info = bean.getMBeanInfo(objectName);
            String mbeanInfoText = MBeanInfoConsoleFormat.formatAsText(info, objectName);
            System.out.println(mbeanInfoText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
