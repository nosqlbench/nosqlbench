# NoSQLBench Spaces

The _spaces_ feature of NoSQLBench allows for users to control how native
drivers are instanced across cycles. This feature is built-in to the core
design of NoSQLBench, so it should work consistently across all adapters.

This section explains how it works and provides some basic practical
guidance for testers.

## Space Basics

Spaces are basically enumerated instances of driver state. Each space holds a
separate instance of a native driver. For example, with the CQL driver adapter,
each cluster object has its own space. You can think of _space_ in general as
_driver instance_.

## Using Spaces

You don't have to enable anything special to use the spaces feature. It is always "on", but by
default the instancing of a native driver is exactly as expected -- once per activity.

However, if you decide to use it, the only thing you have to do is add a `space`
field to any op template. A very good recipe for this is `Mod(10L)`, which simply
takes the value of the current cycle mod 10 as the index of the space to use. To
get the best results, do not convert the binding into a String or other non-numeric type.

## Space Lifecycles

Internally, each driver adapter has a cache of spaces of the type used for that
particular driver adapter. The keys for this cache are integers. When a space
is needed for a given index value, it is lazily created and stored in the cache
using LWT operations. Any binding which produces a numeric value type can provide
the index (instance name) for the space to use. If the space does not exist, it is
lazily created in a thread-safe way and put in the cache. There are no methods
for explicitly purging entries in the cache, nor expiring them, so any space
created will live for the life of the nb process. However, spaces are instrumented
for proper shut-down, so any space which needs to be closed gracefully can be.

## Space Binding in Depth

If the binding for a space produces a numeric value (any java.lang.Number), then the
whole-number integer portion of it is used as the index.

If any other type is used, then an intermediary function is added to the naming logic which
down-converts that type into an integer by caching equivalent values. This mode is slower
and very inefficient, so it is advised to use numeric bindings anywhere you need to have
a great number of spaces (sessions, clients, etc.)

## Sessions and Ops

Operations which must be attached to a given session to be valid, it is the responsibility
of the adapter developer to ensure that any such objects are present. As long as the space
lookup functions are relied on as provided, this should be automatic. In other words, if
"the driver for a given cycle" is not present, then it should be created automatically, and
any associated operations within that cycle should already be implicitly attached to the same
session.

For operations which may span multiple cycles, session-attached operations should cascade the
session ownership in additional operations such that connected access patterns remain connected
to the original driver instance. While this may be implied by default, it is an explicit
design requirement that adapter authors must observe, and that testers may then rely on.
