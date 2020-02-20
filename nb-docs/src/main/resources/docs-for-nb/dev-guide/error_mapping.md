---
title: Error Mapping
weight: 36
menu:
  main:
    parent: Dev Guide
    identifier: Error Mapping
    weight: 13
---

Each activity type should provide its own mapping between thrown errors and the error codes assigned to them.

This is facilitated by the `ErrorMapper` interface. It simply provides a way to initialize a cache-friendly view
of classes which are known exception types to a stable numbering of error codes.

By providing an error mapper for your activity type, you are enabling advanced testing scenarios that deal with
error routing and advanced error handling.

If no error mapper is installed in the ActivityType implementation, then a default one is provided which simply
maps all errors to _unknown_. 