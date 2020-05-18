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

The MarkdownDocs service relies on SPI published services which provide raw markdown sources as described below.

# RawMarkdownSource Services

The `RawMarkdownSource` service is responsible for bundling the raw markdown for a path within a NoSQLBench module. Each
module that wishes to publish markdown docs to users must provide one or more RawMarkdownSource services via SPI. This
is most easily done with a `@Service(RawMarkdownSource.class)` annotation.

## RawMarkdownSource endpoints provide Content

Each instance of a RawMarkdownSource service provides all of the individual markdown files it finds indirectly as
io.nosqlbench.nb.api.content.Content, which allows the internal file content to be read appropriately regardless of
whether it comes from a classpath resource stream, a file on disk, or even a dynamic source like function metadata.

## RawMarkdownSources Aggregator

A service aggregator called RawMarkdownSources provides easy access to all raw markdown sources provided by all
published instances of the service.

# Front Matter Interpretation

There is a set of rules observed by MarkdownDocs for repacking markdown for structured display. These rules are largely
driven by front matter.

## Doc Scope

There are three doc scopes that can be added to source markdown via the `scopes` front matter.

* `cli` - The source content should be included for command-line searching and viewing.
* `web` - The source content should be included for static web documentation.
* `app` - The source content should be included in accompanying documentation for web applications.
* `all` - This is the default scope which includes all of the above.

If no scopes are provided, then a special scope `any` is assigned to source content.

__ THIS IS A WORK IN PROGRESS __

## Topic Names

The `topic` property determines

1. Front matter may be sanity checked for unrecognized properties.
2. All front matter that is considered is required to have at least one topic value.
3. Topic values which contain `, ` or `; ` patterns are auto-split into multiple topics.
4. Topics can be hierarchical. Topics in the form of `cat1/cat2/topicfoo` are considered nested topics, with the
   containing layer being considered a category. The right most word is considered the basic topic name. This means that
   in the above topic name, `cat1` is a topic category containing the `cat2` topic category, which contains the topic
   `topicfoo`.
5. *Topic Expansion* - A topic entry which starts with a caret `^`, contains either of '.*', '.+', or ends with a `$` is
   considered a wildcard topic. It will be treated as a topic pattern which will be compared to known topics. When it
   matches another topic, the matched topic is added to the virtualized topic list of the owning item.
6. `aggregations` are used to physically aggregate content from matching topics onto a markdown source:
   1. Each aggregation is a pattern that is tested against all topics after topic expansion.
   2. When a source item is matched to an aggregation,
   3. wildcards, except that they
      cause all matching topics to be aggregated onto the body of the owning markdown source.
   4. All topics (after topicin order determined by weight. Aggregations are indicated with an `aggregation` property.
   regations are split on commas and semicolons as above, and are always considered patterns for matching. Thus,
   aggregation with none of the regex indicators above will only match topics with the same literal pattern.

7. Front matter will be provided with topical aggregations included, with the following conditions:
   * aggregations properties are elided from the repacked view. Instead, an `included` header is added which lists all
     of the included topics.


## Composite Markdown

When aggregations occur, the resulting markdown that is produces is simply a composite of all of the included markdown
sources. The front matter of the including markdown source becomes the first element, and all other included are added
after this. The front matter of the including markdown becomes the representative front matter for the composite
markdown.

## Indexing Data

Indexing data should be provided in two forms:

1. The basic metadata index which includes topics, titles, and other basic info and logical path info. This view is used
   to build menus for traversal and other simple views of topics as needed for direct presence check, or lookup.
2. A FTS index which includes a basic word index with stemming and other concerns pre-baked. This view is used as a
   cache-friendly searchable index into the above metadata.

