package io.nosqlbench.virtdata.library.basics.shared.from_string;

import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.core.composers.FunctionAssembly;
import io.nosqlbench.virtdata.core.composers.FunctionComposer;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Match any input with a regular expression, and apply the associated
 * function to it, yielding the value. If no matches occur, then the original
 * value is passed through unchanged. Patterns and functions are passed as
 * even,odd pairs indexed from the 0th position. Instead of a function, a
 * String value may be provided as the associated output value.
 */
@ThreadSafeMapper
public class MatchFunc implements Function<String,String>  {

    private final MatchEntry[] entries;

    @Example({"MatchFunc('.*','onevalue')","Match all String inputs, simply returning 'onevalue' as the output value."})
    @Example({"MatchFunc('[0-9]+',Suffix('-is-a-number'))","Append '-is-a-number' to every input which is a sequence of digits"})
    @SuppressWarnings("unchecked")
    public MatchFunc(Object... funcs) {
        if ((funcs.length%2)!=0) {
            throw new RuntimeException("You must provide 'pattern1',func1,... for an even number of arguments.");
        }
        FunctionComposer assembly = new FunctionAssembly();
        entries = new MatchEntry[funcs.length/2];
        for (int i = 0; i < funcs.length; i+=2) {
            Pattern pattern = Pattern.compile(funcs[i].toString());
            Object funcObject = funcs[i+1];
            Function<String,String> function;
            if (funcObject instanceof String) {
                function = (s) -> funcObject.toString();
            } else if (funcObject instanceof Function) {
                function = (Function)funcObject;
            } else {
                function = VirtDataFunctions.adapt(funcObject, Function.class, String.class, true);
            }

            entries[i/2]=new MatchEntry(pattern, function);
        }
    }

    @Override
    public String apply(String s) {
        for (MatchEntry entry : entries) {
            Matcher m = entry.tryMatch(s);
            if (m!=null) {
                String result = entry.f.apply(s);
                return result;
            }
        }
        return s;
    }

    private final static Function<String,String> PASSTHRU = (s) -> s;

    private static class MatchEntry {
        public final Pattern pattern;
        public final Function<String,String> f;
        public MatchEntry(Pattern pattern, Function f) {
            this.pattern = pattern;
            this.f = f;
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
