package io.nosqlbench.driver.jmx;

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


import java.util.function.Function;

public class ValueConverter {

    public static Object convert(String typeName, Object o) {

        if (!typeName.contains(".")) {
            if (Number.class.isAssignableFrom(o.getClass())) {
                switch (typeName) {
                    case "String":
                        return o.toString();
                    case "long":
                    case "Long":
                        return (((Number) o).longValue());
                    case "int":
                    case "Integer":
                        return (((Number) o).intValue());
                    case "double":
                    case "Double":
                        return (((Number) o).doubleValue());
                    case "float":
                    case "Float":
                        return (((Number) o).floatValue());
                    case "short":
                    case "Short":
                        return (((Number) o).shortValue());
                    case "byte":
                    case "Byte":
                        return (((Number) o).byteValue());
                    default:
                        throw new RuntimeException("For numeric values, you can only convert to long,int,double,float,byte,short or String");
                }
            } else {
                String value;
                if (CharSequence.class.isAssignableFrom(o.getClass())) {
                    value = (String) o;
                } else {
                    value = o.toString();
                }
                switch (typeName) {
                    case "String":
                        return value;
                    case "long":
                    case "Long":
                        return Long.parseLong(value);
                    case "int":
                    case "Integer":
                        return Integer.parseInt(value);
                    case "double":
                    case "Double":
                        return Double.parseDouble(value);
                    case "float":
                    case "Float":
                        return Float.parseFloat(value);
                    case "short":
                    case "Short":
                        return Short.parseShort(value);
                    case "byte":
                    case "Byte":
                        return Byte.parseByte(value);
                    default:
                        throw new RuntimeException("For String values, you can only convert to long, int, double, float, short, byte, or String");
                }
            }
        }

        try {
            Class<?> aClass = Class.forName(typeName);
            return aClass.cast(o);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
