---
title: mongoDB CRUD Dataset
weight: 2
---

## Description

The mongoDB CRUD Dataset workflow emulates CRUD operations for the mongoDB using JSON documents from an external dataset.
It's a counterpart of the Stargate's Documents API CRUD Dataset workflow.
Please refer to [http-docsapi-crud-dataset.md](../../../../../driver-http/src/main/resources/activities/documents-api/http-docsapi-crud-dataset.md) for the general workflow design details.

## Indexing

To simulate a realistic situation as much as possible, this workflow allows creation of the indexes using the parameter:

* `indexes` - Specifies the indexes to create. Each document in the array specifies a separate index. Corresponds to the `indexes` field in the [mongoDB *createIndexes* command](https://docs.mongodb.com/manual/reference/command/createIndexes/#mongodb-dbcommand-dbcmd.createIndexes).

If parameter `indexes` is not specify, a dummy sparse index will be created.
