package io.nosqlbench.nb.api.annotations;

import io.nosqlbench.nb.api.spi.Named;

/**
 * An implementation of this type is responsible for taking annotation details and
 * logging them in a useful place.
 */
public interface Annotator extends Named {

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
