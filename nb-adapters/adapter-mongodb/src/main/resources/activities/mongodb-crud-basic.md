---
source: nb-adapters/adapter-mongodb/src/main/resources/activities/mongodb-crud-basic.md

title: mongoDB CRUD Basic
weight: 1
audience: operator
diataxis: howto
tags:
  - mongodb
  - activities
component: drivers
topic: drivers
status: live
owner: "@nosqlbench/drivers"
generated: false
---

## Description

The mongoDB CRUD Basic workflow emulates CRUD operations for the mongoDB using generated JSON documents.
It's a counterpart of the Stargate's Documents API CRUD Basic workflow.
Please refer to [http-docsapi-crud-basic.md](../../../../../adapter-http/src/main/resources/activities/documents-api/http-docsapi-crud-basic.md) for the general workflow design details.

## Indexing

To simulate a realistic situation as much as possible, this workflow creates 3 additional indexes (apart from `_id`) for the collection where documents are stored.
