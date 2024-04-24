---
title: Documents API CRUD using an external Dataset
weight: 3
---

## Description

The Documents API CRUD Dataset workflow targets Stargate's Documents API using JSON documents from an external dataset.
The [dataset](#dataset) is mandatory and should contain a JSON document per row that should be used as the input for write and update operations.
This workflow is perfect for testing Stargate performance using your own JSON dataset or any other realistic dataset.

In contrast to other workflows, this one is not split into ramp-up and main phases.
Instead, there is only the main phase with 4 different load types (write, read, update and delete).

## Named Scenarios

### default

The default scenario for http-docsapi-crud-dataset.yaml runs each type of the main phase sequentially: write, read, update and delete.
This means that setting cycles for each of the phases should be done using the: `write-cycles`, `read-cycles`, `update-cycles` and `delete-cycles`.
The default value for all 4 cycles variables is the amount of documents to process (see [Workload Parameters](#workload-parameters)).

Note that error handling is set to `errors=timer,warn`, which means that in case of HTTP errors the scenario is not stopped.

## Dataset

### JSON Documents

As explained above, in order to run the workflow a file containing JSON documents is needed.
If you don't have a dataset at hand, please have a look at [awesome-json-datasets](https://github.com/jdorfman/awesome-json-datasets).
You can use exposed public APIs to create a realistic dataset of your choice.

For example, you can easily create a dataset containing [Bitcoin unconfirmed transactions](https://gist.github.com/ivansenic/e280a89aba6420acb4f587d3779af774).

```bash
curl 'https://blockchain.info/unconfirmed-transactions?format=json&limit=5000' | jq -c '.txs | .[]' > blockchain-unconfirmed-transactions.json
```

Above command creates a dataset with 5.000 latest unconfirmed transactions.

## Workload Parameters

- `docscount` - the number of documents to process in each step of a scenario (default: `10_000_000`)
- `dataset_file` - the file to read the JSON documents from (note that if number of documents in a file is smaller than the `docscount` parameter, the documents will be reused)
