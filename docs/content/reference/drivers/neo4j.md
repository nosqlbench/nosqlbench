+++
title = "neo4j driver adapter"
description = "Auto-generated reference documentation for neo4j driver adapter"
weight = 16075045
template = "page.html"
date = 2025-11-13

[extra]
quadrant = "reference"
topic = "drivers"
tags = ["auto-generated", "driver", "adapter", "database"]
generator = "BundledMarkdownExporter"
source = "https://github.com/nosqlbench/nosqlbench/blob/main/nb-adapters/adapter-neo4j/src/main/resources/neo4j.md"
+++

# neo4j driver adapter

The neo4j driver adapter is a nb adapter for the Neo4J driver, an open source Java driver for connecting to and
performing operations on an instance of a Neo4J/Aura database. The driver is hosted on github at
https://github.com/neo4j/neo4j-java-driver.

## activity parameters

The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the Neo4J/Aura database:

* db_uri - the URI for the Neo4J instance for the driver to connect to.

## Op Templates

The Neo4J adapter supports six different op types:
- sync_autocommit
- async_autocommit
- sync_read_transaction
- async_read_transaction
- sync_write_transaction
- async_write_transaction

A good reference for when to use each is located at https://neo4j.com/docs/driver-manual/1.7/sessions-transactions/

For these different op types, users can specify appropriate Cypher queries to run against the database


## Examples
All examples provided are in the scope of leveraging Neo4J's vector index capabilities. Although,
arbitrary Cypher queries can be run for most involved graph modeling use cases, only a simple
vector search functionality has been properly worked through, currently.


```yaml
ops:
  example_create_vector_index:
    sync_autocommit: |
      CREATE VECTOR INDEX $index_name IF NOT EXISTS FOR (n:TEMPLATE(node_label,Node))
      ON (n.embedding) OPTIONS
      {indexConfig: {`vector.dimensions`: $dimension, `vector.similarity_function`: $similarity_function}}
    query_params:
      index_name: vector_index
      dimension: TEMPLATE(dimension)
      similarity_function: TEMPLATE(similarity_function,cosine)

  example_insert_node:
    async_write_transaction: |
      CREATE (v:TEMPLATE(node_label,Node) {id: $id, embedding: $vector})
    query_params:
      id: '{id}'
      vector: '{train_vector}'

  example_search:
      async_read_transaction: |
        WITH $query_vector AS queryVector
        CALL db.index.vector.queryNodes($index_name, $k, queryVector)
        YIELD node
        RETURN node.id
      query_params:
        query_vector: '{test_vector}'
        index_name: vector_index
        k: TEMPLATE(k,100)
```
