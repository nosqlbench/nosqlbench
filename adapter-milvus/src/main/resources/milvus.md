# milvus driver adapter
The milvus driver adapter is a nb adapter for the milvus driver, an open source Java driver for connecting to and
performing operations on an instance of a Milvus/Zilliz Vector database. The driver is hosted on github at
https://github.com/milvus-io/milvus-sdk-java

## activity parameters
The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the Milvus/Zilliz database:

* token - In order to use the pinecone database you must have an account. Once the account is created you can [request
            an api key](https://docs.pinecone.io/docs/quickstart#2-get-your-api-key). This key will need to be provided any time a database connection is desired.
* uri - When an Index is created in the database the uri must be specified as well. The adapter will
                use the default value of localhost:19530 if none is provided at runtime.

## Op Templates

The Milvus adapter supports all operations supported by the Java driver published by Milvus.
The official Milvus API reference can be found at
https://docs.pinecone.io/reference/describe_index_stats_post

The operations include:

* Delete
* DescribeIndexStats
* Fetch
* Query
* Update
* Upsert

## Examples
```yaml
ops:
 example_create_collection:
   description: "https://milvus.io/api-reference/java/v2.3.x/Collection/createCollection().md"
   collection_name: "example_collection""
   shards_num: 10
   field_types:
     field1:
       primary_key: true # only for Int64 and Varchar types
       description: field description
       data_type: Varchar
         # Bool, Int8, Int16, Int32, Int64,
         # Float, Double, String, Varchar, BinaryVector, FloatVector
       type_param:
         example_param1: example_pvalue1
       dimension: 1024 # >0
       max_length: 1024 # for String only, >0
       auto_id: false # Generate primary key?
       partition_key: true
   consistency_level: BOUNDED # BOUNDED, SESSION, EVENTUAL
   partition_num: 1024 # number of partitions

```
