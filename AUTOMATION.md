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
