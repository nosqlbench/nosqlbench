---
source: nb-adapters/adapter-tcp/src/main/resources/tcpclient.md
---
# tcpclient

**tcpclient acts like a _client push_ version of stdout over TCP**

The tcpclient driver is based on the behavior of the stdout driver. You configure the tcpclient driver in exactly the
same way as the stdout driver, except for the additional parameters shown here.

The tcpclient driver connects to a configured host and port (a socket address). When a server is listening on that socket,
then the data for each cycle is written via the socket just like stdout would write.

## Examples

Run a stdout activity named 'stdout-test', with definitions from activities/stdout-test.yaml


    ... driver=tcpclient yaml=stdout-test

## Driver Parameters

- **retry_delay** - The internal retry frequency at which the internal cycle loop will attempt to add data to the
  buffer. This applies when the internal buffer is full and no clients are consuming data from it.
  - unit: milliseconds
  - default: 1000
  - dynamic: false
- **retries** - The number of retries which the internal cycle loop will attempt before marking a row of output as
  failed.
  - default: 3
  - dynamic: false

- **ssl** - boolean to enable or disable ssl
  - default: false
  - dynamic: false

  To enable, specifies the type of the SSL implementation with either `jdk` or `openssl`.

  See the ssl help topic for more details with `nb5 help ssl` for more details.

- **host** - this is the name to bind to (local interface address)
  - default: localhost
  - dynamic: false
- **port** - this is the name of the port to listen on
  - default: 12345
  - dynamic: false
- **capacity** - the size of the internal blocking queue
  - default: 10
  - unit: lines of output
  - dynamic: false

## Statement Format

Refer to the help for the stdout driver for details.
