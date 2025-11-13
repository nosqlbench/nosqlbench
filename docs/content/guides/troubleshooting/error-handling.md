+++
title = "Error Handling"
description = "Configure error handlers to control how NoSQLBench responds to exceptions"
weight = 10
template = "page.html"

[extra]
quadrant = "guides"
topic = "troubleshooting"
category = "error-handling"
tags = ["errors", "exceptions", "retry", "troubleshooting", "configuration"]
testable = true
+++

# Error Handling

NoSQLBench provides a standard error handler mechanism available to all drivers. This guide shows how to configure error handling for your workloads.

## Default Behavior

If no error handler is configured, the default is:

```bash
errors=stop
```

This is a **fail-fast default** - any error thrown by an activity will cause it to stop. It's the simplest possible error handler configuration.

## Handler Verbs

The following handler verbs control how errors are processed:

### Available Verbs

- **ignore** - Silently ignore the error. Won't be retried unless combined with `retry`.
- **counter** - Count each uniquely named error with a counter metric.
- **histogram** - Track session time of each uniquely named error with a histogram.
- **meter** - Meter each uniquely named error with a meter metric.
- **retry** - Mark error as retryable. If activity has retries available, operation will be retried. Default: `maxtries=10`.
- **stop** - Allow error to propagate and cause activity to stop.
- **timer** - Combine counter, meter, and histogram for comprehensive error tracking.
- **warn** - Log a warning with error details.
- **code** - Assign specific result code to matching errors. Shorthand: use just a number like `errors=RuntimeException:33,warn`

**You can use any verbs in any order in a handler list.**

## Handler Chain Structure

The error handler is a **list of lists**:
- **Handler Chain** - Top level (entire configuration)
- **Handler List** - Individual entry in the chain
- **Handler Verbs** - Actions within a handler list

**Matching:** The first matching handler list is used. When an exception is thrown, the chain is walked until a match is found. If none match, no handlers apply (same as `ignore`).

**Default handler:** To apply handlers to unmatched exceptions, add `.*` at the end.

### Visual Example

```
errors=Invalid.*:ignore;Runtime.*:warn,histogram;counter,42

                     |-----------|-----------|-----------|
handler chain ==>    | 1         | 2         | 3         |
                     |===========|===========|===========|
error patterns ==>   | Invalid.* | Runtime.* | .*        |
                     |===========|===========|===========|
handler verbs ==>    | ignore    | warn      | counter   |
                     |-----------| histogram | 42        |
                                 |-----------|-----------|
```

**Execution:**
1. Exception thrown
2. Match exception class name against patterns (left to right)
3. First match selects handler list
4. Apply all verbs in that list in order

## Configuration Syntax

### Shorthand Forms

`errors=stop` is shorthand for `errors=.*:stop` (default wildcard pattern).

If error patterns aren't provided, the default wildcard `.*:` is automatically prepended.

### Error Pattern Format

Error patterns are **regular expressions** with limited character set: `[a-zA-Z0-9.-_*+]`

**Multiple patterns:**
```bash
errors='Missing.*,RuntimeEx.*:counter,warn'
```

### Handler Verb Formats

**Basic verb-only:**
```bash
errors=counter,warn
```

**Using JSON:**
```bash
errors='{"handler":"counter"},{"handler":"warn"}'
```

**Using simplified params:**
```bash
errors='handler=counter,handler=warn,handler=code code=42'
```

Handler verbs are shorthand for canonical object definitions with properties. The `handler` property selects the implementation. Each handler may have its own options.

## Building Handler Chains

**Single handler entry:**
```
<error_pattern>:<verb>,<verb>,...
```

**Multiple handler entries:**
```
<pattern1>:<verbs>;<pattern2>:<verbs>;<pattern3>:<verbs>
```

## Examples

### Count Missing Exceptions

Count all exceptions named `Missing.*`, count and warn about `RuntimeEx.*`, ignore everything else:

```bash
errors='Missing.*:counter;RuntimeEx.*:counter,warn;.*:ignore'
```

### Retry IOException

Retry all operations experiencing an `IOException`:

```bash
errors='IOException:retry'
```

### Retry with Limit

Retry IOExceptions up to 5 times:

```bash
errors='IOException:retry' maxtries=5
```

### Track Specific Errors

Track timing for connection errors, retry them, but stop on other errors:

```bash
errors='ConnectionException:timer,retry;.*:stop'
```

### Comprehensive Error Tracking

Count all errors, retry transient ones, stop on fatal ones:

```bash
errors='Timeout.*:counter,retry;Connection.*:counter,retry,warn;.*:counter,stop'
```

### Ignore Expected Errors

Ignore "not found" errors (expected in some workloads), track others:

```bash
errors='NotFoundException:ignore;.*:counter,warn'
```

## Common Patterns

### Development/Debug

Fail fast on any error to catch issues early:

```bash
errors=stop  # Default
```

### Load Testing

Retry transient errors, track all errors, don't stop:

```bash
errors='.*:retry,counter,warn' maxtries=10
```

### Soak Testing

Track errors but keep running:

```bash
errors='.*:timer,retry' maxtries=3
```

### Chaos Testing

Ignore specific injected failures, track unexpected ones:

```bash
errors='InjectedFailure:ignore;.*:counter,stop'
```

## Integration with Metrics

Error handlers integrate with the metrics system. When using `counter`, `histogram`, `meter`, or `timer`, metrics are created automatically:

**Metric naming:**
```
<activity-name>.errors.<exception-name>.<metric-type>
```

**Example:**
```
cql-keyvalue.errors.ConnectionException.timer
cql-keyvalue.errors.TimeoutException.counter
```

Query these metrics using MetricsQL:

```bash
./nb5 mql "SELECT * FROM metrics WHERE name LIKE '%errors%'"
```

## Troubleshooting

**Errors not being caught:**
- Check pattern regex syntax
- Verify exception class name (Java class name, case-sensitive)
- Add `.*:warn` at end to see all unmatched errors

**Too many retries:**
- Reduce `maxtries` parameter
- Review which errors should actually be retried
- Consider `stop` for non-transient errors

**Metrics not appearing:**
- Ensure you're using metric verbs (`counter`, `timer`, etc.)
- Check metric names with `./nb5 mql summary`
- Verify metrics export is configured

## Related Documentation

- **[Activity Parameters](../workload-design/activity-parameters.md)** - All activity configuration options
- **[Standard Metrics](../metrics/standard-metrics.md)** - Understanding metrics
- **[Common Errors](common-errors.md)** - Troubleshooting specific error types
- **[MetricsQL Reference](../../reference/apps/mql.md)** - Querying error metrics

---

*Error handlers provide fine-grained control over error behavior, enabling robust testing even in the presence of transient failures.*
