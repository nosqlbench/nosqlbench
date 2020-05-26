package io.nosqlbench.activitytype.cqld4.config;

import com.datastax.oss.driver.api.core.config.TypedDriverOption;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.StreamSupport;

public class CQLD4OptionsMapper {

    public List<TypedDriverOption<?>> findOptions() {
        Field[] fields = TypedDriverOption.class.getFields();
        ...

    }
}
