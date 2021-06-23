package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpData;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ParsedCommandTest {

    @Test
    public void testParsedCommand() {
        ParsedCommand pc = new ParsedCommand(new OpData().applyFields(Map.of("op", Map.of("stmt", "test"))));
    }

}
