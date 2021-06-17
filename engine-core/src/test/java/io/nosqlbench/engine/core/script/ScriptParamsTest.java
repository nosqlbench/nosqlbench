package io.nosqlbench.engine.core.script;

import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ScriptParamsTest {

    @Test
    public void testThatNullOverridesKeyThrowsBasicError() {
        ScriptParams p = new ScriptParams();
        p.putAll(Map.of("a","b"));
        p.withDefaults(Map.of("c","d"));
        HashMap<String, String> overrides = new HashMap<>();
        overrides.put(null,"test");
        assertThatExceptionOfType(BasicError.class)
                .isThrownBy(() -> p.withOverrides(overrides));
    }

}
