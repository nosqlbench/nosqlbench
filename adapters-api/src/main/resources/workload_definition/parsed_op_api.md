# ParsedOp API

In the workload template examples, we show statements as being formed from a string value. This is a
specific type of statement form, although it is possible to provide structured op templates as well.

**The ParsedOp API is responsible for converting all valid op template forms into a consistent and
unambiguous model.** Thus, the rules for mapping the various forms to the command model must be
precise. Those rules are the substance of this specification.

## Op Synthesis

The method of turning an op template, some data generation functions, and some seed values into an
executable operation is called *Op Synthesis* in NoSQLBench. This is done in incremental stages:

1. During activity initialization, NoSQLBench parses the workload template and op templates
   contained within. Each active op template (after filtering) is converted to a parsed command.
2. The NB driver uses the parsed command to guide the construction of an OpDispenser<T>. This is a
   dispenser of operations that can be executed by the driver's Action implementation.
3. When it is time to create an actual operation to be executed, unique with its own procedurally
   generated payload and settings, the OpDispenser<T> is invoked as a LongFunction<T>. The input
   provided to this function is the cycle number of the operation. This is essentially a seed that
   determines the content of all the dynamic fields in the operation.

This process is non-trivial in that it is an incremental creational pattern, where the resultant
object is contextual to some native API. The command API is intended to guide and enable op
synthesis without tying developers' hands.

## Command Fields

A command structure is intended to provide all the fields needed to fully realize a native
operation. Some of these fields will be constant, or *static* in the op template, expressed simply
as strings, numbers, lists or maps. These are parsed from the op template as such and are cached in
the command structure as statics.

Other fields are only prescribed as recipes. This comes in two parts: 1) The description for how to
create the value from a binding function, and 2) the binding point within the op template. Suppose
you have a string-based op template like this:

```yaml
ops:
    - op1: select * from users where userid={userid}
      bindings:
          userid: ToString();
```

In this case, there is only one op in the list of ops, having a name `op1` and a string form op
template of `select * from users where userid={userid}`.

## Parsed Command Structure

Once an op template is parsed into a *parsed command*, it has the state shown in the data structure
schematic below:

```json

{
    "name": "some-map-name",
    "statics": {
        "s1": "v1",
        "s2": {
            "f1": "valfoo"
        }
    },
    "dynamics": {
        "d1": "NumberNameToString()"
    },
    "captures": {
        "resultprop1": "asname1"
    }
}
```

If either an **op** or **stmt** field is provided, then the same structure as above is used:

```json

{
    "name": "some-string-op",
    "statics": {
    },
    "dynamics": {
        "op": "select username from table where name userid={userid}"
    },
    "captures": {
    }
}
```

The parts of a parsed command structure are:

### command name

Each command knows its name, just like an op template does. This can be useful for diagnostics and
metric naming.

### static fields

The field names which are statically assigned and their values of any type. Since these values are
not generated per-op, they are kept separate as reference data. Knowing which fields are static and
which are not makes it possible for developers to optimize op synthesis.

### dynamic fields

Named bindings points within the op template. These values will only be known for a given cycle.

### variable captures

Names of result values to save, and the variable names they are to be saved as. The names represent
the name as it would be found in the native driver's API, such as the name `userid`
in `select userid from ...`. In string form statements, users can specify that the userid should be
saved as the thread-local variable named *userid* simply by tagging it
like `select [userid] from ...`. They can also specify that this value should be captured under a
different name with a variation like `select [userid as user_id] from ...`. This is the standard
variable capture syntax for any string-based statement form.

# Resolved Command Structure

Once an op template has been parsed into a command structure, the runtime has everything it needs to
know in order to realize a specific set of field values, *given a cycle number*. Within a cycle, the
cycle number is effectively a seed value that drives the generation of all dynamic data for that
cycle.

However, this seed value is only known by the runtime once it is time to execute a specific cycle.
Thus, it is the developer's job to tell the NoSQLBench runtime how to map from the parsed structure
to a native type of executable operation suitable for execution with that driver.

# Interpretation

A command structure does not necessarily describe a specific low-level operation to be performed by
a native driver. It *should* do so, but it is up to the user to provide a valid op template
according to the documented rules of op construction for that driver type. These rules should be
clearly documented by the driver developer.

Once the command structure is provided, the driver takes over and maps the fields into an executable
op -- *almost*. In fact, the driver developer defines the ways that a command structure can be
turned into an executable operation. This is expressed as a *Function<CommandTemplate,T>* where T is
the type used in the native driver's API.

How a developer maps a structure like the above to an operations is up to them. The general rule of
thumb is to use the most obvious and familiar representation of an operation as it would appear to a
user. If this is CQL or SQL, then recommend use that as the statement form. If it GraphQL, use that.
In both of these cases, you have access to

## String Form

Basic operations are made from a statement in some type of query language:

```yaml
ops:
    - stringform: select [userid] from db.users where user='{username}';
      bindings:
          username: NumberNameToString()
```

## Structured Form

Some operations can't be easily represented by a single statement. Some operations are built from a
set of fields which describe more about an operation than the basic statement form. These types of
operations are expressed to NoSQLBench in map or *object* form, where the fields within the op can
be specified independently.

```yaml
ops:
    - structured1:
          stmt: select * from db.users where user='{username}}';
      prepared: true
      consistency_level: LOCAL_QUORUM
      bindings:
          username: NumberNameToString();
    - structured2:
          cmdtype: "put"
          namespace: users
          key: { userkey }
          body: "User42 was here"
      bindings:
          userkey: FixedValue(42)
```

In the first case, the op named *structured1* is provided as a string value within a map structure.
The *stmt* field is a reserved word (synonomous with op and operation). When you are reading an op
from the command API, these will represented in exactly the same way as the stringform example
above.

In the second case,

In the second, the op named *structured form* is provided as a map. Both of these examples would
make sense to a user, as they are fairly self-explanatory.

Op templates may specify an op as either a string or a map. No other types are allowed. However,
there are no restrictions placed on the elements below a map.

The driver developer should not have to parse all the possible structural forms that users can
provide. There should be one way to access all of these in a consistent and unambiguous API.

## Command Structure

Here is an example data structure which illustrates all the possible elements of a parsed command:

```json

{
    "statics": {
        "prepared": "true",
        "consistency_level'"
    }
}
```

Users provide a template form of an operation in each op template. This contains a sketch of what an
operation might look like, and includes the following optional parts:

- properties of the operation, whether meta (like a statement) or payload content
- the binding points where generated field values will be injected
- The names of values to be extracted from the result of a successful operation.

## Statement Forms

Sometimes operations are derived from a query language, and are thus self-contained in a string
form.

When mapping the template of an operation provided by users to an executable operation in some
native driver with specific values, you have to know

* The s
* The substance of the operation: The name and values of the fields that the user provides as part
  of the operation

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
