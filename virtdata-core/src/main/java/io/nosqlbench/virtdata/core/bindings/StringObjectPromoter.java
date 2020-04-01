package io.nosqlbench.virtdata.core.bindings;

import java.math.BigDecimal;
import java.math.BigInteger;

public class StringObjectPromoter {


    /**
     * Specialize the type of an object according to a target class.
     *
     * @param raw        The string representation of an object.
     * @param targetType The target object type
     * @return The promoted form, in the target class, or the original value if the format
     * failed or the class type was not supported.
     */
    public static Object promote(String raw, Class<?> targetType) {
        try {
            if (targetType == String.class) {
                return (raw.matches("^'.+'$")) ? raw.substring(1, raw.length() - 1) : raw;
            } else if (targetType == Double.class || targetType == double.class) {
                Double val = Double.valueOf(raw);
                return (targetType.isPrimitive()) ? val.doubleValue() : val;
            } else if (targetType == Float.class || targetType == float.class) {
                Float val = Float.valueOf(raw);
                return (targetType.isPrimitive()) ? val.floatValue() : val;
            } else if (targetType == Long.class || targetType == long.class) {
                Long val = Long.valueOf(raw);
                return (targetType.isPrimitive()) ? val.longValue() : val;
            } else if (targetType == Integer.class || targetType == int.class) {
                Integer val = Integer.valueOf(raw);
                return (targetType.isPrimitive()) ? val.intValue() : val;
            }
        } catch (Throwable ignored) {
        }

        return raw;

    }

    /**
     * Specialize the form of a string argument around the apparent object type.
     *
     * @param stringArg The raw value in string form
     * @return the specialized object type, or the original string format if nothing was found to be
     */
    public static Object promote(String stringArg) {
        if (stringArg.matches("^'.*'$")) {
            return stringArg.substring(1, stringArg.length() - 1);
        }

        if (stringArg.matches("^\\d+\\.\\d+(E\\d+)?[dD]$")) { // explicit double, no exponent
            try {
                Double doubleValue = Double.valueOf(stringArg);
                return doubleValue;
            } catch (NumberFormatException ignored) {
            }
        } else if (stringArg.matches("^\\d+\\.\\d+(E\\d+)?[fF]$")) { // explicit float, no exponent
            try {
                Float floatValue = Float.valueOf(stringArg.substring(0, stringArg.length() - 1));
                return floatValue;
            } catch (NumberFormatException ignored) {
            }
        } else if (stringArg.matches("^\\d+\\.\\d+(E\\d+)?$")) { // apparently floating point
            try { // default to float
                Float floatValue = Float.valueOf(stringArg);
                if (!Float.isInfinite(floatValue)) {
                    return floatValue;
                }
            } catch (NumberFormatException ignored) {
            }
            try { // fall back to double
                Double doubleValue = Double.valueOf(stringArg);
                if (!doubleValue.isInfinite()) {
                    return doubleValue;
                }
            } catch (NumberFormatException ignored) {
            }
            try { // Fall back to BigDecimal
                BigDecimal val = new BigDecimal(stringArg);
                return val;
            } catch (NumberFormatException ignored) {
            }
        } else if (stringArg.matches("^\\d+[lL]$")) { // explicit long
            try {
                Long longval = Long.valueOf(stringArg.substring(0, stringArg.length() - 1));
                return longval;
            } catch (NumberFormatException ignored) {
            }
        } else if (stringArg.matches("^\\d+$")) { // apparently integer
            try { // default to int
                Integer intValue = Integer.valueOf(stringArg);
                return intValue;
            } catch (NumberFormatException ignored) {
            }
            try { // fall back to long
                Long longval = Long.valueOf(stringArg);
                return longval;
            } catch (NumberFormatException ignored) {
            }
            try { // fall back to BigInteger
                BigInteger val = new BigInteger(stringArg);
                return val;
            } catch (NumberFormatException ignored) {
            }
        } else if (stringArg.equals("true")) {
            return true;
        }
        return stringArg;
    }

    /**
     * If a boxed type would suffice for a constructor call, even though
     * {@link Class#isAssignableFrom(Class)} says the assignment wouldn't work,
     * return true;
     * @param have The class that we have.
     * @param need The class that we need.
     * @return true, if the class that we have would work for a constructor call, due to unboxing.
     */
    public static boolean isAssignableForConstructor(Class<?> have, Class<?> need) {
        if (need.isAssignableFrom(have)) {
            return true;
        }
        if (
                (need == boolean.class && have == Boolean.class) ||
                (need == byte.class && have == Byte.class) ||
                (need == short.class && have == Short.class) ||
                (need == char.class && have == Character.class) ||
                (need == int.class && have == Integer.class) ||
                (need == long.class && have == Long.class) ||
                (need == float.class && have == Float.class) ||
                (need == double.class && have == Double.class)) {
            return true;
        }
        return false;
    }
}
