package io.nosqlbench.engine.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;

/**
 * Encapsulate Command parsing and structure for the NoSQLBench command line.
 * Commands always have a name, sometimes have a list of positional arguments,
 * and sometimes have a map of named parameters.
 * An example of a command tha thas both would look like {@code script test.js p1=v1}
 */
public class Cmd {

    private final static Logger logger = LoggerFactory.getLogger(Cmd.class);

    public enum CmdType {
        script(Arg.of("script_path", s -> s)),
        fragment(Arg.ofFreeform("script_fragment")),
        start(),
        run(),
        await(Arg.of("alias_name")),
        stop(Arg.of("alias_name")),
        waitMillis(Arg.of("millis_to_wait", Long::parseLong));

        private final Arg<?>[] positional;

        CmdType(Arg<?>... positional) {
            this.positional = positional;
        }

        public String[] getPositionalArgNames() {
            String[] names = new String[positional.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = positional[i].name;
            }
            return names;
        }

        public static CmdType valueOfAnyCase(String cmdname) {
            for (CmdType value : values()) {
                if (cmdname.equals(value.toString()) || cmdname.toLowerCase().equals(value.toString().toLowerCase())) {
                    return value;
                }
            }
            return valueOf(cmdname); // let the normal exception take over in this case
        }

        public Arg<?>[] getPositionalArgs() {
            return positional;
        }
    }

    private static final class Arg<T> {
        public final String name;
        public final Function<String, T> converter;
        public final boolean freeform;

        public Arg(String name, Function<String, T> converter, boolean freeform) {
            this.name = name;
            this.converter = converter;
            this.freeform = freeform;
        }

        public static <T> Arg<T> of(String name, Function<String, T> converter) {
            return new Arg<>(name, converter, false);
        }

        public static Arg<String> of(String name) {
            return new Arg<>(name, s -> s, false);
        }
        public static Arg<String> ofFreeform(String name) {
            return new Arg<>(name, s->s, true);
        }
    }


    private Map<String, String> cmdArgs;

    public String getArg(String paramName) {
        return this.cmdArgs.get(paramName);
    }

    private final CmdType cmdType;

    public Cmd(CmdType cmdType, Map<String, String> cmdArgs) {
        this.cmdArgs = cmdArgs;
        this.cmdType = cmdType;
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public Map<String, String> getParams() {
        return cmdArgs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdType.toString());
        sb.append("(");
        if (getParams().size() > cmdType.positional.length) {
            sb.append(toJSONBlock(getParams(), false));
        } else {
            for (String value : getParams().values()) {
                sb.append("'").append(value).append("'").append(",");
            }
            sb.setLength(sb.length() - 1);
        }
        sb.append(");");
        return sb.toString();
    }

    public static Cmd parseArg(LinkedList<String> arglist, PathCanonicalizer fixer) {

        String cmdName = arglist.removeFirst();
        CmdType cmdType = CmdType.valueOfAnyCase(cmdName);

        Map<String, String> params = new LinkedHashMap<>();

        for (Arg<?> arg : cmdType.getPositionalArgs()) {

            String nextarg = arglist.peekFirst();

            if (nextarg == null) {
                throw new InvalidParameterException(
                    "command '" + cmdName + " requires a value for " + arg.name
                        + ", but there were no remaining arguments after it.");
            } else if (arg.freeform) {
                logger.debug("freeform parameter:" + nextarg);
            } else if (nextarg.contains("=")) {
                throw new InvalidParameterException(
                    "command '" + cmdName + "' requires a value for " + arg.name + "" +
                        ", but a named parameter was found instead: " + nextarg);
            } else if (NBCLIOptions.RESERVED_WORDS.contains(nextarg)) {
                throw new InvalidParameterException(
                    "command '" + cmdName + "' requires a value for " + arg.name
                        + ", but a reserved word was found instead: " + nextarg);
            }

            logger.debug("cmd name:" + cmdName + ", positional " + arg.name + ": " + nextarg);
            params.put(arg.name, arg.converter.apply(arglist.removeFirst()).toString());
        }

        while (arglist.size() > 0 &&
            !NBCLIOptions.RESERVED_WORDS.contains(arglist.peekFirst())
            && arglist.peekFirst().contains("=")) {
            String arg = arglist.removeFirst();
            String[] assigned = arg.split("=", 2);
            String pname = assigned[0];
            String pval = assigned[1];


            if (pname.equals("yaml") || pname.equals("workload")) {
                pval = fixer.canonicalizePath(pval);
            }
            if (params.containsKey(pname)) {
                throw new InvalidParameterException("parameter '" + pname + "' is already set for " + cmdType);
            }
            params.put(pname, pval);
        }

        return new Cmd(cmdType, params);
    }

    public static String toJSONBlock(Map<String, String> map, boolean oneline) {

        int klen = map.keySet().stream().mapToInt(String::length).max().orElse(1);
        StringBuilder sb = new StringBuilder();
        List<String> l = new ArrayList<>();
        map.forEach((k, v) -> l.add(
            (oneline ? "" : "    ") + "'" + k + "'"
                + ": " + (oneline ? "" : " ".repeat(klen - k.length())) +
                "'" + v + "'"
        ));
        return "{" + (oneline ? "" : "\n") + String.join(",\n", l) + (oneline ? "}" : "\n}");
    }

    public static String toJSONParams(String varname, Map<String, String> map, boolean oneline) {
        return "// params.size==" + map.size() + "\n" + varname + "=" + toJSONBlock(map, oneline);
    }

}
