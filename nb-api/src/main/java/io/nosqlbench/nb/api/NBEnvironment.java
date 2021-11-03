package io.nosqlbench.nb.api;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.metadata.SessionNamer;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Safer, Easier lookup of property and environment variables, so
 * that commonly used env vars as seen on *n*x systems map to stable
 * system properties where known, but attempt to fall through to
 * the env variables if not.
 *
 * As long as all accesses and mutations for System properties and/or
 * environment variables are marshaled through a singleton instance
 * of this class, then users will not easily make a modify-after-reference
 * bug without a warning or error.
 *
 * Referencing a variable here means that a value was asked for under a
 * name, and a value was returned, even if this was the default value.
 *
 * Properties which contains a dot are presumed to be System properties,
 * and so in that case System properties take precedence for those.
 *
 * Environment variables which are known to map to a stable system property
 * are redirected to the system property.
 *
 * Otherwise, the environment variable is checked.
 *
 * Finally, System properties are checked for names not containing a dot.
 *
 * The first location to return a non-null value is used. Null values are considered
 * invalid in this API, except when provided as a default value.
 */
public class NBEnvironment {
    private Logger logger;

    public static final String NBSTATEDIR = "NBSTATEDIR";
    public static final String NBLIBS = "NBLIBDIR";

    // package private for testing
    NBEnvironment() {
    }

    public final static NBEnvironment INSTANCE = new NBEnvironment();

    private final LinkedHashMap<String, String> references = new LinkedHashMap<>();

    /**
     * These properties are well-defined in the Java specs. This map redirects common
     * environment variable names to the given system property. This allows
     * these symblic environment variables to be provided in a consistent way across
     * hardware architectures.
     */
    private final static Map<String, String> envToProp = Map.of(
        "PWD", "user.dir",
        "HOME", "user.home",
        "USERNAME", "user.name", // Win*
        "USER", "user.name", // *n*x
        "LOGNAME", "user.name" // *n*x
    );

    public NBEnvironment resetRefs() {
        this.references.clear();
        return this;
    }

    public void put(String propname, String value) {
        if (envToProp.containsKey(propname)) {
            throw new RuntimeException("The property you are changing should be considered immutable in this " +
                "process: '" + propname + "'");
        }
        if (references.containsKey(propname)) {
            if (references.get(propname).equals(value)) {
                if (logger != null) {
                    logger.warn("changing already referenced property '" + propname + "' to same value");
                }
            } else {
                throw new BasicError("Changing already referenced property '" + propname + "' from \n" +
                    "'" + references.get(propname) + "' to '" + value + "' is not supported.\n" +
                    " (maybe you can change the order of your options to set higher-level parameters first.)");
            }

        }
        System.setProperty(propname, value);
    }

    /**
     * When a value is returned to be used in an assignment context, this method should
     * be called for reference marking.
     * @param name The name of the parameter
     * @param value The value of the parameter
     * @return The value itself
     */
    private String reference(String name, String value) {
        this.references.put(name, value);
        return value;
    }

    /**
     * Return the value in the first place it is found to be non-null,
     * or the default value otherwise.
     *
     * @param name         The system property or environment variable name
     * @param defaultValue The value to return if the name is not found
     * @return the system property or environment variable's value, or the default value
     */
    public String getOr(String name, String defaultValue, Map<String,String> supplemental) {
        String value = peek(name, supplemental);
        if (value == null) {
            value = defaultValue;
        }
        return reference(name, value);
    }


    public String getOr(String name, String defaultValue) {
        return getOr(name, defaultValue, Map.of());
    }

    /**
     * This is a non-referencing get of a value, and the canonical way to
     * access a value. This method codifies the semantics of whether something is
     * defined or not from the user perspective.
     * @param name The parameter name
     * @return A value, or null if none was found
     */
    private String peek(String name, Map<String,String> supplemental) {
        String value = null;
        if (supplemental.containsKey(name)) {
            value = supplemental.get(name);
            if (value!=null) {
                return value;
            }
        }
        if (name.contains(".")) {
            value = System.getProperty(name.toLowerCase());
            if (value != null) {
                return value;
            }
        }
        if (envToProp.containsKey(name.toUpperCase())) {
            String propName = envToProp.get(name.toUpperCase());
            if (logger != null) {
                logger.debug("redirecting env var '" + name + "' to upper-case property '" + propName + "'");
            }
            value = System.getProperty(propName);
            if (value != null) {
                return value;
            }
        }
        value = System.getProperty(name);
        if (value != null) {
            return value;
        }

        value = System.getenv(name);
        return value;
    }

    /**
     * Attempt to read the variable in System properties or the shell environment. If it
     * is not found, throw an exception.
     *
     * @param name The System property or environment variable
     * @return the variable
     * @throws BasicError if no value was found which was non-null
     */
    public String get(String name) {
        String value = getOr(name, null);
        if (value == null) {
            throw new BasicError("No variable was found for '" + name + "' in system properties nor in the shell " +
                "environment.");
        }
        return value;
    }

    public boolean containsKey(String name) {
        return containsKey(name, Map.of());
    }

    public boolean containsKey(String name, Map<String,String> supplemental) {
        String value = peek(name, supplemental);
        return (value != null);
    }

    /**
     * For the given word, if it contains a pattern with '$' followed by alpha, followed
     * by alphanumeric and underscores, replace this pattern with the system property or
     * environment variable of the same name. Do this for all occurrences of this pattern.
     *
     * For patterns which start with '${' and end with '}', replace the contents with the same
     * rules as above, but allow any character in between.
     *
     * Nesting is not supported.
     *
     * @param word The word to interpolate the environment values into
     * @return The interpolated value, after substitutions, or null if any lookup failed
     */
    public Optional<String> interpolate(String word, Map<String,String> supplemental) {
        Pattern envpattern = Pattern.compile("(\\$(?<env1>[a-zA-Z_][A-Za-z0-9_.]+)|\\$\\{(?<env2>[^}]+)\\})");
        Matcher matcher = envpattern.matcher(word);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String envvar = matcher.group("env1");
            if (envvar == null) {
                envvar = matcher.group("env2");
            }
            String value = peek(envvar,supplemental);
            if (value == null) {
                if (logger != null) {
                    logger.debug("no value found for '" + envvar + "', returning Optional.empty() for '" + word + "'");
                }
                return Optional.empty();
            } else {
                value = reference(envvar, value);
            }
            matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);
        return Optional.of(sb.toString());
    }

    public Optional<String> interpolate(String word) {
        return interpolate(word,Map.of());
    }

    public List<String> interpolateEach(CharSequence delim, String toBeRecombined) {
        String[] split = toBeRecombined.split(delim.toString());
        List<String> mapped = new ArrayList<>();
        for (String pattern : split) {
            Optional<String> interpolated = interpolate(pattern);
            interpolated.ifPresent(mapped::add);
        }
        return mapped;
    }

    /**
     * Interpolate system properties, environment variables, time fields, and arbitrary replacement strings
     * into a single result. Templates such as {@code /tmp/%d-${testrun}-$System.index-SCENARIO} are supported.
     *
     * <hr/>
     *
     * The tokens found in the raw template are interpolated in the following order.
     * <ul>
     *     <li>Any token which exactly matches one of the keys in the provided map is substituted
     *     directly as is. No token sigil like '$' is used here, so if you want to support that
     *     as is, you need to provide the keys in your substitution map as such.</li>
     *     <li>Any tokens in the form {@code %f} which is supported by the time fields in
     *     {@link Formatter}</li> are honored and used with the timestamp provided.*
     *     <li>System Properties: Any token in the form {@code $word.word} will be taken as the name
     *     of a system property to be substited.</li>
     *     <li>Environment Variables: Any token in the form {@code $name}</li> will be takens as
     *     an environment variable to be substituted.</li>
     * </ul>
     *
     * @param rawtext The template, including any of the supported token forms
     * @param millis  The timestamp to use for any temporal tokens
     * @param map     Any additional parameters to interpolate into the template first
     * @return Optionally, the interpolated string, as long as all references were qualified. Error
     * handling is contextual to the caller -- If not getting a valid result would cause a downstream error,
     * an error should likely be thrown.
     */
    public final Optional<String> interpolateWithTimestamp(String rawtext, long millis, Map<String, String> map) {
        String result = rawtext;
        result = SessionNamer.format(result, millis);
        return interpolate(result,map);
    }

    public final Optional<String> interpolateWithTimestamp(String rawText, long millis) {
        return interpolateWithTimestamp(rawText, millis, Map.of());
    }

}
