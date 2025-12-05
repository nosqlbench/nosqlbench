---
title: "Globalvars"
description: "Doc for globalvars."
tags:
  - nb-engine
  - docs
audience: developer
diataxis: reference
component: core
topic: architecture
status: live
owner: "@nosqlbench/devrel"
generated: false
---

globalvars extension
===================

Allows access to the global object map from SharedState.gl_ObjectMap, which allows
for cross-binding and cross-thread data sharing.

```
var result = globalvars.get("result");
```
