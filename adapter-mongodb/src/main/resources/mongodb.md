# MongoDB

This is a driver for MongoDB. It supports the `db.runCommand` API described in [here](https://docs.mongodb.com/manual/reference/command/).

Consult
[MongoDB commands](https://www.mongodb.com/docs/manual/reference/command/)
for details on command structure.

### Example activity definitions

Run a mongodb activity with definitions from activities/mongodb-basic.yaml

```shell
nb5 driver=mongodb yaml=activities/mongo-basic.yaml
```

### MongoDB ActivityType Parameters

- **connection** (Mandatory) - connection string of the target MongoDB.

    Example: `mongodb://127.0.0.1`

- **database** (Mandatory) - target database

    Example: `testdb`
