package io.nosqlbench.engine.cli;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicScriptBufferTest {

    @Test
    public void testScriptInterpolation() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "script_to_interpolate", "parameter1=replaced"});

        BasicScriptBuffer b = new BasicScriptBuffer(null);
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();

        assertThat(s).contains("let foo=replaced;");
        assertThat(s).contains("let bar=UNSET:parameter2");
    }

    @Test
    public void testAutoScriptCommand() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "acommand" });
        BasicScriptBuffer b = new BasicScriptBuffer(null);
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();

        assertThat(s).contains("acommand script text");
    }

    @Test
    public void testScriptParamsSingle() {
        NBCLIOptions opts = new NBCLIOptions(new String[] {
            "script",
            "testscripts/printscript.js",
            "param1=value1"
        });
        BasicScriptBuffer b = new BasicScriptBuffer(null);
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String script = b.getParsedScript();

        assertThat(script).matches("(?s).*a single line.*");
    }

    @Test
    public void testScriptParamsMulti() {
        NBCLIOptions opts = new NBCLIOptions(new String[] {
            "script",
            "testscripts/printscript.js",
            "param1=value1",
            "script",
            "testscripts/printparam.js",
            "paramname=another",
            "param2=andanother"
        });
        BasicScriptBuffer b = new BasicScriptBuffer(null);
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String script = b.getParsedScript();

        assertThat(script).matches("(?s).*a single line.*");
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowErrorForInvalidWaitMillisOperand() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "waitmillis", "noway" });
        BasicScriptBuffer b = new BasicScriptBuffer(null);
        b.add(opts.getCommands().toArray(new Cmd[0]));
        String s = b.getParsedScript();
    }



}
