# Building NoSQLBench

## requirements

You need Java 11 or newer to build NoSQLBench.

# mvn package

To build NoSQLBench in its entirety, you simply need to run

`mvn package`

The steps needed to build the full nb binary are all self-contained
within the Maven build mojo, including the following:

- Building all the maven modules
- Building the nb binary in nb/target/nb using AppImage.
- Building the guidebook app in docsys/src/main/resources/docsys-guidebook
- Building the static guidebook in nb/target/guidebook
- Exporting all the bundled markdown into nb/target/guidebook

# mvn verify

The integration tests for NoSQLBench are run in the verify phase.
You can run these tests with `mvn verify`. These tests are based
on invoking capsule-form scenario scripts and scrutinizing their
output. They can take quite a bit of time, but there is no substitute
for this level of testing, particularly for a tool that is used
to test other systems.


