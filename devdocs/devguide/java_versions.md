---
title: "Java Version Updates"
description: "Developer note: Java Version Updates."
audience: developer
diataxis: explanation
tags:
  - devdocs
component: core
topic: contributing
status: draft
owner: "@nosqlbench/devrel"
generated: false
---

# Java Version Updates

This is a list of all the places that the Java version is referenced in
the NoSQLBench project. If you change versions or need to do
version-specific troubleshooting, this list will help.

- In the Dockerfile, in the parent image "FROM" tag.
  [here](../../Dockerfile)
- In the Maven pom files, under `<source>...</source>`, `<release>...
  </release>`, `<target>...</target>`
  [here](../../mvn-defaults/pom.xml)
- In some cases, a Maven variable '<java.target.version>...</java.target.
  version>` is used.
  [here](../../mvn-defaults/pom.xml)
- In the nb appimage build scripts under nb5/nb-appimage/build-bin.sh.
  [here](../../nb5/nb-appimage/build-bin.sh)
- In the github actions workflows for the Java runtime version
