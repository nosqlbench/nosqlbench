---
source: nb-adapters/adapter-dataapi/src/main/resources/dataapi.md
---

# Data API adapter

This is a NB adapter for the [Data API](https://github.com/stargate/data-api), an HTTP API that can run on top of Cassandra and DataStax Astra DB.

The adapter is built on top of the [Java client](https://github.com/datastax/astra-db-java) for the Data API and exposes the same primitives as the Data API for both schemaless **Collections** and schemaed **Tables** (including User-Defined Types and indexes), in a 1:1 relation between ops and Data API commands wherever possible.

## Command-line parameters

Connection to the Data API for an Astra DB instance requires an **API Endpoint** and an **Application Token**, plus optional specification of a **keyspace** to operate in. Endpoint and token are required parameters when launching a NB workload targeting the Data API:

```
./nb [...] \
    astraToken="..." \                  # required
    astraApiEndpoint="https://..." \    # required
    keyspace="the_keyspace"             # optional ('namespace' is a deprecated alias)
```

These values are used by the adapter to establish a `DataAPIClient`, from which all subsequent operations are spawned.

### Targeting other destinations

The optional `astraDestination` parameter must be used when the target Data API is on a database other than (production) Astra DB.
It accepts values in the Data API client's [DataAPIDestination](https://docs.datastax.com/en/astra-api-docs/_attachments/java-client/com/datastax/astra/client/DataAPIDestination.html) enum and must match the provided endpoint and the token. When omitted, it defaults to `DataAPIDestination.ASTRA` (i.e. production Astra DB).

Example 1 - Astra DB Dev environment:

```
./nb [...] \
    astraToken="AstraCS:..." \
    astraApiEndpoint="https://....apps.astra-dev.datastax.com" \
    keyspace="my_keyspace" \
    astraDestination="ASTRA_DEV"
```

Example 2 - locally-running HCD + Data API setup:

```
./nb [...] \
    astraToken="Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh" \
    astraApiEndpoint="http://localhost:8181" \
    keyspace="my_keyspace" \
    astraDestination="HCD"
```


## Legacy vs. "new-style" operation

In September 2026, a major rework of the ops has been initiated, essentially marking all operations existing so far as "legacy" and introducing a whole new set of ops, coming with more extensible naming conventions (e.g. the legacy `find` op has become `collection_find`). The overhaul has reached its first milestone (collection CRUD and all collection operations).

The legacy operations received bug fixes (plus obvious additive extensions to their behaviour when it made sense to). So, no pre-existing workload will break. However, the user is encouraged to build new workload exclusively using the new-style operations.

For a detailed catalog of the legacy and the new-style operations, with their syntax, consult the "Examples" section below.

### Current status of the overhaul

**Done:**

- All collection DML ops;
- Table support (DML + DDL);
- Create/delete collection DDL;
- Keyspace CRUD;
- Ability to target destinations other than production Astra (e.g. HCD, dev Astra, etc)
- All known bugs in legacy operations are fixed.

**Coming next:**

- Administrative functions on databases (create DB and so on), currently left as they were before the overhaul;
- Ability to supply special Data API-related secrets (such as embedding provider API keys for `$vectorize` operations).

## Supported operations

_Note: items marked as "legacy" must still undergo the in-progress dataapi adapter overhaul. Legacy items with a modern replacement, instead, are not mentioned in the following inventory._

### Collection-related ops

#### Collection CRUD ops

| Op name | Description |
|---|---|
| `db_create_collection` | Create collection |
| `db_delete_collection` | Drop collection |
| `list_collections` | List collections' metadata (legacy) |
| `list_collection_names` | List collection names (legacy) |

#### Collection DML ops

| Op name | Description |
|---|---|
| `collection_insert_one` | Insert a document |
| `collection_insert_many` | Insert documents |
| `collection_find` | Find documents |
| `collection_find_one` | Find first document |
| `collection_find_and_rerank` | Hybrid search with reranking |
| `collection_find_one_and_update` | Find and update document |
| `collection_find_one_and_replace` | Find and replace document |
| `collection_find_one_and_delete` | Find and delete document |
| `collection_update_one` | Update first document |
| `collection_update_many` | Update documents |
| `collection_delete_one` | Delete first document |
| `collection_delete_many` | Delete documents |
| `collection_count_documents` | Count documents |
| `collection_estimated_document_count` | Estimated document count |

### Table-related ops

#### Table CRUD ops

| Op name | Description |
|---|---|
| `db_create_table` | Create table |
| `db_alter_table` | Alter table schema |
| `db_drop_table` | Drop table |
| `db_list_tables` | List tables' metadata |
| `db_list_table_names` | List table names |

#### Table DML ops

| Op name | Description |
|---|---|
| `table_insert_one` | Insert a row |
| `table_insert_many` | Insert rows |
| `table_find` | Find rows |
| `table_find_one` | Find first row |
| `table_update_one` | Update first row |
| `table_delete_one` | Delete first row |
| `table_delete_many` | Delete rows |

#### Table index ops

| Op name | Description |
|---|---|
| `table_create_index` | Create index |
| `table_create_text_index` | Create text index |
| `table_create_vector_index` | Create vector index |
| `table_list_indexes` | List indexes |
| `table_list_index_names` | List index names |
| `db_drop_table_index` | Drop index (a database-level operation)|

#### User-Defined Type (UDT) ops

| Op name | Description |
|---|---|
| `db_create_type` | Create type |
| `db_alter_type` | Alter type |
| `db_drop_type` | Drop type |
| `db_list_types` | List types |
| `db_list_type_names` | List type names |

### Admin/DatabaseAdmin ops

| Op name | Description |
|---|---|
| `dbadmin_create_keyspace` | Create keyspace |
| `dbadmin_list_keyspaces` | List keyspaces |
| `dbadmin_drop_keyspace` | Drop keyspace |
| `create_database` | Create database (legacy) |
| `drop_database` | Drop database (legacy) |
| `list_databases` | List databases (legacy) |
| `get_database_info` | Get database info (legacy) |

## Usage notes (new-style ops)

Most commands support a free-form syntax for some of their parameters, such as the collection definition for `db_create_collection` or the insertee document in `collection_insert_one`. This is to give the most flexibility and future-proofing the op in case new syntax is added by the Data API.

In other cases, when it was deemed more convenient, the syntax is more constrained. All specifications are given in the supporting example op YAMLs under `activities/`.

### Vectors

Many commands allow to supply **vectors**: these can be expressed in the YAML:

- literally, as lists of (floating-point) numbers: `[0.1,-3.2,1.9]`;
- literally, as a string containing comma-separated floating points: `"0.1,-3.2,1.9"`;
- via an appropriate binding, e.g. `"{the_vector_binding}"`.

In any case, whenever the API Client is equipped to do so, vectors are transmitted in the bandwidth-optimized binary-encoded form regardless of how they are given. One _should not_ manually construct the binary-encoding syntax (e.g. as part of an insertee document for a `collection_insert_one` op).

### Batched insert many

The `collection_insert_many` and `table_insert_many` ops allow two types of specification for the documents to insert: explicit list and batched. The latter means that a batch size and a "document template" is supplied, and the op resolves them into a list of `batch_size` documents, obtained by cycling through the bindings (hence consuming as many values for each binding). In other words, the pace at which these binding get consumed is increased `batch_size`-fold. See the full reference linked below for the syntax. _It is suggested to reserve such bindings for usage by a batched insert-many only, to avoid possible overlapping binding evaluations._

### Supported table column types (intentional limitation)

Row values are limited to the types YAML can express natively: strings, numbers and booleans.

Dedicated support for the less common table data types (e.g. `inet`, `duration`, `blob`, `timestamp` and `uuid`) is **deliberately not provided**. Workloads that depend on those types cannot be generally constructed.

## Client vs. 'raw Data API payloads'

An important virtue of the Java Data API client is that it handles headers, authentication, encoding/decoding of payloads and responses - with minimal overhead.

On the other hand, the client builds higher-level convenience features on top of the raw HTTP requests underlying the API: so, for example, what for the client is a single 200-item `insertMany(...)` invocation translates to a certain number of "chunked" Data API requests, each one inserting some of the items and executed with a degree of (client-managed) concurrency that depends on various settings. _This is a kind of sophistication that a typical usage of NB would not want_.

For this reason, the adapter strives to avoid patterns that would trigger such "too complex" behaviours: for instance, the client concurrency for multiple insertions is always hardcoded to one, leaving management of concurrent requests purely on the NB side.

It is, however, an ultimate responsibility of the Data API-savvy NB user to ensure full compliance with this tenet, for example never packing more than a pageful of items into a `collection_insert_many` operation's `documents` property.

## Code organisation

All table-related op dispensers live under the `opdispensers/tables/` subdir and their corresponding op implementations under `ops/tables/`. Likewise, collection ops live in the `opdispensers/collections/` and `ops/collections/` subdirs to keep the codebase clean and modular.

## Examples

Consult the self-documenting YAML examples here:

- [new-style ops full reference](activities/ops_reference.yaml);
    - ([legacy ops full reference](activities/legacy_ops_reference.yaml)).
- a simple [minimal running workload](activities/example_workload.yaml);
    - (a simple [legacy minimal running workload](activities/example_workload_legacy.yaml));
- a realistic, [ready-to-run collection workload](activities/data_api_collection_workload.yaml) demonstrating several ops;
- a comprehensive [ready-to-run table workload](activities/data_api_table_workload.yaml) covering all table ops;
