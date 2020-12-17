package io.nosqlbench.nb.api.annotations;

public interface BuilderFacets {

    interface All extends
            WantsSession, WantsInterval, WantsLayer, WantsLabels, WantsMoreDetailsOrBuild, WantsMoreLabelsOrDetails {
    }

    interface WantsSession {
        /**
         * The session is the global name of a NoSQLBench process which run a scenario. It is required.
         */
        WantsInterval session(String session);
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
        WantsMoreLabelsOrDetails layer(Layer layer);
    }

    interface WantsLabels {
        WantsMoreLabelsOrDetails label(String name, String value);
    }

    interface WantsMoreLabelsOrDetails {
        WantsMoreLabelsOrDetails label(String name, String value);

        WantsMoreDetailsOrBuild detail(String name, String value);
    }

    interface WantsMoreDetailsOrBuild {
        WantsMoreDetailsOrBuild detail(String name, String value);

        Annotation build();
    }

}
