/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api.config.standard;

import java.math.BigDecimal;

public class NBTypeSafeConversions implements NBTypeConverters {

    public static BigDecimal to_BigDecimal(String s) {
        return BigDecimal.valueOf(Double.parseDouble(s));
    }

    public static byte to_byte(String s) {
        return Byte.parseByte(s);
    }

    public static char to_char(String s) {
        return s.charAt(0);
    }

    public static int to_int(String s) {
        return Integer.parseInt(s);
    }

    public static short to_short(int in) {
        if (in > Short.MAX_VALUE || in < Short.MIN_VALUE) {
            throw new RuntimeException("converting " + in + " to short would truncate the value provided.");
        }
        return (short) in;
    }

    public static char to_char(int in) {
        if (in > Character.MAX_VALUE || in < Character.MIN_VALUE) {
            throw new RuntimeException("Converting " + in + " to char would truncate the value provided.");
        }
        return (char) in;
    }

    public static int to_int(char in) {
        return in;
    }

    public static short to_short(char in) {
        return (short) in;
    }

    public static byte to_byte(char in) {
        int v = in;
        if (in > Byte.MAX_VALUE || in < Byte.MIN_VALUE) {
            throw new RuntimeException("Converting " + in + " to byte would truncate the value provided.");
        }
        return (byte) v;
    }

    public static long to_long(char in) {
        return in;
    }

    public static float to_float(char in) {
        return in;
    }

    public static double to_double(char in) {
        return in;
    }

    public static char to_char(double in) {
        if (in > Character.MAX_VALUE || in < Character.MIN_VALUE) {
            throw new RuntimeException("Converting " + in + " to char would truncate the value provided.");
        }
        return (char) in;
    }

    public static char to_char(short in) {
        return (char) in;
    }

    public static char to_char(long in) {
        if (in > Character.MAX_VALUE || in < Character.MIN_VALUE) {
            throw new RuntimeException("Converting " + in + " to char would truncate the value provided.");
        }
        return (char) in;
    }

    public static char to_char(float in) {
        if (in > Character.MAX_VALUE || in < Character.MIN_VALUE) {
            throw new RuntimeException("Converting " + in + " to char would truncate the value provided.");
        }
        return (char) in;
    }

    public static char to_char(byte in) {
        return (char) in;
    }

    public static double to_double(String in) {
        return Double.parseDouble(in);
    }

    public static short to_short(String in) {
        return Short.parseShort(in);
    }

    public static long to_long(String in) {
        return Long.parseLong(in);
    }

    public static float to_float(String in) {
        return Float.parseFloat(in);
    }

    public static boolean to_boolean(String in) {
        return Boolean.parseBoolean(in);
    }

}



