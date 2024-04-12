/*
 * Copyright (c) 2024 nosqlbench
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

/**
 * <P>This package contains an implementation of
 * {@link io.nosqlbench.virtdata.lib.vectors.dnn.circular.CircularPartitioner},
 * a space-filling curve which maps ordinals onto 2-d vectors which fall on the unit circle
 * with increasing density. This allows vector values to get progressive closer together radially
 * as the arc intervals are divided in half at each level of resolution.</P>
 *
 */
package io.nosqlbench.virtdata.lib.vectors.dnn.circular;
