/*
 * Copyright (c) nosqlbench
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

/// This package is the top-level entry point for the NoSQLBench Data API adapter (`dataapi`).
/// It wires together the standard adapter architecture:
/// {@link io.nosqlbench.adapter.dataapi.DataApiDriverAdapter} (registered via `@Service`),
/// {@link io.nosqlbench.adapter.dataapi.DataApiOpMapper} (dispatches to per-op dispensers),
/// and {@link io.nosqlbench.adapter.dataapi.DataApiSpace} (holds the driver client state).
///
/// Op dispensers live in the `opdispensers` sub-package; their corresponding runnable ops
/// live in the `ops` sub-package.
package io.nosqlbench.adapter.dataapi;
