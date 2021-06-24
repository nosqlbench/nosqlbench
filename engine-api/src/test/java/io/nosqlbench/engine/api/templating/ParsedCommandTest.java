package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpData;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedCommandTest {

    @Test
    public void testParsedCommand() {
        ParsedCommand pc = new ParsedCommand(
            new OpData().applyFields(
                Map.of(
                    "op", Map.of(
                        "stmt", "test",
                        "dyna1", "{dyna1}",
                        "dyna2", "{{NumberNameToString()}}"
                    ),
                    "bindings", Map.of(
                        "dyna1", "NumberNameToString()"
                    )
                )
            )
        );
        Map<String, Object> m1 = pc.apply(0);
        assertThat(m1).containsEntry("stmt", "test");
    }

}
