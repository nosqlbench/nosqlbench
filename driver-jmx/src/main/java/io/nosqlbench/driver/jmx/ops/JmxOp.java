package io.nosqlbench.driver.jmx.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

/**
 * All JMX Operations should built on this base type.
 */
public abstract class JmxOp {

    protected final static Logger logger = LoggerFactory.getLogger(JmxOp.class);

    protected JMXConnector connector;
    protected ObjectName objectName;

    public JmxOp(JMXConnector connector, ObjectName objectName) {
        this.connector = connector;
        this.objectName = objectName;
    }

    public MBeanServerConnection getMBeanConnection() {
        MBeanServerConnection connection = null;
        try {
            connection = connector.getMBeanServerConnection();
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Object readObject(String attributeName) {
        try {
            Object value = getMBeanConnection().getAttribute(objectName, attributeName);
            logger.trace("read attribute '" + value + "': " + value);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void execute();
}
