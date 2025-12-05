---
title: "Scenario Invocation"
description: "Developer note: Scenario Invocation."
audience: developer
diataxis: explanation
tags:
  - devdocs
component: core
topic: architecture
status: draft
owner: "@nosqlbench/devrel"
generated: false
---

# Scenario Invocation

```mermaid
flowchart LR

    IO\nbuffers -. "embed" .-> fixtures
    params --> fixtures
    fixtures --> Scenario\ninstance
    Scenario\ninstance --> used\nfixtures
    used\nfixtures -. extract .-> IO\ntraces

```
