package io.nosqlbench.engine.cli;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCLIArgsFileTest {

    @Test
    public void testLoadingArgs() {
        LinkedList<String> result;
        NBCLIArgsFile argsFile = new NBCLIArgsFile();
        result = argsFile.process("--argsfile", "src/test/resources/argsfiles/nonextant.cli");
        assertThat(result).containsExactly();
        result = argsFile.process("--argsfile", "src/test/resources/argsfiles/alphagamma.cli");
        assertThat(result).containsExactly("alpha", "gamma");
    }

    @Test
    public void loadParamsWithEnv() {
        NBCLIArgsFile argsfile = new NBCLIArgsFile();
        LinkedList<String> result = argsfile.process("--argsfile-required", "src/test/resources/argsfiles/home_env.cli");
        System.out.println(result);
    }

    @Test(expected = RuntimeException.class)
    public void testLoadingMissingRequiredFails() {
        LinkedList<String> result;
        NBCLIArgsFile argsFile = new NBCLIArgsFile();
        result = argsFile.process("--argsfile-required", "src/test/resources/argsfiles/nonextant.cli");
    }

    @Test
    public void testLoadingInPlace() {
        LinkedList<String> result;
        LinkedList<String> commands = new LinkedList<>(List.of("--abc", "--def"));

        NBCLIArgsFile argsFile = new NBCLIArgsFile().preload("--argsfile-optional", "src/test/resources/argsfiles/alphagamma" +
                ".cli");
        result = argsFile.process(commands);
        assertThat(result).containsExactly("alpha", "gamma", "--abc", "--def");
    }

    @Test
    public void testLinesToArgs() {
        NBCLIArgsFile argsfile = new NBCLIArgsFile().reserved("reservedword");
        LinkedList<String> args = argsfile.linesToArgs(
                List.of("--optionname argument", "--optionname2", "--opt3 reservedword")
        );
        assertThat(args).containsExactly("--optionname", "argument", "--optionname2", "--opt3", "reservedword");
    }

    @Test
    public void testOptionPinning() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(
                    "tmpfile",
                    "cli",
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-rw-r--"))
            );

            // preloading is a way to have global defaults based on
            // the presence of a default file
            NBCLIArgsFile argsfile = new NBCLIArgsFile().preload(
                    "--argsfile-optional", tempFile.toAbsolutePath().toString()
            );

            LinkedList<String> commandline;

            commandline = argsfile.process(
                    "--pin", "--option1",
                    "--pin", "--option1",
                    "--pin", "--option2", "arg2"
            );

            String filecontents;
            filecontents = Files.readString(tempFile);

            // logging should indicate no changes
            commandline = argsfile.process(
                    "--pin", "--option1",
                    "--pin", "--option1",
                    "--pin", "--option2", "arg2"
            );

            // unpinned options should be discarded
            commandline = argsfile.process(
                    "--unpin", "--option1",
                    "--option4", "--option5"
            );

            assertThat(commandline).containsExactly(
                    "--option4", "--option5"
            );

            assertThat(filecontents).isEqualTo("--option1\n--option2 arg2\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception ignored) {
            }
        }
    }


}