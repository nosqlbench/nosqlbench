# Building NoSQLBench

## requirements

You need Java 12 or newer to build NoSQLBench.

# Building Locally

## mvn package

To build NoSQLBench in its entirety, you simply need to run

`mvn package`

The steps needed to build the full nb binary are all self-contained
within the Maven build mojo, including the following:

- Building all the maven modules
- Building the nb binary in nb/target/nb using AppImage.
- Building the guidebook app in docsys/src/main/resources/docsys-guidebook
- Building the static guidebook in nb/target/guidebook
- Exporting all the bundled markdown into nb/target/guidebook

## mvn verify

The integration tests for NoSQLBench are run in the verify phase.
You can run these tests with `mvn verify`. These tests are based
on invoking capsule-form scenario scripts and scrutinizing their
output. They can take quite a bit of time, but there is no substitute
for this level of testing, particularly for a tool that is used
to test other systems.

# Automation

## CI and Github Actions

This project uses github actions to manage continuous integration.
Below is a sketch of how the automation currently works.

### releases

The releases workflow is responsible for thie following:

1. Build nb and nb.jar binaries and application content.
2. Publish new nb.jar releases to maven via Sonatype OSSRH.
3. Upload release assets to the newly published release.
4. Upload updated docs to the github pages site for docs.nosqlbench.io
5. (future) upload updated javadocs ot the github pages site for javadoc.nosqlbench.io/...

### build

The build workflow simply builds the project and then verifies it in that order
using the standard maven mojo.


