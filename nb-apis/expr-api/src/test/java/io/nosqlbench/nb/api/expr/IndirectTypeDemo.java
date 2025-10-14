package io.nosqlbench.nb.api.expr;

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


import java.net.URI;
import java.util.Map;

/**
 * Demo showing how types without custom toString are displayed indirectly.
 */
public class IndirectTypeDemo {

    // Custom class with no toString override
    static class CustomObject {
        String data = "internal data";
    }

    public static void main(String[] args) {
        ExprPreprocessor preprocessor = new ExprPreprocessor();

        String template = """
            values:
              normalString: {{=str = 'Hello World'}}
              numberValue: {{=num = 42}}
              listValue: {{=items = [1, 2, 3]}}
              customObject: {{=obj = new io.nosqlbench.nb.api.expr.IndirectTypeDemo.CustomObject()}}
            """;

        System.out.println("Demo: Indirect Type Description");
        System.out.println("=" + "=".repeat(79));
        System.out.println();

        ProcessingResult result = preprocessor.processWithContext(
            template,
            URI.create("test://indirect-types"),
            Map.of()
        );

        System.out.println(result.getFormattedContext());
        System.out.println();
        System.out.println("Notice:");
        System.out.println("- Normal types with toString show their values");
        System.out.println("- Custom types without toString show [fully.qualified.ClassName]");
    }
}
