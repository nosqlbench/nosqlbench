package io.nosqlbench.activitytype.cqld4.config;

import com.datastax.oss.driver.api.core.config.DriverOption;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.api.core.config.TypedDriverOption;
import com.datastax.oss.driver.api.core.data.CqlDuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.UUID;

public class CQLD4OptionsMapper {

    public static void apply(OptionsMap optionsMap, String name, String value) {

        for (TypedDriverOption<?> builtin : TypedDriverOption.builtInValues()) {
            DriverOption rawOption = builtin.getRawOption();
            String path = rawOption.getPath();
            if (name.equals(path)) {
                Class<?> rawType = builtin.getExpectedType().getRawType();
                Object convertedValue = adaptTypeValue(value, rawType, name);
                TypedDriverOption<? super Object> option = (TypedDriverOption<? super Object>) builtin;
                optionsMap.put(option, convertedValue);
                return;
            }
        }

        throw new RuntimeException("Driver option " + name + " was not found in the available options.");
    }

    private static Object adaptTypeValue(String value, Class<?> rawOption, String optionName) {
        switch (rawOption.getCanonicalName()) {
            case "java.lang.Boolean":
                return Boolean.parseBoolean(value);
            case "java.lang.Byte":
                return Byte.parseByte(value);
            case  "java.lang.Double":
                return Double.parseDouble(value);
            case "java.lang.Float":
                return Float.parseFloat(value);
            case "java.lang.Integer":
                return Integer.parseInt(value);
            case "java.lang.Long":
                return Long.parseLong(value);
            case "java.lang.Short":
                return Short.parseShort(value);
            case "java.time.Instant":
                return Instant.parse(value);
            case "java.time.ZonedDateTime":
                return ZonedDateTime.parse(value);
            case "java.time.LocalDate":
                return LocalDate.parse(value);
            case "java.time.LocalTime":
                return LocalTime.parse(value);
            case "java.nio.ByteBuffer":
                return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8)); // What else to do here?
            case "java.lang.String":
                return value;
            case "java.math.BigInteger":
                return new BigInteger(value);
            case "java.math.BigDecimal":
                return new BigDecimal(value);
            case "java.util.UUID":
                return UUID.fromString(value);
            case "java.net.InetAddress":
                try {
                    return InetAddress.getByName(value);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            case "com.datastax.oss.driver.api.core.data.CqlDuration":
                return CqlDuration.from(value);
            case "java.time.Duration:":
                return Duration.parse(value);
            default:
//            These appear to be valid types, but there is no record of them used in driver configuration,
//            nor a convenient way to convert them directly from known type and string value without invoking
//            connected metadata machinery from an active session.
//            case "com.datastax.oss.driver.api.core.data.TupleValue":
//            case "com.datastax.oss.driver.api.core.data.UdtValue":

                throw new RuntimeException("The type converter for driver option named " + optionName + " was not " +
                        "found, or is unimplemented. Please file an issue at nosqlbench.io");
        }
    }

}
