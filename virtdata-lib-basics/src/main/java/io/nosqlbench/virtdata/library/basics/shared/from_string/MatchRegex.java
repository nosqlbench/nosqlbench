package io.nosqlbench.virtdata.library.basics.shared.from_string;

import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Match any input with a regular expression, and apply the associated
 * regex replacement to it, yielding the value.
 * If no matches occur, then the original value is passed through unchanged.
 * Patterns and replacements are passed as even,odd pairs indexed from the
 * 0th position. Back-references to matching groups are supported.
 *
 */
@ThreadSafeMapper
public class MatchRegex implements Function<String,String>  {

    private final MatchEntry[] entries;

    @Example({"MatchRegex('.*(25|6to4).*','$1')","Match 25 or 6 to 4 and set the output to only that"})
    @Example({"MatchRegex('([0-9]+)-([0-9]+)-([0-9]+)','$1 $2 $3'", "replaced dashes with spaces in a 10 digit US phone number."})
    @SuppressWarnings("unchecked")
    public MatchRegex(String... specs) {
        if ((specs.length%2)!=0) {
            throw new RuntimeException("You must provide 'pattern1',func1,... for an even number of arguments.");
        }
        entries = new MatchEntry[specs.length/2];
        for (int i = 0; i < specs.length; i+=2) {
            String pattern = specs[i].toString();
            String replacement = specs[i+1];
            entries[i/2]=new MatchEntry(pattern, replacement);
        }
    }

    @Override
    public String apply(String input) {
        for (MatchEntry entry : entries) {
            Matcher m = entry.tryMatch(input);
            if (m!=null) {
                String result = m.replaceAll(entry.replacement);
                return result;
            }
        }
        return input;
    }

    private final static Function<String,String> PASSTHRU = (s) -> s;

    private static class MatchEntry {
        public final Pattern pattern;
        public final String replacement;

        public MatchEntry(String pattern, String replacement) {
            this.pattern = Pattern.compile(pattern);
            this.replacement = replacement;
        }

        public Matcher tryMatch(String s) {
            Matcher matcher = this.pattern.matcher(s);
            if (matcher.matches()) {
                return matcher;
            } else {
                return null;
            }
        }
    }
}
