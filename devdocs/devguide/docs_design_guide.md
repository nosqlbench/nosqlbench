---
title: Docs Design Guide
weight: 10
topics: Docs, Design Guides
---

# Docs Design Guide

This is an example of a well-formed markdown doc source for NBDocs. Everything in this doc is
structured according to the headings. Headings at level one are meant to be descriptive of the whole
document, thus there should only be one level-one heading above. There should always be top level
heading within each raw document. As the paragraph under the top-level heading, this text is
meant to provide top-level information about the contents of this document.

The top level heading does not necessarily mean that the heading is a level-one heading. In this
case it is, but the document is allowed to be hoisted to any heading level between 1 and 6 as long
as the internal structure of the doc is hierachical and properly nested.

The first heading in a document will be taken as the main section label.

Since there is generally only one top-level section in this design approach, there is no way to have
multiple stanzas of top-level content throughout the document _at this level_. This is intentional
and part of keeping content structure in a hierarchical form. If you need to enumerate concepts or
sections with headings at the same level, that need can easily be fulfilled by using multiple
sections at lower levels. Conceptually, if you have a challenge fitting concepts into this
structure, go back to the concept map view of things and decide where the fit before continuing on.

Because the words "Docs Design Guide" are included above in a heading, they will be matched
in searches for topics.

## Basic Conventions

This is the first inner sub-section of the document. The heading here is simple. Since it is
indented further than the heading of the section before it, it is considered a subsection of that.
Level two headings are often rendered slightly smaller than the level one headings, with the same
type visual document partitioning as level one headings like horizontal dividers.

Names of heading should be descriptive and contain no special characters.

A convention of four spaces (and no tabs) is preferred throughout. This provides a familiar layout
for users who are viewing the raw documentation in monospace.

Line lengths are expected to be hard wrapped at 100 characters. This means that users will be able
to see the text rendered cleanly in monospace form even if they have to widen their terminals
*slightly*, but it also means we're not fighting with 80 character limits that no longer make sense.
It is strongly advised that any editor use the auto hard-wrapping and formatting settings of their
editor tools for this.

### Formatting Details

This is the third section within the document, but it is the first subsection of the second section.
Headings at this level are generally visually interior to those at the second level, so you can use
this level to mark a sequence of things that are local to a topic more visually.

**emphasized item**

Emphasized items like the above can be included as shown on the left to demark items which are
nominal in a local sense, but which are considered _only_ content of some other well-known globally
obvious topic or concept. That means you intend for users to find these local topics by way of
finding the other more global name, but you don't want to cloud their view of these fine details
unless they specifically ask for that kind of search.


1.  Lists of things can be indented and elaborated on with self-similar indentation.

    This is another paragraph of the enumerated section. It can have its own interior details.

    1.  This is a list within a list.
        - items in this list can have bullet points.
        - items in this list can have bullet points.

```text
All fenced code is expected to be annotated with a language specifier as above.
```

For example, consider this JSON data:

```json5
{
  "description": "This is a block of json, and it might be syntax highlighted."
}
```

When you want to block indent details, simply use the four space rule.

    This will stand out to users as an example, hopefully to explain in detail
    what is being described above and/or below this content. Generally, it is
    formatted as an inline block with monospace rendering and a clearly different
    background.

