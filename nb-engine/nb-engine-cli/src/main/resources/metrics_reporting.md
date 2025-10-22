# Metrics Reporting Guide

## What You Can Configure

Metrics reporting is opt-in per channel. Each channel can be enabled independently, with its own cadence and configuration. Available reporters include:

- **Console Reporter**
  - Streams metrics to stdout or stderr in a human-readable summary.
  - Optional “counts only” mode for condensed output.
- **Log4J Reporter**
  - Emits metrics through Log4J at the configured level and marker.
- **CSV Reporter**
  - Writes one CSV per metric sample (e.g., `metric_name.csv`) into a directory you choose.
  - Each row contains timestamp + metrics fields.
- **Prometheus Exposition Reporter**
  - Formats metrics in the OpenMetrics text exposition format (useful for scraping endpoints or unit tests).
- **Prometheus Push Gateway Reporter**
  - Pushes metrics to a remote endpoint (Prometheus Pushgateway or compatible API).
  - Supports bearer token authentication and custom labels (e.g., `jobname`, `instance`).
- **SQLite Snapshot Reporter**
  - Stores structured metrics into a local SQLite database for later analysis or archiving.

## Choosing a Cadence

Every reporter requires an interval (in milliseconds or seconds depending on CLI option). When multiple channels are active simultaneously, cadences must align hierarchically:

- Each finer cadence **must divide** each coarser cadence cleanly.
  - Valid example: `console=1s`, `csv=5s`, `prometheus=30s`.
  - Invalid example: `csv=4s`, `prometheus=10s` (10 is not a multiple of 4).
- If you request a smaller interval later, the runtime rebases to that finer cadence automatically.

## Common CLI Options

The following command-line switches (or scenario YAML fields) control reporting. Replace `<interval>` with a duration such as `1s`, `5s`, `30s`, etc.

| Reporter | CLI Option | Key Parameters |
|----------|------------|----------------|
| Console | `--report-console` | `console:<interval>` (optional `one-last-time`) |
| Log4J | `--report-log4j` | `log4j:<interval>` plus log level, marker |
| CSV | `--report-csv-to` | `<file>[:<filter>][:<interval>]` |
| Prometheus text | (usually via HTTP endpoint) | Provide an endpoint to scrape or use CLI preview |
| Prom Push | `--report-prompush-to` | `<endpoint>[,<interval>]` + optional `--prompush-apikeyfile` |
| SQLite snapshot | `--report-sqlite-to` | `<database-file>[,<filter>][,<interval>]` |

### Examples

```bash
# Console at 1-second intervals
nb5 --report-console console:1s …

# Console (1s), CSV (5s to ./metrics), Prom push (30s)
nb5 --report-console console:1s \
    --report-csv-to ./metrics.csv::5s \
    --report-prompush-to https://push.example.com/api/v1/import/prometheus/metrics/job/JOB/instance/INSTANCE,30s \
    --prompush-apikeyfile ~/push.token …

# SQLite snapshot every 10 seconds
nb5 --report-sqlite-to /tmp/nb-metrics.db,,10s …
```

## Labels and Names

All reporters inherit base labels from the component hierarchy. You can append custom labels via CLI (`--add-labels key=value`) or scenario configuration. These labels propagate to metrics output (e.g., CSV columns, Prometheus labels, SQLite tables).

> **Built-in SQLite reporter** – every session automatically creates `logs/<session>_metrics.db` (30 s cadence) and refreshes a `logs/metrics.db` symlink to the current file. Additional SQLite reporters you configure will run alongside this default one.

### Per-channel Metric Filters (Tri-state)

Every reporting channel accepts an optional metric filter. Filters are tri-state:

1. **Default policy** – allow all metrics unless you opt into a stricter mode.
2. **Include clauses** – narrow the set by name, regex, or labels.
3. **Exclude clauses** – remove specific metrics even if they match an include.

Syntax rules:

- Clauses are separated by semicolons (`;`).
- Prefix `!` or `-` on any clause to turn it into an exclusion (e.g., `!stat=mean`).
- Omit regex characters for exact matches; regex is supported when needed.
- To switch to deny-by-default, add `default=false` to the filter expression (or call `defaultAccept(false)` when configuring programmatically).

Examples:

```bash
# CSV only for load activity, excluding mean statistic, deny anything else
--report-csv-to metrics.csv,default=false;name=op_latency_ms;activity=load;!stat=mean,5s

# SQLite snapshot: include counters but exclude any debug-labelled metrics
--report-sqlite-to sqlite.db,name=.*_total;!label=debug,30s
```

YAML equivalent:

```yaml
filters:
  default: false           # deny unless explicitly included
  include:
    - name=op_latency_ms
    - activity=load
  exclude:
    - stat=mean
    - service=debug
```

If a clause list contains both includes and excludes, exclusion wins when both match. Reporters can therefore carve out precise subsets without extra scripting.

## Best Practices

- **Align cadences before launch.** Ensure each requested interval divides the next coarser one.
- **Reuse directories/databases thoughtfully.** CSV and SQLite reporters append to existing files; rotate or archive as needed.
- **Secure credentials.** For Pushgateway, provide API tokens via files or environment variables rather than embedding in command lines.
- **Test locally.** Use short runs with console or CSV reporters to validate metric names/labels before enabling remote endpoints.

That’s it—pick the channels you need, set cadences that line up, and the runtime handles fan-out and buffering under the hood. The internal scheduler ensures every reporter sees consistent snapshots without extra configuration.***
