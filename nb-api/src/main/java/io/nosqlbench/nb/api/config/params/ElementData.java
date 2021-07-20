package io.nosqlbench.nb.api.config.params;

import java.util.Set;

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

    Set<String> getKeys();

    boolean containsKey(String name);

    default String getName() {
        String name = getGivenName();
        if (name!=null) {
            return name;
        }
        return extractElementName();
    }

    String getGivenName();

    default String extractElementName() {
        if (containsKey(NAME)) {
            Object o = get(NAME);
            if (o instanceof CharSequence) {
                return ((CharSequence)o).toString();
            }
        }
        return null;
    }

    default <T> T convert(Object input, Class<T> type) {
        if (type!=null) {
            if (type.isAssignableFrom(input.getClass())) {
                return type.cast(input);
            } else {
                throw new RuntimeException("Conversion from " + input.getClass().getSimpleName() + " to " + type.getSimpleName() +
                    " is not supported natively. You need to add a type converter to your ElementData implementation for " + getClass().getSimpleName());
            }
        } else {
            return (T) input;
        }
    }

    default <T> T get(String name, Class<T> type) {
        Object o = get(name);
        if (o!=null) {
            return convert(o,type);
        } else {
            return null;
        }
    }

    default <T> T lookup(String name, Class<T> type) {
        int idx=name.indexOf('.');
        while (idx>0) { // TODO: What about when idx==0 ?
            // Needs to iterate through all terms
            String parentName = name.substring(0,idx);
            if (containsKey(parentName)) {
                Object o = get(parentName);
                ElementData parentElement = DataSources.element(parentName, o);
                String childName = name.substring(idx+1);
                int childidx = childName.indexOf('.');
                while (childidx>0) {
                    String branchName = childName.substring(0,childidx);
                    Object branchObject = parentElement.lookup(branchName,type);
                    if (branchObject!=null) {
                        ElementData branch = DataSources.element(branchName, branchObject);
                        String leaf=childName.substring(childidx+1);
                        T found = branch.lookup(leaf, type);
                        if (found!=null) {
                            return found;
                        }
                    }
                    childidx=childName.indexOf('.',childidx+1);
                }
                T found = parentElement.lookup(childName,type);
                if (found!=null) {
                    return found;
                }
            }
            idx=name.indexOf('.',idx+1);
        }
        return get(name,type);
    }

}
