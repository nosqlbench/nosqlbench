---
title: utility functions
weight: 40
description: "Doc for funcref functional."
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

## IntFlow

Combine multiple IntUnaryOperators into a single function.

- int -> IntFlow(java.util.function.IntUnaryOperator[]...: ops) -> int


## LongFlow

Combine multiple LongUnaryOperators into a single function.

- long -> LongFlow(java.util.function.LongUnaryOperator[]...: ops) -> long
  - *ex:* `StringFlow(Add(3),Mul(6))` - *Create an integer operator which adds 3 and multiplies the result by 6*


## StringFlow

Combine multiple String functions together into one function.

- java.lang.String -> StringFlow(java.util.function.Function<java.lang.String,java.lang.String>[]...: funcs) -> java.lang.String


