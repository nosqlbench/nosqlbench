package io.nosqlbench.nb.api.config.fieldreaders;

import java.util.Optional;

public interface StaticFieldReader {

    /**
     * @param field The requested field name
     * @return true, if the field is defined.
     */
    boolean isDefined(String field);

    /**
     * @param field The requested field name
     * @param type The required type of the field value
     * @return true if the field is defined <em>and</em> its value is statically defined as assignable to the given type
     */
    boolean isDefined(String field, Class<?> type);

    /**
     * @param fields The requested field names
     * @return true if the field names are all defined
     */
    boolean isDefined(String... fields);

    <T> T getStaticValue(String field, Class<T> classOfT);

    <T> T getStaticValue(String field);

    <T> T getStaticValueOr(String name, T defaultValue);

    <T> Optional<T> getOptionalValue(String field, Class<T> classOfT);

    void assertDefinedStatic(String... fields);
}
