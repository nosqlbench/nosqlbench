package io.nosqlbench.activitytype.cql.datamappers.functions.collectionclobs;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a {@code Map<String,String>} from a long input
 * based on three functions,
 * the first to determine the map size, and the second to populate
 * the map with key objects, and the third to populate the map with
 * value objects. The long input fed to the second and third functions
 * is incremented between entries. Regardless of the object type provided
 * by the second and third functions, {@link Object#toString()}
 * is used to determine the key and value to add to the map.
 *
 * To create Maps of any key and value types, simply use
 * {@link java.util.Map} with
 * an specific key and value mapping functions.
 */

@Categories({Category.collections})
@ThreadSafeMapper
public class StringMapClob implements LongFunction<String> {

    private transient final static ThreadLocal<StringBuilder> tl_sb = ThreadLocal.withInitial(StringBuilder::new);

    private final LongToIntFunction sizeFunc;
    private final LongFunction[] keyFuncs;
    private final LongFunction[] valueFuncs;
    private final Mode mode;
    private final static String BEFORE_RESULT = "{";
    private final static String AFTER_RESULT = "}";
    private final static String KEY_QUOTE ="'";
    private final static String VAL_QUOTE = "'";
    private final static String ASSIGNMENT = ": ";
    private final static String BETWEEN_ENTRIES = ", ";

    @Example({"StringMap(HashRange(3,7),NumberNameToString(),HashRange(1300,1700))",
            "create a map of size 3-7 entries, with a key of type " +
                    "string and a value of type int (Integer by autoboxing)"})
    public StringMapClob(LongToIntFunction sizeFunc,
                         LongFunction<Object> keyFunc,
                         LongFunction<Object> valueFunc) {
        this.mode = Mode.VarSized;

        this.sizeFunc = sizeFunc;
        this.keyFuncs = new LongFunction[1];
        keyFuncs[0] = keyFunc;
        this.valueFuncs = new LongFunction[1];
        valueFuncs[0] = valueFunc;
    }

    @Example({"StringMapClob(NumberNameToString(),HashRange(1300,1700),NumberNameToString(),HashRange(3,7))",
            "create a map of size 2, with a specific function for each key and each value"})
    @SafeVarargs
    public StringMapClob(LongFunction<Object>... objfuncs) {
        this.mode = Mode.Tuples;
        if ((objfuncs.length % 2) != 0) {
            throw new RuntimeException("An even number of functions must be provided.");
        }
        int size = objfuncs.length / 2;
        sizeFunc = (l) -> size;
        keyFuncs = new LongFunction[size];
        valueFuncs = new LongFunction[size];
        for (int i = 0; i < size; i++) {
            keyFuncs[i] = objfuncs[i << 1];
            valueFuncs[i] = objfuncs[(i << 1) + 1];
        }
    }


    @Override
    public String apply(long value) {

        //        "{key='value',key='value'}"

        StringBuilder sb = tl_sb.get();
        sb.setLength(0);
        sb.append(BEFORE_RESULT);

        int size = sizeFunc.applyAsInt(value);

        switch (mode) {
            case VarSized:
                for (int i = 0; i < size; i++) {
                    Object keyObject = keyFuncs[0].apply(value + i);
                    Object valueObject = valueFuncs[0].apply(value + i);

                    sb.append(KEY_QUOTE).append(keyObject).append(KEY_QUOTE);
                    sb.append(ASSIGNMENT);
                    sb.append(VAL_QUOTE).append(valueObject).append(VAL_QUOTE);
                    sb.append(BETWEEN_ENTRIES);
                }
                break;
            case Tuples:
                for (int i = 0; i < keyFuncs.length; i++) {
                    Object keyObject = keyFuncs[i].apply(value + i);
                    Object valueObject = valueFuncs[i].apply(value + i);

                    sb.append(KEY_QUOTE).append(keyObject).append(KEY_QUOTE);
                    sb.append(ASSIGNMENT);
                    sb.append(VAL_QUOTE).append(valueObject).append(VAL_QUOTE);
                    sb.append(BETWEEN_ENTRIES);
                }
                break;
        }
        sb.setLength(sb.length()-BETWEEN_ENTRIES.length());

        sb.append(AFTER_RESULT);
        return sb.toString();
    }

    private enum Mode {
        VarSized,
        Tuples
    }


}
