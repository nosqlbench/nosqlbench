# dynamodb driver

The DynamoDB driver supports a basic set of commands as specified at
[Amazon DynamoDB Docs](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Operations_Amazon_DynamoDB.html).

## Activity Parameters

The activity parameters for this driver are basically properties for the DynamoDB driver.
If any of these are not specified, then they are not applied to the client builder,
thus the default is whatever it is for the native client.

* `region` - The region which the driver should connect to. This is the
  simplest way to configure the rest of the client, since defaults are
  automatically looked up for the region. This is the only option that is
  required.
* `endpoint` - The endpoint for the region. Do not specify this if you have
  already specified region.
* `signing_region` - The signing region for the client. You do not have
  to specify this if you specified region.
* `client_socket_timeout` - adjust the default for the client session. (integer)
* `client_execution_timeout` - adjust the default for the client session. (integer)
* `client_max_connections` - adjust the default for the client session. (integer)
* `client_max_error_retry` - adjust the default for the client session. (integer)
* `client_user_agent_prefix` - adjust the default for the client session. (String)
* `client_consecutive_retries_before_throttling` - adjust the default for
  the client session. (integer)
* `client_gzip` - adjust the default for the client session. (boolean)
* `client_tcp_keepalive` - adjust the default for the client session. (boolean)
* `client_disable_socket_proxy` - adjust the default for the client session. (boolean)
* `client_so_send_size_hint` - adjust the default for the client session. (integer)
* `client_so_recv_size_hint` - adjust the default for the client session. (integer)

## Op Templates

Specifically, the following commands are supported as of this release:

* CreateTable
* GetItem
* PutItem
* Query
* DeleteTable

## Examples

```yaml
ops:

  # the op name, used in logging and metrics
  example-CreateTable:
    # the type and target of the command
    CreateTable: TEMPLATE(table,tabular)
    # map of key structure for the table
    Keys:
      part: HASH
      clust: RANGE
    # attributes of the fields
    Attributes:
      part: S
      clust: S
    # either PROVISIONED or PAY_PER_REQUEST
    BillingMode: PROVISIONED
    # required for BillingMode: PROVISIONED
    ReadCapacityUnits: "TEMPLATE(rcus,40000)"
    # required for BillingMode: PROVISIONED
    WriteCapacityUnits: "TEMPLATE(wcus,40000)"

  example-PutItem:
    # the type and target of the command
    PutItem: TEMPLATE(table,tabular)
    # A json payload
    json: |
      {
       "part": "{part_layout}",
       "clust": "{clust_layout}",
       "data0": "{data0}"
      }

  example-GetItem:
    # the type and target of the command
    GetItem: TEMPLATE(table,tabular)
    # the identifiers for the item to read
    key:
      part: "{part_read}"
      clust: "{clust_read}"
    ## optionally, set a projection
    # projection: projection-spec
    # optionally, override ConsistentRead defaults
    ConsistentRead: true


  example-Query:
    # the type and target of the command
    Query: TEMPLATE(table,tabular)
    # The query key
    key:
      part: "{part_read}"
      clust: "{clust_read}"
    # optionally, override the default for ConsistentRead
    ConsistentRead: true
    # optionally, set a limit
    Limit: "{limit}"
    ## optionally, set a projection
    # projection: projection-spec
    ## optionally, set an exclusive start key
    # ExclusiveStartKey: key-spec

  example-DeleteTable:
    # the type and target of the command
    # the table identifier/name (string) to delete
    DeleteTable: TEMPLATE(table,timeseries)
```
