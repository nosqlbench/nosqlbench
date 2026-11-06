# Building NoSQLBench

## requirements

You need Java 25 or newer to build NoSQLBench.

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

The releases workflow is responsible for the following:

1. Build nb and nb.jar binaries and application content.
2. Publish new nb.jar releases to maven via Sonatype OSSRH.
3. Upload release assets to the newly published release.
4. Upload updated docs to the github pages site for docs.nosqlbench.io
5. (future) upload updated javadocs ot the github pages site for javadoc.nosqlbench.io/...

**Note:** this workflow is one part of the broader release operation, outlined below in the section titled "Overview of the release process".

### build

The build workflow simply builds the project and then verifies it in that order
using the standard maven mojo.

# Overview of the release process

_This section describes the steps for a full end-to-end release. Write access to the repo is required._

**Scenario**: you want to cut a release from current `main`, say `5.25.11-release` (numbers are an example throughout).

**Initial state**:

- an older commit, `010`:
  - is pointed at by a `5.25.10-release` tag
  - corresponds to the "latest" release on Github (`5.25.10` indeed)
  - contains `5.25.10-SNAPSHOT` in its `mvn-defaults/pom.xml`
- the latest commit on `main` is `011` and has no tags
  - in it, the `mvn-defaults/pom.xml` already states `5.25.11-SNAPSHOT` (this happened right after releasing 5.25.10 in some `010-b` commit)

Desired **end state**:

- a new `5.25.11-release` tag on Github, pointing at commit `011`
  - a new release on Github, marked "latest", made from said tag
- a later commit, call it `011-b`, where the `mvn-defaults` file is bumped to `5.25.12-SNAPSHOT` already, for the next iteration

Diagrammatically (past is below), from this state:

```
              pom.xml     tags              releases
              --------------------------------------------
        ^
        |
HEAD   011    [11]
        |
       ...
        |
      010-b   [11]
        |
       010    [10]      5.25.10-release     (artifact 10)
        |
```

to this one:


```
              pom.xml     tags              releases
              --------------------------------------------
        ^
        |
HEAD  011-b   [12]
        |
       011    [11]      5.25.11-release     (artifact 11)
        |
       ...
        |
      010-b   [11]
        |
       010    [10]      5.25.10-release     (artifact 10)
        |
```

_The "preview" release channel is not described here. The mechanism is similar, with a different way to mark them in Github (i.e. no 'latest' whatsoever), and the fact that one may afford themselves a little more freedom in "repositioning" (aka overwriting) a same-named preview, leave it with a gap w.r.t. its corresponding true release, etc._

## Steps

1. Ensure the tag on `main` is one higher than the last released artifact. Then you can go to the repo root and run `./scripts/trigger-release-build`. This will add a new `5.25.11-release` Git tag to this commit (`011`) and push it to the remote. _Note: use `-f` to force-push, e.g. if the tag for some reason already exists and points at an older commit by mistake._
2. Pushing the tag will trigger a Github action for building and releasing from `011`. The action checks that the versions match (tag vs. pom.xml). Go to Github and check the action succeeds. You find the action under "releases" in the left-side navbar, named after the name of the commit with hash `011`.
3. (You may want to work in the same way with the `trigger-preview-build`, and will end up with a preview release made of the same binaries)
4. At this point the "Releases" page of the repo on Github has a new release listed (two if you worked with the preview as well). Locate the newest "release" (check the minor, i.e. the "11" in `5.25.11` in this example) and mark it as "Latest" on the Github UI. Release notes are created automatically for you.
5. Smoke test: download the `nb5` binary from the new "latest" release, `chmod +x` it and launch it with `--version` to see if it matches.
6. Time to bump the version on `main`, to get ready for the next development iteration. Create a branch for the bump (e.g. named `version-bump-12`) and go to that branch.
7. In the repo root, run `./scripts/bump-minor-version`: this will detect the version and change it on `mvn-defaults/pom.xml`
8. Git add + commit + push the changes, submit a PR; then get it merged (approval + merging). _Note: do not push to main directly, which is a protected branch against that._
9. That's it. Everything is like it was originally, but at one version up.
