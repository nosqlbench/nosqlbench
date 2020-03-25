---
title: 10 Named Scenarios
weight: 10
---

# Named Scenarios

There is one final element of a yaml that you need to know about: _named scenarios_.

You can provide named scenarios for a workload like this:

```yaml
# contents of myworkloads.yaml
scenarios:
 default:
  - run driver=diag cycles=10 alias=first-ten
  - run driver=diag cycles=10..20 alias=second-ten
 longrun:
  - run driver=diag cycles=10M
```

This provides a way to specify more detailed workflows that users may want
to run without them having to build up a command line for themselves.

There are two ways to invoke a named scenario.

```
# runs the scenario named 'default' if it exists, or throws an error if it does not.
nb myworkloads
# or
nb myworkloads default

# runs the named scenario 'longrun' if it exists, or throws an error if it does not.
nb myworkloads longrun
```

## Named Scenario Discovery

Only workloads which include named scenarios will be easily discoverable by users
who look for pre-baked scenarios.

## Parameter Overrides

You can override parameters that are provided by named scenarios. Any parameter
that you specify for the name scenario will override parameters of the same name
in the named scenario's script.

## Examples

```yaml
# example-scenarios.yaml
scenarios:
 default:
  - run cycles=3 alias=A driver=stdout
  - run cycles=5 alias=B driver=stdout
bindings:
 cycle: Identity()
 name: NumberNameToCycle()
statements:
 - cycle: "cycle {cycle}\n"
```
