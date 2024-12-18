package io.nosqlbench.adapter.cqld4.validators;

/*
 * Copyright (c) nosqlbench
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


import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapters.api.activityimpl.uniform.Validator;

import java.util.List;

public class Cqld4SingleRowValidator implements Validator<List<Row>> {

    public Cqld4SingleRowValidator() {
    }

    @Override
    public void validate(List<Row> rows) {
        System.out.println("validating rows...");
    }
}
