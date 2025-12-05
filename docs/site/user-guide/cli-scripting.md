---
title: CLI Scripting
description: Build simple scenarios directly from the command line.
audience: user
diataxis: howto
tags:
- site
- docs
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 11
---

Sometimes you want to run workloads in a specific order or insert setup logic between activities.
You do not have to write JavaScript for every scenario—several API calls are exposed directly on the
command line. Each command below acts like a macro for a script fragment; order matters because the
scenario script is built left to right before execution.

## Command-Line Format

Newlines are not allowed mid-script unless you escape them with `\`. Single words without leading
dashes (e.g., `run`) are scenario commands. Subsequent `key=value` arguments apply to the preceding
command only. Any argument that starts with `-` or `--` is treated as a global option and is applied
before the scenario runs.

## Concurrency & Control

Activities run parallel to the scenario script. The scenario concludes only when both the script and
all activities have finished (either naturally or via stop commands).

## Scenario Commands

### `start`

```
start driver=<driver> alias=<alias> ...
```

Starts an activity asynchronously and continues without blocking. Provide a driver name and, ideally,
an alias so you can reference it later.

### `await`

```
await <alias>
```

Pauses the scenario script until the named activity finishes (does not stop the activity).

### `run`

```
run driver=<driver> alias=<alias> ...
```

Equivalent to `start ...` followed by `await <alias>`—runs an activity to completion before
continuing.

### `stop`

```
stop <alias>
```

Stops an activity gracefully (blocking until threads report they are stopped). Falls back to
`forcestop` if necessary.

### `forcestop`

```
forcestop <alias>
```

Immediately shuts down the activity’s thread pool without attempting a graceful stop.

### `waitmillis`

```
waitmillis <milliseconds>
```

Sleeps the scenario script while activities continue running—useful for run-duration control.

### `script`

```
script <script file>
```

Injects the contents of the given file into the scenario script buffer.

### `fragment`

```
fragment <script text>
```

Adds an inline script fragment.

## Example CLI Script

Any sequence of these commands forms a scenario script. This example starts two activities, waits 10
seconds, then awaits/stops individual aliases:

```bash
./nb5 \
  start driver=stdout alias=a cycles=100K workload=cql-iot tags=block:main \
  start driver=stdout alias=b cycles=200K workload=cql-iot tags=block:main \
  waitmillis 10000 \
  await a \
  stop b
```

Narrative:

1. nb5 launches.
2. Activity `a` starts with 100K cycles.
3. Activity `b` starts with 200K cycles.
4. The scenario waits 10 seconds while both run.
5. The scenario blocks until `a` completes.
6. Activity `b` is stopped immediately.

Once the stop command finishes (and the script is exhausted), the scenario exits.
