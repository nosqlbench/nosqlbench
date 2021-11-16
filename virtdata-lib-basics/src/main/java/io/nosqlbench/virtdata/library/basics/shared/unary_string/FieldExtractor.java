package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Extracts out a set of fields from a delimited string, returning
 * a string with the same delimiter containing only the specified fields.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class FieldExtractor implements Function<String,String> {

    private final static Logger logger  = LogManager.getLogger(FieldExtractor.class);

    private final String fields;
    private final String splitDelim;
    private final String printDelim;
    private final int maxIdx;
    private final int[] indexes;
    private final ThreadLocal<StringBuilder> tlsb = ThreadLocal.withInitial(StringBuilder::new);

    @Example({"FieldExtractor('|,2,16')","extract fields 2 and 16 from the input data with '|' as the delimiter"})
    public FieldExtractor(String fields) {
        this.fields = fields;

        String[] indexSpecs = fields.split(",");
        this.printDelim = indexSpecs[0];
        this.splitDelim = "\\" + indexSpecs[0];
        indexes = new int[indexSpecs.length-1];
        for (int i = 1; i <= indexes.length; i++) {
            indexes[i-1] = Integer.valueOf(indexSpecs[i].trim())-1;
        }
        maxIdx = Arrays.stream(indexes).max().orElse(-1);
    }

    private int[] initIndexes(String fields) {
        return indexes;
    }

    @Override
    public String apply(String s) {
        String[] words = s.split(splitDelim);
        if (words.length<maxIdx) {
            logger.warn("Short read on line, indexes: " + Arrays.toString(indexes) + ", line:" + s + ", returning 'ERROR-UNDERRUN'");
            return "ERROR-UNDERRUN in FieldExtractor";
        }
        StringBuilder sb = tlsb.get();
        sb.setLength(0);
        for(int index: indexes) {
            sb.append(words[index]).append(printDelim);
        }
        sb.setLength(sb.length()-printDelim.length());
        return sb.toString();
    }
}
