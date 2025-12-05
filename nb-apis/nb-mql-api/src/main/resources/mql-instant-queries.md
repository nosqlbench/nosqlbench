---
title: "Instant Queries"
description: "API doc for mql-instant-queries."
tags:
  - api
  - docs
audience: developer
diataxis: reference
component: core
topic: api
status: live
owner: "@nosqlbench/devrel"
generated: false
---

# Instant Queries

Get the most recent value for metrics. Instant queries return the latest snapshot for each unique label set.

**Database:** `examples.db`

---

## Example 1: Get All Latest Values

**Command:**
```
instant --metric api_requests_total
```

**Expected Output:**
```
timestamp             | value   | labels
----------------------------------------------------------------------------------
2025-10-23 10:10:00.0 | 11000.0 | endpoint=/users, method=GET, status=200
2025-10-23 10:10:00.0 | 8500.0  | endpoint=/products, method=GET, status=200
2025-10-23 10:10:00.0 | 1500.0  | endpoint=/users, method=POST, status=200
2025-10-23 10:10:00.0 | 75.0    | endpoint=/users, method=GET, status=404
2025-10-23 10:10:00.0 | 15.0    | endpoint=/users, method=GET, status=500

5 rows (Xms)
```

**What it shows:** Retrieves the latest value for all label combinations of a metric. Useful for dashboards showing current state.

**Use cases:**
- Current request counts by endpoint
- Latest error counts
- Current gauge values

---

## Example 2: Filter by Specific Labels

**Command:**
```
instant --metric api_requests_total --labels method=GET,endpoint=/users,status=200
```

**Expected Output:**
```
timestamp             | value   | labels
----------------------------------------------------------------------------------
2025-10-23 10:10:00.0 | 11000.0 | endpoint=/users, method=GET, status=200

1 row (Xms)
```

**What it shows:** Filtering to a specific label combination returns a single time series.

**Use cases:**
- Monitor specific endpoint traffic
- Track particular operation types
- Isolate specific service behavior

---

## Example 3: Query Only Errors

**Command:**
```
instant --metric api_requests_total --labels status=500
```

**Expected Output:**
```
timestamp             | value | labels
--------------------------------------------------------------------------------
2025-10-23 10:10:00.0 | 15.0  | endpoint=/users, method=GET, status=500

1 row (Xms)
```

**What it shows:** Filter by a single label dimension (status code) to see current error count.

**Use cases:**
- Current error count for alerting
- Quick health check
- SLA monitoring

---

## Example 4: Filter Across Multiple Series

**Command:**
```
instant --metric api_requests_total --labels status=200
```

**Expected Output:**
```
timestamp             | value   | labels
----------------------------------------------------------------------------------
2025-10-23 10:10:00.0 | 11000.0 | endpoint=/users, method=GET, status=200
2025-10-23 10:10:00.0 | 8500.0  | endpoint=/products, method=GET, status=200
2025-10-23 10:10:00.0 | 1500.0  | endpoint=/users, method=POST, status=200

3 rows (Xms)
```

**What it shows:** Filtering by a shared label value returns all matching label sets. This groups all successful requests while preserving endpoint/method distinctions.

**Use cases:**
- All successful requests across endpoints
- All error types (status >= 400)
- Traffic patterns by method type

---

## Key Concepts

**Label Filtering:**
- Specify labels as comma-separated key=value pairs
- All specified labels must match (AND logic)
- Unspecified labels can have any value

**Output:**
- `timestamp`: When the snapshot was captured
- `value`: The metric value at that time
- `labels`: All labels for this time series (comma-separated)

**Performance:**
- Instant queries are fast (typically <5ms)
- Queries only the latest snapshot
- Efficient for real-time dashboards
