package io.nosqlbench.nb.api.config.standard;

/*
 * Copyright (c) 2022 nosqlbench
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


import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Shenanigans in the java type system, particularly those around boxing,
 * generics, type-erasure and primitive conversions have brought us here
 * in our attempt to simplify things.
 *
 * In the future, when Java has fewer special cases in the type system,
 * this class can be removed.
 *
 * General purpose strategies for conversion
 * can be injected into the {@link #do_convert(Object, Class)} and
 * {@link #canConvert(Object, Class)}
 * methods.
 */
public class NBTypeConverter {

    private final static List<Class<? extends NBTypeConverters>> CONVERTERS = List.of(NBTypeSafeConversions.class);

    /**
     * The core types should have full set closure on conversions are not narrowing
     */
    public static Set<Class<?>> CORE_TYPES = new HashSet<>() {{
        add(String.class);
        addAll(List.of(byte.class, Byte.class, char.class, Character.class, short.class, Short.class));
        addAll(List.of(int.class, Integer.class, long.class, Long.class));
        addAll(List.of(float.class, Float.class, double.class, Double.class));
    }};

    public static <I, O> boolean canConvert(I input, Class<O> outc) {
        if (outc.equals(input.getClass())) return true; // no conversion needed
        if (outc.isAssignableFrom(input.getClass())) return true; // assignable
        if (ClassUtils.isAssignable(input.getClass(), outc, true)) return true; // assignable with boxing
        if (String.class.isAssignableFrom(outc)) return true; // all things can be strings
        if (outc.isPrimitive() && outc != boolean.class && outc != void.class && (input instanceof Number))
            return true; // via Number conversions
        return (lookup(input.getClass(), outc) != null); // fall-through to helper method lookup
    }

    private static <I, O> Method lookup(Class<I> input, Class<O> output) {
        Method candidate = null;
        for (Class<? extends NBTypeConverters> converters : CONVERTERS) {
            try {
                candidate = converters.getMethod("to_" + output.getSimpleName(), input);
                break;
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (candidate == null) {
            return null;
        }

        if (!candidate.getReturnType().equals(output)) {
            return null;
        }
        if (!((candidate.getModifiers() & Modifier.STATIC) > 0)) {
            return null;
        }
        return candidate;
    }

    private static final Map<Class<?>, Class<?>> REMAP_to_primitive = new HashMap<>() {{
        put(Byte.class, byte.class);
        put(Short.class, short.class);
        put(Integer.class, int.class);
        put(Long.class, long.class);
        put(Float.class, float.class);
        put(Double.class, double.class);
        put(Character.class, char.class);
        put(Boolean.class, boolean.class);
    }};

    public static <T> Optional<T> tryConvert(Object input, Class<T> outType) {
        T converted = do_convert(input, outType);
        return Optional.ofNullable(converted);
    }

    public static <T> T convertOr(Object input, T defaultValue) {
        if (input == null) {
            return defaultValue;
        }
        return convert(input, (Class<T>) defaultValue.getClass());
    }

    public static <T> T convert(Object input, Class<T> outType) {

        T converted = do_convert(input, outType);
        if (converted == null) {
            throw new RuntimeException(
                "Could not find conversion method\n" + methodName(input.getClass(), outType) +
                    "\nYou could implement it, or perhaps this is a type of conversion that should not be supported,\n" +
                    "for example, if it might lose data as a narrowing conversion.");

        }
        return converted;
    }

    private static String methodName(Class<?> inType, Class<?> outType) {
        return "    public static " + REMAP_to_primitive.getOrDefault(outType, outType).getSimpleName() + " to_" +
            REMAP_to_primitive.getOrDefault(outType, outType).getSimpleName()
            + "(" + REMAP_to_primitive.getOrDefault(inType, inType).getSimpleName() + " in) {\n" +
            "    ...\n" +
            "    }";
    }

    private static <T> T do_convert(Object input, Class<T> outType) {

        // Category 0, nothing to do here
        if (outType.equals(input.getClass())) {
            return (T) input;
        }

        if (String.class.isAssignableFrom(outType)) {
            return (T) input.toString();
        }

        // Category 1, happy path, in and out are directly convertible according to JLS assignment
        if (outType.isAssignableFrom(input.getClass())) {
            return outType.cast(input);
        }

        // primitive number -> primitive number (ok)
        // primitive number -> Boxed Number Type (ok)
        // Boxed Number Type -> primitive number (ok)
        // Boxed Number Type -> Boxed Number Type (ERROR)
        Class<?> loutc = REMAP_to_primitive.getOrDefault(outType, outType);

        Class<?> inType = input.getClass();
        Class<?> linc = REMAP_to_primitive.getOrDefault(inType, inType);

        if (loutc.isPrimitive() && loutc != Boolean.TYPE && loutc != Character.TYPE
            && input instanceof Number) {
            if (loutc == long.class) return (T) (Long) ((Number) input).longValue();
            if (loutc == int.class) return (T) (Integer) ((Number) input).intValue();
            if (loutc == float.class) return (T) (Float) ((Number) input).floatValue();
            if (loutc == double.class) return (T) (Double) ((Number) input).doubleValue();
            if (loutc == byte.class) return (T) (Byte) ((Number) input).byteValue();
            if (loutc == short.class) return (T) (Short) ((Number) input).shortValue();
        }

        // Category boxing, assignable with auto-(un)boxing, something that Java libs seem to ignore
        // This might lead to trouble as this method returns true even when intermediate non-boxed
        // types must be used to avoid boxed->boxed conversions
        if (ClassUtils.isAssignable(input.getClass(), outType, true)) {
            return (T) input;
        }

        // Last option: custom methods

        Method converter = lookup(linc, loutc);

        if (converter == null) {
            return null;
        }

        try {
            Object result = converter.invoke(null, input);
            return (T) result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to convert (" + input + ") to " + outType.getSimpleName() + ": " + e, e);
        }
    }


}
