# Calling Bundled Apps from the Command Stream (`runapp`)

NoSQLBench ships a number of *bundled apps* — small standalone tools such as
`stress-report`, `export-docs`, and `cqlgen`. Historically these were only
reachable as the first word on the command line (e.g. `nb5 stress-report ...`),
which meant they could not be combined with a workload run or used as a step
inside a named scenario.

The `runapp` command lets you invoke any bundled app **from within the normal
command stream**, including from named-scenario steps. It bridges the command
stream's `name=value` parameter style to the app's own argument model, so you do
not have to hand-build an argument vector.

## Syntax

    runapp appname=<selector> [name=value]...

- `appname=<selector>` — required; the bundled app to run (e.g.
  `appname=stress-report`). Use `--list-apps` to see the available selectors.
- Every other `name=value` pair is adapted into the named option the app
  expects. For example `logs-dir=logs` is passed to the app as `--logs-dir=logs`.

Because bundled apps expose their option model (via picocli), `runapp` validates
parameter names up front and routes any sub-command selection — so unknown
parameters are reported clearly instead of being silently ignored.

## Example: run a benchmark, then summarize it with `stress-report`

The bundled `dummy_session` workload generates synthetic per-op metrics with no
backend required, which the `stress-report` app can summarize. The two pieces can
be chained in a single command stream — a named-scenario invocation followed by a
`runapp` step:

    nb5 dummy_session free_rate threads=8 cycles=40000 \
        runapp appname=stress-report logs-dir=logs

The named scenario (`dummy_session free_rate`) runs first and writes its metrics
to `logs/metrics.db`. The `runapp` step then runs the `stress-report` app over
that same session's database, producing a cassandra-stress-style summary:

    === NoSQLBench stress-report ===
    Source         : sqlite: .../logs/metrics.db
    Session        : ...
    Runtime        : 0:00:08

    op                      total_ops       errors    mean_ms     ...
    read                        24608           49       0.81      ...
    write                       12304           60       1.97      ...
    slow                         3076            3       5.04      ...
    total                       39988          112       1.49      ...

## Example: `runapp` as a step inside a named scenario

`runapp` can also be one of the steps of a named scenario defined in a workload
YAML. Steps run in order within the same session, so a later `report` step can
summarize the metrics produced by an earlier `bench` step:

```yaml
scenarios:
  bench_and_report:
    bench:  run driver=diag tags=block:main threads=8 cycles=40000 errors=count
    report: runapp appname=stress-report logs-dir=logs
```

Invoke it by scenario name:

    nb5 my_workload.yaml bench_and_report

The `report` step is recognized as an app invocation, so it is *not* injected
with the workload/alias parameters that normal activity steps receive — only the
parameters you specify are passed through to the app.

## Notes

- The app's process-style exit code is honored: a non-zero exit from the app
  fails the command (and thus the scenario) with an error.
- Parameters are adapted to the app's *long* option names, so prefer the long
  form in `name=value` pairs (e.g. `logs-dir=logs`, not a short flag).
- Apps that have no exposed option model receive the `name=value` pairs as
  `--name=value` tokens verbatim.

See also [`commandline_reference.md`](commandline_reference.md) for the full list
of command-stream verbs.
