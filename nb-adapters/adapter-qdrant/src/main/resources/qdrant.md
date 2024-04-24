# qdrant driver adapter

The qdrant driver adapter is a nb adapter for the qdrant driver, an open source Java driver for connecting to and
performing operations on an instance of a Qdrant Vector database. The driver is hosted on GitHub at
https://github.com/qdrant/java-client.

## activity parameters

The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the [Qdrant database](https://qdrant.tech/documentation):

* `token` - In order to use the Qdrant database you must have an account. Once the account is created you can [request
  an api key/token](https://qdrant.tech/documentation/cloud/authentication/). This key will need to be provided any
  time a database connection is desired. Alternatively, the api key can be stored in a file securely and referenced via
  the `token_file` config option pointing to the path of the file.
* `uri` - When a collection/index is created in the database the URI (aka endpoint) must be specified as well. The adapter will
  use the default value of `localhost:6334` if none is provided at runtime. Remember to *not* provide the `https://`
  suffix.
* `grpc_port` - the GRPC port used by the Qdrant database. Defaults to `6334`.
* `use_tls` - option to leverage TLS for the connection. Defaults to `true`.
* `timeout_ms` - sets the timeout in milliseconds for all requests. Defaults to `3000`ms.

## Op Templates

The Qdrant adapter supports [**all operations**](../java/io/nosqlbench/adapter/qdrant/ops) supported by the Java
driver published by Qdrant. The official Qdrant API reference can be found at
https://qdrant.github.io/java-client/io/qdrant/client/package-summary.html

The operations include a full-fledged support for all the APIs available in the Qdrant Java driver.
The following are a couple high level API operations.

# TODO - Below needs to be updated post driver development.
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
    description: "https://qdrant.io/api-reference/java/v2.3.x/Collection/createCollection().md"
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

  # https://qdrant.io/api-reference/java/v2.3.x/Index/dropIndex().md
  example_drop_index:
    drop_index: "exampe_collection_idx_name"
    database_name: "my_database"
    collection_name: "example_collection""

  # https://qdrant.io/api-reference/java/v2.3.x/Collection/dropCollection().md
  example_drop_collection:
    drop_collection: "example_collection"
    database_name: "my_database"

  # https://qdrant.io/api-reference/java/v2.3.x/High-level%20API/insert().md
  example_insert_op:
    insert: "example_collection_name"
    rows:
      field1: "row_key"
      field2: "[1.2, 3.4, 5.6]"

  # https://qdrant.io/api-reference/java/v2.3.x/High-level%20API/search().md
  # https://qdrant.io/api-reference/java/v2.3.x/Query%20and%20Search/search().md
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
