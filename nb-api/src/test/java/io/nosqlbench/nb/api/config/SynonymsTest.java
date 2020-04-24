package io.nosqlbench.nb.api.config;

import io.nosqlbench.nb.api.config.Synonyms;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SynonymsTest {

    @Test
    public void testCanonicalizeText() {
        String before = "test yaml ayamlfoo type btype typea targetrate";
        StringBuilder sb = new StringBuilder();
        String result = Synonyms.canonicalize(before, Synonyms.PARAM_SYNONYMS,
            (s, s2) -> sb.append("replaced ").append(s).append(" with ").append(s2).append("\n"));
        assertThat(result).isEqualTo("test workload ayamlfoo driver btype typea cyclerate");
        assertThat(sb.toString()).isEqualTo("replaced type with driver\n" +
            "replaced targetrate with cyclerate\n" +
            "replaced yaml with workload\n");
    }

}
