package io.nosqlbench.activitytype.cql.datamappers.functions.to_daterange;

import com.datastax.driver.dse.search.DateRange;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.text.ParseException;
import java.util.Date;
import java.util.function.Function;

/**
 * Parses the DateRange format according to <A HREF="https://lucene.apache.org/solr/guide/6_6/working-with-dates
 * .html#WorkingwithDates-DateRangeFormatting">Date Range Formatting</A>.
 * When possible it is more efficient to use the other DateRange methods since they do not require parsing.
 */
@ThreadSafeMapper
public class DateRangeParser implements Function<String, DateRange> {

    private final DateRange.DateRangeBound.Precision precision;

    @Example({"DateRangeParser()}","Convert inputs like '[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]' into " +
        "DateRanges" +
        " "})
    public DateRangeParser(String precision) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
    }

    @Override
    public DateRange apply(String value) {
        try {
            return DateRange.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("unable to parse date rage input '" + value + "': error:" + e.getMessage());
        }
    }
}
