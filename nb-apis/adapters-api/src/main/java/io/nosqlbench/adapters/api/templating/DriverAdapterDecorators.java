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

package io.nosqlbench.adapters.api.templating;

import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;

/**
 * This type simply captures (by extension) any optional decorator
 * interfaces which may be implemented by a {@link DriverAdapter}.
 * Thus, it is mostly for documentation.
 *
 * Decorator interfaces are used within NoSQLBench where implementations are truly optional,
 * and thus would cloud the view of a developer implementing strictly to requirements.
 *
 * You can find any such decorator interfaces specific to driver adapters by looking for
 * all implementations of this type.
 */
public interface DriverAdapterDecorators {
}
