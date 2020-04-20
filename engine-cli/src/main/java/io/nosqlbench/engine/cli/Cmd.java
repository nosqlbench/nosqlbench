package io.nosqlbench.engine.cli;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulate Command parsing and structure for the NoSQLBench command line.
 * Commands always have a name, sometimes have a list of positional arguments,
 * and sometimes have a map of named parameters.
 * An example of a command tha thas both would look like {@code script test.js p1=v1}
 */
public class Cmd {

    private final static Logger logger = LoggerFactory.getLogger(Cmd.class);

    public enum CmdType {
        script("script_path"),
        fragment("script_fragment"),
        start(),
        run(),
        await("alias_name"),
        stop("alias_name"),
        waitmillis("millis_to_wait");

        private final String[] positional;

        CmdType(String... positional) {
            this.positional = positional;
        }

        public String[] getPositionalArgs() {
            return positional;
        }
    }


    private Map<String, String> cmdArgs;

    public String getArg(String paramName) {
        return this.cmdArgs.get(paramName);
    }

    private final CmdType cmdType;

    public Cmd(CmdType cmdType, Map<String,String> cmdArgs) {
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
        return "type:" + cmdType + ((cmdArgs != null) ? ";cmdArgs=" + cmdArgs.toString() : "");
    }

    public static Cmd parseArg(LinkedList<String> arglist, NBCLIOptions options) {

        String cmdName = arglist.removeFirst();
        CmdType cmdType = CmdType.valueOf(cmdName);

        Map<String,String> params = new LinkedHashMap<>();

        for (String paramName : cmdType.getPositionalArgs()) {
            String arg = arglist.peekFirst();
            if (arg==null) {
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

            logger.debug("cmd name:" + cmdName +", positional " + paramName + ": " + arg);
            params.put(paramName,arglist.removeFirst());
        }

        while (arglist.size() > 0 &&
            !NBCLIOptions.RESERVED_WORDS.contains(arglist.peekFirst())
            && arglist.peekFirst().contains("=")) {
            String arg = arglist.removeFirst();
            String[] assigned = arg.split("=", 2);
            String pname = assigned[0];
            String pval = assigned[1];

            if (pname.equals("yaml")||pname.equals("workload")) {
                String yaml = pval;
                Optional<Content<?>> found = NBIO.local().prefix("activities")
                    .prefix(options.wantsIncludes())
                    .name(yaml)
                    .first();
                if (found.isPresent()) {
                    if (!found.get().asPath().toString().equals(yaml)) {
                        logger.info("rewrote path for " + yaml + " as " + found.get().asPath().toString());
                        pval=found.get().asPath().toString();
                    } else {
                        logger.debug("kept path for " + yaml + " as " + found.get().asPath().toString());
                    }
                } else {
                    logger.debug("unable to find " + yaml + " for path qualification");
                }
            }
            if (params.containsKey(pname)) {
                throw new InvalidParameterException("parameter '" + pname + "' is already set for " + cmdType);
            }
            params.put(pname,pval);
        }

        return new Cmd(cmdType, params);
    }

}
