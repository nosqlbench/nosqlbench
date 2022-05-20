package io.nosqlbench.virtdata.api.processors;

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
