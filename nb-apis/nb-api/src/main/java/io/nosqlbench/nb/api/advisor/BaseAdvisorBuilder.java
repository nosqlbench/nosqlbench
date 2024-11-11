package io.nosqlbench.nb.api.advisor;

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


import io.nosqlbench.nb.api.components.core.NBComponent;

public abstract class BaseAdvisorBuilder<ELEMENT, T, SelfT extends BaseAdvisorBuilder<ELEMENT, T, SelfT>>
    extends NBAdvisorPointOrBuilder<ELEMENT> {

    protected NBComponent component;
    protected String name;
    protected String description;

    protected abstract SelfT self();

    public SelfT component(NBComponent component) {
        this.component = component;
        return self();
    }

    public SelfT name(String name) {
        this.name = name;
        return self();
    }

    public SelfT desc(String description) {
        this.description = description;
        return self();
    }

    public abstract <PTYPE> NBAdvisorPoint<PTYPE> build();
}
