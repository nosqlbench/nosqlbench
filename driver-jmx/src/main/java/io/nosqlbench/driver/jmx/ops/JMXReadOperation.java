package io.nosqlbench.driver.jmx.ops;

import io.nosqlbench.driver.jmx.ValueConverter;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.util.Map;

public class JMXReadOperation extends JmxOp {
    public final static String READVAR = "readvar";
    public final static String AS_TYPE = "as_type";
    public final static String AS_NAME = "as_name";
    public final static String SCOPE = "scope";

    protected final String attribute;
    protected final String asType;
    protected final String asName;
    protected final SharedState.Scope scope;

    public JMXReadOperation(JMXConnector connector, ObjectName objectName, String attribute, Map<String, String> cfg) {
        super(connector, objectName);
        this.attribute = attribute;
        this.asType = cfg.remove(AS_TYPE);
        this.asName = cfg.remove(AS_NAME);

        String scopeName = cfg.remove(SCOPE);
        if (scopeName != null) {
            scope = SharedState.Scope.valueOf(scopeName);
        } else {
            scope = SharedState.Scope.process;
        }
    }

    @Override
    public void execute() {
        Object value = readObject(attribute);

        if (asType != null) {
            value = ValueConverter.convert(asType, value);
        }

        String storedName = (asName == null) ? attribute : asName;

        switch (scope) {
            case process:
                SharedState.gl_ObjectMap.put(storedName, value);
                break;
            case thread:
                SharedState.tl_ObjectMap.get().put(storedName, value);
                break;
        }
    }


}
