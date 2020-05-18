# Linearized Operations

NOTE: This is a sketch/work in progress and will not be suitable for earnest review until this notice is removed.

Thanks to Seb and Wei for helping this design along with their discussions along the way.

See https://github.com/nosqlbench/nosqlbench/issues/136

Presently, it is possible to stitch together rudimentary chained operations, as long as you already know how statement
sequences, bindings functions, and thread-local state work. This is a significant amount of knowledge to expect from a
user who simply wants to configure chained operations with internal dependencies.

The design changes needed to make this easy to express are non-trivial and cut across a few of the extant runtime
systems within nosqlbench. This design sketch will try to capture each of the requirements and approached sufficiently
for discussion and feedback.

# Sync and Async

## As it is: Sync vs Async

The current default mode (without `async=`) emulates a request-per-thread model, with operations being planned in a
deterministic sequence. In this mode, each thread dispatches operations from the sequence only after the previous one is
fully completed, even if there is no dependence between them. This is typical of many applications, even today, but not
all.

On the other end of the spectrum is the fully asynchronous dispatch mode enabled with the `async=` option. This uses a
completely different internal API to allow threads to juggle a number of operations. In contrast to the default mode,
the async mode dispatches operations eagerly as long as the user's selected concurrency level is not yet met. This means
that operations may overlap and also occur out of order with respect to the sequence.

Choosing between these modes is a hard choice that does not offer a uniform way of looking at operations. As well, it
also forces users to pick between two extremes of all request-per-thread or all asynchronous, which is becoming less
common in application designs, and at the very least does not rise to the level of expressivity of the toolchains that
most users have access to.

## As it should be: Async with Explicit Dependencies

* The user should be able to create explicit dependencies from one operation to another.
* Operations which are not dependent on other operations should be dispatched as soon as possible within the concurrency
  limits of the workload.
* Operations with dependencies on other operations should only be dispatched if the upstream operations completed
  successfully.
* Users should have clear expectations of how error handling will occur for individual operations as well
  as chains of operations.

# Dependent Ops

We are using the phrase _dependent ops_ to capture the notions of data-flow dependency between ops (implying
linearization in ordering and isolation of input and output boundaries), successful execution, and data sharing within
an appropriate scope.

## As it is: Data Flow

Presently, you can store state within a thread local object map in order to share data between operations. This is using
the implied scope of "thread local" which works well with the "sequence per thread, request per thread" model. This
works because both the op sequence as well as the variable state used in binding functions are thread local.

However, it does not work well with the async mode, since there is no implied scope to tie the variable state to the op
sequence. There can be many operations within a thread operating on the same state even concurrently. This may appear to
function, but will create problems for users who are not aware of the limitation.

## As it should be: Data Flow

* Data flow between operations should be easily expressed with a standard configuration primitive which can work across
  all driver types.
* The scope of data shared should be

The scope of a captured value should be clear to users

## As it is: Data Capture

Presently, the CQL driver has additional internal operators which allow for the capture of values. These decorator
behaviors allow for configured statements to do more than just dispatch an operation. However, they are not built upon
standard data capture and sharing operations which are implemented uniformly across driver types. This makes scope
management largely a matter of convention, which is ok for the first implementation (in the CQL driver) but not as a
building block for cross-driver behaviors.

# Injecting Operations

## As it is: Injecting Operations

Presently operations are derived from statement templates on a deterministic op sequence which is of a fixed length
known as the stride. This follows closely the pattern of assuming each operation comes from one distinct cycle and that
there is always a one-to-one relationship with cycles. This has carried some weight internally in how metrics for cycles
are derived, etc. There is presently no separate operational queue for statements except by modifying statements in the
existing sequence with side-effect binding assignment. It is difficult to reason about additional operations as
independent without decoupling these two into separate mechanisms.

## As it should be: Injecting Operations



## Seeding Context

# Diagrams
![idealized](idealized.svg)

## Op Flow

To track


 Open concerns

- before: variable state was per-thread
- now: variable state is per opflow
- (opflow state is back-filled into thread local as the default implementation)

* gives scope for enumerating op flows, meaning you opflow 0... opflow (cycles/stride)
* 5 statements in sequence, stride=5,

-  scoping for state
- implied data flow dependence vs explicit data flow dependence
- opflow retries vs op retries

discussion

```yaml
bindings:
 yesterday: HashRange(0L,1234234L);
statements:
 - s1-with-binding:    select [userid*] from foobar.baz where day=23
 - s2-with-binding:    select [userid],[yesterday] from accounts where id={id} and timestamp>{yesterday}
 - s3-with-dependency: select login_history from sessions where userid={[userid]}
 - rogue-statement:    select [yesterday] from ...  <--- WARN USER because of explicit dependency below
 - s4:                 select login_history from sessions where userid={[userid]} and timestamp>{yesterday}
 - s5:                 select login_history from sessions where userid={[userid]} and timestamp>{[s2-with-binding/yesterday]}
```

## Dependency Indirection

## Error Handling and DataFlow Semantics

## Capture Syntax

Capturing of variables in statement templates will be signified with `[varname]`. This examples represents the simplest
case where the user just wants to capture a varaible. Thus the above is taken to mean:

- The scope of the captured variable is the OpFlow.
- The operation is required to succeed. Any other operation which depends on a `varname` value will be skipped and
  counted as such.
- The captured type of `varname` is a single object, to be determined dynamically, with no type checking required.
- A field named `varname` is required to be present in the result set for the statement that included it.
- Exactly one value for `varname` is required to be present.
- Without other settings to relax sanity constraints, any other appearance of `[varname]` in another active statement
  should yield a warning to the user.

All behavioral variations that diverge from the above will be signified within the capture syntax as a variation on the
above example.

## Inject Syntax

Similar to binding tokens used in statement templates like '{varname}', it is possible to inject captured variables into
statement templates with the `{[varname]}` syntax. This indicates that the user explicitly wants to pull a value
directly from the captured variable. It is necessary to indicate variable capture and variable injection distinctly from
each other, and this syntax supports that while remaining familiar to the bindings formats already supported.

The above syntax example represents the case where the user simply wants to refer to a variable of a given name. This is
the simplest case, and is taken to mean:

- The scope of the variable is not specified. The value may come from OpFlow, thread, global or any scope that is
  available. By default, scopes should be consulted with the shortest-lived inner scopes first and widened only if
  needed to find the variable.
- The variable must be defined in some available scope. By default, It is an error to refer to a variable for injection
  that is not defined.
- The type of the variable is not checked on access. The type is presumed to be compatible with any assignments which
  are made within whatever driver type is in use.
- The variable is assumed to be a single-valued type.

All behavioral variations that diverge from the above will be signified within the variable injection syntax as a
variation on the above syntax.

## Scenarios to Consider

basic scenario: user wants to capture each variable from one place

advanced scenarios:
-  user wants to capture a named var from one or more places
-  some ops may be required to complete successfully, others may not
-  some ops may be required to produce a value
-  some ops may be required to produce multiple values


