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


public class NBAdvisorBuilder<PTYPE>
    extends BaseAdvisorBuilder<PTYPE, NBAdvisorPoint<PTYPE>, NBAdvisorBuilder<PTYPE>> {

    @Override
    protected NBAdvisorBuilder<PTYPE> self() {
        return this;
    }

    @Override
    public NBAdvisorPoint<PTYPE> build() {
        return (NBAdvisorPoint<PTYPE>) new NBAdvisorPoint<PTYPE>(name, description == null ? name : description);
    }
}
