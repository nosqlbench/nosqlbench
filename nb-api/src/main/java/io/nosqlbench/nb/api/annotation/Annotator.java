package io.nosqlbench.nb.api.annotation;

import io.nosqlbench.nb.spi.Named;

import java.util.Map;

/**
 * An implementation of this type is responsible for taking annotation details and
 * logging them in a useful place.
 */
public interface Annotator extends Named {

    /**
     * Submit an annotation to some type of annotation store or logging or eventing mechanism.
     * Implementations of this service are responsible for mapping the scenarioName, target,
     * and details into the native schema of the target annotation or logging system in whichever
     * way would be the least surprising for a user.
     *
     * The target is the nominative data which identifies the identity of the annotation. This
     * must include enough information to allow the annotation to be homed and located within
     * a target system such that is is visible where it should be seen. This includes all
     * metadata which may be used to filter or locate the annotation, including timestamps.
     *
     * The details contain payload information to be displayed within the body of the annotation.
     *
     * @param sessionName      The name of the scenario
     * @param startEpochMillis The epoch millisecond instant of the annotation, set this to 0 to have it
     *                         automatically set to the current system time.
     * @param endEpochMillis   The epoch millisecond instant at the end of the interval. If this is
     *                         equal to the start instant, then this is an annotation for a point in time.
     *                         This will be the default behavior if this value is 0.
     * @param target           The target of the annotation, fields which are required to associate the
     *                         annotation with the correct instance of a dashboard, metrics, etc
     * @param details          A map of details
     */
    void recordAnnotation(
            String sessionName,
            long startEpochMillis,
            long endEpochMillis,
            Map<String, String> target,
            Map<String, String> details);

}
