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

package io.nosqlbench.virtdata.api.annotations;


import java.lang.annotation.Repeatable;

/**
 * The example annotation allows for a function developer to attach illustrative
 * examples for any given constructor. You can have multiple
 * <pre>@Example</pre> annotation per constructor.
 *
 *
 * A few key formats are allowed
 */
@Repeatable(value = Examples.class)
public @interface Example {
    String[] value();

}
