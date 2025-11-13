+++
title = "Op Templates Explained"
description = "Understanding op template design philosophy and structure"
weight = 20
template = "page.html"

[extra]
quadrant = "explanations"
topic = "concepts"
category = "architecture"
tags = ["op-templates", "workloads", "design", "architecture"]
+++

👉 When it comes to building workloads, the op template is where everything comes together.
Understanding op templates in detail will help you bring other features of NoSQLBench into
focus more quickly.

Each activity that you run in an nb5 scenario is built from operations. These operations are
generated at the time they are needed.
They need a blueprint for what an operation looks like, which we provide in the form of a
simple data structure called an _op template_.

The op template is effectively a JSON object or a YAML map. It is simply a data structure.

# De-sugaring

The op template structure is quite flexible in practice, and only requires you to be as detailed
as necessary for a given situation.

For example, you can provide an op template which looks like this:
```yaml
op: "example {{/*Combinations('a-z')*/}} body"
```
or like this:
```yaml
bindings:
  binding1: Combinations('a-z')
blocks:
 warmup-block:
  ops:
   op1:
    op:
     prepared: "example {binding1} body"
```

**These both mean the same thing!** NoSQLBench performs a structural de-sugaring and
normalization, and then applies property de-normalization and overrides. At the end, it's as if
you used a fully-qualified and detailed format, even if you didn't. The reason you might use
the second form over the first is to provide additional op templates or properties when tailing
for more specific situations.

If you want to know why or how nb5 does this, expand the details below. Otherwise, Let's skip to
the next part!

<details>
<summary>Workload Specification Design Philosophy</summary>

## Why?

Why? Because YAML is not fun for most people if we're being really honest. System
designers and developers have a persistent habit of pushing their configuration problems into
pop-markup formats. Yet, without a truly innovative and widely supported alternative, it's still
a not a *bad* choice. There are really not many practical alternatives that are portable,
supported by many languages, and so ubiquitous that they are immediately recognized by most
users. Further, it plays generally well with JSON, a proper subset and close runner-up, which
is also extended by jsonnet.

Even so, there is still a problem to solve: NoSQLBench needs to support trivial testing
scenarios with trivial configuration *AND* advanced testing scenarios with a detailed configuration.
So, **why not both?**

> Simple things should be simple, complex things should be possible.
> ([Alan Kay](https://en.wikiquote.org/wiki/Alan_Kay))

So in terms of tooling, this provides a rich layering of tools which can scale from the
trivial to the sophisticated.

## How?

The rules for this mechanism are part of the nb5
[workload definition](../../reference/workload-yaml/_index.md)
standard, which covers all the details and corner cases. The nb5 runtime handles all the
structural processing for users and developers, so there is little ambiguity about valid or
equivalent forms. This standard is elevated to a tested specification because it is part of the
core nb5 code base, tested for validity with every build. You can only see a documented and
working specification on this site.

The design principles used when building this standard include:
- If it looks like what it does, and it does what it looks like, then it is valid.
- If there is a reasonable chance of ambiguity, then disallow the pattern, or make the user be
  more specific.

</details>

# Valid Forms

👉 The [Workload Basics](../../tutorials/workload-basics/_index.md) tutorial is a great way to learn about op
templates.

What determines if a given op template is valid or not depends on a couple of things: Can it be
recognized according to the [workload definition](../../reference/workload-yaml/_index.md)
standard?
Can it be
recognized by the specified driver as a valid op template, _according to the field names and values?_

In general, you have three places to look for valid op templates. Here they are in order of
preference:

- The built-in workloads. Use the `--list-workloads` and `--copy <workload>` options to discover
  and copy out some examples. These are documented under
  [discovery options](../../reference/cli/options.md).
- The driver documentation. Each driver should provide clear examples that can be pasted right
  into a new workload if you want. Access the documentation for a specific driver with `nb5 help
  <driver>`.
- The [Workload Basics](../../tutorials/workload-basics/_index.md) tutorial.
- Finally, the detailed [workload definition specification](../../reference/workload-yaml/_index.md),
  if you need, for example to see all the possibilities. Developers will generally want to know
  what can be specified, but those who are just using nb5 will get by easily on the examples.

# Template Form

Let's take a look at the longer example again, with line numbers this time:
```yaml
bindings:
  binding1: Combinations('a-z')
blocks:
 warmup-block:
  ops:
   op1:
    op:
     prepared: "example {binding1} body"
```

We see some of the surrounding workload template format, then the op template, and then the
single op field `prepared`. Here is a line-by-line readout of each part:

```
At the root of the workload template, a bindings property sets global bindings.
 One global binding is defined as "Combinations('a-z')", with name binding1.
At the root of the workload template, the blocks property is defined.
 The first block is named warmup-block.
  The ops property for warmup-block is defined.
   The first op template is named op1. Everything under it is called the op template.
    The op template named op1 has an explicity op fields property.
     The first op field of the op template is named "prepared", and it is a string template.
```
Additionally, the string template above might be called a statement. It could just as well be
`select user_id from mytable where token={user_token}` instead of `example {binding1} body`.
When the entirety of an op template is passed as a string like in the very first example, that
string is stored in an op field called `stmt` automatically.

The part of the string that looks like `{binding1}` is called a bind point. Bind points are the
places where you will inject data into the template later to create a fully constructed
synthetic operation. In this case `binding1` is the _bind point name_. It matches up with a
_binding name_ above (also `binding1`), to create a full blueprint for constructing the whole
string when you need it.

As for the longer example, you might notice that this is a fully mapped structure with no lists.
That is, every property is basically a container for a collection of named elements. If you get
lost in the layers, just remember that everything follows this pattern: From the root inward,
the map keys mean _property name_ > _member name_ > _property name_ > _member name_ ...
until the very end where leaf nodes are simply values.

# Synthetic Op Values

The power of an op template comes from the fact that it is a real template. If your op template
contains a string template `select user_id from mytable where token={user_token}`, you can't
take it as it is and send it to the database. Either your client or your server will throw a
syntax error on the `{user_token}` part. What nb5 is excellent at is working with a (nb5) driver to
create a synthetic operation to execute. For native drivers, the nb5 driver (known in the API as
a _DriverAdapter_) interprets the op template structure, and uses the native driver APIs to
behave exactly as an application might. For other nb5 drivers, like stdout, something else may
be done with the op template, like printing out the rendered op template in schematic form. No
native driver is needed to do this.

# Structure!

So far, you've seen a simple op field with a synthetic op value. The `prefix {bindpoint} suffix`
form is a _string_ template. But what about other forms? **You can have any structural form for an
op field, and it will be handled as a synthetic structure, including lists, maps and strings!**

## example

```yaml
ops:
  op1: "{{/*Identity()*/}}"
  op2:
    op:
     - field1
     - "field2 {bindpoint}"
    tags:
      tag1: list-form
    prepared: true
  op3:
    key1: "{value1}"
    key2: "string {value2}"
    params:
      prepared: true
```

This shows a few example op templates.

## op1

The one named `op1` looks like a string template, but it has no prefix nor suffix in the string.
The double curly brace form removes the need to reference a binding by name. It is an anonymous
binding function directly within the bind point. Further, it isn't necessary to put _only a bind
point_ in a string template like this when you are assigning a string value. That happens
automatically. So we use this case to promote this to a direct binding. That means that the type
of value produced by the binding directly will be used. If it needs to be a string, it will anyway.

## op2

The one named `op2` shows that the `op` property of an op template has special significance. If
you want to do anything beyond a trivial string binding, you can use this to explicitly set the
root of the object used for the op fields. This allows for other properties of the op template
to be stored separately and interpreted separately. Besides reserved op template properties
(like `tag`, `bindings`, `params`, etc.), all other keys in the op template will be put in the
params property. Thus, `prepared` is stored as if you had put it under the `params` block.
This is unambiguous because of the explicit `op` definition.

## op3

The third op template example shows the complimentary scenario to having an explicit op property.
The directly defined params property means that any unknown keyword (like `tag`, `bindings`, etc.
) will automatically be stored under an injected `op` field for you. You can think of the `op`
property as the _payload_, and the `params` property as the _metadata_. For protocols and
drivers that can distinguish between these, the distinction is meaningful. For those that don't,
where the whole protocol is described within an JSON object for example, the params field is
useless.

👉 **When you have a trivial op structure with no need for params, you need to specify neither the
`op` nor the `params` property, and all non-reserved keys will automatically be stored in the
`op`.** This is recommended as the convention for all new drivers. Usage of the `params`
property is still supported, but should only be employed by driver developers when it is
strictly necessary.

# Synthesis!

All the op fields can be fully dynamic! However, it is not efficient for everything about an
operation to be undetermined until cycle time. Therefore, driver developers will often require
certain identifying op fields to be static for the purposes of determining op type. The rules
for this are up to each driver. For example, with the [cqld4 driver](../../reference/drivers/cqld4.md)
, you can
specify that you want a raw, prepared, or other type of statement to be executed, but each op
template must pick one. This is necessary to allow activities to pre-compute or pre-bake much
of the op synthesis logic as it can. This can be done much more efficiently if at least the
type of operation doesn't change from cycle to cycle.
