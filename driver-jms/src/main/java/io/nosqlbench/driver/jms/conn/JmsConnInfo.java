package io.nosqlbench.driver.jms.conn;

import java.util.HashMap;
import java.util.Map;

public class JmsConnInfo {

    protected final String jmsProviderType;
    protected final Map<String, Object> jmsConnConfig;

    protected JmsConnInfo(String jmsProviderType) {
        this.jmsProviderType = jmsProviderType;
        this.jmsConnConfig = new HashMap<>();
    }

    public Map<String, Object> getJmsConnConfig() { return this.jmsConnConfig; }
    public void resetJmsConnConfig() { this.jmsConnConfig.clear(); }
    public void addJmsConnConfigItems(Map<String, Object> cfgItems) { this.jmsConnConfig.putAll(cfgItems); }
    public void addJmsConnConfigItem(String key, Object value) { this.jmsConnConfig.put(key, value); }
    public void removeJmsConnConfigItem(String key) { this.jmsConnConfig.remove(key); }
}
