package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedCommandTest {

    ParsedCommand pc = new ParsedCommand(
        new OpData().applyFields(
            Map.of(
                "op", Map.of(
                    "stmt", "test",
                    "dyna1", "{dyna1}",
                    "dyna2", "{{NumberNameToString()}}",
                    "identity", "{{Identity()}}"
                ),
                "bindings", Map.of(
                    "dyna1", "NumberNameToString()"
                )
            )
        )
    );

    @Test
    public void testParsedCommand() {
        Map<String, Object> m1 = pc.apply(0);
        assertThat(m1).containsEntry("stmt", "test");
        assertThat(m1).containsEntry("dyna1","zero");
        assertThat(m1).containsEntry("dyna2","zero");
        assertThat(m1).containsEntry("identity", 0L);
    }

    @Test
    public void testNewListBinder() {
        LongFunction<List<Object>> lb = pc.newListBinder("dyna1", "identity", "dyna2", "identity");
        List<Object> objects = lb.apply(1);
        assertThat(objects).isEqualTo(List.of("one",1L,"one",1L));
    }

    @Test
    public void testNewMapBinder() {
        LongFunction<Map<String, Object>> mb = pc.newOrderedMapBinder("dyna1", "identity", "dyna2");
        Map<String, Object> objects = mb.apply(2);
        assertThat(objects).isEqualTo(Map.<String,Object>of("dyna1","two","identity",2L,"dyna2","two"));
    }

    @Test
    public void testNewAryBinder() {
        LongFunction<Object[]> ab = pc.newArrayBinder("dyna1", "dyna1", "identity", "identity");
        Object[] objects = ab.apply(3);
        assertThat(objects).isEqualTo(new Object[]{"three","three",3L,3L});
    }
}
