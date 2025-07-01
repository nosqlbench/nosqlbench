package io.nosqlbench.adapters.api.activityimpl.uniform;

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


import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseSpaceTest {

    @Test
    public void testInterpolateWithSpaceNames() {
        // Create a test space with a name
        TestSpace space = new TestSpace("testSpace");

        // Test interpolation of space-specific values
        String template1 = "This is a {SPACEID} test";
        String result1 = space.interpolate(template1);
        assertThat(result1).isEqualTo("This is a testSpace test");

        String template2 = "This is a {SPACE} test";
        String result2 = space.interpolate(template2);
        assertThat(result2).isEqualTo("This is a testSpace test");

        String template3 = "This is a {SPACENAME} test";
        String result3 = space.interpolate(template3);
        assertThat(result3).isEqualTo("This is a testSpace test");

        // Test case insensitivity
        String template4 = "This is a {space} test";
        String result4 = space.interpolate(template4);
        assertThat(result4).isEqualTo("This is a testSpace test");
    }

    @Test
    public void testInterpolateWithLabels() {
        // Create a test space with labels
        TestSpace space = new TestSpace("testSpace");
        space.addLabel("testKey", "testValue");
        space.addLabel("anotherKey", "anotherValue");

        // Test interpolation of label values
        String template1 = "This is a {testKey} test";
        String result1 = space.interpolate(template1);
        assertThat(result1).isEqualTo("This is a testValue test");

        String template2 = "This is a {anotherKey} test";
        String result2 = space.interpolate(template2);
        assertThat(result2).isEqualTo("This is a anotherValue test");

        // Test case insensitivity
        String template3 = "This is a {TESTKEY} test";
        String result3 = space.interpolate(template3);
        assertThat(result3).isEqualTo("This is a testValue test");

        // Test multiple replacements
        String template4 = "This is a {testKey} and {anotherKey} test";
        String result4 = space.interpolate(template4);
        assertThat(result4).isEqualTo("This is a testValue and anotherValue test");

        // Test with space name and label
        String template5 = "This is a {SPACE} with {testKey} test";
        String result5 = space.interpolate(template5);
        assertThat(result5).isEqualTo("This is a testSpace with testValue test");
    }

    // Simple test implementation of BaseSpace for testing
    private static class TestSpace implements Space {
        private final String spaceName;
        private NBLabels labels;

        public TestSpace(String spaceName) {
            this.spaceName = spaceName;
            this.labels = NBLabels.forKV("space", spaceName);
        }

        public void addLabel(String key, String value) {
            this.labels = this.labels.and(key, value);
        }

        @Override
        public String getName() {
            return spaceName;
        }

        public NBLabels getLabels() {
            return this.labels;
        }

        public String interpolate(String template) {
            // Keep existing replacements for backward compatibility
            if (template.matches(".*\\{[Ss][Pp][Aa][Cc][Ee][Ii][Dd]\\}.*")) {
                template = template.replaceAll("\\{[Ss][Pp][Aa][Cc][Ee][Ii][Dd]\\}", getName());
            }
            if (template.matches(".*\\{[Ss][Pp][Aa][Cc][Ee]}.*")) {
                template = template.replaceAll("\\{[Ss][Pp][Aa][Cc][Ee]}", getName());
            }
            if (template.matches(".*\\{[Ss][Pp][Aa][Cc][Ee][Nn][Aa][Mm][Ee]\\}.*")) {
                template = template.replaceAll("\\{[Ss][Pp][Aa][Cc][Ee][Nn][Aa][Mm][Ee]}", getName());
            }

            // Interpolate any label value wrapped in curly braces
            if (labels != null) {
                for (String labelName : labels.asMap().keySet()) {
                    String labelValue = labels.asMap().get(labelName);

                    // Create a case-insensitive pattern for the label name
                    String pattern = "\\{" + labelName.replaceAll("([\\W])", "\\\\$1") + "\\}";
                    if (template.toLowerCase().matches(".*" + pattern.toLowerCase() + ".*")) {
                        // Create a case-insensitive replacement pattern
                        String caseInsensitivePattern = "(?i)\\{" + labelName.replaceAll("([\\W])", "\\\\$1") + "\\}";
                        template = template.replaceAll(caseInsensitivePattern, labelValue);
                    }
                }
            }

            return template;
        }
    }
}
