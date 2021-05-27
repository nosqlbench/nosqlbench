# Command API

Command templates are the third layer of workload templating. As described in other spec documents,
the other layers are:

1. [Workload level templates](templated_workloads.md) - This specification covers the basics of a
   workload template, including the valid properties and structure.
2. [Operation level templates](templated_operations.md) - This specification covers how operations
   can be specified, including semantics and structure.
3. Command level templates, explained below. These are the detailed views of what goes into an op
   template, parsed and structured in a way that allows for efficient use at runtime.

Users do not create command templates directly. Instead, these are the *parsed* form of op templates
as seen by the NB driver. The whole point of a command template is to provide crisp semantics and
structure about what a user is asking a driver to do. Command Template

Command templates are essentially schematics for an operation. They are a structural interpretation
of the content provided by users in op templates. Each op template provided can be converted into a
command template. In short, the op template is the form that users tend to edit in yaml or provided
as a data structure via scripting. **Command templates are the view of an op template as seen by an
NB driver.**

```
### Command Templates

Command templates are part of the workload API.

There exists a need to provide op templates to a myriad of runtime APIs,
and thus it has to be flexible enough to serve them all.

1. In some cases, an operation is based on a query language where the
   query language itself encodes everything needed for specific operation.
   SQL queries are like this. This is a nice simplification, but it is not
   realistic for systems build on modern distributed principles.
2. In most cases, you have both an operation and some qualifying rules
   about how the operation should be handled, such as consistency level.
   Thus, there is a need to provide parameters which can decorate
   operations.
3. In some cases, you have a payload for your operation which is not based
   on a query language, but instead on an object with fields, or a verb
   which determines what other fields are needed. This structure is better
   described as a *command* than a *statement*.
4. Finally, you must support separate both of the latter cases where the
   command or operations is defined in some pseudo-structured way, but it
   also has *separately* a set of qualifying parameters which are
   considered orthogonal, or at least separate from the meaning of the
   operation itself.

To address the full set of these mapping requirements, a type has been
added to NB which provides a structured and pre-baked version of a
resolvable command -- the CommandTemplate.

This type provides a view to the driver builder of all the fields
specified by the user, whether as encoded as a string, such
as `select row from ...`, or by a set of properties such
as `{"verb":"get",
"id":"2343"}`. It also exposes the parameters separately if provided.

### Static vs Dynamic command fields

Further, for each field in the command template, the driver implementor
knows whether this was provided as a static value or one that can only be
realized for a specific cycle (seed data). Thus, it is possible for
advanced op mapping implementations to optimize the way that new
operations are synthesized for efficiency.

For example, if you know that you have a command which has no dynamic
fields in its command template, then it is possible to create a singleton
op template which can simply be re-used. A fully dynamic command template,
in contrast, may need to be realized dynamically for each cycle, given
that you don't know the value of the fields in the command until you know
the cycle value.



```
