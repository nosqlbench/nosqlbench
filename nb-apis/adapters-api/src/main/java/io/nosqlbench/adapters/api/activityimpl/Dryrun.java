package io.nosqlbench.adapters.api.activityimpl;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


public enum Dryrun {
    /**
     * Ops are executed normally, no change to the dispenser behavior.
     */
    none,
    /**
     * Ops will be synthesized, but they will not be executed.
     * This is done by wrapping the synthesized op in a no-op facade
     */
    op,
    /**
     * Ops will print the toString version of their result to stdout.
     * This is done by wrapping the synthesized op in a post-emit facade.
     */
    emit,
    /**
     * Jsonnet evaluation is a one time dry-run and then exit.
     * With this value the run should exit after the first evaluation of jsonnet
     * and Ops are not executed, but should processing fall through then processing
     * will proceed as for none.
     */
    jsonnet,
    /**
     * Expression processing is a one time dry-run and then exit.
     * With this value the run should exit after the first evaluation of expressions
     * and Ops are not executed.
     */
    exprs
}
