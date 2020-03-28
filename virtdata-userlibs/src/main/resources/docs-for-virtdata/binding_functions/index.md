---
title: Binding Functions
weight: 100
---

The functions which you can use to generate data in your workloads are
called *bindings*. They are injected into your statement templates by
name, just as you might do with named parameters in CQL statements.

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

