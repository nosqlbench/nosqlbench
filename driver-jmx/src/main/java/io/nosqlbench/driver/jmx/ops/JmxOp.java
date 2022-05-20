package io.nosqlbench.driver.jmx.ops;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

/**
 * All JMX Operations should built on this base type.
 */
public abstract class JmxOp implements Op,Runnable {

    protected final static Logger logger = LogManager.getLogger(JmxOp.class);

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

    public void run() {
        execute();
    }
}
