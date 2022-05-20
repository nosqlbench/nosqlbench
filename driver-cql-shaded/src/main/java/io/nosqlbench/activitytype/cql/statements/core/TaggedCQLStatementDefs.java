package io.nosqlbench.activitytype.cql.statements.core;

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


import io.nosqlbench.engine.api.util.Tagged;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaggedCQLStatementDefs implements Tagged {

    private List<CQLStatementDef> statements = new ArrayList<>();
    private Map<String,String> tags = new HashMap<>();
    private Map<String,String> params = new HashMap<>();

    public TaggedCQLStatementDefs(Map<String,String> tags, Map<String,String> params, List<CQLStatementDef> statements) {
        this.tags = tags;
        this.params = params;
        this.statements = statements;
    }
    public TaggedCQLStatementDefs(Map<String,String> tags, List<CQLStatementDef> statements) {
        this.tags = tags;
        this.statements = statements;
    }

    public TaggedCQLStatementDefs(List<CQLStatementDef> statements) {
        this.statements = statements;
    }


    public TaggedCQLStatementDefs() {
    }

    public List<CQLStatementDef> getStatements() {
        return statements;
    }

    public void setStatements(List<CQLStatementDef> statements) {
        this.statements = statements;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
