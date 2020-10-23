package io.nosqlbench.engine.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsFile {
    private final Path argsPath;

    public ArgsFile(String path) {
        this.argsPath = Path.of(path);
    }

    public LinkedList<String> doArgsFile(String argsfileSpec, LinkedList<String> arglist) {
        return null;
    }

    private LinkedList<String> spliceArgs(String argsfileSpec, LinkedList<String> arglist) {
        Pattern envpattern = Pattern.compile("(?<envvar>\\$[A-Za-z_]+)");
        Matcher matcher = envpattern.matcher(argsfileSpec);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String envvar = matcher.group("envvar");
            String value = System.getenv(envvar);
            if (value == null) {
                throw new RuntimeException("Env var '" + envvar + "' was not found in the environment.");
            }
            matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);
        Path argfilePath = Path.of(sb.toString());
        List<String> lines = null;
        try {
            lines = Files.readAllLines(argfilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // TODO: finish update logic here
        return arglist;

    }


    public LinkedList<String> pin(LinkedList<String> arglist) {
        return arglist;
    }

    public LinkedList<String> unpin(LinkedList<String> arglist) {
        return arglist;
    }
}
