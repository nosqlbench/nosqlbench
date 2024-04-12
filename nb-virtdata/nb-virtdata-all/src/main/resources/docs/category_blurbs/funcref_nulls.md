---
title: null functions
weight: 40
---

These functions can generate null values. When using nulls in your binding recipes, ensure that you don't generate them
in-line as inputs to other functions. This will lead to errors which interrupt your test. If you must use functions that
generate null values, ensure that they are the only or last function in a chain.

If you need to mark a field to be undefined, but _not set to null_, then use the functions which know how to yield a
VALUE.UNSET, which is a sigil constant within the VirtData runtime. These functions are correctly interpreted by
conformant drivers like the SQL driver so that they will avoid inject the named field into an operation if it has this
special value.
