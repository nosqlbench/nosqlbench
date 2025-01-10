---
source: nb-adapters/adapter-qdrant/src/main/resources/qdrant.md
---

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

The Qdrant adapter supports [**all operations**](../java/io/nosqlbench/adapter/qdrant/ops) supported by the [Java
driver published by Qdrant](https://github.com/qdrant/java-client). The official Qdrant API reference can be found at
https://qdrant.github.io/java-client/io/qdrant/client/package-summary.html

The operations include a full-fledged support for key APIs available in the Qdrant Java driver.
The following are a couple high level API operations.

* Create Collection
* Count Points
* Drop Collection
* Search Points (vectors)
* Create Payload Index
* List Collections
* List Collection Aliases
* List Snapshots
* Collection Info
* Collection Exists

## Examples

Check out the [full example available here](activities/qdrant_vectors_live.yaml).

---
