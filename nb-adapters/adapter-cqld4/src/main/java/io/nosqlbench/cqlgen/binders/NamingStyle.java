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

package io.nosqlbench.cqlgen.binders;

public enum NamingStyle {
    /**
     * All binding names are generated in fully-qualified form, meaning there is a separate binding
     * for every tuple of column, typedef, table, keyspace
     */
    FullyQualified,

    /**
     * Only use the default binding names which are shared among all bind points which have the same
     * data type.
     */
    SymbolicType,

    /**
     * Name bind points uniquely within keyspace, adding only the qualifiers to the name that are
     * needed to avoid ambiguous names when an identifier is used variously within the keyspace
     */
    CondensedKeyspace
}
