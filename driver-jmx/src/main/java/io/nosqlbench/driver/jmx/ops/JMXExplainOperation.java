package io.nosqlbench.driver.jmx.ops;

import io.nosqlbench.driver.jmx.formats.MBeanInfoConsoleFormat;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;

public class JMXExplainOperation extends JmxOp {
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
