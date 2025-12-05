---
title: "Op Template Synthesis Primer"
description: "End-to-end walkthrough of how workloads become executable operations."
audience: developer
diataxis: explanation
tags:
  - workloads
  - pipeline
component: core
topic: architecture
status: live
owner: "@nosqlbench/core"
generated: false
---

# Op Template Synthesis Primer

This guide walks through the pipeline that turns a YAML workload entry into an executable operation inside NoSQLBench (NB). It follows the artifacts that are produced at each stage, explains how data and configuration flow through the system, and highlights the core extension points for driver authors.

```
Workload source (yaml/json/inline/jsonnet, CLI params)
    │
    ├─ TemplateRewriter (TEMPLATE(k,v), ${k} → expr calls)
    ├─ ExprPreprocessor (param substitution & scripting)
    ├─ Jsonnet eval (optional; injects --ext-str args)            ┐ OpsLoader
    ▼                                                             │
RawOpsDocList (docs, blocks, ops, raw bindings)                   │
    │                                                             │
    ├─ OpsDocList merges blocks, tags, bindings, scenarios        │
    │   └─ Tag filters applied                                     │
    ▼
OpTemplate (payload map, params, bindings, tags normalized)
    │
    ├─ Driver preprocessor (per-adapter Map→Map rewrite, optional)
    └─ ParsedTemplateMap (static values, virtdata-driven dynamics)
    ▼
ParsedOp (lambda kit: static accessors + LongFunction binders)
    │
    ├─ Space function resolution (adapter#getSpaceFunc)
    └─ Lambda extenders (optional behaviors based on fields)
    ▼
OpMapper (adapter intent detection)
    ▼
OpDispenser (BaseOpDispenser or custom) ──► OpFunctionComposition
    │                                           ├─ capture wrap (if capture points)
    │                                           ├─ assertion wrap (if verifiers)
    │                                           └─ dryrun wrap (dryrun=none|op|emit|jsonnet|exprs)
    ▼
CycleOp (executable lambda) ──► StandardAction.runCycle (bind → execute → verify → metrics)
```

## 1. Workload ingestion (`OpsLoader`)
* **Inputs**: workload text (`workload=`, `op=`, `stmt=` CLI params), files on disk, or jsonnet modules.
* **Processing stages** (`nb-apis/.../activityconfig/OpsLoader.java`):
  - _Template rewrite_: `TemplateRewriter` converts inline template syntax (`TEMPLATE()`, `${}`) into `expr` calls so the expression engine has single-sourced semantics.
  - _Expression evaluation_: `ExprPreprocessor` runs the rewritten text with the per-run parameter map, allowing parameter substitution, math, conditionals, and custom template functions.
  - _Jsonnet support_: when the source is jsonnet, NB shells out through `SjsonnetMain`, injects `--ext-str` values for provided params, and feeds the rendered JSON back into the same downstream pipeline.
* **Outputs**: `RawOpsDocList` – a normalized, but still raw, representation of the documents, blocks, and op definitions found in the workload.

### Tagging and scenarios
`OpsDocList` (`activityconfig/yaml/OpsDocList.java`) wraps `RawOpsDocList`. It merges document-, block-, and op-level defaults, exposes combined bindings (`getDocBindings()`), collects scenario definitions, and provides `getOps(tagFilter, log)` to materialize `OpTemplate` instances filtered by MBQL-style tag specs.

## 2. Normalizing op templates
`OpTemplate` (`activityconfig/yaml/OpTemplate.java`) represents the canonical form of a single user op after inheritance is resolved:
* `op`: the payload map used to build the driver call.
* `bindings`: virtdata binding specs available to templated fields.
* `params`: configuration values that cascade to `ParsedOp` and beyond. Values specified here override doc/block/activity defaults.
* `tags`: final set of tags after inheritance and overrides.
* Internally, helpers such as `getParsed()` expose the op payload as a `ParsedTemplateString`, and `getParamsAsValueType()` enforces type safety for declared parameters.

From this point onward all downstream stages operate on `OpTemplate` objects.

## 3. Activity initialization and adapter selection (`Activity`)
During `Activity` construction (`nb-engine/nb-engine-core/.../Activity.java`):
1. Op templates are loaded with `loadOpTemplates`, applying the activity-level tag filter and falling back to synthetic templates when supported.
2. For each op template NB resolves which driver adapter to use (`driver`, `type`, or the activity default). It builds the adapter’s configuration model, merges it with YAML-provided defaults, and applies any `NBConfigurable` settings.
3. NB instantiates a `ParsedOp` per op template:
   ```java
   new ParsedOp(opTemplate, adapter.getConfiguration(), List.of(adapter.getPreprocessor()), activityComponent);
   ```
   The adapter’s preprocessor is called exactly once per template and can rename or reshape the payload before deeper parsing.

## 4. ParsedOp: binding static and dynamic data
`ParsedOp` (`adapters-api/.../templating/ParsedOp.java`) is the primary developer API for mapping templates:
* Wraps a `ParsedTemplateMap`, which splits the payload into **static** fields and **dynamic** fields. Dynamic entries become `LongFunction<?>` instances evaluated per cycle.
* Knows about all cascading configuration sources: op payload, `params`, and activity parameters registered in the adapter’s config model.
* Provides rich accessors:
  - `getStaticValue`, `takeStaticValue`, `getAsRequiredFunction`, `getAsFunctionOr`, `getAsCachedFunctionOr`, etc.
  - `requiredFieldOf(...)` and `optionalFieldOf(...)` to support aliases.
  - `newListBinder`, `newOrderedMapBinder`, `newArrayBinder` for bulk extraction.
* Tracks `CapturePoint`s declared via virtdata template syntax so capture-aware dispensers can add result harvesting with no extra parsing.
* Builds per-op metric labels (merging adapter & workload labels) and enforces that labels remain static.

Because `ParsedOp` extends `NBBaseComponent`, it participates in the runtime component tree, giving adapters access to scoped metric builders and logging.

### Lambda extenders and optional behavior
ParsedOp accessors turn template fields into composable lambdas, making it easy to add behavior only when users ask for it. Methods like `getAsOptionalFunction`, `getAsFunctionOr`, and `getAsCachedFunctionOr` return `LongFunction` instances that can wrap a base binder. For example:

```java
LongFunction<MyRequest> base = cycle -> new MyRequest(stmtF.apply(cycle));

LongFunction<MyRequest> withTracing = pop.getAsOptionalFunction("trace", Boolean.class)
    .map(traceF -> (long cycle) -> {
        MyRequest request = base.apply(cycle);
        return traceF.apply(cycle) ? request.enableTracing() : request;
    })
    .orElse(base);

LongFunction<MyRequest> withConsistency = pop.getAsOptionalEnumFunction("cl", ConsistencyLevel.class)
    .map(clF -> (long cycle) -> withTracing.apply(cycle).withConsistency(clF.apply(cycle)))
    .orElse(withTracing);
```

Each extender curries new behavior into the existing lambda, so dispensers simply call `withConsistency.apply(cycle)` regardless of which optional properties were present. This pattern keeps op construction declarative while still supporting feature flags, alternate encodings, and other per-template toggles.

## 5. Spaces and adapter state
Adapters implement `DriverAdapter<OPTYPE, SPACETYPE>` (`adapters-api/.../uniform/DriverAdapter.java`). Two responsibilities matter for op synthesis:
1. `getSpaceFunc(ParsedOp)`: returns a `LongFunction<Space>` that supplies per-cycle driver state. `BaseDriverAdapter` handles caching, optional string-to-index mapping, and custom naming (`space` field in the template). Spaces typically hold client instances, prepared statement caches, etc.
2. `getOpMapper()`: returns the mapper that inspects a `ParsedOp` and chooses / constructs the right dispenser.

Spaces are lazily created and recycled in `ConcurrentSpaceCache`. When the workload specifies `space: {dynamic expr}`, the computed value selects or names the cache entry.

## 6. Mapping templates to dispensers
`OpMapper` (`adapters-api/.../activityimpl/OpMapper.java`) is executed once per active op during activity startup. Implementations:
1. Inspect the `ParsedOp` to determine which adapter operation the template expresses (type fields, required combinations of keys, static vs dynamic expectations, etc.).
2. Build or reuse helper lambdas from the `ParsedOp` (e.g., `LongFunction` for statement text, bind markers, prepared statement cache keys).
3. Construct an `OpDispenser` that captures the necessary lambdas, the `Space` accessor, and any other per-template state.

Keep mapping logic declarative and defensive: all heavy validation belongs here so the hot path stays lean.

## 7. Dispensing executable operations
`OpDispenser<OPTYPE>` (`adapters-api/.../activityimpl/OpDispenser.java`) extends `LongFunction<OPTYPE>` and exposes additional hooks for instrumentation and error context. Most adapters inherit from `BaseOpDispenser` to get:
* Automatic wiring of per-op metrics (`instrument=true|false`, start/stop timer fields).
* Groovy-based verifiers & expected-result helpers (`verifier`, `verifier-init`, `expected-result`) executed post-op.
* Result capture plumbing, dry-run support, and consistent error decoration.

Typical dispenser pattern:
```java
public class MyOpDispenser extends BaseOpDispenser<MyCycleOp, MySpace> {
    private final LongFunction<String> statementF;
    private final LongFunction<MySpace> spaceF;

    public MyOpDispenser(NBComponent parent, ParsedOp op, LongFunction<MySpace> spaceF) {
        super(parent, op, spaceF);
        this.spaceF = spaceF;
        this.statementF = op.getAsRequiredFunction("stmt", String.class);
    }

    @Override
    public MyCycleOp getOp(long cycle) {
        MySpace space = spaceF.apply(cycle);
        String stmt = statementF.apply(cycle);
        return new MyCycleOp(space, stmt);
    }
}
```
`getOp(cycle)` must return an op that is fully ready to execute multiple times (for retries).

## 8. Functional mix-ins around dispensers
After the mapper returns, NB conditionally wraps dispensers (`nb-engine/nb-engine-core/.../OpFunctionComposition.java`), chaining them in a fixed order so that each concern layers cleanly on top of the previous one:
1. **Capture** – `OpCapture.wrapOptionally` inspects `ParsedOp.getCaptures()`. When captures exist and the underlying op implements `UniformVariableCapture`, it swaps in a `CapturingOpDispenser` (`opwrappers/CapturingOpDispenser.java`) which wraps the original op in `CapturingOp`. The wrapper defers to the adapter-provided extractor to emit a `Map<String,?>`.
2. **Assertions** – `OpAssertions.wrapOptionally` looks for Groovy verifier blocks and, when present, installs an `AssertingOpDispenser` (`opwrappers/AssertingOpDispenser.java`). That dispenser injects a `Validator<RESULT>` around the real op so verifier logic runs before the result is considered successful.
3. **Dryrun / emit** – `OpDryrun.wrapOptionally` converts the `dryrun` field into the `Dryrun` enum and chooses between `DryrunOpDispenser` (skip execution) and `ResultPrintingOpDispenser` (emit synthesized result) in `opwrappers`. Both wrappers still bind the original op every cycle to keep instrumentation accurate.

Every wrapper extends `BaseOpDispenser`, calls `adapter.getSpaceFunc(pop)`, and delegates to the real dispenser for binding. That keeps metrics and error-context handling consistent, even though the behavior being added varies by wrapper.

#### Implementation references & cleanup opportunities
- Wrapper entry points:
  - `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityimpl/OpCapture.java`
  - `.../OpAssertions.java`
  - `.../OpDryrun.java`
- Dispenser implementations:
  - `nb-apis/adapters-api/src/main/java/io/nosqlbench/adapters/api/activityimpl/uniform/opwrappers/CapturingOpDispenser.java`
  - `.../AssertingOpDispenser.java`
  - `.../DryrunOpDispenser.java`
  - `.../ResultPrintingOpDispenser.java`

All of these wrappers repeat the same adapter/space plumbing and `BaseOpDispenser` construction. A future cleanup could extract a `DelegatingOpDispenser` helper that accepts the parent dispenser and a `Function<CycleOp<?>, CycleOp<?>>` transformer. That would reduce duplication, make wrapper ordering more declarative, and simplify adding new runtime features (e.g., tracing or circuit-breaking) without yet another bespoke dispenser subtype. The current capture layer also calls `dispenser.getOp(0)` purely to test for `UniformVariableCapture`; moving capability detection into the mapper (or introducing a marker interface on the dispenser itself) would eliminate that eager bind and make the wrapping contract cleaner.

### Dryrun mechanics
The `dryrun` field is consumed as a static config value via `pop.takeEnumFromFieldOr(Dryrun.class, Dryrun.none, "dryrun")`. `OpFunctionComposition` uses the `Dryrun` enum and lets `OpDryrun.wrapOptionally` decorate the dispenser. The `Dryrun` enum is defined in `adapters-api/.../activityimpl/Dryrun.java` and provides type-safe handling of all dryrun modes:

| Value | Effect | Notes |
| --- | --- | --- |
| `none` | Executes normally. | Dispenser is returned unchanged. |
| `op` | Synthesizes ops but replaces execution with a no-op facade. | All binding, capture, verifier, and instrumentation paths still run, guaranteeing parity with real execution minus side effects. |
| `emit` | Synthesizes ops and prints their string form. | Builds on `op`; the facade logs the synthesized request before returning. |
| `jsonnet` | Performs a jsonnet-only dry run. | Stops after jsonnet rendering (see `OpsLoader.evaluateJsonnet`). If control flows past rendering, execution reverts to `none`. Handled in the workload loading phase. |
| `exprs` | Performs an expression-processing-only dry run. | Stops after expression evaluation, displaying the processed workload and scripting context (see `OpsLoader.processExpressions`). Useful for debugging template variables and expr functions. Handled in the workload loading phase. |

The `jsonnet` and `exprs` modes are handled early in `OpsLoader` and cause the process to exit after displaying their respective outputs. The `op` and `emit` modes wrap the dispenser at execution time, allowing full pipeline validation without backend side effects.

Because dry-run behavior composes through wrappers (for `op` and `emit`) or exits early (for `jsonnet` and `exprs`), adapters do not need to special-case dry-run logic. The same dispensers are exercised, which is invaluable when validating templating and lambda extenders without touching a real backend.

## 9. Executing cycle operations
At runtime `StandardAction` (`nb-engine/.../uniform/actions/StandardAction.java`) drives execution:
1. Uses the `OpSequence` (built by `SequencePlanner` from per-op `ratio`) to select a dispenser for the current cycle.
2. Calls `dispenser.getOp(cycle)` inside the bind timer scope; space access and field evaluation happen here.
3. Invokes the resulting `CycleOp.apply(cycle)` inside the execute timer, with retry handling up to `maxtries`. Retries reuse the same dispenser and usually the same `CycleOp`.
4. Runs verifiers and capture hooks, updates metrics, and enforces adapter-level error handling (`NBErrorHandler`).

`CycleOp` itself is a `LongFunction<RESULT>` with the guarantee that repeated invocations are idempotent for the same cycle.

## 10. Mutation & currying timeline
```
[Workload text]
  ├─ Template rewrite & expr processing (OpsLoader)
  ▼
[RawOpsDocList]
  ├─ Defaults merged, tags filtered (OpsDocList)
  ▼
[OpTemplate]
  ├─ Adapter preprocessors (optional Map→Map mutation)
  ├─ ParsedTemplateMap splits static vs dynamic
  ▼
[ParsedOp]
  ├─ LongFunctions derived from bindings
  ├─ Lambda extenders add optional features
  ├─ Space functions resolved (adapter#getSpaceFunc)
  ▼
[OpMapper]
  ├─ Intent detection, selects dispenser class
  ├─ Curries space function into constructor
  ▼
[OpDispenser]
  ├─ BaseOpDispenser instrumentation & verifiers
  ├─ Holds curried lambdas for per-cycle binding
  ▼
[OpFunctionComposition]
  ├─ Capture wrapper (if CapturePoints present)
  ├─ Assertion wrapper (if verifiers configured)
  ├─ Dryrun wrapper (dryrun=none|op|emit|jsonnet|exprs)
  ▼
[CycleOp]
  ├─ Optionally chains via OpGenerator
  ▼
[StandardAction]
  ├─ Retries & error handling (NBErrorHandler)
  ├─ Metrics & timers (bind, execute, verifier)
  ▼
[Results, captures, metrics]
```

## 11. Key artifacts at a glance

| Stage | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Workload ingestion | `OpsLoader` | Reads, rewrites, and renders workload text | Supports YAML, JSON, inline, jsonnet, expr templating |
| Document layer | `OpsDocList`, `OpsDoc`, `OpsBlock` | Merge defaults and tags | Tag filtering logged for traceability |
| Template layer | `OpTemplate` | Canonical per-op data | Holds `op`, `params`, `bindings`, `tags` |
| Parsed layer | `ParsedOp` | Lambda construction kit | Splits static vs dynamic, cascades config |
| Mapping | `OpMapper` | Chooses dispenser type | Adapter-specific interpretation of template |
| Dispensing | `OpDispenser` / `BaseOpDispenser` | Produces executable ops | Adds metrics, verifiers, capture, dry-run |
| Execution | `CycleOp`, `StandardAction` | Runs ops with retries & instrumentation | Uses `OpSequence` for weighted scheduling |

## 12. Extension checklist for driver authors
1. **Document op templates**: Every supported op shape should be described in the adapter’s help markdown. Ensure the mapper logic mirrors the documented signatures.
2. **Implement preprocessing sparingly**: Use `DriverAdapter#getPreprocessor()` only for backward compatibility shims; otherwise keep templates declarative.
3. **Use `ParsedOp` accessors**: Prefer `takeStaticConfigOr`, `getAsRequiredFunction`, and binder helpers over manual map casts; they enforce uniform parameter rules and give better diagnostics.
4. **Capture state in spaces**: Anything expensive or stateful (sessions, prepared statements) belongs in `Space` instances reachable via `getSpaceFunc`.
5. **Leverage `BaseOpDispenser`**: It standardizes metrics, verifiers, and error context so adapter code stays concise.

Armed with these stages and extension points, you can trace any workload from its YAML source to the exact Java code that executes it, and you have a clear template for implementing new driver behavior that fits seamlessly into the NB runtime.
