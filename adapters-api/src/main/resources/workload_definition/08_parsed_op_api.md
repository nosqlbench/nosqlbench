# ParsedOp API

In the workload template examples, we show statements as being formed from a string value. This is a
specific type of statement form, although it is possible to provide structured op templates as well.

**The ParsedOp API is responsible for converting all valid op template forms into a consistent and
unambiguous model.** Thus, the rules for mapping the various forms to the command model must be
precise. Those rules are the substance of this specification.

## Op Synthesis

Executable operations are _created_ on the fly by NoSQLBench via a process called _Op Synthesis_.
This is done incrementally in stages. The following narrative describes this process in logical
stages. (The implementation may vary from this, but it explains the effects, nonetheless.)

Everything here happens **during** activity initialization, before the activity starts running
cycles:

1. *Template Variable Expansion* - If there are template variables, such as
   `TEMPLATE(name,defaultval)` or `<<name:defaultval>>`, then these are expanded
   according to their defaults and any overrides provided in the activity params. This is a macro
   substitution only, so the values are simply interposed into the character stream of the document.
2. *Jsonnet Evaluation* - If the source file was in jsonnet format (the extension was `.jsonnet`)
   then it is interpreted by sjsonnet, with all activity parameters available as external variables.
3. *Structural Normalization* - The workload template (yaml, json, or data structure) is loaded
   into memory and transformed into a standard format. This means taking various list and map
   forms at every level and converting them to a singular standard form in memory.
4. *Auto-Naming* - All elements which do not already have a name are assigned a simple name like
   `block2` or `op3`.
5. *Auto-Tagging* - All op templates are given standard tag values under reserved tag names:
    - **block**: the name of the block containing the op template. For example: `block2`.
    - **name**: the name of the op template, prefixed with the block value and `--`. For example,
      `block2--op1`.
6. *Property De-normalization* - Default values for all the standard op template properties are
   copied from the doc to the block layer unless the same-named key exists. Then the same
   method is applied from the doc layer to the op template layer. **At this point, the op
   templates are effectively an ordered list of data structures, each containing all necessary
   details for use.**
7. *Tag Filtering* - The activity's `tag` param is used to filter all the op templates
   according to their tag map.
8. *Bind Point and Capture Points* - Each op template is now converted into a ParsedOp, which is
   a swiss-army knife of op template introspection and function generation. It is the direct
   programmatic API that driver adapters use in subsequent steps.
    - Any string sequences with bind points like `this has a {bindpoint}` are automatically
      converted to a long -&gt; string function.
    - Any direct references with no surrounding text like `{bindpoint}` are automatically
      converted to direct binding references.
    - Any other string form is cached as a static value.
    - The same process is applied to Lists and Maps, allowing structural templates which read
      like JSON with bind points in arbitrary places.
8. *Op Mapping* - Using the ParsedOp API, each op template is categorized by the active `driver`
   according to that driver's documented examples and type-matching rules. Once the op mapper
   determines what op type a user intended, it uses this information and the associated op
   fields to create an *Op Dispenser*.
9. *Op Sequencing* - The op dispensers are kept as an internal sequence, and installed into a
   [LUT](https://en.wikipedia.org/wiki/Lookup_table) according to their ratios and the specified
   (or default) sequencer. By default, round-robin with bucket exhaustion is used. The ratios
   specified are used directly in the LUT.

When this is complete, you are left with an efficient lookup table which indexes into a set of
OpDispensers. The length of this lookup table is called the _sequence length_, and that value is
used, by default, to set the _stride_ for the activity. This stride determines the size of
per-thread cycle batching, effectively turning each sequence into a thread-safe set of
operations which are serialized, and thus suitable for testing linearized operations with
suitable dependency and error-handling mechanisms. (But wait, there's more!)

## Special Cases

Drivers are assigned to op templates individually, meaning you can specify the driver within an
op template, not even assigning a default for the activity. Further, certain drivers are able to
fill in missing details for op templates, like the `stdout` driver which only requires bindings.

This means that there are distinct cases for configuration which are valid, and these are
checked at initialization time:

- A `driver` must be selected for each op template either directly or via activity params.
- If the whole workload template provided does not include actual op templates **AND** a
  default driver is provided which can create synthetic op templates, it is given the raw
  workload template, incomplete as it is, and asked to provide op templates which have all
  normalization, naming, etc. already done. This is injected before the tag-filtering phase.
- In any case that an actual non-zero list of op templates is provided and tag filtering removes
  them all, an error is thrown.
- If, after tag filtering no op template are in the active list, an error is thrown.

# The ParsedOp

The components of a fully-parsed op template (AKA a ParsedOp) are:

## name

Each ParsedOp knows its name, which is simply the op template name that it was made from. This
is useful for diagnostics, logging, and metrics.

## description

Every named element of a workload may be given a description.

## tags

Every op template has tags, even if they are auto-assigned from the block and op template names.
If you assign explicit tags to an op template, the standard tags are still provided. Thus, it is
an error to directly provide a tag named `block` or `name`.

## bindings

Although bindings are usually defined as workload template level property, they can also be
provided directly as an op field property.

## op fields

The **op** property of an op template or ParsedOp is the root of the op fields. This is a map of
specific fields specified by the user.

### static op fields

Some op fields are simply static values. Since these values are not generated per cycle, they are
kept separate as reference data. Knowing which fields are static and which are not makes it
possible for developers to optimize op synthesis.

### dynamic op fields

Other fields may be specified as recipes, with the actual value to be filled-in once the cycle
value is known. All such fields are known as _dynamic op fields_, and are provided to the op
dispenser as a long function, where the input is always the cycle value and the output is a
type-specific value as determined by the associated binding recipe.

### bind points

This is how dynamic values are indicated. Each bind point in an op template results in some type of
procedural generation binding. These can be references to named bindings elsewhere in the
workload template, or they can be inline.

### capture points

Names of result values to save, and the variable names they are to be saved as. The names represent
the name as it would be found in the native driver's API, such as the name `userid`
in `select userid from ...`. In string form statements, users can specify that the userid should be
saved as the thread-local variable named *userid* simply by tagging it
like `select [userid] from ...`. They can also specify that this value should be captured under a
different name with a variation like `select [userid as user_id] from ...`. This is the standard
variable capture syntax for any string-based statement form.

### params

A backwards-compatible feature called op params is still available. This is another root
property within an op template which can be used to accessorize op fields. By default, any op
field which is not explicitly rooted under the `op` property are put there anyway. This is also
true when there is an explicitly `params` property. However if the op property is provided, then
all non-reserved fields are given to the params property instead. If both the `op` and the
`param` op properties are specified, then no non-reserved op fields are allowed outside of these
root values. Thus it is possible to still support params, but it is **highly** recommended that
new driver developers avoid using this field, and instead allow all fields to be automatically
anchored under the `op` property. This keeps configs terse and simple going forward.

Params may not be dynamic.

# Mapping Rules

A ParsedOp does not necessarily describe a specific low-level operation to be performed by
a native driver. It *should* do so, but it is up to the user to provide a valid op template
according to the documented rules of op construction for that driver type. These rules should be
clearly documented by the driver developer as examples in markdown that is required for every
driver. With this documentation, users can use `nb5 help &lt;driver&gt;` to see exactly how
to create op templates for a given driver.

## String Form

Basic operations are made from a statement in some type of query language:

```yaml
ops:
  - stringform: select [userid] from db.users where user='{username}';
    bindings:
      username: NumberNameToString()
```

# Reserved op fields

The property names `ratio`, `driver`, `space`, are considered reserved by the NoSQLBench runtime.
These are extracted and handled specially by the core runtime.

# Base OpDispenser fields

The BaseOpDispenser, which <s>is</s> will be required as the base implementation of any op
dispenser going forward, provides cross-cutting functionality. These include `start-timers`,
`stop-timers`, `instrument`, and likely will include more as future cross-driver functionality is
added. These fields will be considered reserved property names.

# Optimization

It should be noted that the op mapping process, where user intentions are mapped from op templates to
op dispensers is not something that needs to be done quickly. This occurs at _initialization_
time. Instead, it is more important to focus on user experience factors, such as flexibility,
obviousness, robustness, correctness, and so on. Thus, priority of design factors in this part
of NB is placed more on clear and purposeful abstractions and less on optimizing for speed. The
clarity and detail which is conveyed by this layer to the driver developer will then enable
them to focus on building fast and correct op dispensers. These dispensers are also constructed
before the workload starts running, but are used at high speed while the workload is running.

In essence:
- Any initialization code which happens before or in the OpDispenser constructor should not be
concerned with careful performance optimization.
- Any code which occurs within the OpDispenser#apply method should be as lightweight as is
  reasonable.

