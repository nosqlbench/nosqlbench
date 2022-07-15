/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.cli;

import io.nosqlbench.api.system.NBEnvironment;
import io.nosqlbench.api.errors.BasicError;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <H1>Synopsis</H1>
 *
 * ArgsFile is a command-line modifier which can take linked list of
 * command args and modify it, and/or modify argsfile refrenced in this way.
 *
 * <H1>ArgsFile Selection</H1>
 *
 * During processing, any occurence of '--argsfile' selects the active argsfile and loads
 * it into the command line in place of the '--argsfile' argument. By default the args file
 * will be loaded if it exists, and a warning will be given if it does not.
 *
 * The '--argsfile-required &lt;somepath&gt;' version will throw an error if the args file
 * is not present, but it will not report any warnings or details otherwise.
 *
 * The `--argsfile-optional &lt;somepath&gt; version will not throw an error if the args
 * file is not present, and it will not report any warnings or details otherwise.
 *
 * A prefix command line can be given to ArgsFile to pre-load any settings. In this way
 * it is possible to easily provide a default args file which will be loaded. For example,
 * A prefix command of '--argsfile-optional &lt;somepath&gt;' will load options if they are
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
public class NBCLIArgsFile {
    private Logger logger;
//    = LogManager.getLogger("ARGSFILE");

    // Options which may contextualize other CLI options or commands.
    // These must be parsed first
    public static final String ARGS_FILE = "--argsfile";
    public static final String ARGS_FILE_OPTIONAL = "--argsfile-optional";
    public static final String ARGS_FILE_REQUIRED = "--argsfile-required";
    public static final String ARGS_PIN = "--pin";
    public static final String ARGS_UNPIN = "--unpin";

    private Path argsPath;
    private LinkedList<String> preload;
    private final Set<String> stopWords = new HashSet<>();
    private final LinkedHashSet<String> args = new LinkedHashSet<>();
    LinkedHashSet<String> argsToPin = new LinkedHashSet<>();
    LinkedHashSet<String> argsToUnpin = new LinkedHashSet<>();
    private final Set<String> readPaths = new HashSet<>();

    public NBCLIArgsFile() {
    }

    public NBCLIArgsFile preload(String... preload) {
        this.preload = new LinkedList<String>(Arrays.asList(preload));
        return this;
    }

    /**
     * Indicate which words are invalid for the purposes of matching
     * trailing parts of arguments. The provided words will not
     * be considered as valid values to arguments in any case.
     *
     * @param reservedWords Words to ignore in option values
     * @return this ArgsFile, for method chaining
     */
    public NBCLIArgsFile reserved(Collection<String> reservedWords) {
        this.stopWords.addAll(reservedWords);
        return this;
    }

    /**
     * Indicate which words are invalid for the purposes of matching
     * trailing parts of arguments. The provided words will not
     * be considered as valid values to arguments in any case.
     *
     * @param reservedWords Words to ignore in option values
     * @return this ArgsFile, for method chaining
     */
    public NBCLIArgsFile reserved(String... reservedWords) {
        this.stopWords.addAll(Arrays.asList(reservedWords));
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
                case ARGS_FILE:
                    pinAndUnpin();
                    commandline.removeFirst();
                    String argspath = readWordOrThrow(commandline, "path to an args file");
                    setArgsFile(argspath, Selection.WarnIfMissing);
                    commandline = mergeArgs(this.argsPath, Selection.WarnIfMissing, commandline);
                    break;
                case ARGS_FILE_REQUIRED:
                    commandline.removeFirst();
                    String argspathRequired = readWordOrThrow(commandline, "path to an args file");
                    setArgsFile(argspathRequired, Selection.ErrorIfMissing);
                    commandline = mergeArgs(this.argsPath, Selection.ErrorIfMissing, commandline);
                    break;
                case ARGS_FILE_OPTIONAL:
                    commandline.removeFirst();
                    String argspathOptional = readWordOrThrow(commandline, "path to an args file");
                    setArgsFile(argspathOptional, Selection.IgnoreIfMissing);
                    commandline = mergeArgs(this.argsPath, Selection.IgnoreIfMissing, commandline);
                    break;
                case ARGS_PIN:
                    commandline.removeFirst();
                    argsToPin.addAll(argsToLines(readOptionAndArg(commandline, false)));
                    break;
                case ARGS_UNPIN:
                    commandline.removeFirst();
                    argsToUnpin.addAll(argsToLines(readOptionAndArg(commandline, true)));
                    break;
                default:
                    composed.addLast(commandline.removeFirst());
            }
        }
        pinAndUnpin();
        return composed;
    }

    private void pinAndUnpin() {
        if (this.argsToUnpin.size() == 0 && this.argsToPin.size() == 0) {
            return;
        }
        LinkedHashSet<String> extant = readArgsFile(this.argsPath, Selection.IgnoreIfMissing);
        LinkedHashSet<String> mergedPins = mergePins(this.argsToPin, this.argsToUnpin, extant);
        if (extant.equals(mergedPins)) {
            if (logger != null) {
                logger.info("Pinning resulted in no changes to argsfile '" + this.argsPath.toString() + "'");
            }
        } else {
            if (logger != null) {
                logger.info("Writing updated argsfile '" + this.argsPath.toString() + "' with " +
                        (this.argsToPin.size() + this.argsToUnpin.size()) + " changes");
            }
            writeArgsFile(mergedPins);
        }

        this.argsToPin.clear();
        this.argsToUnpin.clear();

    }


    private LinkedHashSet<String> mergePins(
            LinkedHashSet<String> toPin,
            LinkedHashSet<String> toUnpin,
            LinkedHashSet<String> extant) {

        LinkedHashSet<String> merged = new LinkedHashSet<>();
        merged.addAll(extant);

        for (String arg : toPin) {
            if (argsToUnpin.contains(arg)) {
                throw new RuntimeException("You have both --pin and --unpin for '" + arg + ", I don't know which " +
                        "one you want.");
            }
        }
        for (String arg : toUnpin) {
            if (argsToPin.contains(arg)) {
                throw new RuntimeException("You have both --pin and --unpin for '" + arg + ", I don't know which " +
                        "one you want.");
            }
        }

        for (String toAdd : toPin) {
            if (merged.contains(toAdd)) {
                if (logger != null) {
                    logger.warn("Requested to pin argument again: '" + toAdd + "', ignoring");
                }
            } else {
                if (logger != null) {
                    logger.info("Pinning option '" + toAdd + "' to '" + this.argsPath.toString() + "'");
                }
                merged.add(toAdd);
            }
        }

        for (String toDel : toUnpin) {
            if (merged.contains(toDel)) {
                if (logger != null) {
                    logger.info("Unpinning '" + toDel + "' from '" + this.argsPath.toString() + "'");
                }
                merged.remove(toDel);
            } else {
                if (logger != null) {
                    logger.warn("Requested to unpin argument '" + toDel + "' which was not found in " + argsPath.toString());
                }
            }
        }

        return merged;
    }

    LinkedList<String> mergeArgs(Path argspath, Selection mode, LinkedList<String> commandline) {
        this.args.clear();
        if (this.readPaths.contains(argsPath.toString())) {
            throw new BasicError("Recursive reading of argsfile is detected for '" + argspath.toString() + "'.\n" +
                    "Please ensure that you do not have cyclic references in your arguments for argsfiles.");
        }
        LinkedHashSet<String> loaded = readArgsFile(argspath, mode);
        this.readPaths.add(argsPath.toString());

        List<String> interpolated = loaded.stream()
                .map(p -> {
                    String q = NBEnvironment.INSTANCE.interpolate(p).orElse(p);
                    if (!q.equals(p)) {
                        if (logger != null) {
                            logger.info("argsfile: '" + argsPath.toString() + "': loaded option '" + p + "' as '" + q + "'");
                        }
                    }
                    return q;
                })
                .collect(Collectors.toList());

        LinkedList<String> inArgvForm = linesToArgs(interpolated);
        this.args.addAll(inArgvForm);
        return concat(inArgvForm, commandline);
    }

    private LinkedList<String> concat(Collection<String>... entries) {
        LinkedList<String> composed = new LinkedList<>();
        for (Collection<String> list : entries) {
            composed.addAll(list);
        }
        return composed;
    }

    /**
     * <p>Load the args file into an args array. The returned format follows
     * the standard pattern of args as you would see for a main method, although
     * the internal format is structured to support easy editing and clarity.</p>
     *
     * <p>
     * The args file is stored in a simple option-per-line format which
     * follows these rules:
     * <UL>
     * <LI>Lines ending with a backslash (\) only are concatenated to the next
     * line with the backslash removed.
     * <LI>Line content consists of one option and one optional argument.</LI>
     * <LI>Options must start with at least one dash (-).</LI>
     * <LI>If an argument is provided for an option, it follows the option and a space.</LI>
     * <LI>Empty lines and lines which start with '//' or '#' are ignored.</LI>
     * <LI>Lines which are identical after applying the above rules are elided
     * down to the last occurence.</LI>
     * </UL>
     * </p>
     *
     * <p>
     * This allows for multi-valued options, or options which can be specified multiple
     * times with different arguments to be supported, so long as each occurrence has a
     * unique option value.
     * </p>
     *
     * @param argspath The path of the argsfile to load
     * @param mode     The level of feedback to provide in the case of a missing file
     * @return The argsfile content, structured like an args array
     */
    private LinkedHashSet<String> readArgsFile(Path argspath, Selection mode) {
        LinkedHashSet<String> args = new LinkedHashSet<>();

        if (!assertArgsFileExists(argspath, mode)) {
            return args;
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
        LinkedHashSet<String> loaded = new LinkedHashSet<>();
        for (String s : content) {
            splitword.append(s);
            if (!s.endsWith("\\")) {
                loaded.add(splitword.toString());
                splitword.setLength(0);
            } else {
                splitword.setLength(splitword.length() - 1);
            }
        }
        if (splitword.length() > 0) {
            throw new RuntimeException("unqualified line continuation for '" + splitword + "'");
        }

        return loaded;
    }

    /**
     * Write the argsfile in the format specified by {@link #readArgsFile(Path, Selection)}
     *
     * This method requires that an argsFile has been set by a previous
     * --argsfile or --argsfile-required or --argsfile-optional option.
     *
     * @param args The args to write in one-arg-per-line form
     */
    private void writeArgsFile(LinkedHashSet<String> args) {
        if (this.argsPath == null) {
            throw new RuntimeException("No argsfile has been selected before using the pin option.");
        }

        try {
            Files.createDirectories(
                    this.argsPath.getParent(),
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---"))
            );
            Files.write(this.argsPath, args);
        } catch (IOException e) {
            throw new BasicError("unable to write '" + this.argsPath + "': " + e.getMessage());
        }
    }

    private boolean assertArgsFileExists(Path argspath, Selection mode) {
        if (!Files.exists(argsPath)) {
            switch (mode) {
                case ErrorIfMissing:
                    throw new RuntimeException("A required argsfile was specified, but it does not exist: '" + argspath + "'");
                case WarnIfMissing:
                    if (logger != null) {
                        logger.warn("An argsfile was specified, but it does not exist: '" + argspath + "'");
                    }
                case IgnoreIfMissing:
            }
            return false;
        }
        return true;
    }

    private void setArgsFile(String argspath, Selection mode) {
        Path selected = null;
        String[] possibles = argspath.split(":");
        for (String possible : possibles) {
            Optional<String> expanded = NBEnvironment.INSTANCE.interpolate(possible);
            if (expanded.isPresent()) {
                Path possiblePath = Path.of(expanded.get());
                if (Files.exists(possiblePath)) {
                    selected = possiblePath;
                    break;
                }
            }
        }

        if (selected == null) {
            String defaultFirst = possibles[0];
            defaultFirst = NBEnvironment.INSTANCE.interpolate(defaultFirst)
                .orElseThrow(() -> new RuntimeException("Invalid default argsfile: '" + possibles[0] + "'"));
            selected = Path.of(defaultFirst);
        }

        this.argsPath = selected;
        if (logger != null) {
            logger.debug("argsfile path is now '" + this.argsPath + "'");
        }
    }

    /**
     * Convert argv arguments to consolidated form which is used in the args file.
     * This means that options and their (optional) arguments are on the
     * same line, concatenated with a space after the option.
     *
     * @return The arg-per-line form
     */
    LinkedHashSet<String> argsToLines(List<String> args) {
        LinkedHashSet<String> lines = new LinkedHashSet<>();
        Iterator<String> iter = args.iterator();
        List<String> element = new ArrayList<>();
        while (iter.hasNext()) {
            String word = iter.next();
            if (word.startsWith("-")) {
                if (element.size() > 0) {
                    lines.add(String.join(" ", element));
                    element.clear();
                }
            }
            element.add(word);
        }
        lines.add(String.join(" ", element));
        return lines;
    }

    /**
     * Convert arg lines as used in an args file to the argv which
     * is used on the command line.
     *
     * @return The argv list as you would see with {@code main(String[] argv)}
     */
    LinkedList<String> linesToArgs(Collection<String> lines) {
        LinkedList<String> args = new LinkedList<>();
        for (String line : lines) {
            if (line.startsWith("-")) {
                String[] words = line.split(" ", 2);
                args.addAll(Arrays.asList(words));
            } else {
                args.add(line);
            }
        }
        return args;
    }

    private LinkedList<String> unpin(LinkedList<String> arglist) {
        if (this.argsPath == null) {
            throw new RuntimeException("No argsfile has been selected before using the pin option.");
        }
        return arglist;
    }

    /**
     * Read the current command line option from the argument list,
     * so long as it is a dash or double-dash option, and is not a
     * reserved word, and any argument that goes with it, if any.
     *
     * @param arglist The command line containing the option
     * @return A list containing the current command line option
     */
    private LinkedList<String> readOptionAndArg(LinkedList<String> arglist, boolean consume) {
        LinkedList<String> option = new LinkedList<>();
        ListIterator<String> iter = arglist.listIterator();

        if (!iter.hasNext()) {
            throw new RuntimeException("Arguments must follow the --pin option");
        }
        String opt = iter.next();

        if (!opt.startsWith("-") || stopWords.contains(opt)) {
            throw new RuntimeException("Arguments following the --pin option must not" +
                    " be commands like '" + opt + "'");
        }
        option.add(opt);
        if (consume) {
            iter.remove();
        }

        if (iter.hasNext()) {
            opt = iter.next();
            if (!stopWords.contains(opt) && !opt.startsWith("-")) {
                option.add(opt);
                if (consume) {
                    iter.remove();
                }
            }
        }
        return option;
    }


    /**
     * Consume the next word from the beginning of the linked list. If there is
     * no word to consume, then throw an error with the description.
     *
     * @param commandline A list of words
     * @param description A description of what the next word value is meant to represent
     * @return The next word from the list
     */
    private String readWordOrThrow(LinkedList<String> commandline, String description) {
        String found = commandline.peekFirst();
        if (found == null) {
            throw new RuntimeException("Unable to read argument top option for " + description);
        }
        return commandline.removeFirst();
    }
}
