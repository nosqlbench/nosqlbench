package io.nosqlbench.datamappers.functions.long_localdate;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.time.LocalDate;
import java.util.function.LongFunction;

/**
 * Days since Jan 1st 1970
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class LongToLocalDateDays implements LongFunction<LocalDate> {

    @Example({"LongToLocalDateDays()","take the cycle number and turn it into a LocalDate based on days since 1970"})
    public LongToLocalDateDays (){
    }

    @Override
    public LocalDate apply(long value) {
        return LocalDate.ofEpochDay((int) value & Integer.MAX_VALUE);
   }

}
