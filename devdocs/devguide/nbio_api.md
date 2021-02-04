# NBIO API

The NBIO class provides a way to access file and URL resources from within
a running NoSQLBench process. This centralizes the logic for accessing
files in classpath, filesystem, or on the net.

Managing file, classpath, and URL resources when you need access to all of
them can be complicated business. The NBIO API was introduced to
consolidate this into one easy-to-use API with substantial testing. It
also provides a uniform interface for accessing all of these resource
types so that users have a consistent experience regardless of where their
source data lives.

## Design Intent

Unless there is a good reason to avoid it, developers should delegate to
the NBUI API _anywhere_ in NoSQLBench that a user needs to load a file.
This imbues such call sites with the ability to read local and remote
resources with no further effort required by the programmer.

## API Usage

To use this API, simply call one of the following methods and use method
chaining to delve into what you are looking for:

- `NBIO.all()...` - Look in the filesystem, the classpath, and on the
  net (for any patterns which are a URL).
- `NBIO.fs()...` - Look only in the filesystem.
- `NBIO.classpath()...` - Look only in the classpath.
- `NBIO.local()...` - Look in filesystem or classpath.
- `NBIO.remote()...` - Look only for remote resources.

## Searching Semantics

The fluent-style API will layout the following search parameters for you.
The details on these are explained in the javadoc which will be provided
in most cases as you expand the API. Each of these search parameters
supports zero or more values, and all values are searched that are
provided.

- `prefix(...)` - Zero or more prefix paths to search.
- `name(...)` - Zero or more resources or filenames or urls to look for.
- `extension(...)` - Zero or more extensions to try to match with.

## Getting Results

At runtime, the local process has visibility to many things. Due to how
classpath resources are assembled in pre-JDPA days, it is possible to find
multiple resources under the same name. Thus, you have to be specific
about how many you expect to find, and thus what is considered an error
condition:

- `list()` - All found content in one list
- `one()` - A single source of content should be found -- more or less is
  an error.
- `first()` - At least one source of content should be found, and you only
  want to see the first one.
- `resolveEach()` - List of lists, allows full cardinality visibility when
  using the bulk interface with multiple name terms.

## Consuming Content

To support efficient resolution and consumption of local and remote
content, the type of element returned by the NBIO API is a _Content_
object. However, it provides convenient accessors for getting content when
needed. Remote content will be read lazily only once per NBIO result.
Local content is also read lazily, but it is not cached until GC.

