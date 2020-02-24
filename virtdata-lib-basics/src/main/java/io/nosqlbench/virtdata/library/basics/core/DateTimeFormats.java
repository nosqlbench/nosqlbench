package io.nosqlbench.virtdata.library.basics.core;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * The valid formats, in joda specifier form are documented in {@link DateTimeFormats}
 * <ol>
 * <li>yyyy-MM-dd HH:mm:ss.SSSZ, for example: 2015-02-28 23:30:15.223</li>
 * <li>yyyy-MM-dd HH:mm:ss, for example 2015-02-28 23:30:15</li>
 * <li>yyyyMMdd'T'HHmmss.SSSZ, for example: 20150228T233015.223</li>
 * <li>yyyyMMdd'T'HHmmssZ, for example: 20150228T233015</li>
 * <li>yyyy-MM-dd, for example: 2015-02-28</li>
 * <li>yyyyMMdd, for example: 20150228</li>
 * <li>yyyyMM, for example: 201502</li>
 * <li>yyyy, for example: 2015</li>
 * </ol>
 */
public class DateTimeFormats {

    public final static DateTimeFormatter[] formatters = new DateTimeFormatter[]{


            //2017-06-07 21:39:24.710-0500
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
                    .withChronology(GregorianChronology.getInstance()),

            //2017-06-07 21:39:24
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                    .withChronology(GregorianChronology.getInstance()),

            //20170607T213924.711-0500
            ISODateTimeFormat.basicDateTime()
                    .withChronology(GregorianChronology.getInstance()), // yyyyMMdd'T'HHmmss.SSSZ

            //20170608T023924Z
            ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()), // yyyyMMdd'T'HHmmssZ

            //2017-06-08
            ISODateTimeFormat.date().withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()), // yyyy-MM-dd

            //20170608
            DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()),

            //201706
            DateTimeFormat.forPattern("yyyyMM").withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()),

            //2017
            DateTimeFormat.forPattern("yyyy").withZoneUTC()
                    .withChronology(GregorianChronology.getInstance())
    };

    public static DateTime gregorianCalendarStart =
            new DateTime(1582, 10, 15, 0, 0, DateTimeZone.UTC);


    public static DateTime parseEpochTimeToDateTime(String timeString) {

        List<Exception> exceptions = new ArrayList<>();
        for (DateTimeFormatter dtf : formatters) {
            try {
                DateTime dateTime = dtf.withZoneUTC().parseDateTime(timeString);
                return dateTime;
            } catch (Exception e) {
                exceptions.add(new RuntimeException("as '" + dtf.print(DateTime.now()) + "': " + e.getMessage()));
            }
        }
        String message = "";
        for (Exception e : exceptions) {
            message += e.getMessage() + "\n";
        }
        throw new RuntimeException("Unable to parse [" + timeString + "] with any of the parsers. exceptions:" + message +", examples of valid formats are included above");
    }

    public static long parseEpochTimeToGregorianMillis(String timeString) {

        DateTime dateTime = parseEpochTimeToDateTime(timeString);
        return new Duration(gregorianCalendarStart, dateTime).getMillis();
    }

    public static long parseEpochTimeToTimeUUIDTicks(String timeString) {
        long l = parseEpochTimeToGregorianMillis(timeString);
        return l * 10000;
    }


}
