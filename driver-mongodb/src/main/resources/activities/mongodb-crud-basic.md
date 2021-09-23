---
title: mongoDB CRUD Basic
weight: 1
---

## Description

The mongoDB CRUD Basic workflow emulates CRUD operations for the mongoDB using generated JSON documents.
It's a counterpart of the Stargate's Documents API CRUD Basic workflow.
Please refer to [http-docsapi-crud-basic.md](../../../../../driver-http/src/main/resources/activities/documents-api/http-docsapi-crud-basic.md) for the general workflow design details.

## Indexing

To simulate a realistic situation as much as possible, this workflow creates 3 additional indexes (apart from `_id`) for the collection where documents are stored.
