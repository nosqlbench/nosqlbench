/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.mql.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses time window specifications like "5m", "1h", "30s" into milliseconds.
 * Supports PromQL/MetricsQL time duration format.
 */
public class TimeWindowParser {

    // Pattern: number followed by time unit (s, m, h, d, w, y)
    private static final Pattern TIME_WINDOW_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([smhdwy])$");

    /**
     * Parse a time window string into milliseconds.
     *
     * Supported formats:
     * - "5s" = 5 seconds = 5000ms
     * - "10m" = 10 minutes = 600000ms
     * - "2h" = 2 hours = 7200000ms
     * - "1d" = 1 day = 86400000ms
     * - "1w" = 1 week = 604800000ms
     * - "1y" = 1 year (365 days) = 31536000000ms
     *
     * @param window Time window string (e.g., "5m", "1h")
     * @return Time window in milliseconds
     * @throws InvalidQueryException If the format is invalid
     */
    public static long parseToMillis(String window) throws InvalidQueryException {
        if (window == null || window.trim().isEmpty()) {
            throw new InvalidQueryException("Time window cannot be null or empty");
        }

        String normalized = window.trim().toLowerCase();
        Matcher matcher = TIME_WINDOW_PATTERN.matcher(normalized);

        if (!matcher.matches()) {
            throw new InvalidQueryException(
                "Invalid time window format: '" + window + "'\n" +
                "Expected format: <number><unit> where unit is one of: s, m, h, d, w, y\n" +
                "Examples: 5s, 10m, 2h, 1d"
            );
        }

        double value;
        try {
            value = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new InvalidQueryException("Invalid numeric value in time window: " + window, e);
        }

        if (value <= 0) {
            throw new InvalidQueryException("Time window must be positive, got: " + value);
        }

        String unit = matcher.group(2);
        long millisPerUnit = getMillisPerUnit(unit);

        // Check for overflow
        double resultMs = value * millisPerUnit;
        if (resultMs > Long.MAX_VALUE) {
            throw new InvalidQueryException("Time window too large: " + window);
        }

        return (long) resultMs;
    }

    /**
     * Parse a time window string, throwing RuntimeException on error.
     * Useful for constants where you know the format is valid.
     *
     * @param window Time window string
     * @return Time window in milliseconds
     * @throws RuntimeException If parsing fails
     */
    public static long parseToMillisUnchecked(String window) {
        try {
            return parseToMillis(window);
        } catch (InvalidQueryException e) {
            throw new RuntimeException("Failed to parse time window: " + window, e);
        }
    }

    /**
     * Format milliseconds back to a human-readable time window string.
     * Chooses the most appropriate unit automatically.
     *
     * @param millis Time in milliseconds
     * @return Human-readable string (e.g., "5m", "2h")
     */
    public static String formatMillis(long millis) {
        if (millis == 0) {
            return "0s";
        }

        // Try to find the largest unit that divides evenly
        if (millis % MILLIS_PER_YEAR == 0) {
            return (millis / MILLIS_PER_YEAR) + "y";
        }
        if (millis % MILLIS_PER_WEEK == 0) {
            return (millis / MILLIS_PER_WEEK) + "w";
        }
        if (millis % MILLIS_PER_DAY == 0) {
            return (millis / MILLIS_PER_DAY) + "d";
        }
        if (millis % MILLIS_PER_HOUR == 0) {
            return (millis / MILLIS_PER_HOUR) + "h";
        }
        if (millis % MILLIS_PER_MINUTE == 0) {
            return (millis / MILLIS_PER_MINUTE) + "m";
        }
        if (millis % MILLIS_PER_SECOND == 0) {
            return (millis / MILLIS_PER_SECOND) + "s";
        }

        // Fallback to milliseconds
        return millis + "ms";
    }

    // Time unit constants
    private static final long MILLIS_PER_SECOND = 1000L;
    private static final long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
    private static final long MILLIS_PER_WEEK = 7L * MILLIS_PER_DAY;
    private static final long MILLIS_PER_YEAR = 365L * MILLIS_PER_DAY; // No leap years in PromQL

    private static long getMillisPerUnit(String unit) throws InvalidQueryException {
        return switch (unit) {
            case "s" -> MILLIS_PER_SECOND;
            case "m" -> MILLIS_PER_MINUTE;
            case "h" -> MILLIS_PER_HOUR;
            case "d" -> MILLIS_PER_DAY;
            case "w" -> MILLIS_PER_WEEK;
            case "y" -> MILLIS_PER_YEAR;
            default -> throw new InvalidQueryException("Unknown time unit: " + unit);
        };
    }

    private TimeWindowParser() {
        // Utility class - prevent instantiation
    }
}
