---
title: "Metrics Snapshot Flow Refactor Guide"
description: "Guide to the unified metrics snapshot scheduler and reporter flow."
audience: developer
diataxis: explanation
tags:
  - metrics
  - refactor
component: core
topic: metrics
status: live
owner: "@nosqlbench/metrics"
generated: false
---

# Metrics Snapshot Flow Refactor Guide

## Overview
This branch consolidates metrics capture and fan-out so every reporting channel observes the same deterministic snapshots. A shared `MetricsSnapshotScheduler` now coordinates multiple cadences, while reporters consume immutable `MetricsView` instances that support precise arithmetic across gauges, counters, meters, summaries, and timers.

Use this guide to understand what changed, why it matters, and how to adopt the new flow safely.

## Core Changes
- **MetricsView window semantics**
  - Every snapshot now tracks `windowStart` along with `capturedAt`, keeping interval math explicit.
  - `MetricsView.combine(...)` aggregates intervals, counters, gauges, meters, and summaries with correct weighting and statistical roll-ups.
  - New unit tests cover counter, gauge, meter, and summary aggregation paths, plus window alignment.
- **Snapshot scheduler rebasing**
  - A single scheduler is associated with the root `NBComponent`.
  - Registering a smaller cadence rebases the scheduler while preserving existing consumers.
  - Intervals are validated so each finer cadence divides all coarser cadences exactly.

## Scheduler & Cadence Coordination
- `MetricsSnapshotScheduler.register(...)` coordinates consumers across cadences, enforcing hierarchical divisibility.
- Finer snapshots are buffered until they roll up into coarser windows before emission.
- The scheduler now detaches from its parent on teardown to avoid duplicate component attachments.
- Test coverage
  - `MetricsSnapshotSchedulerTest` exercises hierarchical aggregation, rebase behaviour, and a synthetic reporter to ensure accumulated values match expectations.

## Reporter Integration
- `MetricsSnapshotReporterBase` centralises registration, teardown, and cadence selection for all snapshot-driven reporters.
- Reporters subscribe once and receive immutable `MetricsView` payloads:
  - Console, Log4J, CSV, Prometheus PushGateway, and both SQLite reporters consume the shared snapshots.
- A new integration harness (`MetricsReporterIntegrationTest`) drives the scheduler with deterministic counters and verifies:
  - Prometheus exposition encodes aggregated intervals correctly.
  - CSV writers emit the expected roll-up rows, independent of filename convention.
  - SQLite snapshot reporter stores identical totals in the `sample_value` table.

## Testing & Validation
- Targeted suites: `MetricsViewTest`, `MetricsSnapshotSchedulerTest`, `MetricsReporterIntegrationTest`.
- Run locally:
  ```bash
  cd nb-apis/nb-api
  mvn -Dtest=MetricsViewTest,MetricsSnapshotSchedulerTest,MetricsReporterIntegrationTest test
  ```
- When patching reporters or cadences, expand the integration test with new assertions rather than duplicating one-off harnesses.

## Adoption Checklist
1. **Register reporters through the scheduler.** Any custom reporter should extend `MetricsSnapshotReporterBase` to inherit shared cadence handling.
2. **Validate cadences.** Choose intervals that divide cleanly: e.g., 1s → 5s → 30s. Misaligned intervals now raise descriptive exceptions.
3. **Leverage `MetricsView.combine`.** When aggregating snapshots manually, rely on the updated combine logic to preserve statistical semantics.
4. **Extend tests as needed.** For new reporter channels, add cases to `MetricsReporterIntegrationTest` to keep cross-channel parity verifiable.

Following this path ensures all reporting surfaces display the same data, regardless of cadence or output format, while keeping future additions straightforward.***
