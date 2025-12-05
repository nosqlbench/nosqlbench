---
title: "MetricsQL Query Engine Blueprint"
description: "Design for bringing MetricsQL-style queries to the SQLite metrics store."
audience: developer
diataxis: explanation
tags:
  - metricsql
  - design
component: core
topic: metrics
status: draft
owner: "@nosqlbench/metrics"
generated: false
---

# MetricsQL Query Engine Blueprint for NoSQLBench SQLite Metrics

## Executive Summary

This blueprint outlines a MetricsQL-inspired query engine for NoSQLBench's SQLite metrics database. After researching existing libraries, **no mature Java-based PromQL/MetricsQL parsers exist**. Therefore, this design proposes a **pragmatic hybrid approach**: implement the top 10 most common query patterns as specialized command forms, with an extensible architecture for future full parser integration.

---

## Research Findings

### 1. Existing Parser Libraries
- **VictoriaMetrics/metricsql**: Go-based standalone parser (not usable in Java without JNI)
- **Prometheus PromQL Parser**: Go-based generated parser using goyacc
- **Java Ecosystem**: Only HTTP API clients exist; no native Java PromQL/MetricsQL parsers found
- **Conclusion**: Building or binding to Go parsers is complex; better to start with targeted SQL generators for common patterns

### 2. NoSQLBench SQLite Schema Analysis
The existing schema (`SqliteSnapshotReporter.java`) stores metrics in a normalized OpenMetrics-aligned structure:

**Core Tables:**
- `metric_family` - Logical metrics (name, type, help, unit)
- `sample_name` - Individual samples within a family (e.g., `counter_total`, `latency_seconds`)
- `sample_value` - Time-series data points with label sets
- `sample_quantile` - Quantile values for summaries/timers
- `sample_rate` - Rate statistics (mean, m1, m5, m15)
- `sample_histogram` - Optional HDR histogram payloads

**Label Storage:**
- `label_key`, `label_value`, `label_set`, `label_set_membership` - Normalized dimensional labels

**Key Capabilities:**
- Supports counters, gauges, summaries, histograms, meters, timers
- Stores dimensional labels with efficient joins
- Timestamp-based time series (millisecond precision)
- Optional histogram persistence (Base64-encoded HDR)

### 3. MetricsQL/PromQL Most Common Patterns
Based on research, these are the essential query patterns:

1. **Rate of change** - `rate(metric[5m])` for per-second rate over time window
2. **Total increase** - `increase(metric[1h])` for cumulative growth
3. **Instant value** - `metric{labels}` for current reading
4. **Aggregations** - `sum(metric) by (label)`, `avg()`, `max()`, `min()`, `count()`
5. **Error rates** - `sum(rate(errors[5m])) / sum(rate(requests[5m]))`
6. **Quantile queries** - `histogram_quantile(0.95, metric_bucket)`
7. **Label filtering** - `metric{label="value", label2=~"regex.*"}`
8. **Range queries** - Time-series data over intervals
9. **Binary operators** - Arithmetic/comparison between series
10. **Top-N queries** - `topk(5, metric)` for highest values

---

## Proposed Architecture

### Module Organization: `nb-mql-api`

The query engine will be organized as a new module **`nb-apis/nb-mql-api`** (MetricsQL API) that cleanly separates query functionality from the core metrics writing infrastructure.

#### Module Structure

```
nb-apis/
├── nb-api/                      (existing - minimal changes)
│   ├── SqliteSnapshotReporter   (STAYS - writer functionality)
│   └── MetricInstanceFilter     (STAYS - used by reporter)
│
└── nb-mql-api/                  (NEW - query engine)
    ├── pom.xml                  (depends on nb-api)
    ├── src/main/java/io/nosqlbench/nb/mql/
    │   ├── schema/              (Extracted schema knowledge)
    │   │   ├── MetricsSchema.java              (Schema constants & DDL)
    │   │   ├── MetricsDatabaseReader.java      (Read-only DB access)
    │   │   └── LabelSetResolver.java           (Label hash logic)
    │   │
    │   ├── query/               (Query engine core)
    │   │   ├── MetricsQueryCommand.java        (Command interface)
    │   │   ├── QueryResult.java                (Result data structure)
    │   │   ├── SQLiteQueryBuilder.java         (SQL generation)
    │   │   ├── TimeWindowParser.java           (Parse "5m", "1h", etc.)
    │   │   └── LabelMatcher.java               (Label filter logic)
    │   │
    │   ├── commands/            (Individual query commands)
    │   │   ├── RateCommand.java
    │   │   ├── IncreaseCommand.java
    │   │   ├── InstantCommand.java
    │   │   ├── AggregateCommand.java
    │   │   ├── QuantileCommand.java
    │   │   ├── RangeCommand.java
    │   │   ├── RatioCommand.java
    │   │   ├── TopKCommand.java
    │   │   ├── LabelFilterCommand.java
    │   │   └── BinaryOpCommand.java
    │   │
    │   ├── format/              (Output formatters)
    │   │   ├── TableFormatter.java
    │   │   ├── JsonFormatter.java
    │   │   └── CsvFormatter.java
    │   │
    │   └── cli/                 (CLI entry point)
    │       └── MetricsQLCLI.java               (Main class, picocli)
    │
    └── src/test/java/io/nosqlbench/nb/mql/
        ├── schema/
        │   └── MetricsSchemaTest.java
        ├── commands/
        │   ├── RateCommandTest.java
        │   └── ... (test each command)
        └── integration/
            └── EndToEndQueryTest.java
```

#### Dependency Strategy

**`nb-mql-api/pom.xml` dependencies:**
```xml
<dependency>
    <groupId>io.nosqlbench</groupId>
    <artifactId>nb-api</artifactId>
    <version>${revision}</version>
    <!-- For: NBLabels, schema constants if needed -->
</dependency>

<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.46.0.0</version>
</dependency>

<dependency>
    <groupId>info.picocli</groupId>
    <artifactId>picocli</artifactId>
    <!-- Already in parent -->
</dependency>

<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <!-- For JSON output -->
</dependency>

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <!-- For CSV output -->
</dependency>
```

**Add to `nb-apis/pom.xml`:**
```xml
<modules>
  <module>adapters-api</module>
  <module>expr-api</module>
  <module>nb-api</module>
  <module>nb-mql-api</module>      <!-- NEW -->
  <module>nb-apis-all</module>
</modules>
```

---

### Clean Separation Strategy

#### What STAYS in `nb-api`
- **`SqliteSnapshotReporter`**: Keeps all write functionality
  - Schema creation (`initialiseSchema()`)
  - Prepared statements for inserts
  - Caches for metric families, sample names, labels
  - Write logic (`onMetricsSnapshot()`)

This ensures zero disruption to existing metrics collection.

#### What MOVES to `nb-mql-api`
- **Schema Knowledge (READ-ONLY)**: Extract into `MetricsSchema.java`
  ```java
  public class MetricsSchema {
      // Table/column constants
      public static final String TABLE_METRIC_FAMILY = "metric_family";
      public static final String TABLE_SAMPLE_NAME = "sample_name";
      public static final String TABLE_SAMPLE_VALUE = "sample_value";
      // ... etc

      // Common SQL fragments for queries
      public static String joinLabelsSQL() { ... }
      public static String filterByLabelSQL(String labelKey) { ... }
  }
  ```

- **Label Hash Logic**: Extract `labelSetHash()` method
  ```java
  public class LabelSetResolver {
      public static String computeHash(Map<String, String> labels) {
          // Same logic as SqliteSnapshotReporter.labelSetHash()
          if (labels.isEmpty()) return "{}";
          StringJoiner joiner = new StringJoiner(",", "{", "}");
          labels.entrySet().stream()
              .sorted(Map.Entry.comparingByKey())
              .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));
          return joiner.toString();
      }
  }
  ```

  **Update `SqliteSnapshotReporter`** to use this:
  ```java
  import io.nosqlbench.nb.mql.schema.LabelSetResolver;

  private String labelSetHash(Map<String, String> labels) {
      return LabelSetResolver.computeHash(labels);
  }
  ```

#### Migration Path
1. **Phase 1**: Create `nb-mql-api` skeleton with schema constants
2. **Phase 2**: Extract `LabelSetResolver` and update `SqliteSnapshotReporter` to use it
3. **Phase 3**: Build query engine using schema constants
4. **Phase 4**: Add CLI and package as standalone JAR

This minimizes changes to `nb-api` while enabling clean query functionality.

#### Avoiding Circular Dependencies

**Key Principle**: `nb-mql-api` depends on `nb-api`, but **never the reverse**.

```
nb-api (writer)
   ↑
   │ depends on (optional)
   │
nb-mql-api (reader)
```

**How to achieve this:**
1. **Option A (Recommended)**: Keep `LabelSetResolver` in `nb-mql-api`
   - `SqliteSnapshotReporter` duplicates the `labelSetHash()` method
   - Pro: Zero coupling, complete independence
   - Con: Small code duplication (~10 lines)

2. **Option B**: Extract to shared utility if duplication bothers you
   - Create `nb-apis/nb-metrics-util` (new minimal module)
   - Both `nb-api` and `nb-mql-api` depend on it
   - Pro: No duplication
   - Con: Extra module complexity for small utility

**Recommendation**: Use **Option A**. The `labelSetHash()` method is simple and stable. Duplicating it maintains clean separation and avoids over-engineering. If the logic becomes more complex in the future, refactor to Option B.

**In `nb-mql-api`:**
```java
package io.nosqlbench.nb.mql.schema;

public class LabelSetResolver {
    // Duplicated from SqliteSnapshotReporter - intentional for independence
    public static String computeHash(Map<String, String> labels) {
        if (labels.isEmpty()) return "{}";
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        labels.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));
        return joiner.toString();
    }
}
```

**Keep in `nb-api/SqliteSnapshotReporter.java`:**
```java
private String labelSetHash(Map<String, String> labels) {
    if (labels.isEmpty()) return "{}";
    StringJoiner joiner = new StringJoiner(",", "{", "}");
    labels.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));
    return joiner.toString();
}
```

This ensures `nb-api` remains self-contained and doesn't need `nb-mql-api` to function.

---

### Phase 1: Command-Form Query Interface (MVP)

Implement 10 specialized command classes that translate high-level query intent into optimized SQLite queries.

#### 1.1 Core Components

```
┌─────────────────────────────────────────────────────────────────┐
│                   nb-mql-api Module                             │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              MetricsQL Query CLI Tool                     │ │
│  │     (Bundled standalone app: nb-metricsql.jar)            │ │
│  └────────────────┬──────────────────────────────────────────┘ │
│                   │                                             │
│      ┌────────────┴────────────┐                                │
│      │   QueryCommandRouter    │  ← Parses command + args       │
│      └────────────┬────────────┘                                │
│                   │                                             │
│      ┌────────────┴────────────┐                                │
│      │  Command Implementations │                               │
│      │  - RateCommand           │  ← rate(metric[5m])           │
│      │  - IncreaseCommand       │  ← increase(metric[1h])       │
│      │  - InstantCommand        │  ← metric{labels}             │
│      │  - AggregateCommand      │  ← sum/avg/max/min by (label) │
│      │  - QuantileCommand       │  ← Extract p95, p99           │
│      │  - RangeCommand          │  ← Time-series over interval  │
│      │  - RatioCommand          │  ← error_rate / total_rate    │
│      │  - TopKCommand           │  ← topk(N, metric)            │
│      │  - LabelFilterCommand    │  ← Filter by label patterns   │
│      │  - BinaryOpCommand       │  ← Arithmetic between metrics │
│      └────────────┬────────────┘                                │
│                   │                                             │
│      ┌────────────┴────────────┐                                │
│      │   SQLiteQueryBuilder    │  ← Generates optimized SQL     │
│      └────────────┬────────────┘                                │
│                   │                                             │
│      ┌────────────┴────────────┐                                │
│      │  MetricsDatabaseReader  │  ← Read-only connection        │
│      │  MetricsSchema          │  ← Schema constants            │
│      │  LabelSetResolver       │  ← Label hash logic            │
│      └────────────┬────────────┘                                │
└───────────────────┼─────────────────────────────────────────────┘
                    │
       ┌────────────┴────────────┐
       │    SQLite JDBC Layer    │
       └────────────┬────────────┘
                    │
       ┌────────────┴────────────┐
       │  NoSQLBench Metrics DB  │
       │  (logs/metrics.db)      │
       │                         │
       │  Written by:            │
       │  SqliteSnapshotReporter │  ← Lives in nb-api
       │  (nb-api module)        │
       └─────────────────────────┘
```

#### 1.2 Command Interface

Each command implements:
```java
public interface MetricsQueryCommand {
    String getName();
    String getDescription();
    QueryResult execute(Connection conn, Map<String, Object> params);
    void validate(Map<String, Object> params) throws InvalidQueryException;
}

public record QueryResult(
    List<String> columns,
    List<Map<String, Object>> rows,
    String executedSQL,
    long executionTimeMs
) {}
```

#### 1.3 Output Format Requirements

**All commands must support these output formats:**

1. **table** (default) - ASCII tables for console
   - Aligned columns with headers
   - Row count and execution time footer
   - Truncation for very long values

2. **csv** - Comma-separated values for spreadsheets
   - Standard CSV with headers
   - Proper escaping and quoting
   - Compatible with Excel, Google Sheets

3. **tsv** - Tab-separated values for data processing
   - Tab-delimited with headers
   - No quoting needed for most tools
   - Compatible with awk, cut, paste

4. **json** - Structured JSON for programmatic use
   - Pretty-printed JSON object
   - Includes columns, rows, rowCount, executionTimeMs
   - Compatible with jq, JavaScript

5. **jsonl** (JSON Lines) - Streaming JSON for pipelines
   - One JSON object per row (no array wrapper)
   - Efficient for large result sets
   - Compatible with streaming processors

6. **markdown** - GitHub-flavored markdown tables
   - Formatted tables for documentation
   - Copy-paste into markdown files
   - Compatible with GitHub, GitLab, etc.

**Output Format Encapsulation:**

```java
public interface ResultFormatter {
    String getName();              // "table", "json", etc.
    String getFileExtension();     // "txt", "json", "csv", etc.
    String format(QueryResult result);
}

public class FormatterRegistry {
    private static final Map<String, ResultFormatter> formatters = new LinkedHashMap<>();

    static {
        register(new TableFormatter());
        register(new CsvFormatter());
        register(new TsvFormatter());
        register(new JsonFormatter());
        register(new JsonLinesFormatter());
        register(new MarkdownFormatter());
    }

    public static void register(ResultFormatter formatter);
    public static ResultFormatter get(String name);
    public static List<String> getAvailableFormats();
}
```

**CLI Integration:**
```bash
# Table output (default)
nb5 mql instant --metric ops_total

# JSON output
nb5 mql instant --metric ops_total --format json

# CSV for piping to file
nb5 mql range --metric ops_total --window 5m --format csv > metrics.csv

# JSONL for streaming
nb5 mql range --metric ops_total --window 1h --format jsonl | jq -c '.value'

# Markdown for documentation
nb5 mql instant --metric ops_total --format markdown
```

**Design Principles:**
- Formatters are stateless and reusable
- Registry pattern for easy discovery and extension
- Each formatter handles its own escaping and encoding
- Format selection via --format flag (validated against registry)
- Invalid format falls back to table with warning

#### 1.3 Example Commands

**RateCommand** - Calculate per-second rate:
```bash
nb-metricsql rate --metric activity_ops_total --window 5m --labels activity=write
```
Generates SQL:
```sql
WITH timeseries AS (
  SELECT sv.timestamp_ms, sv.value, ls.hash
  FROM sample_value sv
  JOIN sample_name sn ON sn.id = sv.sample_name_id
  JOIN label_set ls ON ls.id = sv.label_set_id
  WHERE sn.sample = 'activity_ops_total'
    AND sv.timestamp_ms >= ?  -- 5 minutes ago
  ORDER BY sv.timestamp_ms
),
rates AS (
  SELECT
    timestamp_ms,
    hash,
    (value - LAG(value) OVER (PARTITION BY hash ORDER BY timestamp_ms)) /
    ((timestamp_ms - LAG(timestamp_ms) OVER (PARTITION BY hash ORDER BY timestamp_ms)) / 1000.0)
    AS rate_per_sec
  FROM timeseries
)
SELECT datetime(timestamp_ms/1000, 'unixepoch') AS time, rate_per_sec
FROM rates WHERE rate_per_sec IS NOT NULL;
```

**AggregateCommand** - Sum/avg/max/min with grouping:
```bash
nb-metricsql agg --metric ops_total --func sum --by activity,host --last 10m
```
Generates SQL:
```sql
WITH latest_snapshot AS (
  SELECT MAX(timestamp_ms) AS max_ts FROM sample_value
  WHERE timestamp_ms >= ? -- 10m ago
),
labeled_values AS (
  SELECT sv.value, lk.name AS label_key, lv.value AS label_value
  FROM sample_value sv
  JOIN sample_name sn ON sn.id = sv.sample_name_id
  JOIN label_set ls ON ls.id = sv.label_set_id
  JOIN label_set_membership lsm ON lsm.label_set_id = ls.id
  JOIN label_key lk ON lk.id = lsm.label_key_id
  JOIN label_value lv ON lv.id = lsm.label_value_id
  CROSS JOIN latest_snapshot
  WHERE sn.sample = 'ops_total' AND sv.timestamp_ms = latest_snapshot.max_ts
)
SELECT
  MAX(CASE WHEN label_key = 'activity' THEN label_value END) AS activity,
  MAX(CASE WHEN label_key = 'host' THEN label_value END) AS host,
  SUM(value) AS sum_ops
FROM labeled_values
GROUP BY label_value;  -- Simplified; actual impl tracks label set hash
```

**QuantileCommand** - Extract latency percentiles:
```bash
nb-metricsql quantile --metric op_latency_ms --p 0.95 --labels activity=read --last 5m
```
Generates SQL:
```sql
SELECT
  datetime(sv.timestamp_ms/1000, 'unixepoch') AS time,
  sq.quantile,
  sq.quantile_value AS latency_ms
FROM sample_quantile sq
JOIN sample_value sv ON sv.id = sq.sample_value_id
JOIN sample_name sn ON sn.id = sv.sample_name_id
WHERE sn.sample = 'op_latency_ms'
  AND sq.quantile = 0.95
  AND sv.timestamp_ms >= ?  -- 5m ago
ORDER BY sv.timestamp_ms;
```

---

### Phase 2: Extension Points for Full Parser (Future)

#### 2.1 Parser Integration Strategy
When a mature Java PromQL parser becomes available (or if we build one):

1. **Lexer/Parser Module**: Tokenize and parse MetricsQL syntax into AST
   - Use ANTLR4 or JavaCC for grammar definition
   - Reuse PromQL grammar from Prometheus project (adapt for Java)

2. **AST to SQL Translator**: Walk AST nodes and generate SQLite queries
   - Map MetricsQL functions to SQL equivalents
   - Handle range vectors with window functions
   - Translate label matchers to JOIN conditions

3. **Backward Compatibility**: Keep command forms as convenience wrappers
   - `nb-metricsql query 'rate(ops_total[5m])'` → parse and execute
   - `nb-metricsql rate --metric ops_total --window 5m` → direct command

#### 2.2 Design Principles for Future Parser
- **Separation of Concerns**: Parser → AST → SQL Generator → Executor
- **Composability**: Support nested functions and binary operations
- **Type Safety**: Validate metric types (Counter, Gauge, Summary)
- **Performance**: Generate optimized SQL with proper indexes
- **Error Messages**: Clear feedback on unsupported features

---

## Top 12 Command Forms (Detailed Specifications)

### 0. `session` - Session Time Information
**Usage**: `session [--format <format>]`

**Parameters**:
- `--format`: Output format (table, json, csv, markdown, etc.)

**Output**: Session timing information:
- First snapshot timestamp
- Last snapshot timestamp
- Session duration (human-readable)
- Total number of snapshots
- Average snapshot interval
- Snapshot cadence (time between snapshots)

**SQL Strategy**: Query min/max timestamps and count from sample_value table

**Purpose**:
- Understand session timeline for examples
- Determine appropriate time windows for queries
- Verify data collection cadence
- Quick session health check

**Example Output**:
```
Session Information:
  First Snapshot: 2025-10-23 10:00:00 UTC
  Last Snapshot:  2025-10-23 10:10:00 UTC
  Duration:       10 minutes
  Total Snapshots: 5
  Avg Interval:   2m 30s
  Cadence:        ~150 seconds
```

**Implementation Notes**:
- Simple query: `SELECT MIN(timestamp_ms), MAX(timestamp_ms), COUNT(DISTINCT timestamp_ms) FROM sample_value`
- Calculate duration and format human-readable
- Useful for living examples to show current session state
- Should work even with partial/ongoing sessions

---

### 1. `summary` - Database Overview and Metrics Inventory
**Usage**: `summary [--format <format>]`

**Parameters**:
- `--format`: Output format (table, json, csv, markdown, etc.)

**Output**: Summary information about the metrics database including:
- Metric names and types
- Time range (first and last snapshot timestamps)
- Number of samples per metric
- Available label dimensions
- Database file size and snapshot count

**SQL Strategy**: Multiple queries aggregated into summary view

**Purpose**:
- Quick overview of what's in the database
- Discover available metrics without knowing names
- Understand time coverage
- Identify label dimensions for filtering

**Example Output**:
```
Metric Summary:
  Database: logs/metrics.db
  Time Range: 2025-10-23 10:00:00 to 2025-10-23 10:10:00 (10 minutes)
  Total Snapshots: 5

Metrics:
  Name                    | Type    | Samples | First Value | Last Value | Labels
  ----------------------- | ------- | ------- | ----------- | ---------- | ------
  api_requests_total      | COUNTER | 25      | 0.0         | 11000.0    | method, endpoint, status
  api_latency             | SUMMARY | 10      | -           | -          | method, endpoint
```

**Implementation Notes**:
- Aggregate data from metric_family, sample_value, and label tables
- Show time range with human-readable formatting
- List unique label dimensions per metric
- Optionally show sample statistics (min/max/avg)

---

### 1. `sql` - Execute Raw SQLite Queries
**Usage**: `sql --query "<SQL>" [--format <format>]`

**Parameters**:
- `--query`: Raw SQL query (SQLite dialect)
- `--format`: Output format (table, json, csv, tsv, jsonl, markdown)

**Output**: Query results formatted according to --format

**SQL Strategy**: Direct pass-through to SQLite with minimal validation

**Purpose**:
- Advanced users can write custom SQL queries
- Access features not yet covered by high-level commands
- Debugging and exploration
- Custom analytics beyond standard MetricsQL functions

**Safety**:
- Read-only connection prevents modifications
- Query timeout to prevent runaway queries
- Schema introspection available via PRAGMA commands

**Examples**:
```bash
# Custom aggregation
sql --query "SELECT AVG(value) FROM sample_value WHERE ..."

# Schema inspection
sql --query "SELECT * FROM metric_family"

# Complex joins
sql --query "SELECT ... FROM sample_value sv JOIN ..."
```

**Implementation Notes**:
- No query rewriting or transformation
- Expose full SQLite dialect and functions
- Provide schema reference in help/docs
- Include common query patterns in documentation

---

### 1. `rate` - Calculate Rate of Change
**Usage**: `nb-metricsql rate --metric <name> --window <duration> [--labels key=val,...] [--start <time>] [--end <time>]`

**Parameters**:
- `--metric`: Sample name (e.g., `activity_ops_total`)
- `--window`: Time window (5s, 1m, 5m, 1h, 1d)
- `--labels`: Optional label filters (comma-separated)
- `--start/--end`: Optional time range (ISO8601 or relative like `-1h`)

**Output**: Time series of per-second rates

**SQL Strategy**: Use window functions to calculate delta between consecutive points

---

### 2. `increase` - Total Increase Over Window
**Usage**: `nb-metricsql increase --metric <name> --window <duration> [--labels key=val,...]`

**Output**: Total increase in counter value over the window

**SQL Strategy**: `MAX(value) - MIN(value)` within time window per label set

---

### 3. `instant` - Current/Latest Value
**Usage**: `nb-metricsql instant --metric <name> [--labels key=val,...] [--time <timestamp>]`

**Output**: Most recent value for each label set

**SQL Strategy**: `SELECT ... WHERE timestamp_ms = (SELECT MAX(timestamp_ms) ...)`

---

### 4. `agg` - Aggregation Functions
**Usage**: `nb-metricsql agg --metric <name> --func <sum|avg|max|min|count> [--by <label,...>] [--last <duration>]`

**Parameters**:
- `--func`: Aggregation function
- `--by`: Group by labels (optional)
- `--last`: Time window for aggregation

**Output**: Aggregated values, optionally grouped by labels

**SQL Strategy**: Direct SQL aggregates with GROUP BY on label values

---

### 5. `quantile` - Extract Quantiles
**Usage**: `nb-metricsql quantile --metric <name> --p <0.0-1.0> [--labels key=val,...] [--last <duration>]`

**Output**: Quantile values over time

**SQL Strategy**: Query `sample_quantile` table joined with filters

---

### 6. `range` - Time Series Data
**Usage**: `nb-metricsql range --metric <name> --start <time> --end <time> [--labels key=val,...] [--step <duration>]`

**Output**: Raw time series data points

**SQL Strategy**: Simple SELECT with time range and optional downsampling

---

### 7. `ratio` - Calculate Ratios
**Usage**: `nb-metricsql ratio --numerator <metric> --denominator <metric> [--window <duration>]`

**Output**: Ratio of two metrics (useful for error rates, cache hit ratios)

**SQL Strategy**: Two CTEs with division operation

---

### 8. `topk` - Top N Values
**Usage**: `nb-metricsql topk --metric <name> --n <count> [--by <label>] [--last <duration>]`

**Output**: Top N label sets by metric value

**SQL Strategy**: `ORDER BY value DESC LIMIT N`

---

### 9. `filter` - Advanced Label Filtering
**Usage**: `nb-metricsql filter --metric <name> --where "label=~'regex.*' AND label2!='value'"`

**Output**: Metrics matching complex label predicates

**SQL Strategy**: Dynamic WHERE clause generation with regex support

---

### 10. `binop` - Binary Operations
**Usage**: `nb-metricsql binop --left <metric> --op <+|-|*|/|%> --right <metric|scalar>`

**Output**: Result of arithmetic operation between metrics or metric and scalar

**SQL Strategy**: JOIN two metric queries on label set and timestamp, apply operator

---

## Bundled App: `nb-metricsql`

### Packaging Strategy

The `nb-mql-api` module will produce a standalone executable JAR that can be run independently or as part of NoSQLBench.

#### Maven Assembly Configuration

**In `nb-mql-api/pom.xml`:**
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>io.nosqlbench.nb.mql.cli.MetricsQLCLI</mainClass>
                    </manifest>
                </archive>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <finalName>nb-metricsql</finalName>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

This produces: `target/nb-metricsql-jar-with-dependencies.jar`

#### Usage Modes

**Standalone:**
```bash
java -jar nb-metricsql.jar rate --metric activity_ops_total --window 5m
```

**With convenience script:**
```bash
#!/bin/bash
# nb-metricsql wrapper script
exec java -jar "$(dirname "$0")/nb-metricsql.jar" "$@"
```

**Integrated with NoSQLBench** (future):
```bash
nb5 mql rate --metric activity_ops_total --window 5m
```

#### Distribution
- Include in NoSQLBench distribution under `tools/nb-metricsql.jar`
- Provide wrapper scripts for Unix (`nb-metricsql`) and Windows (`nb-metricsql.bat`)
- Document in main NoSQLBench docs under "Metrics Querying"

---

## Implementation Plan

### Milestone 0: Module Setup (1 week) ✅ COMPLETED
- [x] Create `nb-mql-api` module under `nb-apis/`
- [x] Add module to `nb-apis/pom.xml`
- [x] Create package structure: `schema/`, `query/`, `commands/`, `format/`, `cli/`
- [x] Set up dependencies: nb-api, sqlite-jdbc, picocli, gson, commons-csv
- [x] Configure Maven assembly plugin for standalone JAR
- [x] Create `LabelSetResolver` utility class (duplicate from SqliteSnapshotReporter)
- [x] Create `MetricsSchema` with table/column constants
- [x] Create `MetricsDatabaseReader` for read-only connections
- [x] Verify build and module dependencies
- [x] Write unit tests for LabelSetResolver (6 tests passing)

### Milestone 0.5: Test Data Generation (3-4 days)
Generate comprehensive test databases using existing `SqliteSnapshotReporter` API to cover various query scenarios. These databases will be used for unit and integration tests.

#### Test Database Scenarios

1. **`simple_counter.db`** - Basic counter metrics
   - Single counter metric over 5 minutes (10 snapshots @ 30s intervals)
   - Two label dimensions: `{activity=read|write}`, `{host=server1}`
   - Purpose: Test instant queries, simple ranges, basic rate calculations

2. **`multi_dimensional.db`** - Complex label combinations
   - Metrics with 3-4 dimensional labels
   - Examples: `{activity=read|write, host=server1|server2|server3, region=us-east|us-west, env=prod|staging}`
   - Purpose: Test label filtering, aggregations with grouping, topk queries

3. **`latency_timers.db`** - Summary/timer metrics with quantiles
   - Latency metrics with p50, p75, p95, p99 quantiles
   - Count, sum, min, max, mean, stddev
   - Multiple operations: `{operation=select|insert|update|delete}`
   - Purpose: Test quantile queries, statistical aggregations

4. **`rate_calculations.db`** - Counter metrics for rate testing
   - Counters with predictable growth patterns (linear, exponential, step)
   - Sufficient data points for various time windows (5m, 15m, 1h)
   - Purpose: Test rate() and increase() commands with known expected results

5. **`sparse_data.db`** - Gaps and edge cases
   - Missing data points (simulating dropped snapshots)
   - Metrics that start/stop mid-session
   - Zero values and null handling
   - Purpose: Test edge case handling, null safety

6. **`meters.db`** - Meter/throughput metrics
   - Metrics with rate statistics (mean, m1, m5, m15)
   - Purpose: Test meter rate queries

7. **`histograms.db`** - Optional histogram data
   - Metrics with Base64-encoded HDR histograms
   - Purpose: Test histogram persistence and future HDR analysis

8. **`live_session_simulation.db`** - Realistic session data
   - Mix of all metric types as would appear in real NoSQLBench run
   - Multiple workloads with different phases
   - Purpose: Integration testing, realistic query scenarios

#### Implementation Tasks

- [ ] Create `TestDatabaseGenerator` class in `src/test/java/io/nosqlbench/nb/mql/testdata/`
- [ ] Implement generator methods for each scenario using `SqliteSnapshotReporter`
- [ ] Generate databases to `src/test/resources/testdata/` directory
- [ ] Create `TEST_DATABASES.md` documenting:
  - Each database schema and contents
  - Expected query results for common queries
  - Sample queries demonstrating usage
- [ ] Add `TestDatabaseLoader` utility for tests to easily access databases
- [ ] Write validation tests ensuring databases have expected structure

#### Example Generator Structure

```java
public class TestDatabaseGenerator {
    /**
     * Generates simple_counter.db
     * Schema:
     * - Metric: activity_ops_total (COUNTER)
     * - Labels: activity={read,write}, host=server1
     * - Time range: 5 minutes (10 snapshots @ 30s)
     * - Values: Linear growth from 0 to 100
     */
    public static void generateSimpleCounter(Path outputPath) { ... }

    /**
     * Generates multi_dimensional.db
     * Schema: ...
     */
    public static void generateMultiDimensional(Path outputPath) { ... }

    // ... additional generators
}
```

#### Test Database Documentation Structure

**`src/test/resources/testdata/TEST_DATABASES.md`**:
```markdown
# Test Database Documentation

## simple_counter.db

### Purpose
Test basic counter queries, instant values, simple rate calculations.

### Schema
- **Metric Family**: `activity_ops`
  - **Sample**: `activity_ops_total` (COUNTER)
  - **Type**: COUNTER

### Label Dimensions
- `activity`: {read, write}
- `host`: server1

### Time Series Data
- Start: 2025-10-23 10:00:00
- End: 2025-10-23 10:05:00
- Interval: 30 seconds
- Snapshots: 10

### Data Pattern
| Timestamp | activity=read | activity=write |
|-----------|---------------|----------------|
| 10:00:00  | 0             | 0              |
| 10:00:30  | 10            | 15             |
| 10:01:00  | 20            | 30             |
| ...       | ...           | ...            |

### Expected Query Results
```sql
-- Instant query at 10:05:00
SELECT value FROM ... WHERE sample='activity_ops_total'
  AND timestamp = (SELECT MAX(timestamp) FROM sample_value)
  AND activity='read'
-- Expected: 100

-- Rate over 5m (should be ~0.33 ops/sec for read)
...
```

## multi_dimensional.db
...
```

### Milestone 1: Schema & Foundation (2 weeks)
- [ ] Create `MetricsSchema` class with table/column constants
- [ ] Implement `MetricsDatabaseReader` for read-only database access
- [ ] Build `MetricsQueryCommand` interface and base classes
- [ ] Create `QueryResult` and result formatting infrastructure
- [ ] Implement `TimeWindowParser` (parse "5m", "1h", "1d", etc.)
- [ ] Implement `LabelMatcher` for label filtering
- [ ] Build `SQLiteQueryBuilder` utility with prepared statement support
- [ ] Write unit tests with in-memory SQLite fixtures
- [ ] Implement table/JSON/CSV formatters

### Milestone 2: Core Commands (2-3 weeks)
- [ ] Implement `InstantCommand` (simplest - current value)
- [ ] Implement `RangeCommand` (time-series data)
- [ ] Implement `RateCommand` (per-second rate calculation)
- [ ] Implement `IncreaseCommand` (total increase over period)
- [ ] Add comprehensive unit tests for each command
- [ ] Integration tests with real schema

### Milestone 3: Aggregation & Analysis Commands (2 weeks)
- [ ] Implement `AggregateCommand` (sum/avg/max/min with grouping)
- [ ] Implement `QuantileCommand` (extract percentiles)
- [ ] Implement `RatioCommand` (calculate error rates, etc.)
- [ ] Add advanced label filtering (regex, negation)
- [ ] Integration tests for aggregation scenarios

### Milestone 4: Advanced Commands (1-2 weeks)
- [ ] Implement `TopKCommand` (top N by value)
- [ ] Implement `LabelFilterCommand` (advanced filtering)
- [ ] Implement `BinaryOpCommand` (arithmetic operations)
- [ ] Add query optimization hints and explain mode
- [ ] Implement query result caching for repeated queries

### Milestone 5: CLI & Packaging (1 week)
- [ ] Build `MetricsQLCLI` main class with picocli
- [ ] Implement command routing and help system
- [ ] Package as standalone JAR with dependencies
- [ ] Create wrapper scripts (Unix/Windows)
- [ ] Test standalone execution

### Milestone 6: Documentation & Integration (1 week)
- [ ] Create comprehensive user documentation with examples
- [ ] Write developer guide for adding new commands
- [ ] Add integration tests using real NoSQLBench scenarios
- [ ] Create cheat sheet and tutorial
- [ ] Update main NoSQLBench docs with MQL reference

### Milestone 7: Future Enhancements (Optional)
- [ ] Web UI for interactive query building
- [ ] Query history and saved queries
- [ ] Export to Grafana-compatible formats
- [ ] Full MetricsQL parser using ANTLR4
- [ ] Query performance profiling
- [ ] Integration as `nb5 mql` subcommand

---

## Technical Decisions

### Why Not Full Parser Initially?
1. **No existing libraries**: Would require building from scratch
2. **Complexity**: Full PromQL parser is ~3000 LOC in Go
3. **Pragmatism**: 10 commands cover 80%+ of use cases
4. **Time to value**: Working queries in weeks vs. months
5. **Learning curve**: Command forms are more discoverable

### Why SQLite vs. In-Memory Processing?
1. **Data already there**: Metrics are in SQLite
2. **Mature engine**: SQLite has excellent query optimization
3. **Window functions**: Native support for time-series analysis
4. **Disk-based**: Can query large historical datasets
5. **Standard SQL**: Easy to understand generated queries

### Design for Extension
- **Plugin architecture**: Commands register via SPI
- **Query AST**: Commands build intermediate representation that could be generated by parser
- **Testability**: Each command is independently testable
- **Documentation**: Auto-generate help from command metadata

---

## Example Usage Scenarios

### Scenario 1: Troubleshooting Performance Drop
```bash
# What's the current ops rate?
nb-metricsql instant --metric activity_ops_total

# Has throughput dropped?
nb-metricsql rate --metric activity_ops_total --window 5m --labels activity=write

# What's the 95th percentile latency?
nb-metricsql quantile --metric op_latency_ms --p 0.95 --last 10m

# Compare read vs write latency
nb-metricsql agg --metric op_latency_ms --func avg --by activity --last 5m
```

### Scenario 2: Analyzing Load Test Results
```bash
# Total operations executed
nb-metricsql increase --metric activity_ops_total --window 1h

# Error rate over time
nb-metricsql ratio \
  --numerator errors_total \
  --denominator requests_total \
  --window 5m

# Top 5 slowest operations
nb-metricsql topk --metric op_latency_ms --n 5 --by operation --last 1h
```

### Scenario 3: Exporting for Analysis
```bash
# Export all metrics to CSV for spreadsheet analysis
nb-metricsql range \
  --metric '*' \
  --start 2025-10-23T10:00:00Z \
  --end 2025-10-23T11:00:00Z \
  --format csv > metrics.csv

# Get JSON for programmatic processing
nb-metricsql agg --metric ops_total --func sum --format json | jq '.rows[0].sum_ops'
```

---

## Alternative Approaches Considered

### 1. Embed VictoriaMetrics (Rejected)
- **Pros**: Full MetricsQL support, battle-tested
- **Cons**: Go binary, complex integration, different storage format, resource overhead

### 2. HTTP API to Prometheus (Rejected)
- **Pros**: Standard PromQL, ecosystem tools
- **Cons**: Requires running Prometheus, network dependency, format conversion, double storage

### 3. Build Full Parser First (Rejected)
- **Pros**: Complete MetricsQL compatibility
- **Cons**: Months of work, maintenance burden, overkill for MVP

### 4. Python Scripts (Rejected)
- **Pros**: Quick prototypes, pandas for analysis
- **Cons**: External dependency, not bundled with NoSQLBench, slower than Java

---

## Success Criteria

1. **Functionality**: All 10 commands implemented and tested
2. **Performance**: Queries execute in <1s for typical datasets (1M samples)
3. **Usability**: Clear error messages, helpful documentation
4. **Integration**: Works seamlessly with existing NoSQLBench workflows
5. **Extensibility**: Easy to add new commands or upgrade to full parser

---

## Symlink Behavior and Separation of Concerns

### Current Behavior (Unchanged by nb-mql-api)

**Writer Side (`nb-api/SqliteSnapshotReporter`):**
1. Each session creates a database: `logs/<session-name>_metrics.db`
2. `NBSession` creates/updates symlink: `logs/metrics.db` → `<session-name>_metrics.db`
3. Symlink is updated atomically when a new session starts
4. Writer holds an open JDBC connection throughout the session
5. Commits occur after each metrics snapshot (every 30s by default)

**Example:**
```
logs/
├── session_20251023_103000_metrics.db    (active session)
├── session_20251023_092000_metrics.db    (previous session)
└── metrics.db -> session_20251023_103000_metrics.db  (symlink)
```

### Impact on Query Engine (nb-mql-api)

**Good News: Zero Impact on Symlink Creation**
- `nb-mql-api` is **read-only** and never creates or modifies symlinks
- Symlink management stays entirely in `NBSession` (nb-engine-core)
- No code needs to be moved or changed for symlink behavior

**Query Engine Behavior with Symlinks:**

#### 1. Querying Live Sessions
```bash
# Query the currently running session (via symlink)
nb-metricsql rate --db logs/metrics.db --metric ops_total --window 5m
```

**SQLite Behavior:**
- Read-only queries use **shared locks** (SHARED mode)
- Writer uses **reserved locks** for commits (RESERVED → EXCLUSIVE briefly)
- **WAL mode** (Write-Ahead Logging) allows concurrent reads during writes
- SQLite automatically handles lock contention with retries

**Safety Considerations:**
1. ✅ **Safe for concurrent reads**: SQLite WAL mode allows multiple readers + 1 writer
2. ✅ **Consistent reads**: Queries see a consistent snapshot
3. ⚠️ **Symlink race**: If session ends and symlink updates mid-query, query uses old file (harmless)
4. ⚠️ **Lock timeouts**: Very long queries might timeout if writer needs exclusive lock (rare)

#### 2. Querying Historical Sessions
```bash
# Query specific session file directly (no symlink)
nb-metricsql rate --db logs/session_20251023_092000_metrics.db --metric ops_total --window 1h
```

**Safety Considerations:**
1. ✅ **Completely safe**: No writer, no lock contention
2. ✅ **Optimal performance**: Read-only queries on closed databases

#### 3. Default Database Path
```bash
# Default: use logs/metrics.db symlink
nb-metricsql rate --metric ops_total --window 5m
```

**Implementation:**
```java
public class MetricsDatabaseReader {
    private static final Path DEFAULT_DB = Path.of("logs/metrics.db");

    public static Connection connect(Path dbPath) throws SQLException {
        Path resolvedPath = dbPath != null ? dbPath : DEFAULT_DB;
        String jdbcUrl = "jdbc:sqlite:" + resolvedPath.toAbsolutePath();
        Connection conn = DriverManager.getConnection(jdbcUrl);

        // Enable read-only mode for safety
        conn.setReadOnly(true);

        // Use WAL mode if available (already enabled by writer)
        // This allows concurrent reads with writes
        return conn;
    }
}
```

### Handling Edge Cases

#### Edge Case 1: Symlink Deleted/Missing
```java
public static Connection connect(Path dbPath) throws SQLException {
    Path resolvedPath = dbPath != null ? dbPath : DEFAULT_DB;

    if (!Files.exists(resolvedPath)) {
        throw new SQLException(
            "Database not found at: " + resolvedPath + "\n" +
            "Hint: Ensure a NoSQLBench session is running or specify a session file with --db"
        );
    }

    // Follow symlinks automatically (default behavior)
    String jdbcUrl = "jdbc:sqlite:" + resolvedPath.toAbsolutePath();
    Connection conn = DriverManager.getConnection(jdbcUrl);
    conn.setReadOnly(true);
    return conn;
}
```

#### Edge Case 2: Symlink Updated During Query
**Scenario**: Session ends, new session starts, symlink updates mid-query.

**SQLite Behavior**: Query continues on the original file (inode doesn't change).

**Impact**: None - query completes on the previous session's data.

**User Experience**: If user runs the same command twice:
```bash
# First query (session A active)
nb-metricsql instant --metric ops_total
> 1000 ops

# Session A ends, session B starts, symlink updates

# Second query (session B active)
nb-metricsql instant --metric ops_total
> 0 ops  (new session, counters reset)
```

This is **expected behavior** - symlink points to the current session.

#### Edge Case 3: Database Locked
```java
public static Connection connect(Path dbPath) throws SQLException {
    Path resolvedPath = dbPath != null ? dbPath : DEFAULT_DB;
    String jdbcUrl = "jdbc:sqlite:" + resolvedPath.toAbsolutePath();

    // Set busy timeout to handle brief exclusive locks
    Properties props = new Properties();
    props.setProperty("busy_timeout", "5000");  // 5 second timeout

    Connection conn = DriverManager.getConnection(jdbcUrl, props);
    conn.setReadOnly(true);
    return conn;
}
```

### Recommendations for nb-mql-api

1. **Always use read-only connections**: `conn.setReadOnly(true)`
2. **Set busy timeout**: 5-10 seconds to handle brief write locks
3. **Default to symlink**: `logs/metrics.db` for convenience
4. **Allow explicit paths**: `--db <path>` for querying specific sessions
5. **Clear error messages**: Help users diagnose missing/locked databases

### Updated CLI Design

```bash
# Default: query current session via symlink
nb-metricsql rate --metric ops_total --window 5m

# Explicit session file (historical data)
nb-metricsql rate --db logs/session_20251023_092000_metrics.db --metric ops_total --window 1h

# Different working directory
nb-metricsql rate --db /path/to/logs/metrics.db --metric ops_total --window 5m
```

**CLI Options:**
```java
@Option(names = {"-d", "--db"},
        description = "Path to SQLite metrics database (default: logs/metrics.db)")
private Path databasePath = Path.of("logs/metrics.db");
```

### Summary: No Changes Needed to Symlink Behavior

**What stays the same:**
- ✅ `NBSession` creates and manages `logs/metrics.db` symlink
- ✅ `SqliteSnapshotReporter` writes to session-specific files
- ✅ Symlink always points to the current session
- ✅ Historical sessions remain accessible via full paths

**What nb-mql-api adds:**
- ✅ Read-only query access to any database file
- ✅ Defaults to `logs/metrics.db` symlink for convenience
- ✅ Supports explicit paths for querying historical sessions
- ✅ Safe concurrent queries during active sessions (WAL mode)

**The separation of concerns is perfect:**
- **nb-api** (writer): Manages database creation, symlinks, and writes
- **nb-mql-api** (reader): Connects to existing databases read-only

No coordination needed beyond the shared SQLite schema!

---

## Open Questions

1. **Output format defaults**: Table for interactive, JSON for pipes?
2. **Time range syntax**: Support natural language ("last hour") vs strict ISO8601?
3. **Label regex engine**: SQLite REGEXP vs Java Pattern?
4. **Histogram support**: Should we deserialize Base64 HDR histograms for advanced queries?

---

## References

- VictoriaMetrics MetricsQL: https://docs.victoriametrics.com/victoriametrics/metricsql/
- Prometheus OpenMetrics: https://prometheus.io/docs/specs/om/open_metrics_spec/
- NoSQLBench SQLite Schema: `/local/sqlite_metrics.md`
- NoSQLBench Reporter: `SqliteSnapshotReporter.java`
- PromQL Cheat Sheet: https://signoz.io/guides/promql-cheat-sheet/

---

## Next Steps

1. **Validate blueprint** with NoSQLBench maintainers
2. **Create prototype** of RateCommand to validate SQL generation approach
3. **Set up project structure** (`nb-metricsql` module)
4. **Begin Milestone 1** implementation

---

*Blueprint Version: 1.0*
*Author: Claude Code*
*Date: 2025-10-23*
