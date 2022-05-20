package io.nosqlbench.engine.cli;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

    @Test
    public void testLoadingMissingRequiredFails() {
        NBCLIArgsFile argsFile = new NBCLIArgsFile();
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> argsFile.process("--argsfile-required", "src/test/resources/argsfiles/nonextant.cli"));
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
