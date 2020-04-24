package io.nosqlbench.nb.api.config;

import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * This class is just a central reference point for the names of parameters
 * or other configuration-level primitives which have been given better names.
 * For the sake of backwards compatibility, the old names are retained, but
 * deprecated and warned against.
 */
public class Synonyms {

    /**
     * Each entry in this list is a list of synonyms in configuration.
     */
    public final static Map<String, Set<String>> PARAM_SYNONYMS = new HashMap<>() {{
        put("hosts",Set.of("host"));
        put("workload",Set.of("yaml"));
        put("driver",Set.of("type"));
        put("cyclerate",Set.of("targetrate"));
    }};

    /**
     * use this method to convert deprecated
     * @param input A configuration string from a user or file
     * @param synonyms A list of known synonym lists with the preferred values first, like {@link #PARAM_SYNONYMS}
     * @param warnings An BiConsumer which can handle (deprecated, preferred) for subsitutions.
     * @return The configuration string in canonicalized form, with the preferred names used where possible
     */
    public static String canonicalize(String input, Map<String, Set<String>> synonyms, BiConsumer<String,String> warnings) {
        String replaced = input;
        for (Map.Entry<String, Set<String>> syns : synonyms.entrySet()) {
            String preferred = syns.getKey();
            for (String deprecated : syns.getValue()) {
                Pattern p = Pattern.compile("\\b" + deprecated + "\\b");
                String prior = replaced;
                replaced = replaced.replaceAll(p.pattern(),preferred);
                if (!prior.equals(replaced) && warnings!=null) {
                    warnings.accept(deprecated,preferred);
                }
            }
        }
        return replaced;
    }

    public static String canonicalize(String arg, Logger logger) {
        return canonicalize(arg, PARAM_SYNONYMS, (d, p) -> logger.warn(
            "Identified deprecated use of '" + d + ", please use '" + p + "' as the preferred form to remove this warning."
        ));
    }
}
