/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.cmdstream;

import java.util.function.Function;

/**
 * A CmdParam is used to define the valid names of arguments and their types.
 * These can be strictly type checked, or in some cases freeform where strings are
 * are used contextually.
 * @param <T> The base type of the argument when paired with this CmdParam
 */
public class CmdParam<T> {
    public final String name;
    public final Function<String, T> converter;
    public final boolean freeform;

    public CmdParam(String name, Function<String, T> converter, boolean freeform) {
        this.name = name;
        this.converter = converter;
        this.freeform = freeform;
    }

    public static <T> CmdParam<T> of(String name, Function<String, T> converter) {
        return new CmdParam<>(name, converter, false);
    }

    public static CmdParam<String> of(String name) {
        return new CmdParam<>(name, s -> s, false);
    }

    public static CmdParam<String> ofFreeform(String name) {
        return new CmdParam<>(name, s -> s, true);
    }

    public CmdArg assign(String equals, String value) {
        return new CmdArg(this,equals, value);
    }

    public String getName() {
        return this.name;
    }
}
