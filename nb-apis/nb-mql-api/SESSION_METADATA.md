---
title: "Session Metadata Feature"
description: "API doc for SESSION METADATA."
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

# Session Metadata Feature

## Overview

NoSQLBench now automatically captures and stores session metadata in the SQLite metrics database. This provides complete context about when and how metrics were collected, enabling reproducibility and better analysis.

## Architecture

### Database Schema

```sql
CREATE TABLE label_metadata (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    label_set_id INTEGER NOT NULL,
    metadata_key TEXT NOT NULL,
    metadata_value TEXT NOT NULL,
    UNIQUE(label_set_id, metadata_key, metadata_value),
    FOREIGN KEY(label_set_id) REFERENCES label_set(id)
)

CREATE INDEX idx_label_metadata_label_set
ON label_metadata(label_set_id)
```

### Automatically Captured Metadata

When a NoSQLBench session starts, the following metadata is automatically stored:

| Key | Source | Example |
|-----|--------|---------|
| `nb.version` | NBCLI.version | `5.25.0-SNAPSHOT` |
| `nb.commandline` | Command-line args | `nb5 run workload.yaml threads=10 rate=1000` |
| `nb.hardware` | SystemId.getHostSummary() | `Intel i9-9900K 8-core, 32GB RAM, 10Gib network` |

### Data Flow

```
NBCLI.java
  ├─► Captures: version, args, hardware
  └─► NBSession.java
       ├─► Creates SqliteSnapshotReporter
       └─► Calls: reporter.onSessionMetadata(labels, metadata)
            └─► SqliteSnapshotReporter.java
                 └─► Inserts into label_metadata table
```

## API

### MetricsSnapshotConsumer Interface

```java
public interface MetricsSnapshotConsumer {
    void onMetricsSnapshot(MetricsView view);

    /**
     * Called to provide session-level metadata associated with a label set.
     */
    default void onSessionMetadata(NBLabels labels, Map<String, String> metadata) {
        // Default no-op for implementations that don't need metadata storage
    }
}
```

### SqliteSnapshotReporter Implementation

```java
@Override
public void onSessionMetadata(NBLabels labels, Map<String, String> metadata) {
    if (metadata == null || metadata.isEmpty()) {
        return;
    }
    try {
        int labelSetId = resolveLabelSetId(labels);
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            insertLabelMetadata.setInt(1, labelSetId);
            insertLabelMetadata.setString(2, entry.getKey());
            insertLabelMetadata.setString(3, entry.getValue());
            insertLabelMetadata.executeUpdate();
        }
        connection.commit();
    } catch (SQLException e) {
        connection.rollback();
        logger.warn("Unable to write session metadata to SQLite.", e);
    }
}
```

## Usage

### Query Session Metadata

**Option 1: Use the `session` command** (shows timing + metadata):
```bash
$ nb5 mql session --db logs/metrics.db

┌─────────────────────┬─────────────────────┬──────────┬─────────────────┬───────────────┬──────────────┬──────────────┬──────────────────────────────┬─────────────────────────────┐
│ first_snapshot      │ last_snapshot       │ duration │ total_snapshots │ total_samples │ avg_interval │ nb_version   │ nb_commandline               │ nb_hardware                 │
├─────────────────────┼─────────────────────┼──────────┼─────────────────┼───────────────┼──────────────┼──────────────┼──────────────────────────────┼─────────────────────────────┤
│ 2025-10-30 10:00:00 │ 2025-10-30 10:05:00 │ 5m       │ 11              │ 220           │ 30s          │ 5.25.0-SNAP  │ nb5 run workload threads=10  │ Intel i9 8-core 32GB 10Gib  │
└─────────────────────┴─────────────────────┴──────────┴─────────────────┴───────────────┴──────────────┴──────────────┴──────────────────────────────┴─────────────────────────────┘
```

**Option 2: Use the dedicated `metadata` command**:
```bash
$ nb5 mql metadata --db logs/metrics.db

┌─────────────────────────┬────────────────┬──────────────────────────────────────┐
│ label_set               │ metadata_key   │ metadata_value                       │
├─────────────────────────┼────────────────┼──────────────────────────────────────┤
│ {session=my_benchmark}  │ nb.commandline │ nb5 run workload.yaml threads=10     │
│ {session=my_benchmark}  │ nb.hardware    │ Intel i9-9900K 8-core 32GB 10Gib     │
│ {session=my_benchmark}  │ nb.version     │ 5.25.0-SNAPSHOT                      │
└─────────────────────────┴────────────────┴──────────────────────────────────────┘
```

**Option 3: Direct SQL** (for custom queries):
```sql
-- Get specific metadata value
SELECT metadata_value
FROM label_metadata lm
JOIN label_set ls ON ls.id = lm.label_set_id
WHERE ls.hash LIKE '%session=%'
  AND lm.metadata_key = 'nb.commandline';

-- Get all metadata as JSON-like output
SELECT
  ls.hash AS session,
  GROUP_CONCAT(
    metadata_key || '=' || metadata_value,
    ', '
  ) AS metadata
FROM label_metadata lm
JOIN label_set ls ON ls.id = lm.label_set_id
GROUP BY ls.hash;
```

## Use Cases

### 1. Benchmark Reproducibility

Extract the exact command that generated metrics:
```bash
nb5 mql metadata --db logs/2025-10-30_benchmark.db --format json | \
  jq -r '.rows[] | select(.metadata_key == "nb.commandline") | .metadata_value'
```

Output:
```
nb5 run workload.yaml threads=100 rate=50000 duration=5m
```

### 2. Version Tracking

Correlate performance changes with NoSQLBench versions:
```bash
nb5 mql sql --db logs/metrics.db --query \
  "SELECT DISTINCT metadata_value FROM label_metadata WHERE metadata_key = 'nb.version'"
```

### 3. Hardware Context

Compare benchmarks across different systems:
```bash
# Get hardware info from each benchmark run
for db in logs/*_metrics.db; do
  echo "=== $db ==="
  nb5 mql metadata --db $db --format json | \
    jq -r '.rows[] | select(.metadata_key == "nb.hardware") | .metadata_value'
done
```

### 4. Audit Trail

Export complete session metadata for compliance/auditing:
```bash
nb5 mql metadata --db logs/production_benchmark.db --format json > audit_trail.json
```

## Implementation Details

### Files Modified

1. **Schema & Storage** (nb-api):
   - `SqliteSnapshotReporter.java` - Added label_metadata table + storage logic
   - `MetricsSnapshotScheduler.java` - Extended interface with `onSessionMetadata()`

2. **Session Integration** (nb-engine):
   - `NBSession.java` - Added `sendSessionMetadata()` method
   - `NBCLI.java` - Passes version/commandline/hardware to session props

3. **Query API** (nb-mql-api):
   - `MetricsSchema.java` - Added label_metadata constants + documentation
   - `SessionCommand.java` - Enhanced to query and display metadata
   - `MetadataCommand.java` - New dedicated metadata query command
   - `MetadataCLI.java` - CLI wrapper for metadata command

### Test Coverage

- **4 metadata storage tests** (nb-api/SessionMetadataTest.java):
  - Table creation and schema validation
  - Store and retrieve metadata
  - Uniqueness constraint enforcement
  - Multiple label set support

- **2 metadata query tests** (nb-mql-api/MetadataCommandTest.java):
  - Metadata retrieval from command
  - Backward compatibility with older databases

- **Updated session tests** (SessionCommandTest.java):
  - Validates 9 columns (added nb_version, nb_commandline, nb_hardware)

## Backward Compatibility

- ✅ **Older databases**: Commands gracefully handle missing label_metadata table
- ✅ **SessionCommand**: Shows "N/A" for missing metadata
- ✅ **MetadataCommand**: Returns empty result with explanatory note
- ✅ **No breaking changes**: Default interface method = no-op for other consumers

## Benefits

- ✅ **Full reproducibility**: Exact command-line preserved with metrics
- ✅ **Version tracking**: Know which NoSQLBench version generated metrics
- ✅ **Hardware context**: Understand performance relative to system capabilities
- ✅ **Audit trail**: Metadata persists in the database
- ✅ **Zero configuration**: Automatically captured, no user action required
- ✅ **Flexible querying**: Via commands or direct SQL

## Example Session

```bash
# Run a benchmark
$ nb5 run workload.yaml threads=50 rate=10000 --logs-dir logs/

# Later, check what command was used
$ nb5 mql session --db logs/metrics.db

# Output includes full context:
- When it ran (timestamps)
- How long it ran (duration)
- What command was used (nb.commandline)
- Which version was used (nb.version)
- What hardware it ran on (nb.hardware)
```

This makes every benchmark run self-documenting!
