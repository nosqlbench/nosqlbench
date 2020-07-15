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
