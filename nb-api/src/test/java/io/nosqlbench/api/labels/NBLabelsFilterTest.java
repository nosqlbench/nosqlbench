/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.labels;

import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.labels.NBLabelsFilter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NBLabelsFilterTest {

    @Test
    public void testFilterLablesWildcardExclude() {
        NBLabelsFilter f1 = new NBLabelsFilter("+abcd,-.*");
        assertThat(f1.apply(NBLabels.forKV("abc", "def"))).isEqualTo(NBLabels.forKV());
        assertThat(f1.apply(NBLabels.forKV("abcd", "defg"))).isEqualTo(NBLabels.forKV("abcd", "defg"));
        assertThat(f1.apply(NBLabels.forKV("a_bcd", "def"))).isEqualTo(NBLabels.forKV());

    }

    @Test
    public void testFilterLabelsWildcardInclude(){
        NBLabelsFilter f2 = new NBLabelsFilter("-abcd,+.*");
        assertThat(f2.apply(NBLabels.forKV("abc", "def"))).isEqualTo(NBLabels.forKV("abc", "def"));
        assertThat(f2.apply(NBLabels.forKV("abcd", "defg","hijk","lmnop"))).isEqualTo(NBLabels.forKV("hijk","lmnop"));
        assertThat(f2.apply(NBLabels.forKV("a_bcd", "def"))).isEqualTo(NBLabels.forKV("a_bcd","def"));}

    @Test
    public void testFilteredLabelsBasicNames() {
        NBLabelsFilter f3 = new NBLabelsFilter("abcd,bcde,-fghi");
        assertThat(f3.apply(NBLabels.forKV("abc", "def", "abcd","abcd"))).isEqualTo(NBLabels.forKV("abc","def","abcd", "abcd"));
        assertThat(f3.apply(NBLabels.forKV("abcd", "defg"))).isEqualTo(NBLabels.forKV("abcd","defg"));
        assertThat(f3.apply(NBLabels.forKV("bcde","sdf","a_bcd", "def","fghi","fghi"))).isEqualTo(NBLabels.forKV("bcde","sdf","a_bcd","def"));

    }
}
