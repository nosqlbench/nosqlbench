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

package io.nosqlbench.api.annotations;

import io.nosqlbench.api.labels.NBLabeledElement;

public interface AnnotationBuilderFacets {

    interface All extends
        WantsLabeledElement, WantsInterval, WantsLayer, WantsMoreDetailsOrBuild {
    }

    interface WantsLabeledElement {
        /**
         * The session is the global name of a NoSQLBench process which run a scenario. It is required.
         */
        WantsInterval element(NBLabeledElement element);
    }

    interface WantsInterval {

        /**
         * Specify the instant of the annotated event.
         *
         * @param epochMillis
         */
        WantsLayer at(long epochMillis);

        /**
         * An interval annotation spans the time between two instants.
         */
        WantsLayer interval(long startMillis, long endMillis);

        /**
         * Use the current UTC time as the annotation instant.
         */
        WantsLayer now();
    }

    interface WantsLayer {
        WantsMoreDetailsOrBuild layer(Layer layer);
    }

    interface WantsMoreDetailsOrBuild {
        WantsMoreDetailsOrBuild addDetail(String name, String value);
        Annotation build();
    }

}
