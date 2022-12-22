http extension
==============

Allow access to HTTP URLs from within scripts, supporting both basic
get and post methods. In all cases, the returned type is the full
response object, from which the body content can be accessed.

## Examples

Get content from a URL into a string variable:

```
var response= http.get("http://google.com/")
```

Post an empty body to a URL, useful for state-changing calls where
all of the control data is in the URL:

```
var response= http.post("http://some.server/path/to/resource?query=foobarbaz")
```

Post content to a URL, specifying the URL, content value, and content type:
```
var response= http.post("http://some.server/path/to/resource", "this is the data", "text/plain");
```
