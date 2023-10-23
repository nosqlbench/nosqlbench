# Editing NoSQLBench Docs

The docs that are hosted at docs.nosqlbench.io are built from markdown
files found in this project. To make changes or to author these docs, you
need to know where to find them.

## NoSQLBench docs

The core docs are found under the nosqlbench/nosqlbench-build-docs repo.

By browsing this directory structure and looking at the front matter on
each markdown file, you'll get a sense for how they are organized.

For the main user and developer guides, you can submit PRs to the nosqlbench-build-docs repo.

## Doc Overlays
Some sources of documentation, like the driver adapter markdown files are required to be provided by
the NoSQLBench build pipeline in the core nosqlbench project. These are auto-exported as an overlay
into the project repo above.

### Driver Docs

Some of the other docs are found within each driver module. For example,
the cql docs are found in the resources root directory of the cql driver
module. This is the case for any basic docs provided for a driver. The
docs are bundled with modules to allow for them to be maintained by the
driver maintainers directly.

### Binding Function Docs

All the binding function docs are generated automatically from source.
Javadoc source as well as annotation details are used to decorate the
binding functions so that the can be cataloged and shared to the doc site.
To improve the binding function docs, you must improve the markdown
rendering code which is responsible for this.

## Suggestions

The doc system in NoSQLBench is a core element, but it is not really great
yet nor is it done. Any help on improving it is appreciated, in any form.

