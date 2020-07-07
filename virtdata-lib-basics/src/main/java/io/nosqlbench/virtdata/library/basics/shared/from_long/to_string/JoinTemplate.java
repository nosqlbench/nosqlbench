package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Combine the result of the specified functions together with the
 * specified delimiter and optional prefix and suffix.
 */
@ThreadSafeMapper
public class JoinTemplate extends Template implements LongFunction<String>  {

    @Example({"JoinTemplate('--',NumberNameToString(),NumberNameToString())","create values like `one--one`, `two-two`, ..."})
    public JoinTemplate(String delimiter, LongFunction<?>... funcs) {
        super(templateFor("",delimiter,"",funcs), funcs);
    }

    @Example({"JoinTemplate('{',',','}',NumberNameToString(),LastNames())", "create values like '{one,Farrel}', '{two,Haskell}', ..."})
    public JoinTemplate(String prefix, String delimiter, String suffix, LongFunction<?>... funcs) {
        super(templateFor(prefix,delimiter,suffix,funcs), funcs);
    }

    @Example({"JoinTemplate(Add(3),'<',';','>',NumberNameToString(),NumberNameToString(),NumberNameToString())",
    "create values like '<zero;three,six>', '<one;four,seven>', ..."})
    public JoinTemplate(LongUnaryOperator iterop, String prefix, String delimiter, String suffix, LongFunction<?>... funcs) {
        super(iterop, templateFor(prefix,delimiter,suffix,funcs), funcs);

    }
    private static String templateFor(String prefix, String delimiter, String suffix, LongFunction<?>... funcs) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix);
        for (int i = 0; i < funcs.length; i++) {
            sb.append("{}");
            sb.append(delimiter);
        }
        sb.setLength(sb.length()-delimiter.length());
        sb.append(suffix);
        return sb.toString();
    }

}
