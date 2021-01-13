package io.nosqlbench.nb.api.config.params;

/**
 * A generic type-safe reader interface for parameters.
 * TODO: This should be consolidated with the design of ConfigLoader once the features of these two APIs are stabilized.
 *
 * The source data for a param reader is intended to be a collection of something, not a single value.
 * As such, if a single value is provided, an attempt will be made to convert it from JSON if it starts with
 * object or array notation. If not, the value is assumed to be in the simple ParamsParser form.
 */
public interface ElementData {
    String NAME = "name";

    Object get(String name);

    boolean containsKey(String name);

//    default ElementData getChildElementData(String name) {
//        Object o = get(name);
//        return DataSources.element(o);
//        List<ElementData> datas = DataSources.elements(o);
//        if (datas.size() == 0) {
//            return null;
//        } else if (datas.size() > 1) {
//            throw new RuntimeException("expected one element for '" + name + "'");
//        } else {
//            return datas.get(0);
//        }
//    }

    default String getElementName() {
        if (containsKey(NAME)) {
            Object o = get(NAME);
            if (o != null) {
                String converted = convert(o, String.class);
                return converted;
            }
        }
        return null;
    }

    default <T> T convert(Object input, Class<T> type) {
        if (type.isAssignableFrom(input.getClass())) {
            return type.cast(input);
        } else {
            throw new RuntimeException("Conversion from " + input.getClass().getSimpleName() + " to " + type.getSimpleName() +
                " is not supported natively. You need to add a type converter to your ElementData implementation for " + getClass().getSimpleName());
        }
    }

}
