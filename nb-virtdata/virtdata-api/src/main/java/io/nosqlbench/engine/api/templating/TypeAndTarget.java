/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.templating;

import java.util.function.LongFunction;

/**
 * <p>A convenient pattern for users to specify a command is that of <em>type and target</em>. This
 * emphasizes that users often need to specify what kind of action to take, and what subject to
 * take the action on. This concept pervades programming, exmplified by a simple <pre>{@code function(object) } call</pre>
 * </p>
 *
 * <p>
 * To facilitate this pattern in op templates with the help of type safety, this helper type allows
 * for the scanning of a map for a matching enum field. If any map key matches one of the possible
 * enum values, case-insensitively, and with only a single match, then the matching enum value is
 * taken as a specific type of action, and the matching value in the map is taken as the intended target.
 *</p>
 *
 * <p>Further, the target may be indirect, such as a binding, rather than a specific literal or structured
 * value. In such cases, only a lambda-style function may be available. The provided <em>targetFunction</em> is
 * a {@link LongFunction} of Object which can be called to return an associated target value once the
 * cycle value is known.</p>
 *
 * For example, with an enum like <pre>{@code
 * public enum LandMovers {
 *     BullDozer,
 *     DumpTruck
 * }
 * }</pre>
 *
 * and a parsed op like <pre>{@code
 * (json)
 * {
 *  "op": {
 *   "bulldozer": "{dozerid}"
 *   }
 * }
 *
 * (yaml)
 * op:
 *  bulldozer: "{dozerid}
 * }</pre>
 * the result will be returned with the following: <pre>{@code
 *  enumId: (Enum field) BullDozer
 *  field: (String) bulldozer
 *  targetFunction: (long l) -> template function for {dozerid}
 * }</pre>
 * @param <E>
 */
public class TypeAndTarget<E extends Enum<E>,T> {
    public final E enumId;
    public final String field;
    public final LongFunction<T> targetFunction;

    public TypeAndTarget(E enumId, String matchingOpFieldName, LongFunction<T> value) {
        this.enumId = enumId;
        this.field = matchingOpFieldName;
        this.targetFunction = value;
    }
}
