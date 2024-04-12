# milvus driver adapter

The milvus driver adapter is a nb adapter for the milvus driver, an open source Java driver for connecting to and
performing operations on an instance of a Milvus/Zilliz Vector database. The driver is hosted on github at
https://github.com/milvus-io/milvus-sdk-java.

## activity parameters

The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the Milvus/Zilliz database:

* token - In order to use the pinecone database you must have an account. Once the account is created you can [request
  an api key/token](https://milvus.io/docs/users_and_roles.md#Users-and-Roles). This key will need to be provided any
  time a
  database connection is desired.
* uri - When an Index is created in the database the uri must be specified as well. The adapter will
  use the default value of localhost:19530 if none is provided at runtime.

## Op Templates

The Milvus adapter supports [**all operations**](../java/io/nosqlbench/adapter/milvus/ops) supported by the Java
driver
published by Milvus.
The official Milvus API reference can be found at
https://milvus.io/api-reference/java/v2.3.x/About.md

The operations include a full-fledged support for all the APIs available in the Milvus Java driver.
The following are a couple high level API operations.

* Create Collection
* Create Index
* Drop Collection
* Drop Index
* Search (vectors)

## Examples

```yaml
ops:
  example_create_collection:
    create_collection: "example_collection"
    description: "https://milvus.io/api-reference/java/v2.3.x/Collection/createCollection().md"
    collection_name: "example_collection"
    shards_num: 10
    consistency_level: BOUNDED # BOUNDED, SESSION, EVENTUAL
    field_types:
      field1:
        primary_key: true # only for Int64 and VarChar types
        description: "field description"
        data_type: "Varchar"
        # Bool, Int8, Int16, Int32, Int64,
        # Float, Double, String, Varchar, BinaryVector, FloatVector
        type_param:
          example_param1: example_pvalue1
        dimension: 1024 # >0
        max_length: 1024 # for String only, >0
        auto_id: false # Generate primary key?
        partition_key: false # Primary key cannot be the partition key too
      field2:
        primary_key: false
        description: "vector column/field"
        data_type: "FloatVector"
        dimension: 3

  # https://milvus.io/api-reference/java/v2.3.x/Index/dropIndex().md
  example_drop_index:
    drop_index: "exampe_collection_idx_name"
    database_name: "my_database"
    collection_name: "example_collection""

  # https://milvus.io/api-reference/java/v2.3.x/Collection/dropCollection().md
  example_drop_collection:
    drop_collection: "example_collection"
    database_name: "my_database"

  # https://milvus.io/api-reference/java/v2.3.x/High-level%20API/insert().md
  example_insert_op:
    insert: "example_collection_name"
    rows:
      field1: "row_key"
      field2: "[1.2, 3.4, 5.6]"

  # https://milvus.io/api-reference/java/v2.3.x/High-level%20API/search().md
  # https://milvus.io/api-reference/java/v2.3.x/Query%20and%20Search/search().md
  example_search:
    search: "example_collection"
    vector: "[-0.4, 0.3, 0.99]"
    metric_type: "COSINE"
    out_fields:
      - field1
      - field2
    vector_field_name: "field2"
    top_k: 100
    consistency_level: "EVENTUALLY"
```
