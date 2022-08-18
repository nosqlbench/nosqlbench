## Naming Change
* binary name
    - `nb`
    - `nb5`

## Runtime Requirements
* Java 17 + GC improvements
    - woo ZGC
* Deprecated old drivers (conflicts, etc)
    - version conflicts (removing cql drivers 1.9.* 3.*)

## new Docs
javadoc site
developer guide
first-time-user guide
Ideally:
* imported bindings details
* imported plugins details
* imported addons details
* imported app details
* imported scenario script details
* imported workload details
* imported format details


## Added Drivers
- cql driver 4
- dynamodb
- mongodb
- others refreshed

Any driver carried forward has been updated to the DriverAdapter API.
Any driver which is explicitly incompatible with newer driver versions is deprecated and removed.
This includes drivers which would require using Maven shading to avoid conflicts.
This puts NB on a footing to be "Modular Jar" compatible, which is a step towards being fully modular in the jpms sense.

## Bundled Scenario Scripts
- stepup
- optimo
- findmax redux

## Removed Drivers

## New command line options
--docker-prom-retention-days=183d
--list-scripts
--list-apps

## New help topics
## New scripting plugins
- globalvars
- fileaccess
- csvoutput
- shutdown
- s3uploader

## URL support
- s3 (any usage of an S3 URL should work effectively like an equivalent http* URL)

## New binding functions
- SaveGlobalVars + Expr updates
- DecimalFormat
- DateRangeDuring
- DateRangeFunc
- DateRangeOnOrAfter
- DateRangeOnOrBefore
- DateRangeParser
- double -> ToBigDecimal
- int -> ToBigDecimal
- long -> ToBigDecimal
- String -> ToBigDecimal
- distinct HashRange vs HashInterval functions
- ToByteBuffer
- long -> FixedValues() -> double
- String -> ReplaceAll(...) -> String
- String -> ReplaceRegex(...) -> String
- long -> HashRangeScaled(...) -> int
- long -> HashRangeScaled(...) -> long
- int -> HashRangeScaled(...) -> int
- Object -> ToJSON -> String
- Object -> ToJSONPretty -> String
- long -> ByteBufferSizedHashed -> ByteBuffer
- long -> ByteBufferToHex -> String
- long -> HashedByteBufferExtract -> ByteBuffer
- long -> HashedCharBufferExtract -> CharBuffer
- CharBuffer -> ToByteBuffer() -> ByteBuffer
- String -> ToCharBuffer() -> String
- long -> CharBufferExtract() -> CharBuffer
- long -> CharBufImage() -> String
- long -> CurrentEpochMillis() -> long
- long -> ElapsedNanoTime() -> long
- double -> LongStats() -> double
- long -> ThreadNum() -> long
- long -> ThreadNum() -> int
- String -> EscapeJSON() -> String
- long -> Expr(...) -> Object
- long -> ToJavaInstant() -> (Java) Instant
- long -> ToJodaInstant() -> (Joda) Instant
- long -> ToLocalTime() -> LocalTime


## Optimizations
- avoid preparing ops which are not selected
- skip over ops with ratio 0

## UX Improvements
- secure field indirection, username, userfile, password, passfile,
- op templates can pull from activity params for some fields
- All yaml can be in mapping form (everything is named)
- All elements in a yaml can have a description field
- Uniform Workload Format
- Uniform Error Handling
- Uniform Driver API
  - this means:
    - standard core activity params across all drivers, like 'retry'
    - standard metrics across all drivers
    - standard error handler support
    - standard op meta-behaviors, like start-timers and stop-timers across all drivers
    - interoperable data between drivers
    - standard diagnostic tools across all drivers
* Named Scenario subsections
    - `nb5 namedscenario.yaml`
    - `nb5 namedscenario.yaml default.schema`
    - `nb5 namedscenario.yaml [<name>]`
    - `nb5 namedscenario.yaml <scenarioname>.<stepname>`
* Multiple named scenarios can be chained on the command line, just like any command:
  - `nb5 workloadfile scenario scenario2 scenario3.step1`
- Uniform Workload format
* In-Line Bindings
    - `{bindingname}`
    - `{{NumberNameToString()}}`
- anything can be an op template, even a command line option
    - `nb5 run driver=cql op="select * from baselines.keyvalue where foo='{{ToString()}} limit 10"`
* type-safe parameters
    - extraneous parameters are verboten
    - parameter types are adapted as needed or an error is thrown
    - auto-suggested alternatives based on Levenshtein distance
* improved progress meter
    - fine-grained
    - based on actual op metrics in real time instead of internal batching state
* open-ended binding structures now support multi-layer scaffolding of generated values with a
  mix of static and dynamic values. This means you can write templates for JSON which look like
  the payload you want to send.
  * supports all combinations of lists and maps and strings, with interspersed dynamic values
    for individual elements or as collection generators, with literals, templated strings, or
    direct binding references, and in-line bindings recipes.
* argsfile allows defaults to be set programmatically or on the command line.
* tag filtering conjugates
* auto-injected statement block and statement name tags.
  - this means: You can now construct filters for specific blocks or statements simply by
    knowing their name:
  - `tags=block:schema` or `tags='main-.*'`
* safe usage of activity params and template vars are compatible, but may not be ambiguous. This
  means that if you have a template variable in myworkload.yaml, it must be distinctly named
  from any valid activity parameters, or an error is thrown. This eliminates a confusing source
  of ambiguity.
* ANSI color support in some places
* template vars can set defaults the first time they are seen

### Scripting
- scenario.stop(...) now allows regexes for activity aliases


## Core machinery & API Improvements
- dynamic lib loading via jars
- startup logging now captures basic system hardware details and NB version, to assist in
  troubleshooting
- redesigned developer API around op template, op mapper, op dispenser, and parsed op types.
    - This means:
      - ParsedOp - op template -> op mapping -> op dispensing follows an incremental path of
        construction, allowing functional programming methods to be used *only* when needed.
      - This means that you only pay for what you use or change. Op builder methods are only
        added to the construction logic when you need to digress from the defaults.
    * simpler and more powerful API for driver developers
        - consolidated logic into core machinery
        - thin out and focus the driver APIs
        - pick concepts and API terms which are very responsibility focused
            - Op Mapper
            - Op Dispenser
            - ParsedOp (mapping from op templates to native driver calls)
    * support for op generators, or ops that inject other ops into the activity. (LWT
      statement retry, client-side join, etc)
    * Type-And-Target op template convention which represents special op types and their primary
      payload template as a key and value.

- drivers are specified per-op. (The activity param just sets the default)
- native driver contexts, known as a driver `space` allows for instancing native drivers in a data-driven way

## Driver Improvements
* modular diag driver
* cqld4 driver
  * all statement forms are supported
  * allows file or parameter-based configuration
  * some backward support for classic nb cql driver options
  * op templates can support any valid statement builder option

## Bundled Applications

Bundled apps have been part of NoSQLBench for some time, but these were mostly used for
behind-the-scenes work, like generating docs for binding functions. Now, these apps are
discoverable and usable by anybody who runs the `--list-apps` command.
These fall into roughly two categories: 1) NB utilities which can be used to slice and dice
test data and 2) driver-specific utilites which are useful when testing or analyzing a specific
type of system.

- `--list-apps`
- `nb5 <appname> -h`
- `nb5 <appname> ...`

### cqlgen

cqlgen - takes schema.cql tablestats -> workload.yaml
- obfuscation
- weighting by op ratios from tablestats
- point to docs ->

sstablegen

* yaml+nb version checks
    - `min_version: "4.17.15"`


* Mac M1 support
    - as of 08/15, all Mac M1 systems should be supported for the.jar and the docker image

review:
- 7578e91d773a9ea8113250899ef46b7aacf95e70
- 392bbcc5954019ae58956850c980646cef14a1f7
- b6627943e2aed2a80ba1aa13d0a929c1ed04b685
- 394580d66c107127cc68f88cdc64a59e9c481d43
- 0330c18a7ba0904b3b3420b94416b04ee73dd7fb
- 11dd8f62daf3d1603b75cdd85fbb872dbfaac111
