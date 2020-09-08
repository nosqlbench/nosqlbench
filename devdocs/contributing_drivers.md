# Contributing a Driver

Drivers in NoSQLBench are how you get the core machinery to speak a
particular protocol or syntax. At a high level, a NB driver is responsible
for mapping a data structure into an executable operation that can be
called by NoSQLBench.

Internally, the name `ActivityType` is used to name different high level
drivers within NoSQLBench. This should avoid confusion with other driver
terms.

Drivers in NoSQLBench are separate maven modules that get added to the
main nb.jar artifact (and thus the AppImage binary). For now, all these
driver live in-tree, but we may start allowing these to be packaged as
separate jar files. Let us know if this would help your integration
efforts.

## Start with Examples

There are a few activity types which can be used as templates. It is
recommended that you study the stdout activity type as your first example
of how to use the runtime API. The HTTP driver is also fairly new and thus
uses the simpler paths in the API to construct operations. Both of these
recommended as starting points for new developers.

## Consult the Dev Guide

The developers guide is not complete, but it does call out some of the
features that any well-built NB driver should have. If you are one of the
early builders of NB drivers, please help us improve the dev guide as you
find things you wish you had known before, or ways of getting started.

The developer's guid is a work in progress. It lives under
devdocs/devguide in the root of the project.

