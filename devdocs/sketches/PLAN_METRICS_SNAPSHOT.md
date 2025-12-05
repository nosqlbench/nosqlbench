---
title: "Metrics Snapshot Scheduler Consolidation Plan"
description: "Remaining work to unify metrics snapshot scheduling and reporting."
audience: developer
diataxis: explanation
tags:
  - metrics
  - planning
component: core
topic: metrics
status: draft
owner: "@nosqlbench/metrics"
generated: false
---

# Metrics Snapshot Scheduler Consolidation Plan

## Goal
Provide a single snapshot source for all metrics channels with deterministic fan-out across multiple reporting cadences.

## Remaining Work
1. **MetricsView Enhancements**
   - Add `windowStart` (UTC instant) and tighten interval metadata.
   - Provide arithmetic for combining multiple snapshots (gauges, counters, meters, summaries) with tests.

2. **Snapshot Coordinator**
   - Replace single-interval scheduler with a coordinator that registers multiple cadences.
   - Validate cadences (each must divide the next larger exactly); throw descriptive errors otherwise.
   - Buffer snapshots so finer intervals aggregate into coarser snapshots before emission.

3. **Channel Integration**
   - Update all reporters to register through the coordinator, specifying their cadence.
   - Ensure coarser channels receive accumulated snapshots; finer channels see raw intervals.

4. **Testing**
   - Unit tests for MetricsView arithmetic.
   - Integration tests for scheduler fan-out (1s→5s, 5s→30s, etc.).
   - Regression tests for Prom/CSV/SQLite reporters verifying identical data across cadences.

## Notes
- Keep snapshot immutability and metadata alignment with OpenMetrics semantics.
- Minimize allocation; staged buffers must reuse structures where possible.
- Maintain backward compatibility for existing reporter APIs where feasible.
