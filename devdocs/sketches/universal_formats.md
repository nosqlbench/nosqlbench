NoSQLBench has lots of configurable parameters. The more it is used, the more ways we add for specifying, templating, and indirecting values in various places.

This is an attempt to clarify what patterns are currently supported, what a more consistent view would look like, and a plan for getting there without disrupting existing usage scenarios.

## Configuration Points

These represent the known places that a user might need to provide configuration or template values:

* CLI options
    * global parameters - options which affect the whole process, including any scenarios or activities within
    * component configurations - composable behaviors such as Annotations, which may support nested config details
* Script parameters - named parameters provided directly to a scenario script
* Scenario command parameters (including Activity parameters) - named parameters which accessorize activity commands or other scenario commands like `waitfor`
* Op templates in YAML - The field values used to construct op templates
* Driver-specific configuration files - Separate configuration files provided for a driver, such as the pulsar `config.properties` file

Each of these provides an place for a user to specify the behavior of NoSQLBench, and each generally has a different type of flexibility needed. However, there is some overlap in these usage scenarios.

## Indirection Methods

* On NB CLI, filepath arguments are handed to NBIO, which supports some indirection
* In NBConfig logic, the `IMPORT{<nbio path>}` format is allowed, which defers directly to NBIO
* In CQL driver options, `passfile:...` is supported
* In NBEnvironment, the `${name}` and `$name` patterns are supported
* In YAML op templates, any field containing `{name}` is presumed to be a dynamic variable
* In YAML op glue, any field containing `{{name}}` is presumed to be a capture point
* In YAML op glue any field containing `[[...]]` or `[...]` has special meaning.
* In YAML template variables, `<<name:value>>` or `TEMPLATE(name,value)` are supported
* In HTTP driver URl patterns, `E[[...]]` is taken as a URL-Encoded section.


All of these are different types of indirection, and should be distinct in some cases. Yet, there is room for simplification.

## Conventions

Where possible, idiomatic conventions should be followed, such as '${...}' for environment
variables.

## Usage Matrix

The configuration points and the indirection methods should be analyzed for compatibility and
fit on a matrix.

## Sanitization and Safety

The rules for parsing and marshaling different types should be robust and clearly documented for
users.

## Controlling Indirection

You should be able to turn some methods on or off, like environment variables.

## Proposed Universal Form

- `${{<varname>}}`
- `${{<provider>:<varname>}}`




