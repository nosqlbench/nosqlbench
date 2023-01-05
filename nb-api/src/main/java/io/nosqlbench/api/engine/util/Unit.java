/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.api.engine.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unit {

    private final static Logger logger = LogManager.getLogger(Unit.class);

    private static final Pattern numberFmtPattern = Pattern.compile(" *(?<number>(?<whole>[0-9]+)(?<fractional>\\.[0-9]+)?(?<to10power>E[0-9]+)?) *(?<unit>[^ ]+?)? *");
    private static final Pattern numberExponentPattern = Pattern.compile(" *(?<pre>.*?)?(?<number>([0-9]+)(\\.[0-9]+)?+)\\^(?<exponent>[0-9]+)(?<post>.*?)?");

    private static final long nanoPerSecond = 1000000000;
    private static final long bytesPerGB = 1000000000;
    private static final long BytesPerGiB = 1024 * 1024 * 1024;

    public static Optional<Long> msFor(String duration) {
        return durationFor(Duration.MS, duration);
    }

    public static Optional<Long> microsecondsFor(String duration) {
        return durationFor(Duration.US, duration);
    }

    public static Optional<Long> nanosecondsFor(String duration) {
        return durationFor(Duration.NS, duration);
    }

    public static Optional<Long> secondsFor(String duration) {
        return durationFor(Duration.SECOND, duration);
    }

    public static Optional<Long> minutesFor(String duration) {
        return durationFor(Duration.MINUTE, duration);
    }

    public static Optional<Long> durationFor(Duration resultUnit, String spec) {
        Matcher m = numberFmtPattern.matcher(spec);
        if (m.matches()) {
            String numberpart = m.group("number");
            Double base = Double.valueOf(numberpart);
            String unitpart = m.group("unit");
            if (unitpart != null) {
                Duration durationDuration = Duration.valueOfSuffix(unitpart);
                if (durationDuration == null) {
                    throw new RuntimeException("Unable to recognize duration unit:" + unitpart);
                }
                long specnanos = durationDuration.getNanos();
                long resultnanos = resultUnit.getNanos();
                double multiplier = (double) specnanos / (double) resultnanos;
                base = base * multiplier;
            }
            return Optional.of(base.longValue());
        } else {
            logger.error("Parsing error for specifier: '" + spec + "'");
            return Optional.empty();

        }
    }

    public static Optional<Double> doubleCountFor(String spec) {
        return convertDoubleCount(Count.UNIT, spec);
    }

    public static Optional<Double> convertDoubleCount(Count resultUnit, String spec) {
        Matcher e = numberExponentPattern.matcher(spec);
        if (e.matches()) {
            double base= Double.valueOf(e.group("number"));
            double exponent = Double.valueOf(e.group("exponent"));
            double value= Math.pow(base, exponent);
            spec = e.group("pre")+ value + e.group("post");
        }
        Matcher m = numberFmtPattern.matcher(spec);
        if (m.matches()) {
            String numberpart = m.group("number");
            double base = Double.valueOf(numberpart);
            String unitpart = m.group("unit");
            if (unitpart != null) {
                Count specifierUnit = Count.valueOfSuffix(unitpart);
                if (specifierUnit == null) {
                    throw new RuntimeException("Unable to recognized counts unit:" + unitpart);
                }
                double specifierScale = specifierUnit.getMultiplier();
                double resultScale = resultUnit.getMultiplier();
                double multiplier = (specifierScale / resultScale);
                base *= multiplier;
            }
            return Optional.of(base);
        } else {
            logger.error("Parsing error for specifier:'" + spec + "'");
            return Optional.empty();
        }

    }

    public static Optional<Long> longCountFor(String spec) {
        spec = longConversions(spec);
        spec = spec.replace("MAX", String.valueOf(Long.MAX_VALUE));
        try {
            long value = Long.parseLong(spec);
            return Optional.of(value);
        } catch (NumberFormatException ignored) {
        }
        return convertLongCount(Count.UNIT, spec);
    }

    public static Optional<Long> convertLongCount(Count resultUnit, String spec) {
        spec = longConversions(spec);
        Matcher m = numberFmtPattern.matcher(spec);
        if (m.matches()) {
            String numberpart = m.group("number");
            double base = Double.parseDouble(numberpart);
            String unitpart = m.group("unit");
            if (unitpart != null) {
                Count specifierUnit = Count.valueOfSuffix(unitpart);
                if (specifierUnit == null) {
                    throw new RuntimeException("Unable to recognize counts unit:" + unitpart);
                }
                double specifierScale = specifierUnit.getMultiplier();
                double resultScale = resultUnit.getMultiplier();
                double multiplier = (specifierScale / resultScale);
                base *= multiplier;
            }
            return Optional.of((long) base);
        } else {
            logger.error("Parsing error for specifier:'" + spec + "'");
            return Optional.empty();
        }

    }


    public static Optional<Double> bytesFor(String spec) {
        return convertBytes(Bytes.BYTE, spec);
    }

    private static String longConversions(String spec) {
        spec = spec.replace("MAX", String.valueOf(Long.MAX_VALUE));

        Matcher scinoteMatcher = numberFmtPattern.matcher(spec);
        if (scinoteMatcher.matches() &&
                ( scinoteMatcher.group("to10power")!=null
                || scinoteMatcher.group("fractional")!=null)) {
            Double doubleValue = Double.valueOf(scinoteMatcher.group("number"));
            spec = spec.replace(scinoteMatcher.group("number"),String.valueOf(doubleValue.longValue()));
        }

        Matcher exponentMatcher = numberExponentPattern.matcher(spec);
        if (exponentMatcher.matches()) {
            long number = Long.valueOf(exponentMatcher.group("number"));
            long exponent = Long.valueOf(exponentMatcher.group("exponent"));
            if (number == 1L) {
                logger.warn("If you are using exponent notation for '" + spec + "', you'll only ever get 1L.  " +
                        "Did you intend to use scientific notation, where the exponent is implied to the base 10? " +
                        "That looks like 1E5, which is shorthand for 1x10^5, for example.");
            }
            if (number == 2L) {
                if (exponent > 63) {
                    throw new InvalidParameterException("Exponent for powers of two must be 63 or less. It is " + exponent);
                }
                long value = 1L << exponent;
                spec= exponentMatcher.group("pre") + value + exponentMatcher.group("post");
            } else {
                spec= exponentMatcher.group("pre") + (long) Math.pow(number, exponent) + exponentMatcher.group("post");
            }
        }


        return spec;

    }


    public static Optional<Double> convertBytes(Bytes resultUnit, String spec) {
        Matcher m = numberFmtPattern.matcher(spec);
        if (m.matches()) {
            String numberpart = m.group("number");
            double base = Double.valueOf(numberpart);
            String unitpart = m.group("unit");
            if (unitpart != null) {
                Bytes specifierUnit = Bytes.valueOfSuffix(unitpart);
                if (specifierUnit == null) {
                    throw new RuntimeException("Unable to recognized duration unit:" + unitpart);
                }
                long specifierScale = specifierUnit.getBytes();
                long resultScale = resultUnit.getBytes();
                double multiplier = (double) specifierScale / (double) resultScale;
                base *= multiplier;
            }
            return Optional.of(base);
        } else {
            logger.error("Parsing error for specifier:'" + spec + "'");
            return Optional.empty();
        }

    }

    public enum Count {
        UNIT("U", "unit", 1.0),
        KILO("K", "kilo", 1000.0),
        MEGA("M", "mega", 1000000.0),
        BILLION("B", "billion", 1000000000.0),
        GIGA("G", "giga", 1000000000.0),
        TERA("T", "tera", 1000000000000.0),
        PETA("P", "peta", 1000000000000000.0),
        EXA("E", "exa", 1000000000000000000.0);

        private final String label;
        private final String name;
        private final double multiplier;

        Count(String label, String name, double multiplier) {
            this.label = label;
            this.name = name;
            this.multiplier = multiplier;
        }

        public static Count valueOfSuffix(String suffix) {
            for (Count count : Count.values()) {
                if (count.toString().equalsIgnoreCase(suffix)) {
                    return count;
                }
                if (count.label.equalsIgnoreCase(suffix)) {
                    return count;
                }
                if (count.name.equalsIgnoreCase(suffix)) {
                    return count;
                }
            }
            return null;
        }

        public double getMultiplier() {
            return multiplier;
        }
    }

    public enum Bytes {
        BYTE("B", "byte", 1),
        KB("KB", "kilobyte", 1000),
        MB("MB", "megabyte", 1000000),
        GB("GB", "gigabyte", bytesPerGB),
        TB("TB", "terabyte", bytesPerGB * 1000),
        PB("PB", "petabyte", bytesPerGB * 1000000),
        EB("EB", "exabyte", bytesPerGB * bytesPerGB),

        KIB("KiB", "kibibyte", 1024),
        MIB("MiB", "mebibyte", 1024 * 1024),
        GIB("GiB", "gibibyte", BytesPerGiB),
        TIB("TiB", "tebibyte", BytesPerGiB * 1024),
        PIB("PIB", "pebibyte", BytesPerGiB * 1024 * 1024),
        EIB("EiB", "exbibyte", BytesPerGiB * BytesPerGiB);

        private final String name;
        private final long bytes;
        private final String label;

        Bytes(String label, String name, long bytes) {
            this.label = label;
            this.name = name;
            this.bytes = bytes;
        }

        public static Bytes valueOfSuffix(String unitpart) {
            for (Bytes byteUnit : Bytes.values()) {
                if (byteUnit.label.equalsIgnoreCase(unitpart)) {
                    return byteUnit;
                }
                if (byteUnit.name.equalsIgnoreCase(unitpart)) {
                    return byteUnit;
                }
                if ((byteUnit.name.toLowerCase() + "s").equals(unitpart.toLowerCase())) {
                    return byteUnit;
                }
                if (byteUnit.toString().equalsIgnoreCase(unitpart)) {
                    return byteUnit;
                }
            }
            return null;
        }

        public long getBytes() {
            return bytes;
        }
    }

    public enum Duration {
        SECOND("s", "seconds", nanoPerSecond),
        MS("ms", "milliseconds", 1000000),
        US("µs", "microseconds", 1000),
        NS("ns", "nanoseconds", 1),
        MINUTE("M", "minutes", nanoPerSecond * 60),
        HOUR("H", "hours", nanoPerSecond * 60 * 60),
        DAY("D", "days", nanoPerSecond * 60 * 60 * 24),
        WEEK("W", "weeks", nanoPerSecond * 60 * 60 * 24 * 7),
        YEAR("Y", "years", nanoPerSecond * 60 * 60 * 24 * 365);


        private final String name;
        private final String label;
        private final long nanos;

        Duration(String label, String name, long nanos) {
            this.label = label;
            this.name = name;
            this.nanos = nanos;
        }

        public static Duration valueOfSuffix(String spec) {
            for (Duration duration : Duration.values()) {
                if (duration.label.equalsIgnoreCase(spec)) {
                    return duration;
                }
                if (duration.toString().equalsIgnoreCase(spec)) {
                    return duration;
                }
                if (duration.name.equalsIgnoreCase(spec)) {
                    return duration;
                }
            }
            return null;
        }

        public long getNanos() {
            return nanos;
        }
    }


}
