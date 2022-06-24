---
title: Documents API CRUD Basic
weight: 2
---

## Description

The Documents API CRUD Basic workflow targets Stargate's Documents API using generated JSON documents.
The documents used are sharing the same structure and are approximately half a kilobyte in size each:

* each document has 13 leaf values, with a maximum depth of 3
* there is at least one `string`, `boolean`, `number` and `null` leaf
* there is one array with `double` values and one with `string` values
* there is one empty array and one empty map

The example JSON looks like:

```json
{
  "user_id":"56fd76f6-081d-401a-85eb-b1d9e5bba058",
  "created_on":1476743286,
  "gender":"F",
  "full_name":"Andrew Daniels",
  "married":true,
  "address":{
    "primary":{
      "cc":"IO",
      "city":"Okmulgee"
    },
    "secondary":{

    }
  },
  "coordinates":[
    64.65964627052323,
    -122.35334535072856
  ],
  "children":[

  ],
  "friends":[
    "3df498b1-9568-4584-96fd-76f6081da01a"
  ],
  "debt":null
}
```

In contrast to other workflows, this one is not split into ramp-up and main phases.
Instead, there is only the main phase with 4 different load types (write, read, update and delete).

## Named Scenarios

### default

The default scenario for http-docsapi-crud-basic.yaml runs each type of the main phase sequentially: write, read, update and delete.
This means that setting cycles for each of the phases should be done using the: `write-cycles`, `read-cycles`, `update-cycles` and `delete-cycles`.
The default value for all 4 cycles variables is the amount of documents to process (see [Workload Parameters](#workload-parameters)).

Note that error handling is set to `errors=timer,warn`, which means that in case of HTTP errors the scenario is not stopped.

## Workload Parameters

- `docscount` - the number of documents to process in each step of a scenario (default: `10_000_000`)

Note that if number of documents is higher than `read-cycles` you would experience misses, which will result in `HTTP 404` and smaller latencies.


