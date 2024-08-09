# Weaviate driver adapter

The Weaviate driver adapter is a NoSQLBench adapter for the `weaviate` driver, an open-source Java driver
for connecting to and performing operations on an instance of a Weaviate vector database. The driver is 
leveraged from GitHub at https://github.com/weaviate/java-client.

## Run Commands (Remove prior to merge)

### Create Collection Schema
```
java -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar weaviate_vector_live weaviate_vectors.rampup dimensions=25 testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=Glove_25 weaviatehost=letsweave-czgwdrw9.weaviate.network token_file=${workspace_loc:/nosqlbench}/local/weaviate/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:weaviate_1255,instance:vectors,vendor:weaviate_wcd" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### Delete Collection
```
java -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar weaviate_vector_live weaviate_vectors.delete_collection dimensions=25 testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=Glove_25 weaviatehost=letsweave-czgwdrw9.weaviate.network token_file=${workspace_loc:/nosqlbench}/local/weaviate/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:weaviate_1255,instance:vectors,vendor:weaviate_wcd" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### Get all schema
```
java -jar ${workspace_loc:/nosqlbench}/nb5/target/nb5.jar weaviate_vector_live weaviate_vectors.get_collection_schema dimensions=25 testsize=10000 trainsize=1183514 dataset=glove-25-angular filetype=hdf5 collection=Glove_25 weaviatehost=letsweave-czgwdrw9.weaviate.network token_file=${workspace_loc:/nosqlbench}/local/weaviate/apikey --progress console:1s -v --add-labels "dimensions:25,dataset=glove-25" --add-labels="target:weaviate_1255,instance:vectors,vendor:weaviate_wcd" --report-prompush-to https://vector-perf.feat.apps.paas.datastax.com:8427/api/v1/import/prometheus/metrics/job/nosqlbench/instance/vectors --annotators "[{'type':'log','level':'info'},{'type':'grafana','baseurl':'https://vector-perf.feat.apps.paas.datastax.com/'}]" --report-interval 10 --show-stacktraces --logs-max 5
```

### Insert objects
```
TODO
```

### Read objects
```
TODO
```

### TODO - Work on the below

## activity parameters

The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the [Weaviate database](https://weaviate.io/developers/weaviate):

* `token` - In order to use the Weaviate database you must have an account. Once the account is created you can [request
  an api key/token](https://weaviate.io/developers/wcs/quickstart#explore-the-details-panel). This key will need to be 
  provided any time a database connection is desired. Alternatively, the api key can be stored in a file securely and 
  referenced via the `token_file` config option pointing to the path of the file.
* `uri` - When a collection/index is created in the database the URI (aka endpoint) must be specified as well. The adapter will
  use the default value of `localhost:8080` if none is provided at runtime. Remember to *not* provide the `https://`
  suffix.
* `scheme` - the scheme database. Defaults to `http`.

## Op Templates

The Weaviate adapter supports [**all basic operations**](../java/io/nosqlbench/adapter/weaviate/ops) supported by the [Java
driver published by Weaviate](https://github.com/weaviate/java-client). The official Weaviate API reference can be found at
https://weaviate.io/developers/weaviate/api/rest.

The operations include a full-fledged support for key APIs available in the Weaviate Java driver.
The following are a couple high level API operations.

* Create Collection
* Delete Collection
* Get Entire Schema
* Create Objects (vectors)
* Read Objects (vectors)

## Examples

Check out the [full example available here](./activities/weaviate_vectors_live.yaml).

---
