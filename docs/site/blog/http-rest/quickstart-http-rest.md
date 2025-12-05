---
title: HTTP-Rest Starter
description: Learning http-rest using the Stargate date gateway.
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
extra:
  author: Jeff Banks
  published_dt: '2023-04-07'
  thumb: /blog/http-rest/httprestblog-768x461.jpg
---

# Http-Rest-Starter w/ Stargate

<img width="768" height="461" border=3 title="a title" alt="Alt text" src="httprestblog-768x461.jpg">
<p>Author: Jeff Banks (DataStax)</p>
<p>&nbsp;</p>

## Introduction

Welcome to the NoSQLBench Quick Byte, second session in a “Getting Started” series for NoSQLBench. This session 
introduces a new Http-Rest Starter workload now available in version 5 of NoSQLBench.

* If you haven't heard of NoSQLBench, checkout our introduction material [here](https://docs.nosqlbench.io/introduction/).

* If you already have a foundation with NoSQLBench and would like to understand what's included in the most recent 
version, checkout the release notes [here](https://github.com/nosqlbench/nosqlbench/releases).

* If you would like to start with the first session in the NoSQLBench “Getting Started” series, check it out [here](../cql-starter/quickstart-cql.md).

This session illustrates use of http-rest methods, using NoSQLBench v5, along with a Docker deployment of an open source 
data gateway called [Stargate](https://stargate.io/).  
For more information about Stargate, checkout the repository [here](https://github.com/stargate/stargate).

In comparison to the previous [cql-starter](../cql-starter/quickstart-cql.md), this starter focuses on http-rest interactions with the data gateway itself instead 
of via a CQL driver’s interaction with Cassandra.

Let’s get rolling and learn about http-rest operations!


## Setup requirements

This session was tested with:
* Ubuntu (v20.4)
* Docker(v20.10.18)
* Stargate  (v2.0.9)
* NoSQLBench (v5.17.3+)

### Ensure Docker is installed

Download from: [here](https://www.docker.com/)


### Obtain the latest NB5 release

Download from: [here](https://github.com/nosqlbench/nosqlbench/releases)

### Ensure the NB5 binary is accessible

If so, you should be able to see your version installed using:

```
 ./nb5 --version 
 ```

### Clone Stargate locally

Clone from: [here](https://github.com/stargate/stargate)


### Run Stargate services in Docker

Navigate to your local Stargate repository and execute the specified script.

``` 
cd ./stargate/docker-compose/cassandra-4.0/

./start_cass_40_dev_mode.sh
```

Verify the Stargate services are started and healthy.
```
7d0c9076153c stargateio/graphqlapi:v2 "/usr/local/s2i/run" - Up About a minute (healthy)

2757157aa423  stargateio/restapi:v2  "/usr/local/s2i/run" - Up About a minute (healthy)

b0c00f0bdd56   stargateio/docsapi:v2  "/usr/local/s2i/run" - Up About a minute (healthy)

1ab290e89dc6  stargateio/coordinator-4_0:v2  "./starctl" - Up 2 minutes (healthy)
```


## Running the scenario

Now, we are ready to run the http-rest-starter NoSQLBench scenario.

#### Navigate to NB5 binary downloaded & identify workload

```
./nb5 --list-workloads | grep http-rest-starter
```

Example output:
```
/activities/baselines/http-rest-starter.yaml
```

Note: this scenario resides in the adapter-http parent directory for the repository.


#### Optional step
An alternative is to copy the workload configuration listed below to your own local file in a folder of your choosing.  You can name it whatever you like, as you will specify the absolute file path directly when issuing the scenario command.


#### Workload file

This workload file is designed as a basic foundation for continuing to learn NoSQLBench capabilities as well as a starting point for customizing.  You will notice the cycle values are minimal to support local testing.  Adjust as needed for your own usage.

```yaml

min_version: "5.17.3"

description: |
  This starter workload uses an open source data gateway called Stargate,
  which works with a simple key-value data model.
  1. Create a keyspace.
  2. Drop table if existing.
  3. Create table

scenarios:
  default:
    schema: run driver=http tags==block:"schema.*" threads==1 cycles==UNDEF
    rampup: run driver=http tags==block:"rampup.*" cycles===10 threads=auto
    main: run driver=http tags==block:"main.*" cycles===10 threads=auto

bindings:
  request_id: ToHashedUUID(); ToString();
  token: Discard(); Token('<<auth_token:>>','<<auth_uri:http://localhost:8081/v1/auth>>', '<<auth_uid:cassandra>>', '<<auth_pswd:cassandra>>');

  seq_key: Mod(10000000); ToString() -> String
  seq_value: Hash(); Mod(1000000000); ToString() -> String

  rw_key: Uniform(0,10000000)->int; ToString() -> String
  rw_value: Hash(); Uniform(0,1000000000)->int; ToString() -> String


blocks:
  schema:
    ops:
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

      drop-table:
        method: DELETE
        uri: http://<<stargate_host>>:8082/v2/schemas/keyspaces/starter/tables/http_rest_starter
        Accept: "application/json"
        X-Cassandra-Request-Id: "{request_id}"
        X-Cassandra-Token: "{token}"
        Content-Type: "application/json"
        ok-status: "[2-4][0-9][0-9]"

      create-table:
        method: POST
        uri: http://<<stargate_host>>:8082/v2/schemas/keyspaces/starter/tables
        Accept: "application/json"
        X-Cassandra-Request-Id: "{request_id}"
        X-Cassandra-Token: "{token}"
        Content-Type: "application/json"
        body: >2
          {
            "name": "http_rest_starter",
            "columnDefinitions": [
              {
                "name": "key",
                "typeDefinition": "text"
              },
              {
                "name": "value",
                "typeDefinition": "text"
              }
            ],
            "primaryKey": {
              "partitionKey": [
                "key"
              ]
            },
            "ifNotExists": true
          }

  rampup:
    ops:
      rampup-insert:
        method: POST
        uri: http://<<stargate_host>>:8082/v2/keyspaces/starter/http_rest_starter
        Accept: "application/json"
        X-Cassandra-Request-Id: "{request_id}"
        X-Cassandra-Token: "{token}"
        Content-Type: "application/json"
        body: >2
          {
            "key": "{seq_key}",
            "value": "{seq_value}"
          }

  main-read:
    params:
      ratio: 5
    ops:
      main-select:
        method: GET
        uri: http://<<stargate_host>>:8082/v2/keyspaces/starter/http_rest_starter/{rw_key}
        Accept: "application/json"
        X-Cassandra-Request-Id: "{request_id}"
        X-Cassandra-Token: "{token}"
        Content-Type: "application/json"
        ok-status: "[2-4][0-9][0-9]"

  main-write:
    params:
      ratio: 5
    ops:
      main-write:
        method: POST
        uri: http://<<stargate_host>>:8082/v2/keyspaces/starter/http_rest_starter
        Accept: "application/json"
        X-Cassandra-Request-Id: "{request_id}"
        X-Cassandra-Token: "{token}"
        Content-Type: "application/json"
        body: >2
          {
            "key": "{rw_key}",
            "value": "{rw_value}"
          }

```


Before running the scenario, let’s take a look at the layout of the file.  Most of this will be the same layout structure used in most workloads.  As such, this example reveals a large amount of the foundational. In addition, this scenario introduces blocks having HTTP methods included such as: 
* GET
* POST
* DELETE


### Workload layout

As a review, the primary sections of a NoSQLBench workload file include:

* Description - A textual description of what the workload does.
* Scenarios - Set of named scenarios for detailing the intent of the workload and defines that for various blocks (e.g. schema, rampup, main, etc.).
* Params - Optional parameters of interest to reference for applying values.
* Bindings - Named recipes for generated data.  These are referenced in block operations.
* Blocks - Where the labeled operations reside (e.g. schema, rampup, and main).
    * Schema - A block section where the schema is actually defined and created.
* Rampup - Block section for data setup that becomes the backdrop for testing; it’s the density of data outside the metrics collected in the main block.
* Main - Block section that is the target of metrics collection activities.


### Testing operations

The workload operations in the http-rest-starter are quite basic in form, and this is intentional.

The intent is to focus on a simple set of read, write, and delete operations to understand how to work with 
NoSQLBench and Stargate (a data gateway) with http-rest operations.


#### Table and keyspace

For the default scenario, a simple table named `http_rest_starter` will be created with a keyspace named `starter`.  

There will be two fields for our table, `key` and `value`, both with types of `text`.

```
{
  "name": "key",
     "typeDefinition": "text"
}, 
{
   "name": "value",
     "typeDefinition": "text"
}
```


In this scenario, the `key` will become the value for `partitioningKey`:


```
"primaryKey": {
  "partitionKey": [
    "key"
  ]
}
```

#### Default scenario

For this session, the `default` scenario is being used.  As such, all operations are set up to be targeted and executed.

```
scenarios:
 default:
   schema: run driver=http tags==block:"schema.*" threads==1 cycles==UNDEF       
   rampup: run driver=http tags==block:"rampup.*" cycles===3 threads=auto
   main: run driver=http tags==block:"main.*" cycles===3 threads=auto
```

#### Bindings

Values for fields during the schema, rampup, and main operations will come from the bindings section of the file.

Basic examples are included in the http-rest-starter, but this illustrates how bindings supply values to be used by the operations.


```yaml


bindings:

 request_id: ToHashedUUID(); ToString();
 token: Discard(); Token('<<auth_token:>>','<<auth_uri:http://localhost:8081/v1/auth>>', '<<auth_uid:cassandra>>', '<<auth_pswd:cassandra>>');

 seq_key: Mod(10000000); ToString() -> String
 seq_value: Hash(); Mod(1000000000); ToString() -> String

 rw_key: Uniform(0,10000000)->int; ToString() -> String
 rw_value: Hash(); Uniform(0,1000000000)->int; ToString() -> String
 
```


Let’s break down the bindings to understand how they will be used as values in various operations.

* request_id - represents a unique ID used when making the http-rest calls.
* auto_gen_token - this binding uses a newly added function `Token()`, providing the generation of a 
 token required by Stargate. If an `auth_token` value is specified, the rest of the values passed to the Token function are ignored, as the logic to generate
 a new token is not invoked.  If the `auth_token` is not specified, the `auth_uri` can be specified along with the credentials used 
 for requesting a token generation.  Note that the last 3 arguments all have defaults when customizations aren't required.
* seq_key and seq_value - are values generated for use by rampup write operations.
* rw_key and rw_value - are values generated for use by the main read and write operations.

### Running a workload

Let’s run the http-rest-starter.

#### Run scenario

```
./nb5 activities/baselines/http-rest-starter.yaml default stargate_host=localhost
```
Here, the stargate_host is indicating we are targeting the local host services running in Docker.  The port and other URL specifics are included in each of the block operations. 

#### Examine results

After the workload has been run, let’s take a look at the results.

```
docker container exec -it cass40-stargate-coordinator-1 sh
```

#### Stargate log activity
Here you can poke around at the system.log to view the operations that were executed when running the http-rest-starter.

```
cd /stargate/log

tail -100 system.log
```

## Next steps

### More getting started?

Checkout the NoSQLBench getting started section and details for its capabilities for your next testing initiative.  
You can find the starter details [here](https://docs.nosqlbench.io/getting-started/).


### More Http-Rest adapter information?

There are a number of http-rest examples in NoSQLBench.  
In fact, they expand on the use of the Stargate data gateway covering topics such as:

* Documents API
* GraphQL (CQL first approach)
* GraphQL (Schema first approach)


### Want to contribute?

It’s worth mentioning, NoSQLBench is open source, and we are looking for contributions to expand its features!  
Head over to the [contributions](https://docs.nosqlbench.io/dev-guide/contributing) page to find out more.


### Need more advanced scenarios?

There are a number of pre-built scenarios that exist [here](https://docs.nosqlbench.io/getting-started/02-scenarios/).

We will continue to have more Quick Bytes for NoSQLBench in the near future.

Stay tuned, and thank you for reading!
