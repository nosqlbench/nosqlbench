Stacked Scopes – execution shell consolidation

Goal: simplify scope stacking (activity → action → stride → flow slot → cycle) and reduce per-cycle churn by initializing shells once per lifecycle and resetting cheaply per stride, focusing on logical consistency of execution/scheduling rather than explicit parallelism.

Plan
- Introduce StrideContext (sync)
  - Owns per-slot flow contexts (OpFlowState/OpFlowContext[] with ordinal arrays).
  - Holds the current CycleSegment, a reusable CycleResultSegmentBuffer, and a small stride scratch (e.g., space index/name cache).
  - Lifecycle: constructed once per StandardAction; reset(cycleSegment) per stride clears flow slots and resets buffers.
- Space resolution
  - Capture the space resolution function (int index) in StrideContext; per cycle compute spaceIndex once and stash in the flow slot so capture/inject use ints directly. Keep a name→index slow path only for dynamic cases.
- Buffer reuse
  - Reuse CycleResultSegmentBuffer per stride (reset only); keep flow contexts as clear-and-reuse arrays; avoid per-cycle allocations of segments/buffers.
- Dispenser immutability
  - Keep OpDispenser template-scoped and immutable; no stride/flow state injected. All per-cycle/stride state lives in StrideContext and CycleOp instances.
- Async alignment (logical consistency)
  - Mirror StrideContext semantics in async without adding parallel-specific APIs; async shell holds OpTracker and stride-local queues/counters but follows the same reset/per-stride lifecycle and state layout.
- Initialization-once principle
  - Build StrideContext/AsyncStrideContext once at action construction; use reset per stride to clear state.

Outcome: execution shell stack
- Activity (templates, dispensers immutable)
- Action (StandardAction or async)
- StrideContext/AsyncStrideContext (reset per stride)
- Flow contexts per slot (clear arrays per stride)
- CycleOps per cycle (produced and executed)
