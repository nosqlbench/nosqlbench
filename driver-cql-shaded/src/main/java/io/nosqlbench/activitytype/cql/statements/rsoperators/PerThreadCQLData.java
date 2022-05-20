package io.nosqlbench.activitytype.cql.statements.rsoperators;

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


import com.datastax.driver.core.Row;

import java.util.LinkedList;

/**
 * This contains a linked list of {@link Row} objects. This is per-thread.
 * You can use this list as a per-thread data cache for sharing data between
 * cycles in the same thread.
 */
public class PerThreadCQLData {
    public final static ThreadLocal<LinkedList<Row>> rows = ThreadLocal.withInitial(LinkedList::new);
}
