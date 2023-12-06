/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.annotations;

import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.Map;
import java.util.function.Function;

/**
 * This is a general purpose representation of an event that describes
 * a significant workflow detail to users running tests. It can be
 * an event that describes an instant, or it can describe an interval
 * in time (being associated with the interval of time between two
 * canonical events.)
 *
 * This view of an annotation event captures the semantics of what
 * any reportable annotation should look like from the perspective of
 * NoSQLBench. It is up to the downstream consumers to map these
 * to concrete fields or identifiers as appropriate.
 */
public interface Annotation extends NBLabeledElement {

    /**
     * If this is the same as {@link #getEndMillis()}, then the annotation is
     * for an instant in time.
     *
     * @return The beginning of the interval of time that the annotation describes
     */
    long getStartMillis();

    /**
     * If this is the same as {@link #getStartMillis()}, then the annotation
     * is for an instant in time.
     *
     * @return The end of the interval of time that the annotation describes
     */
    long getEndMillis();

    /**
     * Annotations must be associated with a processing layer in NoSQLBench.
     * For more details on layers, see {@link Layer}
     *
     * @return
     */
    Layer getLayer();

    /**
     * The labels which identify what this annotation pertains to. The following labels
     * should be provided for every annotation, when available:
     * <UL>
     * <LI>appname: "nosqlbench"</LI>
     * <LI>alias: The name of the activity alias, if available</LI>
     * <LI>workload: The name of the workload file, if named scenarios are used</LI>
     * <LI>scenario: The name of the named scenario, if named scenarios are used</LI>
     * <LI>step: The name of the named scenario step, if named scenario are used</LI>
     * <LI>usermode: "named_scenario" or "adhoc_activity"</LI>
     * </UL>
     *
     * @return The labels map
     */
    NBLabels getLabels();

    void applyLabelFunction(Function<NBLabels,NBLabels> labelfunc);
    /**
     * The details are an ordered map of all the content that you would want the user to see.
     *
     * @return The details map
     */
    Map<String, String> getDetails();

    static AnnotationBuilderFacets.WantsLabeledElement newBuilder() {
        return new AnnotationBuilder();
    }

    /**
     * This should return {@link Temporal#interval} if the span of time is not an instant, and
     * {@link Temporal#instant}, otherwise.
     */
    Temporal getTemporal();

    String asJson();

    default long getDurationMillis() {
        return getEndMillis()-getStartMillis();
    }

}
