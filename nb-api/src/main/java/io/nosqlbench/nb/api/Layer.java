package io.nosqlbench.nb.api;

public enum Layer {

    /**
     * Events which describe command line arguments, such as parsing,
     * named scenario mapping, or critical errors
     */
    CLI,

    /**
     * Events which describe scenario execution, such as parameters,
     * lifecycle events, and critical errors
     */
    Scenario,

    /**
     * Events which describe scripting details, such as extensions,
     * sending programmatic annotations, or critical errors
     */
    Script,

    /**
     * Events which are associated with a particular activity instance,
     * such as parameters, starting and stopping, and critical errors
     */
    Activity,

    /**
     * Events which are associated with a particular activity thread
     */
    Motor,

    /**
     * Events which are associated with a particular operation or op template
     */
    Operation
}
