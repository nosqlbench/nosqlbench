# kafkaproducer

This is an activity type which allows for a stream of data to be sent to a kafka topic. It is based on the stdout
activity statement format.

## Parameters

- **topic** - The topic to write to for this activity.

### Examples

Refer to the online standard YAML documentation for a detailed walk-through.
An example yaml is below for sending structured JSON to a kafka topic:

    bindings:
     price: Normal(10.0D,2.0D) -> double; Save('price') -> double;
     quantity: Normal(10000.0D,100.0D); Add(-10000.0D); Save('quantity') -> double;
     total: Identity(); Expr('price * quantity') -> double;
     client: WeightedStrings('ABC_TEST:3;DFG_TEST:3;STG_TEST:14');
     clientid: HashRange(0,1000000000) -> long;
    
    statements:
     - |
        \{
          "trade": \{
            "price": {price},
            "quantity": {quantity},
            "total": {total},
            "client": "{client}",
            "clientid":"{clientid}"
          \}
        \}
