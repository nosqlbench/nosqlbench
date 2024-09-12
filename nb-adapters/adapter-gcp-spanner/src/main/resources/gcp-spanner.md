# Google Spanner driver adapter
The Azure AI Search driver adapter is a NoSQLBench adapter for the `azure-aisearch` driver, a Java driver
for connecting to and performing operations on an instance of a Azure AI Search vector database. The driver is
leveraged from GitHub at https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/azure-search-documents/.

## Run Commands (Remove prior to merge)

### Create Collection Schema
```
java -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar weaviate_vector_live weaviate_vectors.rampup dimensions=25 testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=Glove_25 weaviatehost=letsweave-czgwdrw9.weaviate.network token_file=${workspace_loc:/nosqlbench}/local/weaviate/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:weaviate_1255,instance:vectors,vendor:weaviate_wcd" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### Delete Collection
```
java -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar azure_aisearch_vectors_live azure_aisearch_vectors.delete_index dimensions=25 testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=glove_25 similarity_function=cosine azureaisearchhost=https://stratperf-aisearch-central-india-free-tier.search.windows.net token_file=${workspace_loc:/nosqlbench}/local/azure_aisearch/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:azure_aisearch,instance:vectors,vendor:azure_aisearch" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### List Indexes
```
java --enable-preview -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar azure_aisearch_vectors_live azure_aisearch_vectors.list_indexes dimensions=25 similarity_function=cosine testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=glove_25 azureaisearchhost=https://stratperf-aisearch-central-india-free-tier.search.windows.net token_file=${workspace_loc:/nosqlbench}/local/azure_aisearch/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:azureaisearch,instance:vectors,vendor:azureaisearch" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### Upload Documents
```
java --enable-preview -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar azure_aisearch_vectors_live azure_aisearch_vectors.upload_documents dimensions=25 similarity_function=cosine testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=glove_25 azureaisearchhost=https://stratperf-aisearch-central-india-free-tier.search.windows.net token_file=${workspace_loc:/nosqlbench}/local/azure_aisearch/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:azureaisearch,instance:vectors,vendor:azureaisearch" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### Search Documents
```
java --enable-preview -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar azure_aisearch_vectors_live azure_aisearch_vectors.search_documents dimensions=25 similarity_function=cosine testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=glove_25 azureaisearchhost=https://stratperf-aisearch-central-india-free-tier.search.windows.net token_file=${workspace_loc:/nosqlbench}/local/azure_aisearch/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:azureaisearch,instance:vectors,vendor:azureaisearch" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```


## Activity Parameters

The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the [Azure AI Search database](https://learn.microsoft.com/en-us/rest/api/searchservice/?view=rest-searchservice-2024-07-01):

* `token` - In order to use the Weaviate database you must have an account. Once the account is created you can [request
  an api key/token](https://weaviate.io/developers/wcs/quickstart#explore-the-details-panel). This key will need to be
  provided any time a database connection is desired. Alternatively, the api key can be stored in a file securely and
  referenced via the `token_file` config option pointing to the path of the file.
* `endpoint` - When a collection/index is created in the database the URI (aka endpoint) must be specified as well. The adapter will
  use the default value of `localhost:8080` if none is provided at runtime.
* `api_version` - the api version to be used by the search client. Defaults to the latest service/api version supported
  by the version of client SDK.

## Op Templates

The Azure AI Search adapter supports [**all basic operations**](../java/io/nosqlbench/adapter/azure-aisearch/ops) supported by the [Java
client SDK published by Azure AI Search](https://github.com/weaviate/java-client). The official Azure AI Search API reference can be
found at https://learn.microsoft.com/en-us/rest/api/searchservice/operation-groups?view=rest-searchservice-2024-07-01.

The operations include a full-fledged support for key APIs available in the Java SDK client.
The following are a couple high level API operations.

* Create or Update Index
* Delete Index
* List Indexes
* Upload Documents (vectors)
* (Vector) Search Documents (vectors)

## Examples

Check out the [full example workload available here](./activities/azure_aisearch_vectors_live.yaml).

---
