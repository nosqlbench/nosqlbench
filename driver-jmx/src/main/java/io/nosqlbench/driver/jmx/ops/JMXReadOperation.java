package io.nosqlbench.driver.jmx.ops;

import io.nosqlbench.driver.jmx.ValueConverter;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.apache.commons.math4.analysis.function.Exp;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;

public class JMXReadOperation extends JmxOp {
    public final static String READVAR = "readvar";
    public final static String AS_TYPE = "as_type";
    public final static String AS_NAME = "as_name";

    protected final String attribute;
    protected final String asType;
    protected final String asName;

    public JMXReadOperation(JMXConnector connector, ObjectName objectName, String attribute, Map<String, String> cfg) {
        super(connector, objectName);
        this.attribute = attribute;
        this.asType = cfg.remove(AS_TYPE);
        this.asName = cfg.remove(AS_NAME);
    }

    @Override
    public void execute() {
        Object value = readObject(attribute);

        if (asType != null) {
            value = ValueConverter.convert(asType, value);
        }
        String storedName = (asName == null) ? attribute : asName;

        SharedState.tl_ObjectMap.get().put(storedName, value);
    }


}
