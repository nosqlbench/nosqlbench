package io.nosqlbench.driver.pulsar.ops;

public enum EndToEndStartingTimeSource {
    NONE, // no end-to-end latency calculation
    MESSAGE_PUBLISH_TIME, // use message publish timestamp
    MESSAGE_EVENT_TIME, // use message event timestamp
    MESSAGE_PROPERTY_E2E_STARTING_TIME // use message property called "e2e_starting_time" as the timestamp
}
