package io.nosqlbench.nb.api.components.core;

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


import io.nosqlbench.nb.api.advisor.NBAdvisorPoint;
import io.nosqlbench.nb.api.advisor.conditions.Conditions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NBAdvisorPointTest {

    @Test
    public void testAdvisorPointBasics() {
        NBAdvisorPoint<String> nap = new NBAdvisorPoint<>("labels should have no hyphens");
        nap.add(Conditions.NoHyphensError);
        nap.add(Conditions.NoSpacesWarning);

        String[] spaceErrors = nap.errorMessages("one two three");
        assertThat(spaceErrors)
            .containsExactly(new String[]{"WARN: String 'one two three' should not contain spaces"});

        String[] hyphenErrors = nap.errorMessages("one-two");
        assertThat(hyphenErrors)
            .containsExactly(new String[]{"ERROR: String 'one-two' should not contain hyphens"});

        String[] bothErrors = nap.errorMessages("one-two three");
        assertThat(bothErrors)
            .containsExactly(new String[]{
                "ERROR: String 'one-two three' should not contain hyphens",
                "WARN: String 'one-two three' should not contain spaces"
            });

	//int count = nap.evaluate();
	//assertThat(count).isEqualTo(4);
    }

}
