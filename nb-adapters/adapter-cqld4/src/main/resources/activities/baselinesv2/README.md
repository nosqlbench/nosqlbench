---
source: nb-adapters/adapter-cqld4/src/main/resources/activities/baselinesv2/README.md

title: "Baselines Version 2"
description: "Adapter doc for README."
audience: operator
diataxis: howto
tags:
  - cqld4
  - drivers
component: drivers
topic: drivers
status: live
owner: "@nosqlbench/drivers"
generated: false
---

# Baselines Version 2

In order to avoid changing those tests and possibly impacting
results without warning, the baseline sets are being kept
in separate directories.

## Status

This directory is for baselines version 2. These files are the current
in-development set of baselines, and may change in minor ways, or have
additional workloads added, for example. If you are performing baselines
over a period of time and need the workloads to be perfectly stable,
it is best to copy these to your test assets under a distinct name and
call them from there.

To further disambiguate the workloads, each one has a version '2'
appended to the filename.

