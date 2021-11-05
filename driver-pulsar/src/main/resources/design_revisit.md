# TODO : Design Revisit -- Advanced Driver Features

**NOTE**: The following text is based on the original multi-layer API
caching design which is not fully implemented at the moment. We need to
revisit the original design at some point in order to achieve maximum
testing flexibility.

To summarize, the original caching design has the following key
requirements:

* **Requirement 1**: Each NB Pulsar activity is able to launch and cache
  multiple **client spaces**
* **Requirement 2**: Each client space can launch and cache multiple
  Pulsar operators of the same type (producer, consumer, etc.)
* **Requirement 3**: The size of each Pulsar operator specific cached
  space can be configurable.

In the current implementation, only requirement 2 is implemented.

* For requirement 1, the current implementation only supports one client
  space per NB Pulsar activity
* For requirement 3, the cache space size is not configurable (no limit at
  the moment)

## Other Activity Parameters

- **maxcached** - A default value to be applied to `max_clients`,
  `max_producers`, `max_consumers`.
    - default: `max_cached=100`
- **max_clients** - Clients cache size. This is the number of client
  instances which are allowed to be cached in the NoSQLBench client
  runtime. The clients cache automatically maintains a cache of unique
  client instances internally. default: _maxcached_
- **max_operators** - Producers/Consumers/Readers cache size (per client
  instance). Limits the number of instances which are allowed to be cached
  per client instance. default: _maxcached_

## API Caching

This driver is tailored around the multi-tenancy and topic naming scheme
that is part of Apache Pulsar. Specifically, you can create an arbitrary
number of client instances, producers (per client), and consumers (per
client) depending on your testing requirements.

Further, the topic URI is composed from the provided qualifiers of
`persistence`, `tenant`, `namespace`, and `topic`, or you can provide a
fully-composed value in the `persistence://tenant/namespace/topic`
form.

### Instancing Controls

Normative usage of the Apache Pulsar API follows a strictly enforced
binding of topics to producers and consumers. As well, clients may be
customized with different behavior for advanced testing scenarios. There
is a significant variety of messaging and concurrency schemes seen in
modern architectures. Thus, it is important that testing tools rise to the
occasion by letting users configure their testing runtimes to emulate
applications as they are found in practice. To this end, the NoSQLBench
driver for Apache Pulsar provides a set controls within its op template
format which allow for flexible yet intuitive instancing in the client
runtime. This is enabled directly by using nominative variables for
instance names where needed. When the instance names are not provided for
an operation, defaults are used to emulate a simple configuration.

Since this is a new capability in a NoSQLBench driver, how it works is
explained below:

When a pulsar cycles is executed, the operation is synthesized from the op
template fields as explained below under _Op Fields_. This happens in a
specific order:

1. The client instance name is resolved. If a `client` field is provided,
   this is taken as the client instance name. If not, it is set
   to `default`.
2. The named client instance is fetched from the cache, or created and
   cached if it does not yet exist.
3. The topic_uri is resolved. This is the value to be used with
   `.topic(...)` calls in the API. The op fields below explain how to
   control this value.
4. For _send_ operations, a producer is named and created if needed. By
   default, the producer is named after the topic_uri above. You can
   override this by providing a value for `producer`.
5. For _recv_ operations, a consumer is named and created if needed. By
   default, the consumer is named after the topic_uri above. You can
   override this by providing a value for `consumer`.

The most important detail for understanding the instancing controls is
that clients, producers, and consumers are all named and cached in the
specific order above.
