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
 * <P>This package contains wrapper logic for the CQL driver to allow more
 * detailed diagnostic data to be captured. Because the Driver "v4" tries
 * to protect developers from themselves and nearly disallows extension,
 * lots of boilerplate had to be added to implement a wrapper.</p>
 *
 * <p>The purpose of this code is to see basic details from the load balancer's behavior.
 * The operative part of this package is simply
 * {@link io.nosqlbench.adapter.cqld4.wrapper.Cqld4LoadBalancerObserver},
 * which intercepts query plan logic and logs details at a configurable level of details.
 * </P>
 *
 * TODO: adapt diag markers from http
 * TODO: inline the diagnostic filters into op dispenser logic
 * TODO: make load balancer diagnostics record scoreboard data isochronously to a separate file
 */
package io.nosqlbench.adapter.cqld4.wrapper;
