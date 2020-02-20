# Virtual DocSys

## Inspiring Examples

These are doc sites that have examples of good docs.

- [Apache Groovy](http://groovy-lang.org/documentation.html)
- [Prometheus](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [NetData](https://docs.netdata.cloud/)
- [Optimizely](https://developers.optimizely.com/x/solutions/javascript/reference/index.html)
- [Elastic Step by Step](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search.html)
- [Javascript.info](https://javascript.info/strict-mode)
- [TiKV docs - explore the tabs](https://tikv.org/docs/3.0/concepts/overview/)
- [rocket](https://rocket.rs/v0.4/overview/)

## Architecture

The RealSimpleDoc system is meant to be embedded within
other projects. It has been design to require a minimum of
additional programming at the web layer, while allowing the tool
builder to focus on direct content transforms of file types.

It is also designed to allow multiple projects to contribute
documentation from their constituent components, with
the contents being layered and composited dynamically.

Thus, the primary interface to the web server is provided as a filesystem
instance that presents the directory structure and file content as
the designer would want the user to see it, including content from
multiple contributing components. This shifts the classic problem
of doing server-side web programming in-depth for basic content
authoring to one of simply having the right transformers in place.

## MetaFS

The content provided to the web server from the filesystem is provided
by a set of filesystem modules that are collectively called MetaFS. This
consists of three specific filesystems which each serve a simple purpose:

### VirtualFS

The VirtualFS filesystem type is simply a way to provide a view to a
filesystem that is rooted at some directory path in a host filesystem.
For example, a VirtFS that is created with a host filesystem path of
`/usr/local/branson` will contain a Path entry for `/` which will look
and behave like a root directory, but all contents accessed via this
path will come directly from the host's filesystem `/usr/local/branson`.

### LayerFS

This filesystem type implements the ability to combine multiple filesystems
in a layered fashion. Any attempt to access a file or directory in this
filesystem will cause an internal request to the filesystems that have
been added. LayerFS follows a couple basic rules when answering a request:

1. For calls that access attributes, the first response that 
   contains attributes for a file that does exist will be used.
2. When opening a file for write, the first filesystem which 
   is writable will be used to open the file.
3. Any requests which ask whether a file is readable or writable 
   will have their answers filtered to match the effective best case.

### RenderFS

The RenderFS filesystem takes a set of transformers that are associated with a
source and target file extension. The rules observed by this filesystem are:

1. For directory listings, if there is a file with one of the source extensions,
but the target file for that basename does not exist, then the directory listing
is modified to show both.
2. For file attribute views, if a call fails internally to find file attributes,
and a source version of the base name exist for that extension, then a virtual
attribute view is created that has all the same attributes as the base name, but
with a different file name.
3. If there is attempt to read file contents which are of a known target extension,
where the source file exists, the content will be rendered from the source file
and returned. 
