---
title: "Dry Run Mode Design Notes"
description: "Specification for the dryrun parameter semantics and implementation."
audience: developer
diataxis: explanation
tags:
  - dryrun
  - design
component: core
topic: ops
status: live
owner: "@nosqlbench/core"
generated: false
---

# Dry Run Mode Design Notes

This document captures how the `dryrun` option is surfaced to users, how each mode behaves, and how the feature is implemented inside NoSQLBench. The goal is to make it easy to explain the knobs to users and to orient developers who need to adjust or extend the behavior.

---

## 1. User-Facing Contract

- **Configuration surface**: `dryrun` is a standard activity parameter (`BaseDriverAdapter#getConfigModel` exposes it as `Param.defaultTo("dryrun", "none")`). It can be provided on the command line or in YAML:
  - CLI: `nb5 run driver=... workload=... dryrun=emit`
  - YAML: top-level `params` or per-op `params`
  - Per-op override: add `dryrun: op` (or `emit`) inside an individual op template; it takes precedence over activity-level settings.
- **Supported values (enum `io.nosqlbench.adapters.api.activityimpl.Dryrun`)**:

| Mode | Phase affected | Summary |
| --- | --- | --- |
| `none` | N/A | Normal execution. |
| `op` | Runtime dispensing | Bind every op, but replace the final `CycleOp` with a no-op wrapper (returns `null`). |
| `emit` | Runtime dispensing | Bind and execute every op, printing the result to stdout before returning it. |
| `jsonnet` | Workload ingestion | Evaluate jsonnet, print generated workload + stderr, then exit. |
| `exprs` | Expression preprocessing | Evaluate `${...}` / `<<...>>` expressions once, dump rendered workload + context, then exit. |

There is no dedicated CLI flag; users supply the literal enum value. Unknown values are logged and treated as `none`.

---

## 2. Lifecycle Overview

The option is consulted in three places along the workload pipeline:

```
Activity params / op params
    │
    ▼
OpsLoader.parseDryrunParam()
    ├─ dryrun=jsonnet   → one-shot jsonnet evaluation, exit
    ├─ dryrun=exprs     → one-shot expression preprocessing, exit
    └─ other values     → normal preprocessing
        │
        ▼
Activity.createOpSourceFromParsedOps()
    └─ pop.takeEnumFromFieldOr("dryrun") → per-op dryrun enum
        │
        ▼
OpFunctionComposition.wrapOptionally()
    └─ OpDryrun.wrapOptionally(...) → optional dispenser wrapper
```

Two early modes (`jsonnet`, `exprs`) happen before any ops are mapped. The runtime modes (`op`, `emit`, `none`) are applied once per active op template as part of the dispenser-wrapping chain:

```
ParsedOp --(OpMapper)--> Base dispenser
    └─ capture wrap?       (OpCapture.wrapOptionally)
        └─ assertion wrap? (OpAssertions.wrapOptionally)
            └─ dryrun wrap (OpDryrun.wrapOptionally)
                 └─ final dispenser
```

This ordering means dry-run behavior composes on top of capture/assertion logic without adapters having to special-case anything.

---

## 3. Implementation Details

### 3.1 Parameter wiring
- `BaseDriverAdapter#getConfigModel()` registers `dryrun` with default `"none"`. This makes the parameter available at the activity level.
- `ParsedOp#takeEnumFromFieldOr(Dryrun.class, Dryrun.none, "dryrun")` (in `Activity#createOpSourceFromParsedOps`) pulls the enum value, resolving from:
  1. Static op payload (`op` block)
  2. Op-level `params`
  3. Activity parameters (CLI/YAML)
- The resolved value is passed into `OpFunctionComposition.wrapOptionally(...)`.

### 3.2 Early short-circuit modes (`jsonnet`, `exprs`)
- **Location**: `OpsLoader.processExpressions` and `OpsLoader.evaluateJsonnet`.
- **Behavior**:
  - When `dryrun=exprs`, NB runs the expression preprocessor once (`ExprPreprocessor.processWithContext`), prints the rendered YAML and expression context, flushes streams, then `System.exit(0)`.
  - When `dryrun=jsonnet`, NB runs jsonnet, prints stdout/stderr, and exits with code `0` on success or `2` on errors.
- These modes prevent the activity layer from ever loading op templates, making them ideal for debugging workload generation.

### 3.3 Runtime modes (`none`, `op`, `emit`)
- **Entry point**: `OpDryrun.wrapOptionally(...)` in `nb-engine-core`.
- **Wrappers**:
  - `Dryrun.none`: returns the original dispenser unchanged.
  - `Dryrun.op`: wraps with `DryrunOpDispenser` → produces `DryrunOp` that discards execution (`apply` returns `null`).
  - `Dryrun.emit`: wraps with `ResultPrintingOpDispenser` → produces `ResultPrintingOp` that calls the original op, prints the result, then returns it.
- Both wrapper dispensers:
  - Extend `BaseOpDispenser` (they inherit metrics, verifier plumbing, and space access).
  - Capture the original dispenser and delegate to it for binder creation so all binding/instrumentation still happens.
  - Are inserted *after* capture/assertion wrappers (as seen in `OpFunctionComposition.wrapOptionally`). This ensures dry-run doesn’t disrupt result capture or verification setup.

### 3.4 Interaction with space management & lambdas
- Because wrappers extend `BaseOpDispenser`, the `LongFunction<Space>` returned by the adapter is passed through unchanged. Space access happens inside the original dispenser before the dry-run wrapper gets involved.
- Optional features expressed via `ParsedOp` (e.g., lambda extenders for tracing or consistency levels) continue to apply, because dry-run wrappers are the final transformation stage around the constructed `CycleOp`.
- Metrics:
  - `DryrunOp` never calls the delegate, so `StandardAction` records the bind and execute timer, but the internal operation is a no-op. This is acceptable for rehearsal runs, but users should know it doesn’t emit backend traffic.
  - `ResultPrintingOp` still executes the real operation, so metrics and side effects stay intact while providing additional visibility.

---

## 4. Behavior Matrix

| Mode | Workload loaded | Ops bound | Ops executed | Output | Exit behavior |
| --- | --- | --- | --- | --- | --- |
| `none` | ✔ | ✔ | ✔ | Normal | Normal |
| `op` | ✔ | ✔ | ✖ (returns `null`) | None | Normal |
| `emit` | ✔ | ✔ | ✔ | Prints result per cycle | Normal |
| `jsonnet` | ✖ | ✖ | ✖ | Jsonnet stdout/stderr dump | Exit (0 on success, 2 on failure) |
| `exprs` | ✖ | ✖ | ✖ | Rendered workload + expr context | Exit (0) |

---

## 5. Sequence Diagram

```
User args ("dryrun=emit")
    │
    ├─> OpsLoader.parseDryrunParam()
    │       └─ returns Dryrun.none (for runtime modes)
    │
    └─> Activity.createOpSourceFromParsedOps()
            ├─ pop.takeEnumFromFieldOr("dryrun") → Dryrun.emit
            ├─ OpMapper.apply(...) → base dispenser
            └─ OpFunctionComposition.wrapOptionally(...)
                  ├─ OpCapture.wrapOptionally(...)
                  ├─ OpAssertions.wrapOptionally(...)
                  └─ OpDryrun.wrapOptionally(... Dryrun.emit)
                       └─ ResultPrintingOpDispenser wraps base
```

For `dryrun=jsonnet` or `dryrun=exprs`, the sequence stops in `OpsLoader`, so `Activity` is never invoked.

---

## 6. Considerations & Future Improvements

- **Wrapper uniformity**: All dispenser wrappers (capture/assertion/dryrun) duplicate space handling and `BaseOpDispenser` setup. A shared delegating base could reduce boilerplate and make it easier to add new runtime modifiers.
- **User feedback**:
  - `emit` can produce a high volume of console output. Consider adding throttling or an option to direct output to a file.
  - `op` returns `null`; if adapters expect non-null results downstream, additional guards may be needed. Currently, `StandardAction` only checks for exceptions, so this is safe.
- **Extensibility**: Because `Dryrun` is an enum, adding new modes is a matter of updating the enum, `OpsLoader`, and `OpDryrun`. Keep both digestion and runtime paths in sync when new modes are introduced.
- **Command-line discoverability**: Documentation (CLI help or driver guides) should explicitly list the modes to avoid guesswork by users.

---

## 7. Quick Reference

- Early-stage diagnostics:
  - `dryrun=jsonnet` → “Does my jsonnet render correctly?”
  - `dryrun=exprs` → “Did my `${variable}` substitutions resolve?”
- Runtime rehearsals:
  - `dryrun=op` → “Is my workload binding correctly without touching the backend?”
  - `dryrun=emit` → “What requests/results are produced? Show me inline.”
- Default:
  - `dryrun=none` → Full execution.

With these notes, you should be able to explain the feature end-to-end and navigate the core code paths that implement it.
