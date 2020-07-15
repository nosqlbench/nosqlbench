package io.nosqlbench.driver.jmx.ops;

import io.nosqlbench.driver.jmx.ValueConverter;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.apache.commons.math4.analysis.function.Exp;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

public class JMXReadOperation extends JmxOp {
    private final String attribute;
    private final String asType;
    private final String asName;

    public JMXReadOperation(JMXConnector connector, ObjectName objectName, String attribute, String asType, String asName) {
        super(connector, objectName);
        this.attribute = attribute;
        this.asType = asType;
        this.asName = asName;
    }

    @Override
    public void execute() {
        try {
            Object value = getMBeanConnection().getAttribute(objectName, this.attribute);
            logger.trace("read attribute '" + value +"': " + value);

            if (asType!=null) {
                value = ValueConverter.convert(asType,value);
            }

            String storedName = (asName==null) ? attribute : asName;
            SharedState.tl_ObjectMap.get().put(storedName,value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
