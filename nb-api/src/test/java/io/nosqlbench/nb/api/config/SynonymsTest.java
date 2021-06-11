package io.nosqlbench.nb.api.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SynonymsTest {

    @Test
    public void testCanonicalizeText() {
        String before = "test yaml ayamlfoo type btype typea targetrate";
        StringBuilder sb = new StringBuilder();
        String result = Synonyms.canonicalize(before, Synonyms.PARAM_SYNONYMS,
            (s, s2) -> sb.append("replaced ").append(s).append(" with ").append(s2).append("\n"));
        assertThat(result).isEqualTo("test workload ayamlfoo driver btype typea rate");
        assertThat(sb.toString()).isEqualTo("replaced type with driver\n" +
                "replaced targetrate with rate\n" +
                "replaced yaml with workload\n");
    }

}
