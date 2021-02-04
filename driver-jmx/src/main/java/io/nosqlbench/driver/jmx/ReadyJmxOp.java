package io.nosqlbench.driver.jmx;

import io.nosqlbench.driver.jmx.ops.JMXExplainOperation;
import io.nosqlbench.driver.jmx.ops.JMXPrintOperation;
import io.nosqlbench.driver.jmx.ops.JMXReadOperation;
import io.nosqlbench.driver.jmx.ops.JmxOp;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class ReadyJmxOp implements LongFunction<JmxOp> {

    private final CommandTemplate command;

    public ReadyJmxOp(CommandTemplate command) {
        this.command = command;
    }

    public JmxOp apply(long value) {
        Map<String, String> cmdmap = command.getCommand(value);
        JMXConnector connector = bindConnector(cmdmap);

        if (!cmdmap.containsKey("object")) {
            throw new RuntimeException("You must specify an object in a jmx operation as in object=...");
        }

        ObjectName objectName = null;
        try {
            String object = cmdmap.get("object");
            if (object == null) {
                throw new RuntimeException("You must specify an object name for any JMX operation.");
            }
            objectName = new ObjectName(object);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }

        if (cmdmap.containsKey(JMXReadOperation.READVAR)) {
            return new JMXReadOperation(connector, objectName, cmdmap.get(JMXReadOperation.READVAR), cmdmap);
        } else if (cmdmap.containsKey(JMXPrintOperation.PRINTVAR)) {
            return new JMXPrintOperation(connector, objectName, cmdmap.get(JMXPrintOperation.PRINTVAR), cmdmap);
        } else if (cmdmap.containsKey(JMXExplainOperation.EXPLAIN)) {
            return new JMXExplainOperation(connector, objectName);
        }

        throw new RuntimeException("No valid form of JMX operation was determined from the provided command details:" + cmdmap.toString());
    }

    private JMXConnector bindConnector(Map<String, String> cmdmap) {

        Map<String, Object> connectorEnv = new HashMap<>();
        String username = cmdmap.remove("username");
        String password = cmdmap.remove("password");
        username = SecureUtils.readSecret("JMX username", username);
        password = SecureUtils.readSecret("JMX password", password);
        if (username != null && password != null) {
            connectorEnv.put(JMXConnector.CREDENTIALS, new String[]{username, password});
        }

        JMXConnector connector = null;
        try {
            JMXServiceURL url = bindJMXServiceURL(cmdmap);
            connector = JMXConnectorFactory.connect(url, connectorEnv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connector;
    }

    private JMXServiceURL bindJMXServiceURL(Map<String, String> cmdmap) {
        JMXServiceURL url = null;
        try {
            if (cmdmap.containsKey("url")) {
                url = new JMXServiceURL(cmdmap.get("url"));
            } else {
                if (cmdmap.containsKey("host")) {
                    throw new RuntimeException("You must provide at least a host if you do not provide a url.");
                }
                String protocol = cmdmap.get("protocol");
                String host = cmdmap.get("host");
                int port = Optional.ofNullable(cmdmap.get("port")).map(Integer::parseInt).orElse(0);
                String path = cmdmap.get("path");
                url = new JMXServiceURL(protocol, host, port, path);

            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

}
