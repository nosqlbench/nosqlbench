/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata;

/// Whether a caller will accept the whole facet when the window it
/// asked for cannot be resolved.
///
/// Some formats have no ordinal-to-byte mapping this layer can compute.
/// Asking for a small window of one of those and quietly fetching the
/// entire facet is the exact surprise windowed prefetch exists to
/// prevent, so the fallback is refused unless the caller says
/// otherwise. This only concerns a window that was actually asked for:
/// a prefetch with no window is a request for the whole facet, and
/// fetching it is not a fallback.
public enum WholeFacetFallback {
    /// An unresolvable window is an error. The default posture.
    REFUSE,
    /// Fetch the entire facet rather than failing. The caller has seen
    /// the size — [PrefetchPlan#facetBytes] — and accepted it.
    ALLOW
}
