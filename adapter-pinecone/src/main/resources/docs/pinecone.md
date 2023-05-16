# pinecone driver

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
    filters:
      - filter_field: query_filter_field
        operator: [$lt, $gt, $eq, ...]
        comparator: query_compval
      - filter_field: query_filter_field
        operator: [$lt, $gt, $eq, ...]
        comparator: query_compval
    # Indicates whether vector values are included in the response.
    include_values: boolean
    # Indicates whether metadata is included in the response as well as the ids.
    include_metadata: boolean
    query_vectors:
      - id: 1
        values: csv_separated_floats
        top_k: int_val
        namespace: string_val
        filters:
          - filter_field: query_vector_filter_field
            operator: comparison_operator
            comparator: comparator_val
          - filter_field: query_filter_field
            operator: [$lt, $gt, $eq, ...]
            comparator: query_compval
        sparse_values:
          indices: list_of_ints
          values: list_of_floats
      - id: 2
        values: csv_separated_floats
        top_k: int_val
        namespace: string_val
        filters:
          - filter_field: query_vector_filter_field
            operator: comparison_operator
            comparator: comparator_val
          - filter_field: query_vector_filter_field
            operator: comparison_operator
            comparator: comparator_val
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
    filter: <field operator compval>

# A describe index stats op. Specify metadata filters to narrow the range of indices described.
  describe-index-stats-example:
    type: describe-index-stats
    index: describe_index
    filter: <field operator compval>

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
    set_metadata:
      - key1: val1
      - key2: val2
      - key3: val3
    sparse_values:
      - sparse_id: 1
        indices: list_of_ints
        values: list_of_floats
      - sparse_id: 2
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
        top_k: int_val
        namespace: string_val
        filter:
          filter_field: query_vector_filter_field
          operator: comparison_operator
          comparator: comparator_val
        sparse_values:
          indices: list_of_ints
          values: list_of_floats
      - id: 2
        values: csv_separated_floats
        top_k: int_val
        namespace: string_val
        filter:
          filter_field: query_vector_filter_field
          operator: comparison_operator
          comparator: comparator_val
        sparse_values:
          indices: list_of_ints
          values: list_of_floats


```
