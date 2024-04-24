---
title: datetime functions
weight: 20
---

Functions in this category know about times and dates, datetimes, seconds or millisecond epoch times, and so forth.

Some of the functions in this category are designed to allow testing of UUID types which are usually designed to avoid
determinism. This makes it possible to test systems which depend on UUIDs but which require determinism in test data.
This is strictly for testing use. Breaking the universally-unique properties of UUIDs in production systems is a bad
idea. Yet, in testing, this determinism is quite useful.

