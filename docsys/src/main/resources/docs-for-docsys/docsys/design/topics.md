# Topics in Files

# DEPRECATED

# DEPRECATED


Since DocSys is meant to make it easy to add docs to a system
without doing much extra management, the ability to label and find
topics of interest must be built in.

That means that topics will be mostly provided by convention, where
the primary content simply _has_ a topic due to it's proper labeling.
With Markdown being the primary format for most or all of the primary
documentation, topics will be taken from specific places:

1. The directory structure containing the markdown files.
2. The files containing the markdown.
3. The headings within the markdown.

## Topic Structure

Of the three sources of information above, different facets of topics
are represented. Anything under a given directory name will automatically
be assigned to a category of that directory name. Further, if a directory
has a markdown file with the right content, it may specifically set
the category name. This is a future feature that will likely be called
_markdown annotations_ in DocSys. For now, the directory name itself is
simply used as a category name.

Each markdown file has a filename, but we aren't worried about that. The file
name itself is not significant to the content, and can be used to organize
the ordering of files in directory. For example, it is suitable to call
a markdown file `00_somecontent.md` since we aren't paying attention to the
file name. However, the file should have a canonical topic associated with it,
for when you want to find files that speak to specific topics as a whole.
These topics are called `File Topic`s, and are automatically given the topic
name of the first heading in the file.

## Headings as Topics

Within a markdown file, the level of heading determines how topics are nested together.
Any heading that is at a deeper level than the one that came immediately before it
is considered a sub-topic of that heading.

## Views of Topics

There are multiple ways to view the topics in the content provided:

### Topic List

As a list of topics, irrespective of the source and structure. In this form,
all the topics are presented as a flat list. This is suitable for indexing
topics, for example.

### File Topics

As a list of file topics. This view of topics provides all the topic names
that appear first for each file. This is suitable for finding or browsing
a set of round topics which are each round and cohesive within a single
markdown file. These topics *do* include the subtopics per file, but they
are provided as properties of the top level topics as given.

Because the file topics take the first heading as the name as the topic
name for the whole file, the first heading is not represented duplicitously
within the sub topics owned by a file topic. (It is simply promoted to the
file level)

### Header Topics

As a header topic tree. in this form, all of the topics that are found in any
markdown file are included in a hierarchic form. However, a file may contain
multiple topics at the same top level. This form does not include all the
file topics above as containers of their respective header topics. All headers
at the top-most level of topics are provided, even if there are multiple topics
that tie for the top-most level in a given file.

Since header structure within a markdown file is not generally strictly kept,
the header levels are *scrunched* before being presented. This means that,
starting from the top, each header that is a direct subtopic is hoisted up
to the next available level number such that there are no skipped levels. This
makes presentation of topics much easier for rendering.

## Naming

Within the view model, these elements are named

- topics.list
- topics.headers
- topics.files







