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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadyCQLStatementsTemplate {

    private final List<ReadyCQLStatementTemplate> readyStatementList = new ArrayList<>();

    public void addTemplate(ReadyCQLStatementTemplate t) {
        this.readyStatementList.add(t);
    }

    public List<ReadyCQLStatement> resolve() {
        return readyStatementList.stream()
                .map(ReadyCQLStatementTemplate::resolve)
                .collect(Collectors.toList());
    }

    public int size() {
        return readyStatementList.size();
    }

}
