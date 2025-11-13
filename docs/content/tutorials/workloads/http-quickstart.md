+++
title = "HTTP/REST Quickstart"
description = "Step-by-step guide to HTTP/REST API testing with NoSQLBench and Stargate"
weight = 20
template = "page.html"
date = 2023-04-07

[extra]
quadrant = "tutorials"
topic = "workloads"
category = "http"
tags = ["http", "rest", "api", "stargate", "quickstart"]
testable = true
author = "Jeff Banks (DataStax)"
+++

# HTTP/REST Quickstart with Stargate

Learn HTTP/REST API testing with NoSQLBench using the Stargate data gateway.

**Previous tutorial:** If you haven't completed the [CQL Quickstart](cql-quickstart.md), start there for foundational concepts.

## What You'll Learn

- Running NoSQLBench with the HTTP driver
- Testing REST APIs with Stargate data gateway
- HTTP methods: GET, POST, DELETE
- Authentication with token generation
- HTTP-specific workload patterns

## Prerequisites

- Docker installed ([download](https://www.docker.com/))
- NoSQLBench v5.17.3 or later ([latest release](https://github.com/nosqlbench/nosqlbench/releases/latest))
- Basic understanding of REST APIs

## Setup

### 1. Get NoSQLBench

```bash
curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5
chmod +x nb5
./nb5 --version
```

### 2. Clone Stargate

[Stargate](https://stargate.io/) is an open-source data gateway for Cassandra.

```bash
git clone https://github.com/stargate/stargate.git
cd stargate/docker-compose/cassandra-4.0/
```

### 3. Start Stargate Services

```bash
./start_cass_40_dev_mode.sh
```

**Verify services are healthy:**

```bash
docker ps
```

Expected output (all services should show "healthy"):
```
stargateio/graphqlapi:v2   Up (healthy)
stargateio/restapi:v2      Up (healthy)
stargateio/docsapi:v2      Up (healthy)
stargateio/coordinator-4_0 Up (healthy)
```

## Running the Workload

### 1. Verify Workload Availability

```bash
./nb5 --list-workloads | grep http-rest-starter
```

Expected: `/activities/baselines/http-rest-starter.yaml`

### 2. Run the Scenario

```bash
./nb5 activities/baselines/http-rest-starter.yaml default stargate_host=localhost
```

This workload:
1. Creates a keyspace via REST API
2. Drops existing table (if any)
3. Creates a new table
4. Loads rampup data (10 records)
5. Runs mixed read/write operations (10 cycles each)

## Understanding the HTTP Workload

### Workload Structure

```yaml
scenarios:
  default:
    schema: run driver=http tags==block:"schema.*" threads==1 cycles==UNDEF
    rampup: run driver=http tags==block:"rampup.*" cycles===10 threads=auto
    main: run driver=http tags==block:"main.*" cycles===10 threads=auto

bindings:
  request_id: ToHashedUUID(); ToString();
  token: Discard(); Token('<<auth_token:>>',
    '<<auth_uri:http://localhost:8081/v1/auth>>',
    '<<auth_uid:cassandra>>',
    '<<auth_pswd:cassandra>>');
  seq_key: Mod(10000000); ToString() -> String
  seq_value: Hash(); Mod(1000000000); ToString() -> String
  rw_key: Uniform(0,10000000)->int; ToString() -> String
  rw_value: Hash(); Uniform(0,1000000000)->int; ToString() -> String
```

### Key Bindings

- **request_id** - Unique ID for each HTTP request
- **token** - Auto-generated Stargate auth token using `Token()` function
  - If `auth_token` is provided, uses it directly
  - Otherwise generates token via `auth_uri` with `auth_uid`/`auth_pswd`
- **seq_key/seq_value** - Sequential data for rampup
- **rw_key/rw_value** - Random data for read/write operations

### HTTP Operations

#### Create Keyspace (POST)

```yaml
create-keyspace:
  method: POST
  uri: http://<<stargate_host>>:8082/v2/schemas/keyspaces
  Accept: "application/json"
  X-Cassandra-Request-Id: "{request_id}"
  X-Cassandra-Token: "{token}"
  Content-Type: "application/json"
  body: >2
    {
      "name": "starter",
      "replicas": 1
    }
```

#### Delete Table (DELETE)

```yaml
drop-table:
  method: DELETE
  uri: http://<<stargate_host>>:8082/v2/schemas/keyspaces/starter/tables/http_rest_starter
  X-Cassandra-Request-Id: "{request_id}"
  X-Cassandra-Token: "{token}"
  ok-status: "[2-4][0-9][0-9]"  # Accept 2xx, 3xx, 4xx status codes
```

#### Create Table (POST)

```yaml
create-table:
  method: POST
  uri: http://<<stargate_host>>:8082/v2/schemas/keyspaces/starter/tables
  X-Cassandra-Token: "{token}"
  body: >2
    {
      "name": "http_rest_starter",
      "columnDefinitions": [
        {"name": "key", "typeDefinition": "text"},
        {"name": "value", "typeDefinition": "text"}
      ],
      "primaryKey": {
        "partitionKey": ["key"]
      },
      "ifNotExists": true
    }
```

#### Read Data (GET)

```yaml
main-select:
  method: GET
  uri: http://<<stargate_host>>:8082/v2/keyspaces/starter/http_rest_starter/{rw_key}
  X-Cassandra-Token: "{token}"
  ok-status: "[2-4][0-9][0-9]"
```

#### Write Data (POST)

```yaml
main-write:
  method: POST
  uri: http://<<stargate_host>>:8082/v2/keyspaces/starter/http_rest_starter
  X-Cassandra-Token: "{token}"
  body: >2
    {
      "key": "{rw_key}",
      "value": "{rw_value}"
    }
```

### HTTP Methods Used

- **POST** - Create resources (keyspace, table, records)
- **GET** - Read data
- **DELETE** - Remove resources

### Table Schema

Simple key-value table:
- **key** (text) - Partition key
- **value** (text) - Data value

## Examining Results

View Stargate logs:

```bash
docker container exec -it cass40-stargate-coordinator-1 sh
cd /stargate/log
tail -100 system.log
```

You'll see the HTTP operations executed during the workload run.

## Customization

### Copy the Workload

```bash
./nb5 --copy http-rest-starter
```

### Modify Scenarios

Edit `http-rest-starter.yaml` to adjust:
- Cycle counts for larger tests
- Ratios for read/write mix
- URLs for different endpoints
- Request headers

### Scale Up

```bash
./nb5 ./http-rest-starter.yaml default \
  stargate_host=localhost \
  rampup-cycles=100000 \
  main-cycles=1000000
```

## Next Steps

- **[HTTP Driver Reference](../../reference/drivers/http.md)** - Complete HTTP driver documentation
- **[Workload Design](../../guides/workload-design/)** - Create custom HTTP workloads
- **[Binding Functions](../../reference/bindings/)** - Advanced data generation
- **More Examples** - Explore Stargate examples:
  - Documents API
  - GraphQL (CQL-first)
  - GraphQL (Schema-first)

## Related Documentation

- **[CQL Quickstart](cql-quickstart.md)** - Compare with CQL driver approach
- **[HTTP Driver](../../reference/drivers/http.md)** - HTTP driver options and features
- **[Stargate Documentation](https://stargate.io/docs/)** - Learn more about Stargate

---

*This tutorial demonstrates HTTP/REST testing using NoSQLBench with the Stargate data gateway. For direct database testing, see the [CQL Quickstart](cql-quickstart.md).*
