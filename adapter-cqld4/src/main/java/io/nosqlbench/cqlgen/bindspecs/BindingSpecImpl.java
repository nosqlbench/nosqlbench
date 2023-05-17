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

package io.nosqlbench.cqlgen.bindspecs;

import io.nosqlbench.api.config.NBLabeledElement;

public class BindingSpecImpl implements BindingSpec {
    private NBLabeledElement target;
    private double cardinality;
    private String typedef;

    public BindingSpecImpl(final NBLabeledElement target) {
        this.target = target;
    }

    @Override
    public NBLabeledElement getTarget() {
        return this.target;
    }

    @Override
    public String getTypedef() {
        return this.typedef;
    }

    @Override
    public double getCardinality() {
        return BindingSpec.super.getCardinality();
    }

    public void setTarget(final NBLabeledElement target) {
        this.target = target;
    }

}
