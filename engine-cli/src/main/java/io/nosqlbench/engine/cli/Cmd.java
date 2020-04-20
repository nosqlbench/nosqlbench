package io.nosqlbench.engine.cli;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
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
        fragment(Arg.of("script_fragment")),
        start(),
        run(),
        await(Arg.of("alias_name")),
        stop(Arg.of("alias_name")),
        waitmillis(Arg.of("millis_to_wait", Long::parseLong));

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

        public Arg<?>[] getPositionalArgs() {
            return positional;
        }
    }

    private static final class Arg<T> {
        public final String name;
        public final Function<String, T> converter;

        public Arg(String name, Function<String, T> converter) {
            this.name = name;
            this.converter = converter;
        }

        public static <T> Arg<T> of(String name, Function<String, T> converter) {
            return new Arg<>(name, converter);
        }

        public static Arg<String> of(String name) {
            return new Arg<>(name, s -> s);
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
        CmdType cmdType = CmdType.valueOf(cmdName);

        Map<String, String> params = new LinkedHashMap<>();

        for (Arg<?> paramName : cmdType.getPositionalArgs()) {
            String arg = arglist.peekFirst();
            if (arg == null) {
                throw new InvalidParameterException("command '" + cmdName + " requires a value for " + paramName
                    + ", but there were no remaining arguments after it.");
            }
            if (arg.contains("=")) {
                throw new InvalidParameterException("command '" + cmdName + "' requires a value for " + paramName + "" +
                    ", but a named parameter was found instead: " + arg);
            }
            if (NBCLIOptions.RESERVED_WORDS.contains(arg)) {
                throw new InvalidParameterException("command '" + cmdName + "' requires a value for " + paramName
                    + ", but a reserved word was found instead: " + arg);
            }

            logger.debug("cmd name:" + cmdName + ", positional " + paramName + ": " + arg);
            params.put(paramName.name, paramName.converter.apply(arglist.removeFirst()).toString());
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
                +": " + (oneline ? "" : " ".repeat(klen - k.length())) +
                "'" + v + "'"
        ));
        return "{" + (oneline ? "" : "\n") + String.join(",\n", l) + (oneline ? "}" : "\n}");
    }

    public static String toJSONParams(String varname, Map<String, String> map, boolean oneline) {
        return "// params.size==" + map.size() + "\n" + varname + "=" + toJSONBlock(map, oneline);
    }

}
