package io.nosqlbench.virtdata.api.processors;

import io.nosqlbench.virtdata.api.annotations.Category;

import java.util.List;

/**
 * Provide data about a function, suitable for building a documentation site.
 */
public interface DocFuncData {

    /**
     * @return the package name for the documented type
     */
    String getPackageName();

    /**
     * @return Return the categories for this function.
     */
    Category[] getCategories();

    /**
     * @return the the {@link Class#getSimpleName()} of the class element
     */
    String getClassName();

    /**
     * Javadoc for the class, or null if there is none.
     * @return a String of class javadoc data, or null if none
     */
    String getClassJavadoc();

    /**
     * The input type for the apply method in the documented function class.
     * Documented function classes must always implement a Java 8 functional interface.
     * @return the input type name
     */
    String getInType();

    /**
     * The output type for the apply method in the documented function class.
     * Documented function classes must always implement a Java 8 functional interface.
     * @return the output type name
     */
    String getOutType();

    /**
     * The list of constructors for this documented type.
     * @return a list of constructor models
     */
    List<DocCtorData> getCtors();
}
