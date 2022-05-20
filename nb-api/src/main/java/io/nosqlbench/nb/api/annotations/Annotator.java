package io.nosqlbench.nb.api.annotations;

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


/**
 * An implementation of this type is responsible for taking annotation details and
 * logging them in a useful place.
 */
public interface Annotator {

    /**
     * Submit an annotation to some type of annotation store, logging or eventing mechanism.
     * Implementations of this service are responsible for mapping the scenario and labels
     * into appropriate key data, and the details in to a native payload. The least surprising
     * and most obvious mapping should be used in each case.
     *
     * For details on constructing a useful annotation to submit to this service, see {@link Annotation#newBuilder()}
     */
    void recordAnnotation(Annotation annotation);

}
