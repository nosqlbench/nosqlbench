package io.nosqlbench.activitytype.cql.datamappers.functions.long_localdate;

import com.datastax.driver.core.LocalDate;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Days since Jan 1st 1970
 */
@ThreadSafeMapper
public class LongToLocalDateDays implements LongFunction<LocalDate> {
    @Override
    public LocalDate apply(long value) {
        return LocalDate.fromDaysSinceEpoch((int) value % Integer.MAX_VALUE);
    }

    @Example({"LongToLocalDateDays()","take the cycle number and turn it into a LocalDate based on days since 1970"})
    public LongToLocalDateDays (){
    }
}
