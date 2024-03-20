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
 * <P>The predicates module defines a few key elements which work together to
 * allow driver adapters to share a common structure for predicate forms.
 * These include:
 * <UL>
 *     <LI>{@link io.nosqlbench.virtdata.predicates.ast.PredicateExpr} - an
 *     abstract syntax which captures the range of predicate forms which are supported.</LI>
 *     <LI>{@link io.nosqlbench.virtdata.predicates.types.PredicateAdapter} - an
 *     adapter type which can allow individual driver adapters to create a compatible
 *     predicate clause or expression object from a
 *     {@link io.nosqlbench.virtdata.predicates.ast.PredicateExpr}</LI>
 *     <LI>{@link io.nosqlbench.virtdata.predicates.types.PredicateSerDes}
 *     - serialization and deserialization functionality which allows
 *     the core machinery of NB to read the common format to and from the
 *     {@link io.nosqlbench.virtdata.predicates.ast.PredicateExpr} form.</LI>
 * </UL>
 * </P>
 */
package io.nosqlbench.virtdata.predicates;
