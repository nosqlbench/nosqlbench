package io.nosqlbench.engine.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <H1>Synopsis</H1>
 *
 * ArgsFile is a command-line modifier which can take linked list of
 * command args and modify it, and/or modify argsfile refrenced in this way.
 *
 * <H1>ArgsFile Selection</H1>
 *
 * During processing, any occurence of '-argsfile' selects the active argsfile and loads
 * it into the command line in place of the '-argsfile' argument. By default the args file
 * will be loaded if it exists, and a warning will be given if it does not.
 *
 * The '-argsfile-required &lt;somepath&gt;' version will throw an error if the args file
 * is not present, but it will not report any warnings or details otherwise.
 *
 * The `-argsfile-optional &lt;somepath&gt; version will not throw an error if the args
 * file is not present, and it will not report any warnings or details otherwise.
 *
 * A prefix command line can be given to ArgsFile to pre-load any settings. In this way
 * it is possible to easily provide a default args file which will be loaded. For example,
 * A prefix command of '-argsfile-optional &lt;somepath&gt;' will load options if they are
 * available in the specified file, but will otherwise provide no feedback to the user.
 *
 * <H1>ArgsFile Injection</H1>
 *
 * When an argsfile is loaded, it reads a command from each line into the current position
 * of the command line. No parsing is done. Blank lines are ignored. Newlines are used as the
 * argument delimiter, and lines that end with a backslash before the newline are automatically
 * joined together.
 *
 * <H1>ArgsFile Diagnostics</H1>
 *
 * All modifications to the command line should be reported to the logging facility at
 * INFO level. This assumes that the calling layer wants to inform users of command line injections,
 * and that the user can select to be notified of warnings only if desired.
 *
 * <H1>Environment Variables</H1>
 *
 * Simple environment variable substitution is attempted for any pattern which appears as '$' followed
 * by all uppercase letters and underscores. Any references of this type which are not resolvable
 * will cause an error to be thrown.
 */
public class ArgsFile {
    private final static Logger logger = LoggerFactory.getLogger(ArgsFile.class);

    private Path argsPath;
    private LinkedList<String> preload;

    public ArgsFile() {
    }

    public ArgsFile preload(String... preload) {
        this.preload = new LinkedList<String>(Arrays.asList(preload));
        return this;
    }

    private enum Selection {
        // Ignore if not present, show injections at info
        IgnoreIfMissing,
        // Warn if not present, but show injections at info
        WarnIfMissing,
        // throw error if not present, show injections at info
        ErrorIfMissing
    }

    public LinkedList<String> process(String... args) {
        return process(new LinkedList<String>(Arrays.asList(args)));
    }

    public LinkedList<String> process(LinkedList<String> commandline) {
        if (preload != null) {
            LinkedList<String> modified = new LinkedList<String>();
            modified.addAll(preload);
            modified.addAll(commandline);
            preload = null;
            commandline = modified;
        }
        LinkedList<String> composed = new LinkedList<>();
        while (commandline.peekFirst() != null) {
            String arg = commandline.peekFirst();
            switch (arg) {
                case "-argsfile":
                    commandline.removeFirst();
                    String argspath = readWordOrThrow(commandline, "path to an args file");
                    setArgsFile(argspath, Selection.WarnIfMissing);
                    commandline = loadArgs(this.argsPath, Selection.WarnIfMissing, commandline);
                    break;
                case "-argsfile-required":
                    commandline.removeFirst();
                    String argspathRequired = readWordOrThrow(commandline, "path to an args file");
                    setArgsFile(argspathRequired, Selection.ErrorIfMissing);
                    commandline = loadArgs(this.argsPath, Selection.ErrorIfMissing, commandline);
                    break;
                case "-argsfile-optional":
                    commandline.removeFirst();
                    String argspathOptional = readWordOrThrow(commandline, "path to an args file");
                    setArgsFile(argspathOptional, Selection.IgnoreIfMissing);
                    commandline = loadArgs(this.argsPath, Selection.IgnoreIfMissing, commandline);
                    break;
                case "-pin":
                    commandline.removeFirst();
                    commandline = pinArg(commandline);
                    break;
                case "-unpin":
                    commandline.removeFirst();
                    commandline = unpinArg(commandline);
                    break;
                default:
                    composed.addLast(commandline.removeFirst());
            }

        }
        return composed;
    }

    private LinkedList<String> loadArgs(Path argspath, Selection mode, LinkedList<String> commandline) {
        if (!assertArgsFileExists(argspath, mode)) {
            return commandline;
        }
        List<String> lines = null;
        try {
            lines = Files.readAllLines(argspath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> content = lines.stream()
                .filter(s -> !s.startsWith("#"))
                .filter(s -> !s.startsWith("/"))
                .filter(s -> !s.isBlank())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        StringBuilder splitword = new StringBuilder();
        LinkedList<String> loaded = new LinkedList<>();
        for (String s : content) {
            splitword.append(s);
            if (!s.endsWith("\\")) {
                loaded.addLast(splitword.toString());
                splitword.setLength(0);
            } else {
                splitword.setLength(splitword.length() - 1);
            }
        }
        if (splitword.length() > 0) {
            throw new RuntimeException("unqualified line continuation for '" + splitword.toString() + "'");
        }

        Iterator<String> injections = loaded.descendingIterator();
        while (injections.hasNext()) {
            String injection = injections.next();
            injection = injectEnv(injection);
            commandline.addFirst(injection);
        }

        return commandline;
    }

    private boolean assertArgsFileExists(Path argspath, Selection mode) {
        if (!Files.exists(argsPath)) {
            switch (mode) {
                case ErrorIfMissing:
                    throw new RuntimeException("A required argsfile was specified, but it does not exist: '" + argspath + "'");
                case WarnIfMissing:
                    logger.warn("An argsfile was specified, but it does not exist: '" + argspath + "'");
                case IgnoreIfMissing:
            }
            return false;
        }
        return true;
    }

    private void setArgsFile(String argspath, Selection mode) {
        this.argsPath = Path.of(argspath);
//        assertIfMissing(this.argsPath,mode);
    }

    private String readWordOrThrow(LinkedList<String> commandline, String description) {
        String found = commandline.peekFirst();
        if (found == null) {
            throw new RuntimeException("Unable to read argument top option for " + description);
        }
        return commandline.removeFirst();
    }

    private LinkedList<String> pinArg(LinkedList<String> commandline) {
        if (this.argsPath == null) {
            throw new RuntimeException("No argsfile has been selected before using the pin option.");
        }
        return commandline;
    }

    private LinkedList<String> unpinArg(LinkedList<String> commandline) {
        if (this.argsPath == null) {
            throw new RuntimeException("No argsfile has been selected before using the unpin option.");
        }
        return commandline;
    }

    private String injectEnv(String word) {
        Pattern envpattern = Pattern.compile("(?<envvar>\\$[A-Z_]+)");
        Matcher matcher = envpattern.matcher(word);
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
        return sb.toString();
    }

    public LinkedList<String> pin(LinkedList<String> arglist) {
        return arglist;
    }

    public LinkedList<String> unpin(LinkedList<String> arglist) {
        return arglist;
    }
}
