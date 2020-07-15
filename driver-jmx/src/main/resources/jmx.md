# JMX Driver

The JMX Driver allows you to do various JMX operations against a JMX service URL
and object name.

You must specify the service URL and object name in the op template. Alternately, you can specify
the protocol, host, port and path. Each cycle, the full JMX operation is derived from the
op template, and executed.

In the first version of this driver, only reads are supported.

# Connection Options

JMX transports can be configured in a myriad of ways. The options below allow you to add
connection options such as SSL and authentication.

- **ssl** - Use SSL settings provided. Thes SSL settings are from the NoSQLBench standard
  SSL support

# Example Operations

## readvar

The readvar operation is used to read a named attribute of the named object and store it in the
thread local variable map.

```
statements:
  - read1:
     url: service:jmx:rmi:///jndi/rmi://dsehost:7199/jmxrmi
     object: org.apache.cassandra.metrics:type=Compaction,name=PendingTasks
     readvar: Value
     as_type: int
     as_name: pending_tasks     
```

The `as_type` and `as_name` are optional, and if provided will set the name and data type used in
the thread local variable map.

- *as_type* can be any of long, int, double, float, byte, short, or String. If the original type
is convertable to a number, then it will be converted to a number and then to the desired type. If it
is not, it will be converted to String form first and then to the desired type. If the type name
contains dots, as in a fully-qualified class name, then direct class casting will be used if the
types are compatible.

A combined format is available if you don't want to put every command property on a separate line.
In this format, the first entry in the command map is taken as the command name and a set of key=value
command arguments. It is semantically equivalent to the above example, only more compact.

```
statements:
  - read1: readvar=Value as_type=int as_name=pending_tasks     
    url: service:jmx:rmi:///jndi/rmi://dsehost:7199/jmxrmi
    object: org.apache.cassandra.metrics:type=Compaction,name=PendingTasks
```

## explain

If you want to see the details about a managed object, you can use the explain command:

```
statements:
  - explain1:
     url: service:jmx:rmi:///jndi/rmi://dsehost:7199/jmxrmi
     object: org.apache.cassandra.metrics:type=Compaction,name=PendingTasks
     explain: object
```

This will use the MBeanInfo to interrogate the named management bean and provide a summary
of it's available attriburtes, operations, notifications, and constructors to stdout.
This is not meant for bulk testing, but more for explaining and documenting JMX beans.