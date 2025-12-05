---
title: Using Bindings
weight: 15
description: "Doc for using bindings."
tags:
  - nb-virtdata
  - docs
audience: developer
diataxis: reference
component: virtdata
topic: bindings
status: live
owner: "@nosqlbench/devrel"
generated: false
---

The functions which you can use to generate data in your workloads are
mapped into your operations by name, just like you would do with a
prepared statement, for example.

These functions can be stitched together in small recipes. When you give
these mapping functions useful names in your workloads, they are called
bindings.

Here is an example:

```yaml
bindings:
 numbers: NumberNameToString()
 names: FirstNames()
```

These are two bindings that you can use in your workloads. The names on the left
are the _binding names_ and the functions on the right are the _binding recipes_.
Altogether, we just call them _bindings_.

