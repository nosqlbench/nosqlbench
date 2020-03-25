# tcp activity type

There are two tcp activity types, tcpclient and tcpserver which
allow for the generation of data via a tcp client or server.

## Example activity definitions

Run a stdout activity named 'stdout-test', with definitions from activities/stdout-test.yaml
~~~
... driver=tcpclient yaml=stdout-test
~~~

Run a stdout activity named 'stdout-test', with definitions from activities/stdout-test.yaml
~~~
... driver=tcpserver yaml=stdout-test
~~~

## ActivityType Parameters

- **ssl** -  boolean to enable or disable ssl
    (defaults to false)
- **host** - this is the name of the output file
    (defaults to "localhost")
- **port** - this is the name of the output file
    (defaults to "12345")

## Configuration

This activity type uses the uniform yaml configuration format.
For more details on this format, please refer to the
[Standard YAML Format](http://docs.nosqlbench.io/user-guide/standard_yaml/)

## Statement Format

Refer to stdout help for details on the statement format for the tcp
activity types
