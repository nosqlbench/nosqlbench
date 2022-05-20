package io.nosqlbench.activitytype.cql.statements.modifiers;

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


import com.datastax.driver.core.Statement;

/**
 * Provides a modular way for any CQL activities to modify statements before execution.
 * Each active modifier returns a statement in turn.
 */
public interface StatementModifier {
    Statement modify(Statement unmodified, long cycleNum);
}
