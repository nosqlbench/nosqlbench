# Pulsar driver

This driver allows you to produce and consume Apache Pulsar messages with
NoSQLBench.

## Example Statements

The simplest pulsar statement looks like this:

```yaml
statement: send='{this is a test message}'
```

In this example, the statement is sent by a producer with a default
_topic_uri_ of `persistent://public/default/default`.

A complete example which uses all the available fields:

```yaml
statements:
    - name1:
        send: "Payload test with number {numbername}"
        persistent: true
        tenant: {test_tenant}
        namespace: {test_namespace}
        topic: {test_topic}
        client: {clientid}
        producer: {producerid}
```

In this example, we define a producer send operation, identified as
`name1` in metrics. The full topic_uri is constructed piece-wise by the
provided values of `persistent`, `tenant`, `namespace`, and
`topic`. The client instance used by NoSQLBench is controlled by the name
of the `client` field. The instance of the producer used to send messages
with that client is controlled by the `producer` property.

Details on object instancing are provided below under __Instancing
Controls__

## Driver Features

### API Caching

This driver is tailored around the multi-tenancy and topic naming scheme
that is part of Apache Pulsar. Specifically, you can create an arbitrary
number of client instances, producers (per client), and consumers (per
client) depending on your testing requirements.

Further, the topic URI is composed from the provided qualifiers of
`persistence`, `tenant`, `namespace`, and `topic`, or you can provide a
fully-composed value in the `persistence://tenant/namespace/topic`
form.

### Schema Support

Schema support will be added after the initial version is working.

### Instancing Controls

Normative usage of the Apache Pulsar API follows a strictly enforced
binding of topics to produces and consumers. As well, clients may be
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

## Op Fields

Thees fields are used to define of a single pulsar client operation. These
fields, taken together, are called the _op template_, and each one is
called an _op template field_, or simply _template field_. You may specify
them as `literal values` or as `{binding_anchors}` to be qualified at
runtime for each and every cycle. When necessary, the appropriate API
scaffolding is created automatically and cached by the NoSQLBench driver
for Apache Pulsar such as clients, producers, and consumers.

- **send** - If this op field is provided, then its value is used as the
  payload for a send operation. The value may be static or dynamic as in
  a `{binding_anchor}`.
    - default: _undefined_
- **producer** - If provided, the string value of this field determines
  the name of the producer to use in the operation. The named producer
  will be created (if needed) and cached under the designated client
  instance. Because producers are bound to a topic at initialization, the
  default behavior is to create a separate producer per topic_uri per
  client. The producer field is only consulted if the _send_ field is
  defined.
    - default: `{topic_uri}`

- **recv** - If this op field is provided, then its value is used to
  control how a message is received. Special handling of received data is
  possible but will be added in a future version. For now, the default
  behavior of a recv operation is simply to receive a single message.
    - default: _undefined_
- **consumer** - If provided, the string value of this field determines
  the name of the consumer to use in the operation. The named consumer
  will be created (if needed) and cached under the designated client
  instance. Because consumers are bound to a topic at instantiation, the
  default behavior is to create a separate consumer per topic_uri per
  client. The _consumer_ field is only consulted if the _recv_ field is
  defined.
    - default: `{topic_uri}`

- **topic_uri** - The fully defined topic URI for a producer or consumer (
  for a send or recv operation). The format is
  `{persistent|non-persistent}://tenant/namespace/topic` as explained
  at [Topics](https://pulsar.apache.org/docs/en/concepts-messaging/#topics)
  . You may provide the full topic_uri in any valid op template form -- A
  full or partial binding, or a static string. However, it is an error to
  provide this field when any of `topic`, `tenant`, `namespace`, or
  `persistence` are provided, as these fields are used to build the
  _topic_uri_ piece-wise. On the contrary, it is not an error to leave all
  of these fields undefined, as there are defaults that will fill in the
  missing pieces.
- **persistence** - Whether or not the topic should be persistent. This
  value can any one of (boolean) true or false, "true", "false",
  "persistent" or "non-persistent".
    - default: `persistent`
- **tenant** - Defines the name of the tenant to use for the operation.
    - default: `public`
- **namespace** - Defines the namespace to use for the operation.
    - default: `default`
- **topic** - Defines the topic to be used for the operation, and thus the
  topic to be used for the producer or consumer of the operation.
  -default: `default`
- **client** - If this op field is provided, then the value is used to
  name a client instance. If this client instance is not already cached,
  it will be created and used for this operation.
    - default: `default`

## Activity Parameters

- **url** - The pulsar url to connect to.
- **maxcached** - A default value to be applied to `max_clients`,
  `max_producers`, `max_consumers`.
    - default: `max_cached=100`
- **max_clients** - Clients cache size. This is the number of client
  instances which are allowed to be cached in the NoSQLBench client
  runtime. The clients cache automatically maintains a cache of unique
  client instances internally. default: _maxcached_
- **max_producers** - Producers cache size (per client instance). Limits
  the number of producer instances which are allowed to be cached per
  client instance. default: _maxcached_
- **max_consumers** - Consumers cache size (per client instance). Limits
  the number of consumer instances which are allowed to be cached per
  client instance.

## Metrics

- clients
- avg_producers
- avg_consumers
- standard metrics ... TBD

.. this need to be configurable

- sent-{tenant}-{namespace}-{topic}
- recv-{tenant}-{namespace}-{topic}
- sent-{tenant}-{topic}
- recv-{tenant}-{topic}
- sent-{namespace}-{topic}
-
