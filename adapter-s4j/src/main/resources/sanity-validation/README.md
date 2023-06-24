# Overview

The `e2e-nbs4j-sanity.sh` is used to run an end-to-end testing flow using NB S4J adapter. It includes a various
of message sender and receiver combinations that cover main S4J functionalities. This test fills gap where the
unit tests can't cover. It is suggested to always run this script before committing the code changes to the
official NB repo for the following NB modules:
* `adapter-pulasr` (Native Pulsar)
* `adapter-kafka`  (Starlight for Kafka)
* `adapter-s4r`    (Starlight for RabbitMQ)
* `adapter-s4j`    (Starlight for JMS)

The reason is all these modules depend upon Apache Pulsar client library, either directly or indirectly. Sometimes
conflict, including runtime ones, could happen. When the conflict happens, it may impact the testing capability
of using one of the these modules. The end to end sanity validation script is used to prevent this from happening
and thus makes sure unwanted code changes don't end up in the official repo.

# Pulsar Cluster

Running this script requires a live Pulsar cluster. It is recommended to use an Astra Streaming tenant for this
purpose. The corresponding Pulsar cluster connection information should be put in a `client.conf` file.

Please **NOTE** that the target Pulsar cluster should have the following tenant/namespace/topic created before
running the script:
* nbtest/default/s4j-sanity


# Usage

Simply run the script from a command line to kick off the sanity validation.
```bash
$ e2e-nbs4j-sanity.sh -c </path/to/cient.conf>
```

The execution result will be recorded in a log file with the following naming pattern:
```bash
$ e2e-nbs4j-sanity-YYYYMMDDhhmmss.log
```
