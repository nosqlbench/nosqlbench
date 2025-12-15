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
 * Test library for annotation support
 */

import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec

// Test 1: Can we annotate a field in a script? (Likely NO - fields can't have METHOD target)
// @ExprFunctionSpec(name = "testFunc", synopsis = "testFunc(x)", description = "Test function")
// testFunc = { x -> x * 2 }

// Test 2: Can we define a class with annotated methods in a script? (Likely YES)
class TestFunctions {
    @ExprFunctionSpec(name = "multiply", synopsis = "multiply(a, b)", description = "Multiplies two numbers")
    static def multiply(a, b) {
        return a * b
    }
}

// Test 3: Can we define methods at script level? (YES - but they're on the Script class)
@ExprFunctionSpec(name = "scriptMethod", synopsis = "scriptMethod(x)", description = "A script-level method")
def scriptLevelMethod(x) {
    return x * 3
}
