package io.nosqlbench.engine.api.activityconfig;

import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDoc;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StatementsLoaderTest {

    @Test
    public void testTemplateVarSubstitution() {
        StmtsDocList stmtsDocs = StatementsLoader.loadPath(null, "activities/template_vars", "src/test/resources");
        assertThat(stmtsDocs).isNotNull();
        List<StmtsDoc> docs = stmtsDocs.getStmtDocs();
        assertThat(docs).hasSize(1);
        StmtsDoc stmtsBlocks = docs.get(0);
        Map<String, String> bindings = stmtsBlocks.getBindings();
        assertThat(bindings).isEqualTo(Map.of(
            "b1a","Prefix(\"prefix\")",
            "b1b","Prefix(\"prefix\")",
            "b2a","Suffix(\"suffix\")",
            "b2b","Suffix(\"suffix\")"));
    }

    @Test()
    public void testInvalidYamlProperties() {
        Exception caught = null;
        try {
            StatementsLoader.loadPath(null, "activities/invalid_prop", "src/test/resources");
        } catch (Exception e) {
            caught = e;
        }
        assertThat(caught).isNotNull();
    }



}
