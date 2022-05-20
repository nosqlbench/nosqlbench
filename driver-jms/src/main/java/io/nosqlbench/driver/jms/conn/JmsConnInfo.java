package io.nosqlbench.driver.jms.conn;

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
