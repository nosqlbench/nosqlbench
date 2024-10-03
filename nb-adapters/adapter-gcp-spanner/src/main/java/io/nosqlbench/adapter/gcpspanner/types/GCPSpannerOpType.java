/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.gcpspanner.types;

/**
 * All the spanner rpc api calls are defined <a href="https://cloud.google.com/spanner/docs/reference/rpc">here</a>, representing a
 * guide to the set of operations we should define if we want to implement full Spanner api support.
 * <p>
 * NOTE that the vector search functionality is still in pre-GA and is not available through rpc calls other than simply
 * calling ExecuteSql. The SQL functionality related to vector indices is documented
 * <a href="https://cloud.google.com/spanner/docs/reference/standard-sql/data-definition-language#vector_index_statements">here</a>
 * <p>
 * KNN and ANN search through Google SQL are documented respectively
 * <a href="https://cloud.google.com/spanner/docs/find-k-nearest-neighbors">here</a>
 * and
 * <a href="https://cloud.google.com/spanner/docs/find-approximate-nearest-neighbors#query-vector-embeddings">here</a></a>
 */
public enum GCPSpannerOpType {
    create_database_ddl,
    update_database_ddl,
    insert,
    execute_dml,
}
