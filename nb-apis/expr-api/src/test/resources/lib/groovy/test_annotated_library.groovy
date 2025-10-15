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

/*
 * @Library
 * Test library demonstrating annotation-based function metadata
 */

import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec
import io.nosqlbench.nb.api.expr.annotations.ExprExample

/**
 * Multiplies two numbers together
 */
@ExprFunctionSpec(
    name = "multiply",
    synopsis = "multiply(a, b)",
    description = "Multiplies two numbers and returns the result"
)
@ExprExample(args = ["2", "3"], expect = "6")
@ExprExample(args = ["5", "10"], expect = "50")
def multiply(a, b) {
    return a * b
}

/**
 * Adds two numbers together
 */
@ExprFunctionSpec(
    name = "add",
    synopsis = "add(a, b)",
    description = "Adds two numbers and returns the sum"
)
@ExprExample(args = ["1", "2"], expect = "3")
def add(a, b) {
    return a + b
}

/**
 * Squares a number
 */
@ExprFunctionSpec(
    name = "square",
    synopsis = "square(n)",
    description = "Returns the square of a number"
)
@ExprExample(args = ["5"], expect = "25")
def square(n) {
    return n * n
}
