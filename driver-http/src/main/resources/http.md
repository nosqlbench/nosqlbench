# HTTP driver

This driver allows you to make http requests using the native HTTP client that is bundled with the
JVM. It supports free-form construction of requests.

You specify what a request looks like by providing a set of request parameters. They can be in
either literal (static) form with no dynamic data binding, or they can each be in a string template
form that draws from data bindings. Each cycle, a request is assembled from these parameters and
executed.

## Example Statements

The simplest possible statement form looks like this:

```yaml
statement: http://google.com/
```

Or, you can have a list:

```yaml
# A list of statements
statements:
 - http://google.com/
 - http://amazon.com/
```

Or you can template the values used in the URI, and even add ratios:

```yaml
# A list of named statements with variable fields and specific ratios:
statements:
 - s1: http://google.com/search?query={query}
   ratio: 3
 - s2: https://www.amazon.com/s?k={query}
   ratio: 2
bindings:
 query: WeightedStrings('function generator;backup generator;static generator');
```

You can even make a detailed request with custom headers and result verification conditions:

```yaml
# Require that the result be status code 200-299 match regex "OK, account id is .*" in the body
statements:
  - method: GET
    uri: https://google.com/
    version: HTTP/1.1
    "Content-Type": "application/json"
    ok-status: 2[0-9][0-9]
    ok-body: ^(OK, account id is .*)$
```

For those familiar with what an HTTP request looks like on the wire, the format below may be
familiar. This isn't actually the content that is submitted, but it is recognized as a valid way to
express the request parameters in a familiar and condensed form. A custom config parser makes this
form available fo rhose who want to emulate a well-known pattern:

```yaml
statements:
  - s1: |
     GET https://google.com/ HTTP/1.1
     Content-Type: application/json
    ok-status: 2[0-9][0-9]
    ok-body: ^(OK, account id is.*)$
```

Of course, in the above form, the response validators are still separate parameters.

## Bindings

All request fields can be made dynamic with binding functions. To make a request that has all
dynamic fields, you can do something like this:

```yaml
statements:
  - s1: |
     {method} {scheme}://{host}:{port}/{path}?{query} {version}
     Content-Type: {content_type}
     Token: {mybearertoken}

     {body}
```

The above example is in the inline request form. It is parsed and interpreted internally as if you
had configured your op template like this:

```yaml
statements:
  - method: {method}
    uri: {scheme}://{host}:{port}/{path}?{query}
    version: {version}
    "Content-Type": {content_type}
    "Token": {mybearertoken}
    body: {body}
```

The above two examples are semantically identical, only the format is different. Notice that the
expansion of the URI is still captured in a field called uri, with all of the dynamic pieces
stitched together in the value. You can't use arbitrary request fields. Every request field must
from (method, uri, version, body, ok-status, ok-body) or otherwise be capitalized to signify an HTTP
header.

The HTTP RFCs do not require headers to be capitalized, but they are capitalized ubiquitously in
practice, so we follow that convention here for clarity. Headers are in-fact case-insensitive, so
any issues created by this indicate a non-conformant server/application implementation.

For URIs which are fully static (There are no dynamic fields, request generation will be much
faster, since the request is fully built and cached at startup.


## Request Fields

At a minimum, a **URI** must be provided. These are enough to build a request with.
All other request fields are optional and have reasonable defaults:

- **uri** - This is the URI that you might put into the URL bar of your browser. There is no
  default. Example: `https://en.wikipedia.org/wiki/Leonhard_Euler`
- **method** - An optional request method. If not provided, "GET" is assumed. Any method name will
  work here, even custom ones that are specific to a given target system. No validation is done for
  standard method names, as there is no way to know what method names may be valid.
- **version** - The HTTP version to use. If this value is not provided, the default version for the
  Java HttpClient is used. If it is provided, it must be one of 'HTTP/1.1' or 'HTTP/2.0'.
- **body** - The content of the request body, for methods which support it.
- **ok-status** - An optional set of rules to verify that a response is valid. This is a
  simple comma or space separated list of integer status codes or a pattern which is used as a regex
  against the string form of a status code. If any characters other than digits spaces and commas
  are found in this value, then it is taken as a regex. If this is not provided, then any status
  code which is >=200 and <300 is considered valid.
- **ok-body** - An optional regex pattern which will be applied to the body to verify that it is a
  valid response. If this is not provided, then content bodies are read, but any content is
  considered valid.

Any other statement parameter which is capitalized is taken as a request header. If additional
fields are provided which are not included in the above list, or which are not capitalized, then an
error is thrown.

## Error Handling & Retries

Presently, no determination is made about whether or not an errored response *should* be retryable.
More contextual error handling may be added in a future version.

## SSL Support

SSL should work for any basic client request that doesn't need custom SSL configuration. If needed,
more configurable SSL support will be added.

## Client Behavior

### TCP Sessions

The HTTP clients are allocated one to each thread. The TCP connection caching is entirely left to
the defaults for the current HttpClient library that is bundled within the JVM.

### Chunked encoding and web sockets

Presently, this driver only does basic request-response style requests. Thus, adding headers which
take TCP socket control away from the HttpClient will likely yield inconsistent (or undefined)
results. Support may be added for long-lived connections in a future release.

## HTTP Activity Parameters

- **client_scope** - default: activity - One of activity, or thread. This controls how many
  clients instances you use with an HTTP activity. By default, all threads will use the same
  client instance.
- **follow_redirects** - default: normal - One of never, always, or normal. Normal redirects
  are those which do not redirect from HTTPS to HTTP.
- **diagnostics** - default: none -
  This setting is a selector for what level of verbosity you will get on console. If you set
  this to true, you'll get every request and response logged to console. This is only for
  verifying that a test is configured and to spot check services before running higher scale
  tests.
  If you want finer control over how much information diagnostics provides, you can specify
  a comma separated list of the below.
  - all - Includes all of the below categories
  - stats - Counts of redirects, headers, body length, etc
  - headers - include header details
  - content - include
  - a number, like 3000 - causes the diagnostics to be reported only on this cycle modulo
- **timeout** - default: tbd -
  Sets the
