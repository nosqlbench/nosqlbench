package io.nosqlbench.engine.api.templating;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandTemplateTest {

    @Test
    public void testCommandTemplate() {
        StmtsDocList stmtsDocs = StatementsLoader.loadString("" +
            "statements:\n" +
            " - s1: test1=foo test2=bar",
            Map.of()
        );
        OpTemplate optpl = stmtsDocs.getStmts().get(0);
        CommandTemplate ct = new CommandTemplate(optpl);
        assertThat(ct.isStatic()).isTrue();
    }

    @Test
    public void testCommandTemplateFormat() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StmtsDocList stmtsDocs = StatementsLoader.loadString("" +
            "statements:\n" +
            " - s1: test1=foo test2={bar}\n" +
            "   bindings:\n" +
            "    bar: NumberNameToString();\n",
            Map.of()
        );
        OpTemplate optpl = stmtsDocs.getStmts().get(0);
        CommandTemplate ct = new CommandTemplate(optpl);
        String format = gson.toJson(ct);
        System.out.println(format);

    }

}
