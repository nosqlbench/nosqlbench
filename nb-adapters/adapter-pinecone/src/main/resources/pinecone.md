# pinecone driver adapter
The pinecone driver adapter is a nb adapter for the pinecone driver, an open source Java driver for connecting to and
performing operations on an instance of a Pinecone Vector database. The driver is hosted on github at
https://github.com/pinecone-io/pinecone-java-client

## activity parameters
The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the Pinecone database:

* api key - In order to use the pinecone database you must have an account. Once the account is created you can [request
            an api key](https://docs.pinecone.io/docs/quickstart#2-get-your-api-key). This key will need to be provided any time a database connection is desired.
* environment - When an Index is created in the database the environment must be specified as well. The adapter will
                use the default value of us-east-1 if none is provided at runtime.
* project name - A project name must also be provided at time of index creation. This name then needs to be provided
                 to the adapter at runtime.

## Op Templates

The Pinecone adapter supports all operations supported by the Java driver published by Pinecone.
The official Pinecone API reference can be found at
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
  # A pinecone query op
  query-example:
    type: query
    index: query_index
    # The query vector. Use these fields if only querying a single vector. If querying multiple use the
    # query_vectors structure below.
    vector: my_array_of_floats
    namespace: query_namespace
    # The number of results to return for each query.
    top_k: int_query_topk
    # You can use vector metadata to limit your search. See https://www.pinecone.io/docs/metadata-filtering/
    filter:
      filterfield: metadata_field
      operator: [$lt, $eq, $gt, ...]
      comparator: value
    # Indicates whether vector values are included in the response.
    include_values: boolean
    # Indicates whether metadata is included in the response as well as the ids.
    include_metadata: boolean
    query_vectors:
      - id: 1
        values: csv_separated_floats
        top_k: int_val
        namespace: string_val
        filter:
          filterfield: metadata_field
          operator: [$lt, $eq, $gt, ...]
          comparator: value
        sparse_values:
          indices: list_of_ints
          values: list_of_floats
      - id: 2
        values: csv_separated_floats
        top_k: int_val
        namespace: string_val
        filter:
          filterfield: metadata_field
          operator: [$lt, $eq, $gt, ...]
          comparator: value
        sparse_values:
          indices: list_of_ints
          values: list_of_floats

# A delete op
# If specified, the metadata filter here will be used to select the vectors to delete. This is mutually exclusive
# with specifying ids to delete in the ids param or using delete_all=True. delete_all indicates that all vectors
# in the index namespace should be deleted.
  delete-example:
    type: delete
    index: delete_index
    namespace: delete_namespace
    ids: csv_list_of_vectors_to_delete
    deleteall: [true,false]
    filter:
      filterfield: metadata_field
      operator: [$lt, $eq, $gt, ...]
      comparator: value

# A describe index stats op. Specify metadata filters to narrow the range of indices described.
  describe-index-stats-example:
    type: describe-index-stats
    index: describe_index
    filter:
      filterfield: metadata_field
      operator: [$lt, $eq, $gt, ...]
      comparator: value

# A pinecone fetch op
  fetch-example:
    fetch: fetch_index
    namespace: fetch_namespace
    ids: csv_list_of_vectors_to_fetch

# A pinecone update op
  update-example:
    type: update
    index: update_index
    id: string_id
    values: list_of_floats
    namespace: update_namespace
    metadata:
      key1: val1
      key2: val2
      key3: val3
    sparse_values:
      indices: list_of_ints
      values: list_of_floats

# A pinecone upsert op
  upsert-example:
    type: upsert
    index: upsert_index
    namespace: upsert_namespace
    upsert_vectors:
      - id: 1
        values: csv_separated_floats
        sparse_values:
          indices: list_of_ints
          values: list_of_floats
        metadata:
          key1: val1
          key2: val2
      - id: 2
        values: csv_separated_floats
        sparse_values:
          indices: list_of_ints
          values: list_of_floats


```
