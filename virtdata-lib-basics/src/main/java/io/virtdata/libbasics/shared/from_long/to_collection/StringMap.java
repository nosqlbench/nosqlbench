package io.virtdata.libbasics.shared.from_long.to_collection;

import io.virtdata.annotations.*;

import java.util.HashMap;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
/**
 * Create a {@code Map<String,String>} from a long input
 * based on three functions,
 * the first to determine the map size, and the second to populate
 * the map with key objects, and the third to populate the map with
 * value objects. The long input fed to the second and third functions
 * is incremented between entries. Regardless of the object type provided
 * by the second and third functions, {@link java.lang.Object#toString()}
 * is used to determine the key and value to add to the map.
 *
 * To create Maps of any key and value types, simply use {@link Map} with
 * an specific key and value mapping functions.
 */

@Categories({Category.collections})
@ThreadSafeMapper
public class StringMap implements LongFunction<java.util.Map<String, String>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction[] keyFuncs;
    private final LongFunction[] valueFuncs;
    private final Mode mode;

    @Example({"StringMap(HashRange(3,7),NumberNameToString(),HashRange(1300,1700))",
            "create a map of size 3-7 entries, with a key of type " +
                    "string and a value of type int (Integer by autoboxing)"})
    public StringMap(LongToIntFunction sizeFunc,
                     LongFunction<Object> keyFunc,
                     LongFunction<Object> valueFunc) {
        this.mode = Mode.VarSized;

        this.sizeFunc = sizeFunc;
        this.keyFuncs = new LongFunction[1];
        keyFuncs[0]=keyFunc;
        this.valueFuncs = new LongFunction[1];
        valueFuncs[0]=valueFunc;
    }

    @Example({"StringMap(NumberNameToString(),HashRange(1300,1700),NumberNameToString(),HashRange(3,7))",
            "create a map of size 2, with a specific function for each key and each value"})
    @SafeVarargs
    public StringMap(LongFunction<Object>... objfuncs) {
        this.mode = Mode.Tuples;
        if ((objfuncs.length%2)!=0) {
            throw new RuntimeException("An even number of functions must be provided.");
        }
        int size = objfuncs.length / 2;
        sizeFunc=(l) -> size;
        keyFuncs = new LongFunction[size];
        valueFuncs = new LongFunction[size];
        for (int i = 0; i < size; i++) {
            keyFuncs[i]=objfuncs[i<<1];
            valueFuncs[i] = objfuncs[(i<<1)+1];
        }
    }


    @Override
    public java.util.Map<String,String> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        HashMap<String,String> map = new HashMap<>(size);
        switch (mode) {
            case VarSized:
                for (int i = 0; i < size; i++) {
                    Object keyObject = keyFuncs[0].apply(value + i);
                    Object valueObject = valueFuncs[0].apply(value + i);
                    map.put(String.valueOf(keyObject), String.valueOf(valueObject));
                }
                break;
            case Tuples:
                for (int i = 0; i < keyFuncs.length; i++) {
                    Object keyObject = keyFuncs[i].apply(value +i);
                    Object valueObject = valueFuncs[i].apply(value+i);
                    map.put(String.valueOf(keyObject),String.valueOf(valueObject));
                }
                break;
        }
        return map;
    }

    private enum Mode {
        VarSized,
        Tuples
    }


}
