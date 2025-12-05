---
title: "Examples As Tests"
description: "Developer note: Examples As Tests."
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

# Examples As Tests


## Time Based Functions

### ToHashedUUID

This function allows you to take a long input and hash it and then
convert the result into a java.util.UUID at the finest resolution
 possible.

```functest
ToHashedUUID();
```
