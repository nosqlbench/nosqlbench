package io.nosqlbench.nb.api.advisor.conditions;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.nb.api.advisor.NBAdvisorCondition;
import org.apache.logging.log4j.Level;

/**
 * <P>The mappings between logging levels for conditions and the advisory
 * levels should be consistent, so they will be described here to start.</P>
 *
 * <P>These are the levels which conditions should use:</P>
 * <OL>
 *     <LI>Level.ERROR (internal to conditions, cause immediate exceptions)</LI>
 *     <LI>Level.WARN (conditions which should inform the user about a likely issue)</LI>
 *     <LI>Level.INFO (conditions which may inform the user about a possible issue)</LI>
 * </OL>
 *
 * <P>This means that the following three behaviors are possible:
 * <OL>
 *     <LI>{@link io.nosqlbench.nb.api.advisor.NBAdvisorLevel#none} - Only ERROR level results throw exceptions, no
 *     results are presented.</LI>
 *     <LI>{@link io.nosqlbench.nb.api.advisor.NBAdvisorLevel#validate} - Only ERROR level results throw exceptions,
 *     all results are presented. </LI>
 *     <LI>{@link io.nosqlbench.nb.api.advisor.NBAdvisorLevel#enforce} - ERROR and WARN levels throw exceptions.</LI>
 * </P>
 *
 */
public class Conditions {

    public static NoHyphens NoHyphensError = new NoHyphens(Level.ERROR);
    public static NoHyphens NoHyphensWarning = new NoHyphens(Level.WARN);
    public static NoSpaces NoSpacesError = new NoSpaces(Level.ERROR);
    public static NoSpaces NoSpacesWarning = new NoSpaces(Level.WARN);
    public static ValidName ValidNameError = new ValidName(Level.ERROR);
    public static ValidName ValidNameWarning = new ValidName(Level.WARN);

}
