package io.nosqlbench.components;

import io.nosqlbench.api.spi.SimpleServiceLoader;
import io.nosqlbench.nb.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

public class NBComponentLoader {
    public static <C extends NBComponent> C load(NBComponent parent, String selector, Class<C> clazz) {
        ServiceLoader<C> loader = ServiceLoader.load(clazz);
        ServiceLoader.Provider<C> cp = loader.stream().filter(p -> {
            Service service = Arrays.stream(p.type().getAnnotationsByType(Service.class)).findFirst().orElseThrow();
            return service.selector().equals(selector);
        }).findFirst().orElseThrow();
        try {
            Constructor<? extends C> ctor = cp.type().getConstructor(NBComponent.class);
            return ctor.newInstance(parent);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
