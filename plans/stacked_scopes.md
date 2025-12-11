Stacked Scopes – execution shell consolidation

Goal: simplify scope stacking (activity → action → stride → flow slot → cycle) and reduce per-cycle churn by initializing shells once per lifecycle and resetting cheaply per stride, focusing on logical consistency of execution/scheduling rather than explicit parallelism.

Plan (sync-focused)
- Documentation standard
  - New/updated types use Java 25 `///` markdown Javadoc with short diagrams where helpful.
- Introduce StrideContext (sync)
  - Owns per-slot flow contexts (OpFlowState/OpFlowContext[] with ordinal arrays).
  - Holds the current CycleSegment, a reusable CycleResultSegmentBuffer, and a small stride scratch (e.g., cached space resolution function). Per-cycle space selection can be cached for the current cycle only; do not persist spaces across cycles/stride.
  - Lifecycle: constructed once per StandardAction; reset(cycleSegment) per stride clears flow slots and resets buffers.
- Space resolution
  - Capture the space resolution function (int index) in StrideContext; per cycle compute spaceIndex once and stash in the flow slot so capture/inject use ints directly. Keep a name→index slow path only for dynamic cases.
- Buffer reuse
  - Reuse CycleResultSegmentBuffer per stride (reset only); keep flow contexts as clear-and-reuse arrays; avoid per-cycle allocations of segments/buffers.
- Dispenser immutability
  - Keep OpDispenser template-scoped and immutable; no stride/flow state injected. All per-cycle/stride state lives in StrideContext and CycleOp instances.
- Initialization-once principle
  - Build StrideContext once at action construction; use reset per stride to clear state.
  - Optional: provide a thin “context factory” for other execution modes (e.g., future async refactor) that mirrors StrideContext semantics without per-cycle allocation.
- Stride runner (virtual threads)
  - Replace CoreMotor with a stride runner per slot: fetch CycleSegment, reset StrideContext, call StrideAction.runStride with before/after hooks for rate limiting and metrics.
  - Launch runners on virtual threads (or structured tasks) in ActivityLoader; preserve user-visible semantics (cycles/stride/threads/rate limits/metrics/errors/outputs).

Outcome: execution shell stack
- Activity (templates, dispensers immutable)
- Action (StandardAction)
- StrideContext (reset per stride)
- Flow contexts per slot (clear arrays per stride)
- CycleOps per cycle (produced and executed)
