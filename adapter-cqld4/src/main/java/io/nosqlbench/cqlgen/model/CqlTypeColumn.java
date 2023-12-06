/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.cqlgen.model;

import io.nosqlbench.nb.api.labels.NBLabels;

public class CqlTypeColumn extends CqlColumnBase {

    CqlType type;

    public CqlTypeColumn(final String colname, final String typedef, final CqlType usertype) {
        super(colname, typedef);
        type = usertype;
    }

    @Override
    protected String getParentFullName() {
        return this.type.getFullName();
    }

    public CqlType getType() {
        return this.type;
    }

    public void setType(final CqlType type) {
        this.type = type;
    }

    @Override
    public NBLabels getLabels() {
        return super.getLabels().and("name", this.type.getName());
    }
}
