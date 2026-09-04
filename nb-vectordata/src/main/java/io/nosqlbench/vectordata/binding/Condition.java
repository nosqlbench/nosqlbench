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
package io.nosqlbench.vectordata.binding;

import io.nosqlbench.vectordata.anode.PNode.OpType;

/// One condition of a predicate, prepared. The statement fragment is
/// `field op ?`; only the comparands move per record.
///
/// @param field the metadata field this condition constrains
/// @param parameter what to call the parameter: the field's name unless a runtime overrode it
/// @param op the comparison
/// @param bindType what the parameter binds as — **from the field's tag,
///   not the comparand's variant**. An [io.nosqlbench.vectordata.anode.MValue]
///   has 29 variants and a comparand six, so a predicate over a `DateTime`
///   field carries an `Int` comparand; typing the parameter from that
///   would collapse every temporal and UUID column to a bigint or a string
/// @param arity how many comparands this condition carries: one for the
///   scalar operators, a membership set for `IN`
public record Condition(String field, String parameter, OpType op, BindType bindType, int arity) { }
