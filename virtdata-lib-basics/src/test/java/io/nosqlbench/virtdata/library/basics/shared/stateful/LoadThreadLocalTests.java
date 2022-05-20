package io.nosqlbench.virtdata.library.basics.shared.stateful;

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


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoadThreadLocalTests {

    @Test
    public void testSaveLoadDouble() {
        new Clear().apply(0L);
        SaveDouble saver = new SaveDouble("doublename");
        LoadDouble loader = new LoadDouble("doublename");
        double passiveOutput = saver.applyAsDouble(123L);
        double loadedValue = loader.apply(456L);
        assertThat(passiveOutput).isEqualTo(123L);
        assertThat(loadedValue).isEqualTo(123L);
    }

    @Test
    public void testSaveLoadInteger() {
        new Clear().apply(0L);
        SaveInteger saver = new SaveInteger("intname");
        LoadInteger loader = new LoadInteger("intname");
        int passiveOutput = saver.applyAsInt(123);
        int loadedValue = loader.apply(456);
        assertThat(passiveOutput).isEqualTo(123);
        assertThat(loadedValue).isEqualTo(123);
    }

    @Test
    public void testSaveLoadFloat() {
        new Clear().apply(0L);
        SaveFloat saver = new SaveFloat("floatname");
        LoadFloat loader = new LoadFloat("floatname");
        float passiveOutput = saver.apply(123f);
        float loadedValue = loader.apply(456f);
        assertThat(passiveOutput).isEqualTo(123f);
        assertThat(loadedValue).isEqualTo(123f);
    }

    @Test
    public void testSaveLoadLong() {
        new Clear().apply(0L);
        SaveLong saver = new SaveLong("longname");
        LoadLong loader = new LoadLong("longname");
        long passiveOutput = saver.applyAsLong(123L);
        long loadedValue = loader.apply(456L);
        assertThat(passiveOutput).isEqualTo(123L);
        assertThat(loadedValue).isEqualTo(123L);
    }

    @Test
    public void testSaveLoadString() {
        new Clear().apply(0L);
        SaveString saver = new SaveString("longname");
        LoadString loader = new LoadString("longname");
        String passiveOutput = saver.apply("stringval");
        String loadedValue = loader.apply(456L);
        assertThat(passiveOutput).isEqualTo("stringval");
        assertThat(loadedValue).isEqualTo("stringval");
    }

}
