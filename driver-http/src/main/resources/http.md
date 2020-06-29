# HTTP driver

This driver allows you to make http requests using the native HTTP client that is bundled with the
JVM. It supports free-form construction of requests.

## Example Statements

You can use an _inline request template_ form below to represent a request as it would be submitted
according to the HTTP protocol. This isn't actually the content that is submitted, but it is
recognized as a valid way to express the request parameters in a familiar and condensed form:

```yaml
statements:
 - s1: |
    POST http://{host}:{port}/{path}?{query} HTTP/1.1
    Content-Type: application/json
    token: mybearertokenfoobarbazomgwtfbbq
    
    {body} 
```

You can also provide the building blocks of a request in named fields:

```yaml
  - method: GET
    version: HTTP/1.1
    "Content-Type": "application/json"
    body: {body}
    path: {path}
    ok-status: 2[0-9][0-9]
    ok-body: ^(OK, account id is .*)$
```

As you may guess from the above example, some reserved words are recognized as standard request
parameters. They are explained here in more detail:

- **method** - An optional request method. If not provided, "GET" is assumed. Any method name will
  work here, even custom ones that are specific to a given target system. No validation is done for
  standard method names, as there is no way to know what method names may be valid.
- **host** - The name of the host which should go into the URI. This can also be an ip address if
  you do not need support for virtual hosts. If there are multiple hosts provided to the activity,
  then this value is selected in round-robin style. **default: localhost**
- **port** - The post to connect to. If it is provided, then it is added to the URI, even if it is
  the default for the scheme (80 for http, or 443 for https)
- **path** - The path component of the URI.
- **query** - A query string. If this is provided, it is appended to the path in the URI with a
  leading question mark.
- **version** - The HTTP version to use. If this value is not provided, the default version for the
  Java HttpClient is used. If it is provided, it must be one of 'HTTP_1_1', or 'HTTP_2'.
- **body** - The content of the request body, for methods which support it.
- **ok-status** - An optional set of rules to use to verify that a response is valid. This is a
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
Contextual error handling may be added in a future version.

## SSL Support

SSL Support will be added before this driver is considered ready for general use.

## Client Behavior

### TCP Sessions

The HTTP clients are allocated one to each thread. The TCP connection caching is entirely left to
the defaults for the current HttpClient library that is bundled within the JVM.

### Chunked encoding and web sockets

Presently, this driver only does basic request-response style requests. Thus, adding headers which
take TCP socket control away from the HttpClient will likely yield inconsistent (or undefined)
results. Support may be added for long-lived connections in a future release.
