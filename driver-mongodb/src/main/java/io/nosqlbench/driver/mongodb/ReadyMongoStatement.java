package io.nosqlbench.driver.mongodb;

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


import com.mongodb.ReadPreference;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ReadyMongoStatement {

    private final StringBindings bindings;
    private final ReadPreference readPreference;

    public ReadyMongoStatement(OpTemplate stmtDef) {
        ParsedTemplate paramTemplate = new ParsedTemplate(stmtDef.getStmt().orElseThrow(), stmtDef.getBindings());
        BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
        StringBindingsTemplate template = new StringBindingsTemplate(stmtDef.getStmt().orElseThrow(), paramBindings);

        this.bindings = template.resolve();
        this.readPreference = stmtDef.getOptionalStringParam("readPreference")
                .map(ReadPreference::valueOf)
                .orElse(ReadPreference.primary());
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public Bson bind(long value) {
        return Document.parse(bindings.bind(value));
    }
}
