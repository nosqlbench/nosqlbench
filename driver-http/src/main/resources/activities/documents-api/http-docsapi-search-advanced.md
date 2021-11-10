---
title: Documents API Search Advanced
weight: 2
---

## Description

The Documents API Search Advanced workflow targets Stargate's Documents API using generated JSON documents. Specifically, it looks to benchmark more advanced search cases, using both complex boolean logic and filters that aren't natively supported by the underlying data store.
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

The advanced search workload can test the following `where` clauses:
- in: match1 IN [0]
- not-in: match2 NOT IN ["false"]
- mem-and: match2 EQ "true" AND match3 NOT EQ false
- mem-or: match1 LT 1 OR match3 EXISTS
- complex1: match1 EQ 0 AND (match2 EQ "true" OR match3 EQ false)
- complex2: (match1 LTE 0 OR match2 EQ "false") AND (match2 EQ "false" OR match3 EQ true)
- complex3: (match1 LTE 0 AND match2 EQ "true") OR (match2 EQ "false" AND match3 EQ true)

## Workload Parameters

- `docscount` - the number of documents to write during rampup (default: `10_000_000`)
- `docpadding` - the number of fields to add to each document; useful for writing larger documents. A value of e.g. `5` would make each document have 20 leaf values, instead of 15. (default: `0`)
- `match-ratio` - a value between 0 and 1 detailing what ratio of the documents written should match the search parameters. If match-ratio is e.g. `0.1` then approximately one-tenth of the documents will have `match1`, `match2`, and `match3` values that are `0`, `"true"`, and `true`, respectively. (default: `0.01`)


