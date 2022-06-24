# HTTP driver

This driver allows you to make http requests using the native HTTP client
that is bundled with the JVM. It supports free-form construction of
requests.

You specify what a request looks like by providing a set of request
parameters. They can be in either literal (static) form with no dynamic
data binding, or they can each be in a string template form that draws
from data bindings. Each cycle, a request is assembled from these
parameters and executed.

## Example Statements

The simplest possible statement form looks like this:

```yaml
op: http://google.com/
```

Or, you can have a list:

```yaml
# A list of statements
ops:
    - http://google.com/
    - http://amazon.com/
```

Or you can template the values used in the URI, and even add ratios:

```yaml
# A list of named statements with variable fields and specific ratios:
ops:
    - s1: http://google.com/search?query={query}
      ratio: 3
    - s2: https://www.amazon.com/s?k={query}
      ratio: 2
bindings:
    query: >
        WeightedStrings('function generator;backup generator;static generator');
        UrlEncode();
```

You can even make a detailed request with custom headers and result
verification conditions:

```yaml
# Require that the result be status code 200-299 match regex "OK, account id is .*" in the body
ops:
    - get-from-google:
      method: GET
      uri: "https://google.com/"
      version: "HTTP/1.1"
      Content-Type: "application/json"
      ok-status: "2[0-9][0-9]"
      ok-body: "^(OK, account id is .*)$"
```

For those familiar with what an HTTP request looks like on the wire, the
format below may be familiar. This isn't actually the content that is
submitted, but it is recognized as a valid way to express the request
parameters in a familiar and condensed form. A custom config parser makes
this form available fo rhose who want to emulate a well-known pattern:

```yaml
ops:
    - s1: |
          GET https://google.com/ HTTP/1.1
          Content-Type: application/json
      ok-status: 2[0-9][0-9]
      ok-body: ^(OK, account id is.*)$
```

Of course, in the above form, the response validators are still separate
parameters.

## Bindings

All request fields can be made dynamic with binding functions. To make a
request that has all dynamic fields, you can do something like this:

```yaml
ops:
    - s1: |
          {method} {scheme}://{host}:{port}/{path}?{query} {version}
          Content-Type: {content_type}
          Token: {mybearertoken}

          {body}
```

The above example is in the inline request form. It is parsed and
interpreted internally as if you had configured your op template like
this:

```yaml
ops:
    - method: { method }
      uri: { scheme }://{host}:{port}/{path}?{query}
      version: { version }
      "Content-Type": { content_type }
      "Token": { mybearertoken }
      body: { body }
```

The above two examples are semantically identical, only the format is
different. Notice that the expansion of the URI is still captured in a
field called uri, with all the dynamic pieces stitched together in the
value. You can't use arbitrary request fields. Every request field must
from (method, uri, version, body, ok-status, ok-body) or otherwise be
capitalized to signify an HTTP header.

The HTTP RFCs do not require headers to be capitalized, but they are
capitalized ubiquitously in practice, so we follow that convention here
for clarity. Headers are in-fact case-insensitive, so any issues created
by this indicate a non-conformant server/application implementation.

For URIs which are fully static (There are no dynamic fields, request
generation will be much faster, since the request is fully built and
cached at startup.

## Request Fields

At a minimum, a **URI** must be provided. This is enough to build a
request with. All other request fields are optional and have reasonable
defaults:

- **uri** - This is the URI that you might put into the URL bar of your
  browser. There is no default.
  Example: `https://en.wikipedia.org/wiki/Leonhard_Euler`
  If the uri contains a question mark '?' as a query delimiter, then all
  embedded sections which are contained within `URLENCODE[[` ... `]]`
  sections are preprocessed by the HTTP driver. This allows you to keep
  your test data in a recognizable form. This is done at startup, so there
  is no cost during the test run. As an added convenience, binding points
  which are within the encoded block will be preserved, so
  both `https://en.wikipedia.org/URLENCODE[[wiki/]]{topic}` and
  `https://en.wikipedia.org/URLENCODE[[wiki/{topic}]]` will yield the same
  configuration. For a terser form, you can use `E[[...]]`. You must also
  ensure that the values that are inserted at binding points are produced
  in a valid form for a URI. You can use the `URLEncode()`
  binding function where needed to achieve this.
- **method** - An optional request method. If not provided, "GET" is
  assumed. Any method name will work here, even custom ones that are
  specific to a given target system. No validation is done for standard
  method names, as there is no way to know what method names may be valid.
- **version** - The HTTP version to use. If this value is not provided,
  the default version for the Java HttpClient is used. If it is provided,
  it must be one of 'HTTP/1.1' or 'HTTP/2.0'.
- **body** - The content of the request body, for methods which support
  it.
- **ok-status** - An optional set of rules to verify that a response is
  valid. This is a simple comma or space separated list of integer status
  codes or a pattern which is used as a regex against the string form of a
  status code. If any characters other than digits spaces and commas are
  found in this value, then it is taken as a regex. If this is not
  provided, then any status code which is >=200 and <300 is considered
  valid.
- **ok-body** - An optional regex pattern which will be applied to the
  body to verify that it is a valid response. If this is not provided,
  then content bodies are read, but any content is considered valid.

Any other statement parameter which is capitalized is taken as a request
header. If additional fields are provided which are not included in the
above list, or which are not capitalized, then an error is thrown.

## Error Handling & Retries

By default, a request which encounters an exception is retried up to 10
times. If you want to change this, set another value to the
`retries=` activity parameters.

Presently, no determination is made about whether an errored
response *should* be retryable, but it is possible to configure this if
you have a specific exception type that indicates a retryable operation.

The HTTP driver is the first NB driver to include a completely
configurable error handler chain. This is explained in the
`error-handlers` topic. By default, the HTTP activity's error handler is
wired to stop the activity for any error encountered.

## SSL Support

SSL should work for any basic client request that doesn't need custom SSL
configuration. If needed, more configurable SSL support will be added.

## Client Behavior

### TCP Sessions

The HTTP clients are allocated one to each thread. The TCP connection
caching is entirely left to the defaults for the current HttpClient
library that is bundled within the JVM.

### Chunked encoding and web sockets

Presently, this driver only does basic request-response style requests.
Thus, adding headers which take TCP socket control away from the
HttpClient will likely yield inconsistent (or undefined)
results. Support may be added for long-lived connections in a future
release. However, chunked encoding responses are supported, although they
will be received fully before being processed further. Connecting to a long-lived
connection that streams chunked encoding responses indefinitely will have
undefined results.

## HTTP Activity Parameters

- **follow_redirects** - default: normal - One of never, always, or
  normal. Normal redirects are those which do not redirect from HTTPS to
  HTTP.

- **diagnostics** - default: none - synonym: **diag**
  example: `diag=brief,1000` - print diagnostics for every 1000th cycle,
  including only brief details as explained below.

  This setting is a selector for what level of verbosity you will get on
  the console. If you set this to `diag=all`, you'll get every request and
  response logged to console. This is only for verifying that a test is
  configured and to spot check services before running higher scale tests.

  All the data shown in diagnostics is post-hoc, directly from the
  response provided by the internal HTTP client in the Java runtime.

  If you want finer control over how much information diagnostics
  provides, you can specify a comma separated list of the below.

    - headers - show headers
    - stats - show basic stats of each request
    - data - show all of each response body this setting
    - data10 - show only the first 10 characters of each response body
      this setting supersedes `data`
    - data100 - show only the first 100 characters of each response body
      this setting supersedes `data10`
    - data1000 - show only the first 1000 characters of each response body
      this setting supersedes `data100`
    - redirects - show details for interstitial request which are made
      when the client follows a redirect directive like a `location`
      header
    - requests - show details for requests
    - responses - show details for responses
    - codes - shows explanatory details (high-level) of http response status codes
    - brief - Show headers, stats, requests, responses, and 10 characters
    - all - Show everything, including full payloads and redirects
    - a modulo - any number, like 3000 - causes the diagnostics to be
      reported only on this cycle modulo. If you set `diag=300,brief`
      then you will get the brief diagnostic output for every 300th
      response.

  The requests, responses, and redirects settings work in combination.
  For example, if you specify responses, and redirect, but not requests,
  then you will only see the response portion of all calls made by the
  client. All available filters layer together in this way.

- **timeout** - default: forever - Sets the timeout of each request in
  milliseconds.
