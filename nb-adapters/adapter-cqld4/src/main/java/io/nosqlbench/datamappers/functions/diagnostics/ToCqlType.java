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

package io.nosqlbench.datamappers.functions.diagnostics;

import com.datastax.oss.driver.api.core.type.DataTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

/**
 * Shows the compatible CQL type most associated with the incoming Java type.
 */
//@ThreadSafeMapper
//@Categories({Category.diagnostics})
public class ToCqlType implements Function<Object, String> {

    private final static Map<String, String> typemap = new HashMap<String, String>() {{

        for (Field field : DataTypes.class.getFields()) {
            int mods = field.getModifiers();
            int req = Modifier.STATIC & Modifier.FINAL & Modifier.PUBLIC;
            if ((mods&req)<req) {
                continue;
            }
            if (!field.getName().toUpperCase(Locale.ROOT).equals(field.getName())) {
                continue;
            }
        }
        put("unsupported in this version"," additional work required ");
    }};

    private final ThreadLocal<StringBuilder> tlsb = ThreadLocal.withInitial(StringBuilder::new);

    @Override
    public String apply(Object o) {
        String canonicalName = o.getClass().getCanonicalName();
        String cqlTypeName = typemap.get(canonicalName);
        StringBuilder sb = tlsb.get();
        sb.setLength(0);
        if (cqlTypeName!=null) {
            return sb.append(canonicalName).append(" -> ").append(cqlTypeName).toString();
        }
        return findAlternates(o,canonicalName);
    }

    private String findAlternates(Object o, String canonicalName) {
        StringBuilder sb = tlsb.get();

        if (List.class.isAssignableFrom(o.getClass())) {
            sb.append(canonicalName).append("<");

            if (((List)o).size()>0) {
                Object o1 = ((List) o).get(0);
                String elementType = o1.getClass().getCanonicalName();
                sb.append(elementType).append("> -> List<");
                sb.append(typemap.getOrDefault(elementType,"UNKNOWN")).append(">");
                return sb.toString();
            }
            return sb.append("?> -> List<?>").toString();
        }
        if (Map.class.isAssignableFrom(o.getClass())) {
            sb.append(canonicalName).append("<");
            if (((Map)o).size()>0) {
                Map.Entry next = (Map.Entry) ((Map) o).entrySet().iterator().next();
                String keyType = next.getKey().getClass().getCanonicalName();
                String valType = next.getValue().getClass().getCanonicalName();
                sb.append(keyType).append(",").append(valType).append("> -> Map<");
                sb.append(typemap.getOrDefault(keyType,"UNKNOWN")).append(",");
                sb.append(typemap.getOrDefault(valType,"UNKNOWN")).append(">");
                return sb.toString();
            }
            return sb.append("?,?> -> Map<?,?>").toString();
        }
        if (Set.class.isAssignableFrom(o.getClass())) {
            sb.append(canonicalName).append("<");
            if (((Set)o).size()>0) {
                Object o1=((Set)o).iterator().next();
                String elementType = o1.getClass().getCanonicalName();
                sb.append(elementType).append("> -> Set<");
                sb.append(typemap.getOrDefault(elementType,"UNKNOWN")).append(">");
                return sb.toString();
            }
            return sb.append("?> -> Set<?>").toString();
        }
        return typemap.getOrDefault(o.getClass().getSuperclass().getCanonicalName(), "UNKNOWN");
    }
}
