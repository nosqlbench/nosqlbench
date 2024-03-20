/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.milvus;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MilvusUtilsTest {

    @Test
    void testSplitNames() {
        assertThrows(AssertionError.class, () -> MilvusUtils.splitNames(null));
        assertThrows(AssertionError.class, () -> MilvusUtils.splitNames(""));
        assertEquals(List.of("abc"), MilvusUtils.splitNames("abc"));
        assertEquals(List.of("abc", "def"), MilvusUtils.splitNames("abc def"));
        assertEquals(List.of("abc", "def"), MilvusUtils.splitNames("abc,def"));
        assertEquals(List.of("abc", "def"), MilvusUtils.splitNames("abc , def"));
    }

    @Test
    void testSplitLongs() {
        assertThrows(AssertionError.class, () -> MilvusUtils.splitLongs(null));
        assertThrows(AssertionError.class, () -> MilvusUtils.splitLongs(""));
        assertEquals(List.of(123L), MilvusUtils.splitLongs("123"));
        assertEquals(List.of(123L, 456L), MilvusUtils.splitLongs("123 456"));
        assertEquals(List.of(123L, 456L), MilvusUtils.splitLongs("123,456"));
        assertEquals(List.of(123L, 456L), MilvusUtils.splitLongs("123 , 456"));
        assertThrows(NumberFormatException.class, () -> MilvusUtils.splitLongs("abc"));
    }

    @Test
    void testMaskDigits() {
        assertThrows(AssertionError.class, () -> MilvusUtils.maskDigits(""));
        assertThrows(AssertionError.class, () -> MilvusUtils.maskDigits(null));
        assertEquals("abc", MilvusUtils.maskDigits("abc"));
        assertEquals("***", MilvusUtils.maskDigits("123"));
        assertEquals("abc***def", MilvusUtils.maskDigits("abc123def"));
    }
}
