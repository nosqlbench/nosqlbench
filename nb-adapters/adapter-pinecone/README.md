---
title: "Pinecone Adapter Status"
description: "Notes on why the Pinecone adapter is disconnected from the main build."
audience: developer
diataxis: explanation
tags:
  - pinecone
  - drivers
component: drivers
topic: drivers
status: deprecated
owner: "@nosqlbench/drivers"
generated: false
---

This driver has been disconnected from the main build and artifacts due to
it being abandoned by pinecone.

If it is needed for anything in the future, a special build should be done
which disables other modules, as there are incompatible versions of grpc
between this module and others.
