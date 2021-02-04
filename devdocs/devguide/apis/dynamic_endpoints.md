# Dynamic Endpoints

NoSQLBench is more than just a test client. It is a test scenario
execution machine with many pluggable behaviors. It also has a daemon mode
which can be used to enable different types of persistent services:

- Documentation Server
- [NBUI](../nbui/README.md) - The fledgling NoSQLBench UI
- Workload Services

These are all enabled by a set of web services backed by jetty and powered
by jax-rs and related libraries.

## Service Discovery

When NoSQLBench is invoked in appserver mode (classically called
`docserver` mode), it starts up with a set of web services. Like many
other runtime elements of NB, these services are discovered internally as
long as they implement the WebServiceObject tagging interface and are
registered as a service with an annotation like

    @Service(WebServiceObject.class)

Any such class is processed as a jax-rs annotated class and automatically
added to the active services accordingly.

This is all you have to do to add endpoints to NoSQLBench!

## Useful Links

* [JAX-RS 2.0 spec](http://download.oracle.com/otn-pub/jcp/jaxrs-2_0-fr-eval-spec/jsr339-jaxrs-2.0-final-spec.pdf)


