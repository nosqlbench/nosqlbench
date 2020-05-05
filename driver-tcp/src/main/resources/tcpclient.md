# Driver: tcpclient

**tcpclient acts like a _client push_ version of stdout over TCP**

The tcpclient driver is based on the behavior of the stdout driver. You configure the tcpclient driver in exactly the
same way as the stdout driver, except for the additional parameters shown here.

The tcpclient driver connects to the configured server address and port (a socket address). When connected, it sends any
buffered lines of data to the server.

If the buffer is primed with data when the client is connected to a server, it will send all of the data at once. After
this, data is added to the buffer at whatever cyclerate the activity is configured for. If you add data to the buffer
faster than you can send it to a connected server, you will have a number of failed operations.

However, the opposite is not true. You should generally ensure that you can send the data as fast as you provide it, and
the error counts give you a relatively easy way to verify this. If you wish to disable this behavior, set the retries to
a very high value. In this case, the tries metric will still give you some measure of internal buffer saturation.

## Examples

Run a stdout activity named 'stdout-test', with definitions from activities/stdout-test.yaml
~~~
... driver=tcpserver yaml=stdout-test
~~~

## Driver Parameters

- **retry_delay** - The internal retry frequency at which the internal cycle loop will attempt to add data to the
  buffer. This applies when the internal buffer is full and the client isn't able to send data from it.
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
- **host** - this is the name to connect to (remote server IP address)
  - default: localhost
  - dynamic: false
- **port** - this is the name of the port to connect to (remote server port)
  - default: 12345
  - dynamic: false
- **capacity** - the size of the internal blocking queue
  - default: 10
  - unit: lines of output
  - dynamic: false

## Statement Format

Refer to the help for the stdout driver for for details.

