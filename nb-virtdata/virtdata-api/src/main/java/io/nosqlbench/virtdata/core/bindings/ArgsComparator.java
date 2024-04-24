/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.virtdata.core.bindings;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.*;

public class ArgsComparator implements Comparator<Constructor<?>> {
    public enum MATCHRANK {
        DIRECT,
        CONVERTED,
        BOXED,
        INCOMPATIBLE

    }
    private final Object[] parameters;

    public ArgsComparator(Object[] parameters) {
        this.parameters = parameters;
    }

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP = new HashMap<>(32) {{
        put(Integer.class, int.class);
        put(Byte.class, byte.class);
        put(Character.class, char.class);
        put(Boolean.class, boolean.class);
        put(Double.class, double.class);
        put(Float.class, float.class);
        put(Long.class, long.class);
        put(Short.class, short.class);
        put(Void.class, void.class);

        put(int.class, Integer.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(boolean.class, Boolean.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(long.class, Long.class);
        put(short.class, Short.class);
        put(void.class, Void.class);
    }};

    @Override
    public int compare(Constructor<?> o1, Constructor<?> o2) {
        return Integer.compare(matchRank(o1, parameters).ordinal(), matchRank(o2, parameters).ordinal());
    }

    /**
     * Establish a priority value (lower is better) based on how well the arguments
     * match to the given constructor's parameters.
     *
     * Note: The distinction between primitives and boxed types is lost here,
     * as the primitive version of Class<?> is only accessible via {@link Long#TYPE}
     * and similar, so primitive matching and auto-boxed matching are effectively
     * the same rank.
     *
     *  rank 0 -> all arguments are the same type or boxed type
     *  rank 1 -> all arguments are assignable, without autoboxing
     *  rank 2 -> all arguments are assignable, with autoboxing
     *  rank 3 -> not all arguments are assignable
     * @param ctor - constructor
     * @param arguments - arguments to match against
     * @return a lower number for when arguments match parameters better
     */
    public MATCHRANK matchRank(Constructor<?> ctor, Object[] arguments) {
        int paramLen = ctor.getParameterCount();
        int argsLen = arguments.length;

        if (paramLen!=argsLen && !ctor.isVarArgs()) {
            return MATCHRANK.INCOMPATIBLE;
        }

        int len = arguments.length; // only consider varargs if some provided
        MATCHRANK[] ranks = new MATCHRANK[len];

        Class<?>[] ptypes = ctor.getParameterTypes();
        Class<?>[] atypes = Arrays.stream(arguments).map(Object::getClass).toArray(i -> new Class<?>[i]);

        for (int position = 0; position < len; position++) {
            Class<?> ptype = ptypes[position];
            Class<?> atype = (position<atypes.length) ? atypes[position] : atypes[atypes.length-1];
            Class<?> across = WRAPPER_TYPE_MAP.get(atype);
            Class<?> pcross = WRAPPER_TYPE_MAP.get(ptype);

            if (atype.isPrimitive()==ptype.isPrimitive() && atype.equals(ptype)) {
                ranks[position] = MATCHRANK.DIRECT;
            } else if (across != null && pcross != null && (across.equals(ptype) || pcross.equals(atype))) {
                ranks[position] = MATCHRANK.DIRECT;
            } else if (ClassUtils.isAssignable(atype, ptype, false)) {
                ranks[position] = MATCHRANK.CONVERTED;
            } else if (ClassUtils.isAssignable(atype, ptype, true)) {
                ranks[position] = MATCHRANK.BOXED;
            } else {
                ranks[position] = MATCHRANK.INCOMPATIBLE;
            }
        }
        Integer maxOrdinal = Arrays.stream(ranks).map(MATCHRANK::ordinal).max(Integer::compare).orElse(0);
        return MATCHRANK.values()[maxOrdinal];
    }
}
