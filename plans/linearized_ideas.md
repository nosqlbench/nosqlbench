Linearized Operations – staged plan (optimized, flow-aware)

Stage 1: Flow context + capture/inject foundation
- Flow context
  - Define `OpFlowContext` API (lifecycle per stride slot, optional space key, creation/clear hooks on stride boundaries).
  - Storage: per-action flow maps keyed by flow-id (and space when present), using lock-free atomics with `ConcurrentHashMap`/primitive maps; pool context objects per stride to limit alloc churn; remain virtual-thread-friendly (no blocking monitors).
  - Provide adapter-agnostic context accessors so capture/inject/bindings can migrate off `SharedState` without relying on thread-locals.
  - Assign `opflow-id` for diagnostics/metrics.
- Capture wiring
  - Honor `[field]` / `[field as alias]` and explicit `capture:` lists/maps; avoid eager `getOp(0)` probing by using an adapter/op capability flag when wrapping.
  - Capture wrapper writes into flow context while returning native result (verifiers/metrics intact).
  - Policies: required vs optional; multi-row selection (first/all/index); type coercion via casts; warnings on unused/undefined captures.
  - Implementation: keep extractor pure; reuse small mutable maps behind unmodifiable views to cut per-row allocs; prefer generated lambdas over reflection.
- Injection wiring
  - Extend templating to parse `{[name]}` (with cast/alias) and resolve from `OpFlowContext`, with legacy-scope fallback only by opt-in.
  - Missing required → dependency-unmet marker; optional → null/empty; enforce casting on inject paths.
  - Provide adapter helpers so renderers can substitute context values without bespoke logic.
  - Implementation: pre-compile injection lookups into lightweight resolvers (indices/handles) to avoid string map lookups; share parsed artifacts across flows when safe.
- Metrics/errors for Stage 1
  - Add `opflow-id`/space labels on capture/inject events; introduce result codes for dependency-unmet vs hard fail at this layer.
- Tests/QA
  - Unit: capture → context → inject; required vs optional; multi-row selection; type-cast enforcement; `{[name]}` parsing/resolution.
  - Adapter: CQL happy-path capture/inject; non-string `capture:` templates.
  - Demo: stdout/mock workload capturing and reusing values, checked into CI.

Stage 2: Dependency-aware scheduling (sync + async)
- Planner
  - Build DAG per block/stride from bind→capture references (including `capture:`); validate missing refs/cycles; precompute topo order once per plan.
  - Record per-flow dependencies and required/optional edges.
- Runtime scheduler
  - Readiness tracking via atomic counters and lock-free queues; skip gating ops with unmet required captures; optional inject defaults.
  - Works for both StandardAction and async paths; enforce in-flight caps per flow to avoid unbounded queues; reuse lightweight task records per flow.
  - Semantics: distinct states for success/fail/skip/dependency-unmet/retry.
  - Virtual-thread friendly: avoid blocking monitors; allow structured/virtual threads to park on non-blocking queues if needed.
- Metrics/errors for Stage 2
  - Label metrics/logs with `opflow-id`/space; emit structured events for dependency skip/fulfill; integrate new result codes into `NBErrorHandler`.
- Tests/QA
  - Unit/integration: DAG validation (cycles, missing refs), readiness/skip vs fail semantics, async overlap with independent ops, in-flight cap behavior.
  - Adapter: CQL flow with dependent ops gated by captures; verify skips reported distinctly.
  - Demo: workload showing dependent ops held until prerequisites complete; asserts metrics/log labels and NBErrorHandler behavior.

Stage 3: Robustness, compound flows, and diagnostics
- Wrapper ergonomics
  - Add delegating `OpDispenser` base for wrappers to reduce boilerplate; ensure flow context + native result propagate through chains.
- `OpGenerator` and compound ops
  - Propagate flow context/id across generated ops; support batch/compound ops with layered binding (cycle → batch → stmt) and captures feeding flow context.
  - Keep generated op chains allocation-light; recycle op/task records where safe.
- Spaces and timers
  - Flow context keyed by (flow, space) when `space` is dynamic; clarify timer semantics or add flow-aware timers if needed.
- Retries and error handling
  - Define per-op retry respecting dependency state; optional per-flow retry to re-run flow; clarify interaction with `maxtries`.
  - Flow-aware error handler hook so composed flows can cancel dependents on hard fail, mark aggregate partial, or retry fan-out branches independently.
- Diagnostics and metrics
  - Per-flow tracing: structured events for bind, execute, capture, dependency-skip/fulfill, and aggregation points (fan-out/fan-in) with `opflow-id`/space labels.
  - Metrics/logging: keep label resolution zero-alloc (preformatted labels, cached metric handles); prefer async-friendly event buffers over synchronous logging.
- Tests/QA
  - Unit/integration: `OpGenerator` propagation, batch/compound capture→inject, dynamic space isolation, wrapper correctness.
  - Performance: no eager `getOp(0)` binds; no unbounded queue growth; scheduler fast-path microbenchmarks on virtual threads.
  - Docs: behavior matrix, error codes, timer scope, retry semantics.
  - Demo: fan-out/fan-in flow with aggregation status and error-handler hooks; batch/compound example; runnable in CI asserting expected outcomes.
