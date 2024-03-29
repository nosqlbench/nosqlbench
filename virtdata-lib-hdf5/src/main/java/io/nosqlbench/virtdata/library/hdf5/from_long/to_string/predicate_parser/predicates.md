# Description

The predicates parser is a way of adding predicates/filters/lmiting expressions to the vector queries generated by the bindings used in nb tests of various vector databases. The predicates will be stored as a dataset within an hdf5 file, which supports "file system like" data storage such as this. The possibility for expanding to either other data sources or other storage formats exists for the future but the initial implementation will be json within hdf5.

# Usage
Usage will be adapter dependent, as the bindings necessary to support different adapter types will vary.

## CQL
ops:
  select_ann_limit:
    raw: |
      SELECT * FROM TEMPLATE(keyspace,baselines).TEMPLATE(table,vectors) {query_predicates} ORDER BY value ANN OF {test_floatlist} LIMIT TEMPLATE(select_limit,100);

query_predicates: HdfDatasetToCqlPredicates("testdata/TEMPLATE(dataset).hdf5", "/predicates", "defaultCql")

In this case the parser needs to return a string representation of the predicates that limit the result set returned by the query using the expected cql syntax.

## Pinecone filter
op1:
  
  query: "test-index"
  
  vector: "0.8602578079921012,0.12103044768221516,0.7737329191858439,0.4521093269320254,0.29351661477669416,0.4261807015226558,0.14131665592103335,0.882370813029422,0.4412833140430886,0.9916525700115515"
  
  namespace: "example_namespace"
  
  top_k: 10
  
  include_values: true
  
  include_metadata: true
  
  filter:
  
    filterfield: {filterfield_predicate}
  
    operator: {operator_predicate}
  
    comparator: {comparator_predicate}

filterfield_predicate: HdfDatasetToPcFilterPredicates("testdata/TEMPLATE(dataset).hdf5", "/predicates", "pcfilter", "field")

operator_predicate: HdfDatasetToPcFilterPredicates("testdata/TEMPLATE(dataset).hdf5", "/predicates", "pcfilter", "operator")

comparator_predicate: HdfDatasetToPcFilterPredicates("testdata/TEMPLATE(dataset).hdf5", "/predicates", "pcfilter", "comparator")

The same parser instance should be used across the three predicate portions with the desired field an additional parameter in order to avoid having to reparse for each binding.

