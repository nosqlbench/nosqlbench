/*
 * Copyright (c) 2022 nosqlbench
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

package io.virtdata;

import io.nosqlbench.virtdata.library.basics.shared.distributions.DelimFrequencySampler;
import io.nosqlbench.virtdata.library.basics.shared.distributions.WeightedStringsFromCSV;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVFrequencySampler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegratedAliasMethodTests {

    @Test
    public void testCensusData() {
        WeightedStringsFromCSV surnames = new WeightedStringsFromCSV("Name", "prop100k", "data/surnames");
        String n = surnames.apply(2343);
        assertThat(n).isEqualTo("Conaway");
    }

    @Test
    public void testCSVFrequencySampler() {
        CSVFrequencySampler names= new CSVFrequencySampler("data/countries", "COUNTRY_CODE" );
        String n = names.apply(23);
        assertThat(n).isEqualTo("TK");
    }

    @Test
    public void testDelimFrequencySampler() {
        DelimFrequencySampler names= new DelimFrequencySampler(
                "data/countries",
                "COUNTRY_CODE",
                ','
        );
        String n = names.apply(23);
        assertThat(n).isEqualTo("TK");
    }
}
