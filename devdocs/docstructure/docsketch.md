# Doc System

This is a consolidation of all the doc system work thus far. This draft is meant to outline the basic features of the
doc system at a high level, but with suitable detail for an initial refactoring. In general this builds on existing work
in the doc system but with some adaptations for current needs, across CLI, apps, and reference material.

## Content Organization

All content loaded from any source is organized internally into a tree of sections by:

* Front Matter Topics
* Header Level

The source path of content does not matter. However, each unit of source material is considered its own section, with
zero or more additional subsections.

A root section is the container of all sections which are not homed under another section.

## Headings

In some cases, it is appropriate to consolidate individual docs into larger views. In order to facilitate this, all
sections within markdown structure are enumerated according to

- The front matter in the content source, specifically the topics assigned
- The heading structure within the doc

Thus, when the doc content is processed into the cohesive view needed by a user, all sections of all provided content
are cross-referenced and organized into sections.

The location of a document within the source filesystem or archive is not important. Topics

## Content Naming



## Content Searching
