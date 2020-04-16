/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class RawStmtDef extends BlockParams {

    private String statement;

    public RawStmtDef() {
    }

    public RawStmtDef(String name, String statement) {
        setName(name);
        this.statement = statement;
    }

    @SuppressWarnings("unchecked")
    public RawStmtDef(String defaultName, Map<String, Object> map) {

        Optional.ofNullable((String) map.remove("stmt")).ifPresent(this::setStmt);
        Optional.ofNullable((String) map.remove("statement")).ifPresent(this::setStmt);
        Optional.ofNullable((String) map.remove("name")).ifPresent(this::setName);
        Optional.ofNullable((String) map.remove("desc")).ifPresent(this::setDesc);
        Optional.ofNullable((String) map.remove("description")).ifPresent(this::setDesc);

        Optional.ofNullable((Map<String, String>) map.remove("tags")).ifPresent(this::setTags);
        Optional.ofNullable((Map<String, String>) map.remove("bindings")).ifPresent(this::setBindings);
        Optional.ofNullable((Map<String, String>) map.remove("params")).ifPresent(this::setParams);


        // Depends on order stability, relying on LinkedHashMap -- Needs stability unit tests
        if (this.statement == null) {
            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            if (!iterator.hasNext()) {
                throw new RuntimeException("undefined-name-statement-tuple:" +
                        " The statement is not set, and no statements remain to pull 'name: statement' values from." +
                        " For more details on this error see " +
                        "the troubleshooting section of the YAML format" +
                        " docs for undefined-name-statement-tuple");
            }
            Map.Entry<String, Object> firstEntry = iterator.next();
            setStmt((String) firstEntry.getValue());
            map.remove(firstEntry.getKey());
            if (getName().isEmpty()) {
                setName(firstEntry.getKey());
            } else {
                throw new RuntimeException("redefined-name-in-statement-tuple: Statement name has already been set by name parameter. Remove the name parameter for a statement definition map." +
                        " For more details on this error see " +
                        "the troubleshooting section in the " +
                        "YAML format docs for redefined-name-statement-tuple");
            }
        }
        if (getName().isEmpty()) {
            setName(defaultName);
        }

        map.forEach((key, value) -> getParams().put(key, String.valueOf(value)));
    }

    public String getStmt() {
        return statement;
    }

    private void setStmt(String statement) {
        this.statement = statement;
    }
    
    public String getName() {
        return getParams().getOrDefault("name", super.getName());
    }
}
