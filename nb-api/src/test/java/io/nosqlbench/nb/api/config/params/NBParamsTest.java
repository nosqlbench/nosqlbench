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

package io.nosqlbench.nb.api.config.params;


import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NBParamsTest {

    @Test
    public void testMapObject() {
        Element one = NBParams.one(Map.of("key1", "value1", "key2", new Date()));
        assertThat(one.get("key1", String.class)).isPresent();
        assertThat(one.get("key1", String.class)).containsInstanceOf(String.class);
        assertThat(one.get("key1", String.class)).contains("value1");
    }

    @Test
    public void testNestedMapObject() {
        Element one = NBParams.one(Map.of("key1", Map.of("key2", "value2")));
        assertThat(one.get("key1.key2", String.class)).containsInstanceOf(String.class);
        assertThat(one.get("key1.key2", String.class)).contains("value2");
    }

    @Test
    public void testNestedMixedJsonMapParams() {
        Element one = NBParams.one("{\"key1\":{\"key2\":\"key3=value3 key4=value4\"}}");
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class)).contains("value3");
        assertThat(one.get("key1.key2.key4", String.class)).contains("value4");
    }

    @Test
    // Fixed this. Agreed it is is generally unwieldy and unuseful as an example although it does test the underlying resolver mechanisms
    public void testNestedMixedJsonParamsMap() {
        String source = "{\"key1\":\"key2={\\\"key3\\\":\\\"value3\\\",\\\"key4\\\":\\\"value4\\\"}\"}";
        Element one = NBParams.one(source);
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class)).contains("value3");
        assertThat(one.get("key1.key2.key4", String.class)).contains("value4");
    }

    @Test
    public void testNestedMixedMapJsonParams() {
        Element one = NBParams.one(Map.of("key1", "{ \"key2\": \"key3=value3 key4=value4\"}"));
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class)).contains("value3");
        assertThat(one.get("key1.key2.key4", String.class)).contains("value4");
    }

    @Test
    public void testNestedMixedMapParamsJson() {
        Element one = NBParams.one(Map.of("key1", "key2: {\"key3\":\"value3\",\"key4\":\"value4\"}"));
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class)).contains("value3");
        assertThat(one.get("key1.key2.key4", String.class)).contains("value4");
    }

    @Test
    public void testJsonText() {
        Element one = NBParams.one("{\"key1\":\"value1\"}");
        assertThat(one.get("key1", String.class)).isPresent();
        assertThat(one.get("key1", String.class)).contains("value1");
    }

    @Test
    public void testNamedFromJsonSeq() {
        Element one = NBParams.one("[{\"name\":\"first\",\"val\":\"v1\"},{\"name\":\"second\",\"val\":\"v2\"}]");
        assertThat(one.get("first.val", String.class)).isPresent();
        assertThat(one.get("first.val", String.class)).contains("v1");
    }

    @Test
    public void testNamedFromMapSeq() {
        Element one = NBParams.one(List.of(Map.of("name", "first", "val", "v1"), Map.of("name", "second", "val", "v2")));
        assertThat(one.get("first.val", String.class)).isPresent();
        assertThat(one.get("first.val", String.class)).contains("v1");
    }


    @Test
    public void testDepthPrecedence() {
        Map<String, Object> a1 = Map.of(
            "a1", Map.of("b1", "v_a1_b1"),
            "a2.b2", Map.of("c2", "v_a2.b2_c2"),
            "a3",Map.of("b3.c3","v_a3_b3.c3-1"),
            "a3.b3",Map.of("c3","v_a3.b3_c3-2"),
            "a4.b4",Map.of("c4","v_a4.b4_c4-3"),
            "a5",Map.of("b5.c5","v_a5_b5.c5-4")
        );

        Element e = NBParams.one("testdata",a1);
        assertThat(e.get("a1",Map.class)).contains(Map.of("b1","v_a1_b1"));
        assertThat(e.get("a2.b2",Map.class)).contains(Map.of("c2","v_a2.b2_c2"));
        assertThat(e.get("a3.b3",Map.class)).contains(Map.of("c3","v_a3.b3_c3-2"));
        assertThat(e.get("a3.b3.c3",String.class)).contains("v_a3_b3.c3-1");
        assertThat(e.get("a4.b4.c4")).contains("v_a4.b4_c4-3");
        assertThat(e.get("a5.b5.c5")).contains("v_a5_b5.c5-4");

        // So far, this code does not decompose logical structure for things passed in composite name elements
        // This is not needed, maybe ever.
        assertThat(e.get("a5.b5")).isEmpty();
    }


}
