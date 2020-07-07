package io.nosqlbench.engine.cli;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CmdTest {

    private final static PathCanonicalizer p = new PathCanonicalizer();

    @Test
    public void testCmdForWaitMillis() {

        Cmd cmd = Cmd.parseArg(new LinkedList<String>(List.of("waitmillis", "234")), p);
        assertThat(cmd.getArg("millis_to_wait")).isEqualTo("234");
        assertThat(cmd.toString()).isEqualTo("waitMillis('234');");
    }

    @Test
    public void testCmdForStart() {
        Cmd cmd = Cmd.parseArg(new LinkedList<>(List.of("start","type=stdout","otherparam=foo")),p);
        assertThat(cmd.toString()).isEqualTo("start({\n" +
            "    'type':       'stdout',\n" +
            "    'otherparam': 'foo'\n" +
            "});");
    }

}
