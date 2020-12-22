package io.nosqlbench.engine.clients.grafana;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GTimeUnit {

    private enum Durations {
        s("second", 1000L),
        m("minute", 60000L),
        h("hour", 3600000L),
        d("day", 86400000L),
        w("week", 86400000L * 7L),
        M("month", 86400000L * 30L),
        y("year", 86400000L * 365L);


        private final String unitName;
        private final long millisPerUnit;

        Durations(String unitName, long millisPerUnit) {
            this.unitName = unitName;
            this.millisPerUnit = millisPerUnit;
        }

        public long durationInMillis(long count) {
            return count * millisPerUnit;
        }

        public long durationInSeconds(long count) {
            return (count * millisPerUnit) / 1000L;
        }

    }

    private static final Pattern absolute = Pattern.compile("\\d+");
    private static final Pattern ISO_INSTANT = Pattern.compile(
            "(?<year>\\d+)-(?<month>\\d+)-(?<day>\\d+) " +
                    "(?<hour>\\d+):(?<minute>\\d+):(?<second>\\d+)(\\.(?<millis>\\d+))?Z?"
    );
    private static final Pattern relUnit = Pattern.compile(
            "now((?<value>[-+]\\d+)(?<unit>[smhdwMy]))?(\\/(?<interval>[smhdwMy]))?"
    );

    public static long epochSecondsFor(String spec) {
        if (spec.startsWith("now")) {
            Matcher relative = relUnit.matcher(spec);
            if (relative.matches()) {
                if (relative.group("interval") != null) {
                    throw new RuntimeException("date field boundaries like '"
                            + spec +
                            "' are not supported yet.");
                }
                long now = System.currentTimeMillis() / 1000L;
                long delta = 0L;
                String v = relative.group("value");
                String u = relative.group("unit");
                if (v != null && u != null) {
                    long value = Long.parseLong(v);
                    Durations duration = Durations.valueOf(u);
                    delta = duration.durationInSeconds(value);
                }

                long timevalue = now + delta;
                return timevalue;
            } else {
                throw new RuntimeException("unable to match input '" + spec + "'");
            }

        } else if (spec.matches("\\\\d+")) {
            return Long.parseLong(spec);
        } else {
            Matcher matcher = ISO_INSTANT.matcher(spec);
            if (matcher.matches()) {
                ZonedDateTime parsed = ZonedDateTime.parse(spec);
                return parsed.toEpochSecond();
            } else {
                throw new RuntimeException("Unrecognized format for grafana time unit '" + spec + "'");
            }
        }
    }
}
