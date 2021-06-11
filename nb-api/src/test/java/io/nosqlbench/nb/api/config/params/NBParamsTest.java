package io.nosqlbench.nb.api.config.params;


import org.junit.jupiter.api.Disabled;
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
        assertThat(one.get("key1", String.class).get()).isOfAnyClassIn(String.class);
        assertThat(one.get("key1", String.class).get()).isEqualTo("value1");
    }

    @Test
    public void testNestedMapObject() {
        Element one = NBParams.one(Map.of("key1", Map.of("key2", "value2")));
        assertThat(one.get("key1.key2", String.class).get()).isOfAnyClassIn(String.class);
        assertThat(one.get("key1.key2", String.class).get()).isEqualTo("value2");
    }

    @Test
    public void testNestedMixedJsonMapParams() {
        Element one = NBParams.one("{\"key1\":{\"key2\":\"key3=value3 key4=value4\"}}");
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class).get()).isEqualTo("value3");
        assertThat(one.get("key1.key2.key4", String.class).get()).isEqualTo("value4");
    }

    @Test
    @Disabled("This case is unwieldy and generally not useful")
    public void testNestedMixedJsonParamsMap() {
        Element one = NBParams.one("{\"key1\":\"key2={\"key3\":\"value3\",\"key4\":\"value4\"}\"}");
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class).get()).isEqualTo("value3");
        assertThat(one.get("key1.key2.key4", String.class).get()).isEqualTo("value4");
    }

    @Test
    public void testNestedMixedMapJsonParams() {
        Element one = NBParams.one(Map.of("key1", "{ \"key2\": \"key3=value3 key4=value4\"}"));
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class).get()).isEqualTo("value3");
        assertThat(one.get("key1.key2.key4", String.class).get()).isEqualTo("value4");
    }

    @Test
    public void testNestedMixedMapParamsJson() {
        Element one = NBParams.one(Map.of("key1", "key2: {\"key3\":\"value3\",\"key4\":\"value4\"}"));
        assertThat(one.get("key1.key2.key3", String.class)).isPresent();
        assertThat(one.get("key1.key2.key3", String.class).get()).isEqualTo("value3");
        assertThat(one.get("key1.key2.key4", String.class).get()).isEqualTo("value4");
    }

    @Test
    public void testJsonText() {
        Element one = NBParams.one("{\"key1\":\"value1\"}");
        assertThat(one.get("key1", String.class)).isPresent();
        assertThat(one.get("key1", String.class).get()).isEqualTo("value1");
    }

    @Test
    public void testNamedFromJsonSeq() {
        Element one = NBParams.one("[{\"name\":\"first\",\"val\":\"v1\"},{\"name\":\"second\",\"val\":\"v2\"}]");
        assertThat(one.get("first.val", String.class)).isPresent();
        assertThat(one.get("first.val", String.class).get()).isEqualTo("v1");
    }

    @Test
    public void testNamedFromMapSeq() {
        Element one = NBParams.one(List.of(Map.of("name", "first", "val", "v1"), Map.of("name", "second", "val", "v2")));
        assertThat(one.get("first.val", String.class)).isPresent();
        assertThat(one.get("first.val", String.class).get()).isEqualTo("v1");
    }

}
