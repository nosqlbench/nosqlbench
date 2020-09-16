# NBDocs - NoSQLBench Docs

__THIS IS A WORK IN PROGRESS__

In order to keep the structure of NoSQLBench modular enough to allow for easy extension by
contributors, yet cohesive in how it presents documentation and features to users, it is necessary
to provide internal services which aggregate content by subject matter into a consumable whole that
can be used by the documentation system.

## MarkdownDocs Service

The primary markdown service that is meant to be consumed by the documetnation system is known simply as

    MarkdownDocs

Static methods on this class will provide all of the markdown content in pre-baked and organized
form. The markdown service is responsible for reading all the raw markdown sources and organizing
their content into a single cohesive structure. MardownDocs finds all content that is provided by
individual MarkdownProvider services, as described below.

All of the rules for how raw markdown content is to be combined are owned by the MarkdownDocs
service.

The MarkdownDocs service relies on SPI published services which provide raw markdown sources as
described below.

## RawMarkdownSource Services

The `RawMarkdownSource` service is responsible for bundling the raw markdown for a path within a
NoSQLBench module. Each module that wishes to publish markdown docs to users must provide one or
more RawMarkdownSource services via SPI. This is most easily done with a
`@Service(RawMarkdownSource.class)` annotation.

## RawMarkdownSource endpoints provide Content

Each instance of a RawMarkdownSource service provides all of the individual markdown files it finds
indirectly as io.nosqlbench.nb.api.content.Content, which allows the internal file content to be
read appropriately regardless of whether it comes from a classpath resource stream, a file on disk,
or even a dynamic source like function metadata.

## RawMarkdownSources Aggregator

A service aggregator called RawMarkdownSources provides easy access to all raw markdown sources
provided by all published instances of the service.

## Front Matter Interpretation

There is a set of rules observed by MarkdownDocs for repacking markdown for structured display.
These rules are largely driven by front matter.

### Doc Scope

The `scope` front matter property determines where content should be visible. This is useful for
providing documentation about a topic or concept that can be pulled into multiple places. There are
three doc scopes that can be added to source markdown via the `scopes` front matter. This is a
multi-valued property

* `cli` - The source content should be included for command-line searching and viewing.
* `web` - The source content should be included for static web documentation.
* `app` - The source content should be included in accompanying documentation for web applications.
* `all` - This is the default scope which includes all of the above.

If no scopes are provided, then a special scope `any` is assigned to source content.

**Examples**

```yaml
---
scopes: all
---
```

```yaml
---
scopes: cli,web
---
```

### Topic Names

The `topic` front-matter property determines the assocation between a documentation fragment and the
ways that a user might name or search for it. All content within NBDocs falls within one or more
nested topics. That is, raw content could be homed under a simple topic name, or it could be homed
under a topic which has a another topic above.

1. All front matter that is considered is required to have at least one topic value.
2. Topic values which contain `, ` or `; ` patterns are auto-split into multiple topics.
3. Topics can be hierarchical. Topics in the form of `cat1/cat2/topicfoo` are considered nested
   topics. Topics which contain other topics are called topic categories, but they are also topics.
4. Topics can be literal values or they can be patterns which match other topics.

**examples**

```yaml
---
topics: cli, parameters
---
```

### Topic Aggregation

Topics can be placeholders for matching other topics. When a topic name starts with a caret `^`,
contains either of `.*`, or `.+`, or ends with a `$`, it is called a topic pattern.

The patterns are regular expressions, even though the patterns above are used explicitly to enable
pattern detection.

This allows for content to be included in other places. When source content is marked with a topic
pattern, any other source content that is matched by that pattern is included in views of its raw
content. Further, the topics which are matched are added to an `included` property in the matching
content's front matter. The matched content is not affected in any other way and is still visible
under the matched topic names.

It is considered an error to create a circular reference between different topics. This condition is
checked explicitly by the topic mapping logic.

**examples**

```yaml
---
title: owning topic
topics: ^cli$, foobarbaz
---

# Main Content

main content
```

```yaml
---
title: included topic
topics: cli, parameters
---

## Included content

included content
```

Taking the two source documents above, the markdown loading system would present the following
document which globs the second one onto the first:

```yaml
---
title: owning topic
topics: cli, foobarbaz, parameters
---

# Main Content

main content

## Included Content

included content

```

Within an aggregate source like the above, all included sections will be ordered according to the
weight front-matter property first, and then by the title, and then by the name of the interior
top-level heading of source item.

## Logical Structure

According to the rules and mechanisms above, it is possible to organize all the provided content
into a clean and consistent strucure for search and presentation.

The example below represents a schematic of what content sources will be provided in every document
after loading and processing:

```
---
title: <String>
scope: <Set<String>>
topics: <Set<String>>
included: <Set<String>>
weight: <Number>
---
<optional content>
```

In specific:

**title** will be provided as a string, even if it is empty. **scope** will be provided as a set of
strings, possibly an empty set. **topics** will be provided as a set of strings, possibly an empty
set. **included** will be provided as a set of strings, possibly an empty set. **weight** will be
provided as a number, possibly 0.

Headings and content may or may not be provided, depending on how the content is aggregated and what
topics are matched for content aggregation.

## Repackaged Artifacts

The documentation aggregator system may be asked to store repackaged forms. These are suggested:

1. A metadata.json which includes the basic metadata of all the documentation entries, including:
   1. module path of logical document
   2. title
   3. topics
   4. included
   5. weight
2. A manifest.json which includes:
   1. module path of logical document
   2. title of document
   3. heading structure of document
3. A topics.json which includes the topic structure of all topics and headings
4. A full-text search index.

With these artifacts provides as services, clients can be efficient in discovering content and
making it searchable for users.

## Topic Mapping Logic

Topics may be transitive. There is no clear rationale for avoiding this, and at least one good  
reason to allow it. The method for validating and resolving topic aggregations, where there may be
layers of dependencies, is explained here:

__BEING REWRITTEN__
1. All content sources are put into a linked list in no particular order.
2. The list is traversed from head to tail repeatedly.
3. When the head of the list is an aggregating source, and all of the matching elements are
   non-aggregating, then the element converted to a non-aggregating element.
4. If the head of the list is a non-aggregating source, it is moved to the tail.
5. When all of the elements of the list are non-aggregating, the mapping is complete.
6. When the
