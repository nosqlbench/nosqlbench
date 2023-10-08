package io.nosqlbench.scenarios;

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


import io.nosqlbench.components.NBComponent;

public class WindowSampler {
    private final NBComponent base;

    public WindowSampler(NBComponent component) {
        this.base = component;
        component.find().metric("doesnot=exist");
    }

    public Sample sample() {
        return new Sample(1.0d,2.0d);
    }

    public static record Sample(double rate, double p99) { }
}
