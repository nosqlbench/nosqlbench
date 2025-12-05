---
title: "Command-Line Reference"
description: "Doc for commandline reference."
tags:
  - nb-engine
  - docs
audience: developer
diataxis: reference
component: core
topic: architecture
status: live
owner: "@nosqlbench/devrel"
generated: false
---

# Command-Line Reference

This document lists the primary command-line flags, sub-commands, and scripting
keywords accepted by `${PROG}`. It complements the shorter
[`commandline.md`](commandline.md) overview, which highlights the options you are
most likely to reach for day-to-day.

The CLI follows a _command + named-argument_ pattern:

- Any token without a dash (`-`) is treated as a command and is converted into
  scenario script logic.
- Any token in `name=value` form is treated as a named parameter for the most
  recent command.
- Long-form options (`--option`) alter global behaviour and may only appear
  once. Short options (`-v`, `-vv`, …) stack for convenience.

Examples:

```text
${PROG} start driver=diag alias=example
${PROG} run driver=stdout op="row={{Identity()}}"
${PROG} --logs-dir logs/myrun --session-name smoke
```

---

## Discovery & Introspection

| Option | Description |
|--------|-------------|
| `--help` | Display the high-level help text. |
| `--version` | Print the full version string with Maven coordinates. |
| `${PROG} help topics` | List bundled help topics. |
| `${PROG} help <activity>` | Show help specific to an activity/driver. |
| `--list-drivers` | Enumerate available driver adapters. |
| `--list-exprs` | List bundled expression helper functions. |
| `--list-scenarios` | List scenario scripts on the classpath. |
| `--list-workloads` | List workloads that contain the scenarios. |
| `--list-metrics driver=diag` | Discover metrics available for a driver. |
| `--copy <resource>` | Copy an embedded workload or file locally. |

---

## Scenario Construction Commands

Commands can be chained in sequence; the resulting script is executed after
parsing.

| Command | Description |
|---------|-------------|
| `script <file> [name=value]...` | Add a scenario script file (with parameter interpolation). |
| `activity [name=value]...` | Launch an activity definition inline. |
| `start [name=value]...` | Start an activity without waiting for completion. |
| `run [name=value]...` | Start an activity and wait for completion. |
| `await <alias>` | Wait for a previously started activity to finish. |
| `stop <alias>` | Request a graceful stop of an activity. |
| `forceStop <alias>` | Force-stop an activity immediately. |
| `waitmillis <ms>` | Insert a pause between scripted commands. |
| `fragment '<javascript>'` | Inject an ECMAScript fragment into the scenario. |

Named parameters (e.g., `driver=cql`, `threads=4`, `cycles=1M`) follow each
command and are applied in-place.

---

## Logging & Diagnostics

| Option | Description |
|--------|-------------|
| `--logs-dir <dir>` | Store session log files under the specified directory. |
| `--logs-max <count>` | Retain only the most recent N log files. |
| `--logs-level <level>` | Set the default log level (OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL). |
| `--log-level-override <class:LEVEL,...>` | Override logging for specific packages/classes. |
| `--logging-pattern <pattern>` | Set a pattern for both console and file logs. |
| `--console-pattern <pattern>` | Pattern for console logs only. |
| `--logfile-pattern <pattern>` | Pattern for file logs only. |
| `--ansi=enabled|disabled` | Force ANSI colour usage (console defaults to auto-detect). |
| `-v`, `-vv`, `-vvv` | Increase console logging level to INFO, DEBUG, or TRACE. |

Pattern helpers:

- `TERSE` – `%8r %-5level [%t] %-12logger{0} %msg%n%throwable`
- `VERBOSE` – `%d{DEFAULT}{GMT} [%t] %logger %-5level: %msg%n%throwable`
- `TERSE-ANSI`, `VERBOSE-ANSI` – colourised equivalents

---

## Progress & Console Behaviour

| Option | Description |
|--------|-------------|
| `--progress console:<interval>` | Emit progress updates to the console. |
| `--progress logonly:<interval>` | Emit progress updates to logs only. |
| `--ansi=enabled|disabled` | Explicitly toggle ANSI support (also listed above). |

Interval values accept standard duration suffixes (`5s`, `1m`, `10m`, …).
Progress is automatically disabled if verbose console logging is requested.

---

## Metrics, Telemetry & Reporting

| Option | Description |
|--------|-------------|
| `--report-csv-to <dir>` | Write metrics snapshots to CSV files in `<dir>`. |
| `--report-graphite-to <host[:port]>` | Publish metrics to Graphite/Carbon. |
| `--report-prom-push-to <uri[,interval]>` | Push metrics to a Prometheus Pushgateway. |
| `--report-sqlite-to <jdbc-url[,interval][,pattern]>` | Create an additional SQLite snapshot channel (beyond the default session database). |
| `--report-interval <seconds>` | Default interval for CSV/Graphite reporting. |
| `--metrics-prefix <prefix>` | Prefix metric names for Graphite exports. |
| `--log-histograms <file[:regex[:interval]]>` | Persist raw HDR histograms. |
| `--log-histostats <file[:regex[:interval]]>` | Persist histogram summary stats as CSV. |
| `--hdr-digits <digits>` | Configure histogram precision (default `3`). |
| `--classic-histograms <prefix[:regex]>` | Enable legacy (decaying) histogram emitters. |
| `--add-labels key=value,...` | Append additional labels to every metric. |
| `--set-labels key=value,...` | Replace the default label set entirely. |
| `--summary` | Force console summary output when the session finishes. |
| `--report-summary-to <dest>` | Configure summary destinations (comma-separated). |
| `--session-name <name>` | Override the auto-generated session identifier. |

### SQLite Snapshot Behaviour

- A default SQLite database (`<session>_metrics.db`) is created in the logs
  directory. A `metrics.db` symlink mirrors the active session file.
- Use `--report-sqlite-to jdbc:sqlite:/path/to/custom.db` to add more channels
  with different intervals or label filters.

Summary destinations support:

- `stdout[:min-duration]`, `stderr[:min-duration]`
- File paths (supporting `_SESSION_` and `_LOGS_` macros)

---

## Session, Docker & Environment Toggles

| Option | Description |
|--------|-------------|
| `--session-name <name>` | Customise session labels and log file names. |
| `--progress <mode>` | Control the progress reporter target and cadence. |
| `--docker-prom-retention-days=<duration>` | Configure retention when auto-launching the docker telemetry stack. |
| `--prompush-apikeyfile <path>` | Provide an API key for the Prometheus push reporter. |

When `--docker-prom-retention-days` is set, the CLI will start local Docker
services (Prometheus, Grafana, etc.), wire them together, and export the default
dashboard automatically.

---

## Scenario Script Utilities

The following options and commands simplify script authoring:

- `--args-file <path>` – Load additional CLI parameters from a file.
- Parameters supplied via `name=value` can be referenced inside scripts using
  `${param}` style placeholders.
- Script fragments (`fragment '...'`) run within the ECMAScript engine, giving
  access to the full scripting API.

Refer to [`cli-scripting.md`](cli-scripting.md) for deeper coverage of inline
scenario authoring.

---

## Putting It Together (Examples)

```text
# Run a stdout workload, push metrics to CSV & SQLite, and enable console progress
${PROG} \
  --logs-dir logs/smoke --session-name smoke1 \
  --report-csv-to metrics/csv \
  --report-sqlite-to jdbc:sqlite:metrics/custom.db,30s \
  --progress console:30s \
  run driver=stdout alias=printf op="row={{Identity()}}"

# Launch a diag workload with Prometheus push and verbose logging
${PROG} \
  --logs-level INFO --progress logonly:1m -vv \
  --report-prom-push-to http://pushgateway:9091,15s \
  run driver=diag threads=2 cycles=10000
```

---

## Additional Resources

- [`commandline.md`](commandline.md) – concise overview of the most commonly
  used options.
- [`cli-scripting.md`](cli-scripting.md) – in-depth guide to command-line
  scripting patterns.
- `${PROG} help <topic>` – contextual help within the CLI itself.

If you encounter an option that is not documented here, please open an issue or
submit a documentation update so the reference remains complete.
