# Bundled Docs

In order to keep the structure of NoSQLBench modular enough to allow for easy extension by contributors, yet cohesive in
how it presents documentation and features to users, it is necessary to provide internal services which aggregate
content by subject matter into a consumable whole that can be used by the documentation system.

# MarkdownDocs Service

The primary markdown service that is meant to be consumed by the documetnation system is known simply as

    MarkdownDocs

Static methods on this class will provide all of the markdown content in pre-baked and organized form. The markdown
service is responsible for reading all the raw markdown sources and organizing their content into a single cohesive
structure. MardownDocs finds all content that is provided by individual MarkdownProvider services, as described below.

All of the rules for how raw markdown content is to be combined are owned by the MarkdownDocs service.

# MarkdownProvider Service

The `MarkdownProvider` service is responsible for bundling the raw markdown for a path within a NoSQLBench module. Each

module that wishes to publish markdown docs to users must provide one or more MarkdownProvider services via SPI. This is
most easily done with a `@Service(MarkdownProvider.class)` annotation.

The MarkdownProvider service provides all of the individual markdown files it finds indirectly as
io.nosqlbench.nb.api.content.Content, which allows the internal file content to be read appropriately regardless of
whether it comes from a classpath resource stream, a file on disk, or even a dynamic source like function metadata.



