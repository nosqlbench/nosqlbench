package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

/*
 * Copyright (c) 2022 nosqlbench
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


/**
 * This is the root type of any operation which is used in a NoSQLBench
 * DriverAdapter. It is a tagging interface for incremental type validation
 * in the NB runtime. You probably don't want to use it directly.
 *
 * Instead, use these:
 * <ul>
 *  <li>{@link CycleOp}</li> - An interface that will called if there is nothing to consume
 *  the result type from your operation. In some cases preparing a result body to
 *  hand down the chain is more costly, so implementing this interface allows
 * </ul>
 *
 * either {@link CycleOp} or {@link ChainingOp} (but not both!)
 *
 * In the standard flow of an activity, either of the above interfaces is called
 * so long as an Op implements one of them.
 */
public interface Op {
}
