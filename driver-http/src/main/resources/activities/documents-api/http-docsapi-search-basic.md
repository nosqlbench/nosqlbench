---
title: Documents API Search Basic
weight: 2
---

## Description

The Documents API Search Basic workflow targets Stargate's Documents API using generated JSON documents. Specifically, it looks to benchmark basic search cases, using the filters that are supported by the underlying data store.
By default, the documents used are sharing the same structure and are approximately half a kilobyte in size each:

* Each document has 15 leaf values, with a maximum depth of 3
* there is at least one `string`, `boolean`, `number` and `null` leaf
* there is one array with `double` values and one with `string` values
* there is one empty array and one empty map

The example JSON looks like:

```json
{
  "user_id":"56fd76f6-081d-401a-85eb-b1d9e5bba058",
  "created_on":1476743286,
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
  "debt":null,
  "match1": 0, // or 1000
  "match2": "true", // or "false"
  "match3": true // or false
}
```

## Filters tested

The basic search workload can test the following `where` clauses:
- eq: match3 EQ true
- lt: match1 LT 1
- and: match1 LT 1 AND match2 EQ "true"
- or: match1 LT 1 OR match2 EQ "true" or match3 EQ true
- or-single-match: match1 LT 1 OR match2 EQ "notamatch"

## Workload Parameters

- `docscount` - the number of documents to write during rampup (default: `10_000_000`)
- `docpadding` - the number of fields to add to each document; useful for writing larger documents. A value of e.g. `5` would make each document have 20 leaf values, instead of 15. (default: `0`)
- `match-ratio` - a value between 0 and 1 detailing what ratio of the documents written should match the search parameters. If match-ratio is e.g. `0.1` then approximately one-tenth of the documents will have `match1`, `match2`, and `match3` values that are `0`, `"true"`, and `true`, respectively. (default: `0.01`)
- `fields` - the URL-encoded value for `fields` that you would send to the Docs API. This restricts the fields returned during benchmarking.


