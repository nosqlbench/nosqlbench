+++
title = "Release Management"
description = "NoSQLBench release process and channels"
weight = 60
template = "page.html"

[extra]
quadrant = "development"
topic = "contributing"
category = "releases"
tags = ["releases", "process", "deployment"]
+++

This is partly documenting the current flow, partly aspirational. Within the first few releases
of NB5, this should all be standard and reliable. This notice will be removed at that time.

# Release Channels

We have a three track release system:
- **build** artifacts are produced from every successful build on a branch.
  - _build_ artifacts:
    [build workflow](https://github.com/nosqlbench/nosqlbench/actions/workflows/build.yml)
  - _build_ docs:
    [http://builddocs.nosqbench.io/](http://builddocs.nosqlbench.io)
- **preview** artifacts are promoted to the releases section as pre-release (only) versions.
  - _preview_ artifacts:
    [releases](https://github.com/nosqlbench/nosqlbench/releases/)
  - _preview_ docs:
    [http://previewdocs.nosqbench.io/](http://previewdocs.nosqlbench.io)
- **release** artifacts are eventually promoted to the releases under a proper release name and tag:
    - _release_ artifacts:
      [releases](https://github.com/nosqlbench/nosqlbench/releases/)
    - _release_ docs:
      [http://docs.nosqbench.io/](http://docs.nosqlbench.io)

This is a system of incremental promotion from build to preview to release, with documentation
included. This allows us to preview new features and capabilities with the community in a safe
way, only accepting previews which are up to release standards.

# Repositories

Of course, managing this flow requires a few more repositories than usual, since we are hosting
the docs sites on github pages. This means we need the main code repo for NoSQLBench, as well as
a separate repo for each of the three docs sites:

The main repo for `nb5` is at [nosqlbench.io](http://nosqlbench.io). This is just a convenient
name for [https://github.com/nosqlbench/nosqlbench](https://github.com/nosqlbench/nosqlbench).

This allows convenient access to [issues](http://nosqlbench.io/issues), and
[pull requests](http://nosqlbench.io/pulls) too.

The three docs sites and their corresponding
URLs are:
1. [nosqlbench-build-docs](https://github.com/nosqlbench/nosqlbench-build-docs), accessed at
   [http://builddocs.nosqlbench.io](http://builddocs.nosqlbench.io)
2. [nosqlbench-preview-docs](https://github.com/nosqlbench/nosqlbench-preview-docs), accessed at
   [http://previewdocs.nosqlbench.io](http://previewdocs.nosqlbench.io)
3. [nosqlbench-docs](https://github.com/nosqlbench/nosqlbench-docs), the main docs
   site, at [http://docs.nosqlbenhc.io](http://docs.nosqlbench.io)

# Automation

All of the CI & CD build automation for NoSQLBench is hosted in github actions.

# Release Criteria

Before a preview release can be promoted to a main release, the following must happen:

1. All high CVE alerts for potential issues must be addressed. We use multiple static analysis
   tools for this. Any high severity issues are required to be addressed, even if this means
   that they are flagged as a false positive after further review.
2. All integration test must pass.
3. New functionality must be documented in the docs site. Ideally this includes a what's new update.

# Release Notifications

When a new release is made, the community should be notified in the usual places.
This isn't active, yet but we intend to support:

* discord
* this docs site
* twitter and/or mastodon
* corporate sponsor sites / blog feeds
