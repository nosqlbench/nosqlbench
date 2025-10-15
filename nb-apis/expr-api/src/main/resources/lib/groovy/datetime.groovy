/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * @Library
 * Date and time utilities for NoSQLBench expressions.
 * Provides functions for date/time formatting, parsing, manipulation,
 * and timestamp generation.
 */

import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec
import io.nosqlbench.nb.api.expr.annotations.ExprExample
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@ExprFunctionSpec(
    synopsis = "nowMillis()",
    description = "Get the current timestamp in milliseconds since epoch"
)
def nowMillis() {
    System.currentTimeMillis()
}

@ExprFunctionSpec(
    synopsis = "nowSeconds()",
    description = "Get the current timestamp in seconds since epoch"
)
def nowSeconds() {
    (System.currentTimeMillis() / 1000) as long
}

@ExprFunctionSpec(
    synopsis = "nowNanos()",
    description = "Get the current timestamp in nanoseconds"
)
def nowNanos() {
    System.nanoTime()
}

@ExprFunctionSpec(
    synopsis = "formatNow(pattern)",
    description = "Format the current date/time with a pattern"
)
@ExprExample(expectNotNull = true)
def formatNow(pattern) {
    LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
}

@ExprFunctionSpec(
    synopsis = "currentDate()",
    description = "Get current date in ISO format (YYYY-MM-DD)"
)
@ExprExample(expectNotNull = true)
def currentDate() {
    LocalDate.now().toString()
}

@ExprFunctionSpec(
    synopsis = "currentTime()",
    description = "Get current time in ISO format (HH:mm:ss)"
)
@ExprExample(expectNotNull = true)
def currentTime() {
    LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString()
}

@ExprFunctionSpec(
    synopsis = "currentDateTime()",
    description = "Get current date/time in ISO format"
)
@ExprExample(expectNotNull = true)
def currentDateTime() {
    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()
}

@ExprFunctionSpec(
    synopsis = "reformatDate(dateStr, fromPattern, toPattern)",
    description = "Parse a date string and format it differently"
)
@ExprExample(args = ["'2024-01-01'", "'yyyy-MM-dd'", "'MM/dd/yyyy'"], expect = "'01/01/2024'")
def reformatDate(dateStr, fromPattern, toPattern) {
    def from = DateTimeFormatter.ofPattern(fromPattern)
    def to = DateTimeFormatter.ofPattern(toPattern)
    def date = LocalDate.parse(dateStr, from)
    date.format(to)
}

@ExprFunctionSpec(
    synopsis = "addDays(dateStr, days)",
    description = "Add days to a date string"
)
@ExprExample(args = ["'2024-01-01'", "7"], expect = "'2024-01-08'")
def addDays(dateStr, days) {
    LocalDate.parse(dateStr).plusDays(days).toString()
}

@ExprFunctionSpec(
    synopsis = "addWeeks(dateStr, weeks)",
    description = "Add weeks to a date string"
)
@ExprExample(args = ["'2024-01-01'", "2"], expect = "'2024-01-15'")
def addWeeks(dateStr, weeks) {
    LocalDate.parse(dateStr).plusWeeks(weeks).toString()
}

@ExprFunctionSpec(
    synopsis = "addMonths(dateStr, months)",
    description = "Add months to a date string"
)
@ExprExample(args = ["'2024-01-01'", "3"], expect = "'2024-04-01'")
def addMonths(dateStr, months) {
    LocalDate.parse(dateStr).plusMonths(months).toString()
}

@ExprFunctionSpec(
    synopsis = "addYears(dateStr, years)",
    description = "Add years to a date string"
)
@ExprExample(args = ["'2024-01-01'", "1"], expect = "'2025-01-01'")
def addYears(dateStr, years) {
    LocalDate.parse(dateStr).plusYears(years).toString()
}

@ExprFunctionSpec(
    synopsis = "daysBetween(startDate, endDate)",
    description = "Calculate days between two dates"
)
@ExprExample(args = ["'2024-01-01'", "'2024-01-15'"], expect = "14")
def daysBetween(startDate, endDate) {
    def start = LocalDate.parse(startDate)
    def end = LocalDate.parse(endDate)
    ChronoUnit.DAYS.between(start, end)
}

@ExprFunctionSpec(
    synopsis = "dayOfWeek(dateStr)",
    description = "Get the day of week from a date (1=Monday, 7=Sunday)"
)
@ExprExample(args = ["'2024-01-01'"], expect = "1")
def dayOfWeek(dateStr) {
    LocalDate.parse(dateStr).dayOfWeek.value
}

@ExprFunctionSpec(
    synopsis = "dayOfMonth(dateStr)",
    description = "Get the day of month from a date"
)
@ExprExample(args = ["'2024-01-15'"], expect = "15")
def dayOfMonth(dateStr) {
    LocalDate.parse(dateStr).dayOfMonth
}

@ExprFunctionSpec(
    synopsis = "monthOfYear(dateStr)",
    description = "Get the month from a date (1-12)"
)
@ExprExample(args = ["'2024-06-15'"], expect = "6")
def monthOfYear(dateStr) {
    LocalDate.parse(dateStr).monthValue
}

@ExprFunctionSpec(
    synopsis = "yearOf(dateStr)",
    description = "Get the year from a date"
)
@ExprExample(args = ["'2024-01-01'"], expect = "2024")
def yearOf(dateStr) {
    LocalDate.parse(dateStr).year
}

@ExprFunctionSpec(
    synopsis = "isPast(dateStr)",
    description = "Check if a date is in the past"
)
def isPast(dateStr) {
    LocalDate.parse(dateStr).isBefore(LocalDate.now())
}

@ExprFunctionSpec(
    synopsis = "isFuture(dateStr)",
    description = "Check if a date is in the future"
)
def isFuture(dateStr) {
    LocalDate.parse(dateStr).isAfter(LocalDate.now())
}

@ExprFunctionSpec(
    synopsis = "isLeapYear(year)",
    description = "Check if a year is a leap year"
)
@ExprExample(args = ["2024"], expect = "true")
@ExprExample(args = ["2023"], expect = "false")
def isLeapYear(year) {
    Year.of(year as int).isLeap()
}

@ExprFunctionSpec(
    synopsis = "startOfWeek(dateStr)",
    description = "Get the start of the week for a date (Monday)"
)
@ExprExample(args = ["'2024-01-03'"], expect = "'2024-01-01'")
def startOfWeek(dateStr) {
    def date = LocalDate.parse(dateStr)
    date.minusDays(date.dayOfWeek.value - 1).toString()
}

@ExprFunctionSpec(
    synopsis = "endOfWeek(dateStr)",
    description = "Get the end of the week for a date (Sunday)"
)
@ExprExample(args = ["'2024-01-01'"], expect = "'2024-01-07'")
def endOfWeek(dateStr) {
    def date = LocalDate.parse(dateStr)
    date.plusDays(7 - date.dayOfWeek.value).toString()
}

@ExprFunctionSpec(
    synopsis = "startOfMonth(dateStr)",
    description = "Get the start of the month for a date"
)
@ExprExample(args = ["'2024-01-15'"], expect = "'2024-01-01'")
def startOfMonth(dateStr) {
    LocalDate.parse(dateStr).withDayOfMonth(1).toString()
}

@ExprFunctionSpec(
    synopsis = "endOfMonth(dateStr)",
    description = "Get the end of the month for a date"
)
@ExprExample(args = ["'2024-01-15'"], expect = "'2024-01-31'")
def endOfMonth(dateStr) {
    def date = LocalDate.parse(dateStr)
    date.withDayOfMonth(date.lengthOfMonth()).toString()
}

@ExprFunctionSpec(
    synopsis = "millisToDate(millis)",
    description = "Convert a timestamp (millis) to ISO date string"
)
@ExprExample(args = ["1609459200000"], expect = "'2021-01-01'")
def millisToDate(millis) {
    Instant.ofEpochMilli(millis as long)
           .atZone(ZoneId.systemDefault())
           .toLocalDate()
           .toString()
}

@ExprFunctionSpec(
    synopsis = "millisToDateTime(millis)",
    description = "Convert a timestamp (millis) to ISO datetime string"
)
@ExprExample(expectNotNull = true)
def millisToDateTime(millis) {
    Instant.ofEpochMilli(millis as long)
           .atZone(ZoneId.systemDefault())
           .toLocalDateTime()
           .truncatedTo(ChronoUnit.SECONDS)
           .toString()
}

@ExprFunctionSpec(
    synopsis = "dateToMillis(dateStr)",
    description = "Convert ISO date string to timestamp (millis)"
)
@ExprExample(args = ["'2021-01-01'"], expect = "1609459200000")
def dateToMillis(dateStr) {
    LocalDate.parse(dateStr)
             .atStartOfDay(ZoneId.systemDefault())
             .toInstant()
             .toEpochMilli()
}

@ExprFunctionSpec(
    synopsis = "currentDateInZone(timezone)",
    description = "Get current date in a specific timezone"
)
@ExprExample(expectNotNull = true)
def currentDateInZone(timezone) {
    LocalDate.now(ZoneId.of(timezone)).toString()
}

@ExprFunctionSpec(
    synopsis = "currentTimeInZone(timezone)",
    description = "Get current time in a specific timezone"
)
@ExprExample(expectNotNull = true)
def currentTimeInZone(timezone) {
    LocalTime.now(ZoneId.of(timezone)).truncatedTo(ChronoUnit.SECONDS).toString()
}
