---
title: datetime functions
weight: 20
---

## DateTimeParser

This function will parse a String containing a formatted date time, yielding a DateTime object. If no arguments are provided, then the format is set to

```
yyyy-MM-dd HH:mm:ss.SSSZ
```

. For details on formatting options, see @see [DateTimeFormat](https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html)

- java.lang.String -> DateTimeParser() -> org.joda.time.DateTime
  - *notes:* Initialize the parser with the default pattern of <pre>yyyy-MM-dd HH:mm:ss.SSSZ</pre>.
  - *ex:* `DateTimeParser()` - *parse any date in the yyyy-MM-dd HH:mm:ss.SSSZ format*
- java.lang.String -> DateTimeParser(java.lang.String: dateTimePattern) -> org.joda.time.DateTime
  - *notes:* Initialize the parser with the given pattern. With this form, if any input fails to parse,
or is null or empty, then an exception is thrown.
@param dateTimePattern The pattern which represents the incoming format.
  - *ex:* `DateTimeParser('yyyy-MM-dd')` - *parse any date in the yyyy-MM-dd format*
- java.lang.String -> DateTimeParser(java.lang.String: dateTimePattern, java.lang.String: defaultTime) -> org.joda.time.DateTime
  - *notes:* Initialize the parser with the given pattern and default value. In this form, if any
input fails to parse, then exceptions are suppressed and the default is provided instead.
At initialization, the default is parsed as a sanity check.
@param dateTimePattern The pattern which represents the incoming format.
@param defaultTime An example of a formatted datetime string which is used as a default.
  - *ex:* `DateTimeParser('yyyy-MM-dd','1999-12-31')` - *parse any date in the yyyy-MM-dd format, or return the DateTime represented by 1999-12-31*


## StartingEpochMillis

This function sets the minimum long value to the equivalent unix epoch time in milliseconds. It simply adds the input value to this base value as determined by the provided time specifier. It wraps any overflow within this range as well.

- long -> StartingEpochMillis(java.lang.String: baseTimeSpec) -> long
  - *ex:* `StartingEpochMillis('2017-01-01 23:59:59')` - *add the millisecond epoch time of 2017-01-01 23:59:59 to all input values*


## StringDateWrapper

This function wraps an epoch time in milliseconds into a String as specified in the format. The valid formatters are documented at @see [DateTimeFormat API Docs](https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html)

- long -> StringDateWrapper(java.lang.String: format) -> java.lang.String


## ToDate

Convert the input value to a {@code Date}

- long -> ToDate(int: spacing, int: repeat_count) -> java.util.Date
- long -> ToDate(int: spacing) -> java.util.Date
- long -> ToDate() -> java.util.Date


## ToDateTime

Convert the input value to a {@code org.joda.time.DateTime}

- long -> ToDateTime(int: spacing, int: repeat_count) -> org.joda.time.DateTime
- long -> ToDateTime(java.lang.String: spacing) -> org.joda.time.DateTime
- long -> ToDateTime() -> org.joda.time.DateTime


## ToEpochTimeUUID

Converts a long UTC timestamp in epoch millis form into a Version 1 TimeUUID according to [RFC 4122](https://www.ietf.org/rfc/rfc4122.txt). This means that only one unique value for a timeuuid can be generated for each epoch milli value, even though version 1 TimeUUIDs can normally represent up to 10000 distinct values per millisecond. If you need to access this level of resolution for testing purposes, use {@link ToFinestTimeUUID} instead. This method is to support simple mapping to natural timestamps as we often find in the real world.

For the variants that have a String argument in the constructor, this is
a parsable datetime that is used as the base time for all produced values.
Setting this allows you to set the start of the time range for all timeuuid
values produced. All times are parsed for UTC. All time use ISO date ordering,
meaning that the most significant fields always go before the others.

The valid formats, in joda specifier form are:

1. yyyy-MM-dd HH:mm:ss.SSSZ, for example: 2015-02-28 23:30:15.223
2. yyyy-MM-dd HH:mm:ss, for example 2015-02-28 23:30:15
3. yyyyMMdd'T'HHmmss.SSSZ, for example: 20150228T233015.223
4. yyyyMMdd'T'HHmmssZ, for example: 20150228T233015
5. yyyy-MM-dd, for example: 2015-02-28
6. yyyyMMdd, for example: 20150228
7. yyyyMM, for example: 201502
8. yyyy, for example: 2015

- long -> ToEpochTimeUUID() -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a per-host node and empty clock data.
The node and clock components are seeded from network interface data. In this case,
the clock data is not seeded uniquely.
  - *ex:* `ToEpochTimeUUID()` - *basetime 0, computed node data, empty clock data*
- long -> ToEpochTimeUUID(long: node) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and empty clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param node a fixture value for testing that replaces node and clock bits
  - *ex:* `ToEpochTimeUUID(5234)` - *basetime 0, specified node data (5234), empty clock data*
- long -> ToEpochTimeUUID(long: node, long: clock) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and specific clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param node  a fixture value for testing that replaces node bits
@param clock a fixture value for testing that replaces clock bits
  - *ex:* `ToEpochTimeUUID(31,337)` - *basetime 0, specified node data (31) and clock data (337)*
- long -> ToEpochTimeUUID(java.lang.String: baseSpec) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a per-host node and empty clock data.
The node and clock components are seeded from network interface data. In this case,
the clock data is not seeded uniquely.

@param baseSpec - a string specification for the base time value
  - *ex:* `ToEpochTimeUUID('2017-01-01T23:59:59')` - *specified basetime, computed node data, empty clock data*
- long -> ToEpochTimeUUID(java.lang.String: baseSpec, long: node) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and empty clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param baseSpec - a string specification for the base time value
@param node     a fixture value for testing that replaces node and clock bits
  - *ex:* `ToEpochTimeUUID('2012',12345)` - *basetime at start if 2012, with node data 12345, empty clock data*
- long -> ToEpochTimeUUID(java.lang.String: baseSpec, long: node, long: clock) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and specific clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param baseSpec - a string specification for the base time value
@param node     a fixture value for testing that replaces node bits
@param clock    a fixture value for testing that replaces clock bits
  - *ex:* `ToEpochTimeUUID('20171231T1015.243',123,456)` - *ms basetime, specified node and clock data*


## ToFinestTimeUUID

Converts a count of 100ns intervals from 1582 Julian to a Type1 TimeUUID according to [RFC 4122](https://www.ietf.org/rfc/rfc4122.txt). This allows you to access the finest unit of resolution for the purposes of simulating a large set of unique timeuuid values. This offers 10000 times more unique values per ms than {@link ToEpochTimeUUID}. For the variants that have a String argument in the constructor, this is a parsable datetime that is used as the base time for all produced values. Setting this allows you to set the start of the time range for all timeuuid values produced. All times are parsed for UTC. All time use ISO date ordering, meaning that the most significant fields always go before the others. The valid formats, in joda specifier form are:

1. yyyy-MM-dd HH:mm:ss.SSSZ, for example: 2015-02-28 23:30:15.223
2. yyyy-MM-dd HH:mm:ss, for example 2015-02-28 23:30:15
3. yyyyMMdd'T'HHmmss.SSSZ, for example: 20150228T233015.223
4. yyyyMMdd'T'HHmmssZ, for example: 20150228T233015
5. yyyy-MM-dd, for example: 2015-02-28
6. yyyyMMdd, for example: 20150228
7. yyyyMM, for example: 201502
8. yyyy, for example: 2015

- long -> ToFinestTimeUUID() -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a per-host node and empty clock data.
The node and clock components are seeded from network interface data. In this case,
the clock data is not seeded uniquely.
  - *ex:* `ToFinestTimeUUID()` - *basetime 0, computed node data, empty clock data*
- long -> ToFinestTimeUUID(long: node) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and empty clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param node a fixture value for testing that replaces node and clock bits
  - *ex:* `ToFinestTimeUUID(5234)` - *basetime 0, specified node data (5234), empty clock data*
- long -> ToFinestTimeUUID(long: node, long: clock) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and specific clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param node  a fixture value for testing that replaces node bits
@param clock a fixture value for testing that replaces clock bits
  - *ex:* `ToFinestTimeUUID(31,337)` - *basetime 0, specified node data (31) and clock data (337)*
- long -> ToFinestTimeUUID(java.lang.String: baseTimeSpec) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a per-host node and empty clock data.
The node and clock components are seeded from network interface data. In this case,
the clock data is not seeded uniquely.

@param baseTimeSpec - a string specification for the base time value
  - *ex:* `ToFinestTimeUUID('2017-01-01T23:59:59')` - *specified basetime, computed node data, empty clock data*
- long -> ToFinestTimeUUID(java.lang.String: baseTimeSpec, long: node) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and empty clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param baseTimeSpec - a string specification for the base time value
@param node         a fixture value for testing that replaces node and clock bits
  - *ex:* `ToFinestTimeUUID('2012',12345)` - *basetime at start if 2012, with node data 12345, empty clock data*
- long -> ToFinestTimeUUID(java.lang.String: baseTimeSpec, long: node, long: clock) -> java.util.UUID
  - *notes:* Create version 1 timeuuids with a specific static node and specific clock data.
This is useful for testing so that you can know that values are verifiable, even though
in non-testing practice, you would rely on some form of entropy per-system to provide
more practical dispersion of values over reboots, etc.

@param node         a fixture value for testing that replaces node bits
@param clock        a fixture value for testing that replaces clock bits
@param baseTimeSpec - a string specification for the base time value
  - *ex:* `ToFinestTimeUUID('20171231T1015.243',123,456)` - *ms basetime, specified node and clock data*


## ToJodaDateTime

Convert the input value to a {@code org.joda.time.DateTime}

- long -> ToJodaDateTime(int: spacing, int: repeat_count) -> org.joda.time.DateTime
- long -> ToJodaDateTime(java.lang.String: spacing) -> org.joda.time.DateTime
- long -> ToJodaDateTime() -> org.joda.time.DateTime


## ToMillisAtStartOfDay

Return the epoch milliseconds at the start of the day for the given epoch milliseconds.

- long -> ToMillisAtStartOfDay() -> long
  - *ex:* `ToMillisAtStartOfDay()` - *return millisecond epoch time of the start of the day of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfDay(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfDay('America/Chicago')` - *return millisecond epoch time of the start of the day of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfHour

Return the epoch milliseconds at the start of the hour for the given epoch milliseconds.

- long -> ToMillisAtStartOfHour() -> long
  - *ex:* `ToMillisAtStartOfHour()` - *return millisecond epoch time of the start of the hour of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfHour(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfHour('America/Chicago')` - *return millisecond epoch time of the start of the hour of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfMinute

Return the epoch milliseconds at the start of the minute for the given epoch milliseconds.

- long -> ToMillisAtStartOfMinute() -> long
  - *ex:* `ToMillisAtStartOfMinute()` - *return millisecond epoch time of the start of the minute of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfMinute(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfMinute('America/Chicago')` - *return millisecond epoch time of the start of the minute of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfMonth

Return the epoch milliseconds at the start of the month for the given epoch milliseconds.

- long -> ToMillisAtStartOfMonth() -> long
  - *ex:* `ToMillisAtStartOfMonth()` - *return millisecond epoch time of the start of the month of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfMonth(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfMonth('America/Chicago')` - *return millisecond epoch time of the start of the month of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfNamedWeekDay

Return the epoch milliseconds at the start of the most recent day that falls on the given weekday for the given epoch milliseconds, including the current day if valid.

- long -> ToMillisAtStartOfNamedWeekDay() -> long
  - *ex:* `ToMillisAtStartOfNamedWeekDay()` - *return millisecond epoch time of the start of the most recent Monday (possibly the day-of) of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfNamedWeekDay(java.lang.String: weekday) -> long
  - *ex:* `ToMillisAtStartOfNamedWeekDay('Wednesday')` - *return millisecond epoch time of the start of the most recent Wednesday (possibly the day-of) of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfNamedWeekDay(java.lang.String: weekday, java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfNamedWeekDay('Saturday','America/Chicago'')` - *return millisecond epoch time of the start of the most recent Saturday (possibly the day-of) of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfNextDay

Return the epoch milliseconds at the start of the day after the day for the given epoch milliseconds.

- long -> ToMillisAtStartOfNextDay() -> long
  - *ex:* `ToMillisAtStartOfNextDay()` - *return millisecond epoch time of the start of next day (not including day-of) of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfNextDay(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfNextDay('America/Chicago')` - *return millisecond epoch time of the start of the next day (not including day-of) of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfNextNamedWeekDay

Return the epoch milliseconds at the start of the next day that falls on the given weekday for the given epoch milliseconds, not including the current day.

- long -> ToMillisAtStartOfNextNamedWeekDay() -> long
  - *ex:* `ToMillisAtStartOfNextNamedWeekDay()` - *return millisecond epoch time of the start of the next Monday (not the day-of) of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfNextNamedWeekDay(java.lang.String: weekday) -> long
  - *ex:* `ToMillisAtStartOfNextNamedWeekDay('Wednesday')` - *return millisecond epoch time of the start of the next Wednesday (not the day-of) of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfNextNamedWeekDay(java.lang.String: weekday, java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfNextNamedWeekDay('Saturday','America/Chicago'')` - *return millisecond epoch time of the start of the next Saturday (not the day-of) of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfSecond

Return the epoch milliseconds at the start of the second for the given epoch milliseconds.

- long -> ToMillisAtStartOfSecond() -> long
  - *ex:* `ToMillisAtStartOfSecond()` - *return millisecond epoch time of the start of the second of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfSecond(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfSecond('America/Chicago')` - *return millisecond epoch time of the start of the second of the provided millisecond epoch time, using timezone America/Chicago*


## ToMillisAtStartOfYear

Return the epoch milliseconds at the start of the year for the given epoch milliseconds.

- long -> ToMillisAtStartOfYear() -> long
  - *ex:* `ToMillisAtStartOfYear()` - *return millisecond epoch time of the start of the year of the provided millisecond epoch time, assuming UTC*
- long -> ToMillisAtStartOfYear(java.lang.String: timezoneId) -> long
  - *ex:* `ToMillisAtStartOfYear('America/Chicago')` - *return millisecond epoch time of the start of the year of the provided millisecond epoch time, using timezone America/Chicago*


