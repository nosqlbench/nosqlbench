package io.nosqlbench.engine.cli;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgsFileTest {

    @Test
    public void testLoadingArgs() {
        LinkedList<String> result;
        ArgsFile argsFile = new ArgsFile();
        result = argsFile.process("-argsfile", "src/test/resources/argsfiles/nonextant.cli");
        assertThat(result).containsExactly();
        result = argsFile.process("-argsfile", "src/test/resources/argsfiles/alphagamma.cli");
        assertThat(result).containsExactly("alpha", "gamma");
    }

    @Test(expected = RuntimeException.class)
    public void testLoadingMissingRequiredFails() {
        LinkedList<String> result;
        ArgsFile argsFile = new ArgsFile();
        result = argsFile.process("-argsfile-required", "src/test/resources/argsfiles/nonextant.cli");
    }

    @Test
    public void testLoadingInPlace() {
        LinkedList<String> result;
        LinkedList<String> commands = new LinkedList<>(List.of("--abc", "--def", "-argsfile", "src/test/resources/argsfiles/alphagamma.cli"));
        ArgsFile argsFile = new ArgsFile().preload("-argsfile-optional", "src/test/resources/argsfiles/alphagamma.cli");
        result = argsFile.process(commands);
        assertThat(result).containsExactly("alpha", "gamma", "--abc", "--def", "alpha", "gamma");

    }

}