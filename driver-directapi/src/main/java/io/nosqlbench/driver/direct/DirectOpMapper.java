package io.nosqlbench.driver.direct;

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


import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

public class DirectOpMapper implements OpMapper<DirectCall> {

    @Override
    public OpDispenser<DirectCall> apply(ParsedOp cmd) {

        String pkg = cmd.getOptionalValue("package", String.class).orElse("java.lang");
        String cls = cmd.getStaticValue("class");
        String fq = pkg + "." + cls;
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName(fq);

            Class<?> finalClazz = clazz;
            Optional<Field> staticfield =
                cmd.getOptionalValue("staticfield", String.class)
                    .map(name -> {
                        try {
                            return finalClazz.getDeclaredField(name);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
            if (staticfield.isPresent()) {
                Field sfield = staticfield.get();
                if ((sfield.getModifiers() | Modifier.STATIC) > 0) {
                    instance = sfield.get(null);
                    clazz = instance.getClass();

                } else {
                    throw new OpConfigError("staticfield '" + cmd.getStaticValue("staticfield", String.class) + "' is not static");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String methodName = cmd.getStaticValue("method");

        Map<String, Object> protomap = cmd.apply(0L);
        List<Class<?>> protoargs = new ArrayList<>();
        List<String> argnames = protomap.keySet().stream()
            .filter(n -> n.startsWith("_"))
            .collect(Collectors.toList());

        LongFunction<List<Object>> argsbinder = cmd.newListBinder(argnames);
        List<Object> args = argsbinder.apply(0L);
        List<Class<?>> types = args.stream().map(Object::getClass).collect(Collectors.toList());

        Class<?>[] argTypes = types.toArray(new Class<?>[0]);

        Method method = null;
        try {
            method = clazz.getMethod(methodName, argTypes);
            return new StaticMethodOpDispenser(method, instance, cmd.newArrayBinder(argnames));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
