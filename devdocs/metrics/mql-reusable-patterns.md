---
title: "MetricsQL Reusable Patterns"
description: "Existing SQL building blocks to accelerate MetricsQL command development."
audience: developer
diataxis: explanation
tags:
  - metricsql
  - patterns
component: core
topic: metrics
status: live
owner: "@nosqlbench/metrics"
generated: false
---

# Reusable Patterns from Existing MQL Commands

## SQL Building Patterns

### 1. Common Query Structure (CTE Pattern)

All existing commands use Common Table Expressions (CTEs) for clean query structure:

```java
// From InstantCommand.java
sql.append("WITH latest_snapshot AS (\n");
sql.append("  SELECT MAX(timestamp_ms) AS max_ts\n");
sql.append("  FROM sample_value\n");
sql.append("),\n");
sql.append("labeled_samples AS (\n");
sql.append("  -- main query logic\n");
sql.append(")\n");
sql.append("SELECT ... FROM labeled_samples");
```

**Reusable Pattern**: Create a `CTEBuilder` class:

```java
public class CTEBuilder {
    private final List<CTE> ctes = new ArrayList<>();

    public CTEBuilder with(String name, String query) {
        ctes.add(new CTE(name, query));
        return this;
    }

    public String build(String finalSelect) {
        StringBuilder sql = new StringBuilder("WITH ");
        for (int i = 0; i < ctes.size(); i++) {
            if (i > 0) sql.append(",\n");
            sql.append(ctes.get(i).toSQL());
        }
        sql.append("\n").append(finalSelect);
        return sql.toString();
    }
}
```

### 2. Label Filtering Pattern

Consistent pattern for filtering by labels across all commands:

```java
// From multiple commands
for (String labelKey : labelFilters.keySet()) {
    sql.append("    AND mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append(" IN (\n");
    sql.append("      SELECT lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID).append("\n");
    sql.append("      FROM label_set_membership lsm\n");
    sql.append("      JOIN label_key lk ON lk.id = lsm.label_key_id\n");
    sql.append("      JOIN label_value lv ON lv.id = lsm.label_value_id\n");
    sql.append("      WHERE lk.name = ? AND lv.value = ?\n");
    sql.append("    )\n");
}
```

**Reusable Method**:
```java
public static String buildLabelFilterClause(Map<String, String> labels, List<Object> params) {
    if (labels.isEmpty()) return "";

    StringBuilder clause = new StringBuilder();
    for (Map.Entry<String, String> entry : labels.entrySet()) {
        clause.append(" AND mi.label_set_id IN (\n");
        clause.append("   SELECT lsm.label_set_id FROM label_set_membership lsm\n");
        clause.append("   JOIN label_key lk ON lk.id = lsm.label_key_id\n");
        clause.append("   JOIN label_value lv ON lv.id = lsm.label_value_id\n");
        clause.append("   WHERE lk.name = ? AND lv.value = ?)\n");
        params.add(entry.getKey());
        params.add(entry.getValue());
    }
    return clause.toString();
}
```

### 3. Time Range Pattern

Standard approach for time filtering:

```java
// From RateCommand.java
Long startTimestampMs = null;
Long endTimestampMs = null;

if (params.containsKey("window")) {
    String window = (String) params.get("window");
    long windowMs = TimeWindowParser.parseToMillis(window);
    endTimestampMs = getCurrentMaxTimestamp(conn);
    startTimestampMs = endTimestampMs - windowMs;
}

// In SQL building:
if (startTimestampMs != null) {
    sql.append(" AND sv.timestamp_ms >= ? AND sv.timestamp_ms <= ?");
}
```

**Reusable Class**:
```java
public class TimeRangeFilter {
    private final Long startMs;
    private final Long endMs;

    public static TimeRangeFilter fromWindow(Connection conn, String window) {
        long windowMs = TimeWindowParser.parseToMillis(window);
        long endMs = getCurrentMaxTimestamp(conn);
        return new TimeRangeFilter(endMs - windowMs, endMs);
    }

    public String toSQLClause(String timestampColumn) {
        if (startMs == null) return "";
        return String.format(" AND %s >= ? AND %s <= ?",
                            timestampColumn, timestampColumn);
    }

    public void addParameters(PreparedStatement ps, int startIndex) {
        if (startMs != null) {
            ps.setLong(startIndex, startMs);
            ps.setLong(startIndex + 1, endMs);
        }
    }
}
```

### 4. Label Concatenation Pattern

Used for displaying labels in results:

```java
// From multiple commands
sql.append("GROUP_CONCAT(lk.name || '=' || lv.value, ', ') AS labels");
```

**Reusable Constant**:
```java
public static final String LABEL_CONCAT_SQL =
    "GROUP_CONCAT(lk.name || '=' || lv.value, ', ') AS labels";
```

### 5. Window Function Patterns

For rate calculations and time-based operations:

```java
// From RateCommand.java
sql.append("LAG(value) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_value,\n");
sql.append("LAG(timestamp_ms) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_timestamp\n");
```

**Reusable Templates**:
```java
public class WindowFunctionTemplates {
    public static String lagValue(String partitionBy, String orderBy) {
        return String.format("LAG(value) OVER (PARTITION BY %s ORDER BY %s)",
                            partitionBy, orderBy);
    }

    public static String leadValue(String partitionBy, String orderBy) {
        return String.format("LEAD(value) OVER (PARTITION BY %s ORDER BY %s)",
                            partitionBy, orderBy);
    }

    public static String rowNumber(String partitionBy, String orderBy) {
        return String.format("ROW_NUMBER() OVER (PARTITION BY %s ORDER BY %s)",
                            partitionBy, orderBy);
    }
}
```

## Command Interface Patterns

### 1. Parameter Validation Pattern

All commands follow similar validation:

```java
@Override
public void validate(Map<String, Object> params) throws InvalidQueryException {
    // Required parameter check
    if (!params.containsKey("metric")) {
        throw new InvalidQueryException("Missing required parameter: metric");
    }

    // Type validation
    Object metric = params.get("metric");
    if (!(metric instanceof String) || ((String) metric).trim().isEmpty()) {
        throw new InvalidQueryException("Parameter 'metric' must be a non-empty string");
    }

    // Optional parameter validation
    if (params.containsKey("labels")) {
        Object labels = params.get("labels");
        if (!(labels instanceof Map)) {
            throw new InvalidQueryException("Parameter 'labels' must be a Map<String, String>");
        }
    }
}
```

**Reusable Validator**:
```java
public class ParameterValidator {
    private final Map<String, Object> params;
    private final List<String> errors = new ArrayList<>();

    public ParameterValidator requireString(String key) {
        if (!params.containsKey(key)) {
            errors.add("Missing required parameter: " + key);
        } else {
            Object value = params.get(key);
            if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
                errors.add("Parameter '" + key + "' must be a non-empty string");
            }
        }
        return this;
    }

    public ParameterValidator requireMap(String key) {
        if (params.containsKey(key)) {
            if (!(params.get(key) instanceof Map)) {
                errors.add("Parameter '" + key + "' must be a Map");
            }
        }
        return this;
    }

    public void validate() throws InvalidQueryException {
        if (!errors.isEmpty()) {
            throw new InvalidQueryException(String.join("\n", errors));
        }
    }
}
```

### 2. Query Result Building Pattern

Standard approach for building results:

```java
List<String> columns = new ArrayList<>();
columns.add("timestamp");
columns.add("value");
columns.add("labels");

List<Map<String, Object>> rows = new ArrayList<>();

try (ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("timestamp", rs.getTimestamp("timestamp"));
        row.put("value", rs.getDouble("value"));
        row.put("labels", rs.getString("labels"));
        rows.add(row);
    }
}

return new QueryResult(columns, rows, sql, executionTime);
```

**Reusable Builder**:
```java
public class QueryResultBuilder {
    private final List<String> columns;
    private final List<Map<String, Object>> rows = new ArrayList<>();

    public QueryResultBuilder(String... columnNames) {
        this.columns = Arrays.asList(columnNames);
    }

    public QueryResultBuilder fromResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String column : columns) {
                row.put(column, rs.getObject(column));
            }
            rows.add(row);
        }
        return this;
    }

    public QueryResult build(String sql, long executionTime) {
        return new QueryResult(columns, rows, sql, executionTime);
    }
}
```

## Utility Methods to Extract

### From Existing Commands

1. **getCurrentMaxTimestamp()** - Used by multiple commands
```java
private long getCurrentMaxTimestamp(Connection conn) throws SQLException {
    String sql = "SELECT MAX(" + MetricsSchema.COL_SV_TIMESTAMP_MS +
                 ") FROM " + MetricsSchema.TABLE_SAMPLE_VALUE;
    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            return rs.getLong(1);
        }
        throw new SQLException("No data in database");
    }
}
```

2. **MetricsSchema join helpers** - Already available:
- `joinMetricInstance()`
- `joinSampleName()`
- `joinLabelSet()`
- `joinAllLabels()`
- `joinAllLabelsWithSampleName()`

## Test Patterns

### Test Database Generation

From `TestDatabaseGenerator.java`:
```java
public class TestDatabaseGenerator {
    // Generate counter metrics
    public void generateCounter(String name, Map<String, String> labels,
                                int points, Duration interval);

    // Generate gauge metrics
    public void generateGauge(String name, Map<String, String> labels,
                              int points, double min, double max);
}
```

### Test Command Pattern

From existing test classes:
```java
@Test
public void testBasicQuery() throws Exception {
    // Setup test data
    generator.generateCounter("test_metric", Map.of("job", "api"), 100, Duration.ofMinutes(1));

    // Execute command
    Map<String, Object> params = Map.of(
        "metric", "test_metric",
        "window", "5m"
    );
    QueryResult result = command.execute(conn, params);

    // Verify results
    assertNotNull(result);
    assertTrue(result.getRows().size() > 0);
}
```

## Integration Points for MetricsQL Parser

### 1. Command Registry Extension

Create a new command that uses the parser:

```java
public class MetricsQLCommand implements MetricsQueryCommand {
    private final MetricsQLParser parser = new MetricsQLParser();
    private final SQLTransformer transformer = new SQLTransformer();

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params) {
        String query = (String) params.get("query");

        // Parse MetricsQL
        ASTNode ast = parser.parse(query);

        // Transform to SQL
        SQLQuery sqlQuery = transformer.transform(ast);

        // Execute using existing patterns
        try (PreparedStatement ps = conn.prepareStatement(sqlQuery.getSql())) {
            sqlQuery.bindParameters(ps);
            // ... rest follows existing pattern
        }
    }
}
```

### 2. Reuse Existing Formatters

The output formatting infrastructure is already complete:
- `TableFormatter`
- `JsonFormatter`
- `CsvFormatter`
- `TsvFormatter`
- `JsonLinesFormatter`
- `MarkdownFormatter`

### 3. CLI Integration

Extend `MetricsQLCLI` to add the new query command:

```java
@Command(name = "query", description = "Execute MetricsQL query")
public class QueryCLI extends AbstractQueryCommandCLI {
    @Parameters(description = "MetricsQL query")
    private String query;

    @Override
    protected Map<String, Object> buildCommandParams() {
        return Map.of("query", query);
    }
}
```

## Summary of Reusable Components

✅ **Already Available**:
- TimeWindowParser - Duration parsing
- MetricsSchema - Schema constants and joins
- ResultFormatter hierarchy - Output formatting
- LabelSetResolver - Label operations
- TestDatabaseGenerator - Test data generation
- CLI framework (picocli) - Command parsing
- ANTLR runtime - Parser generation

✅ **Extract from Existing**:
- CTE building patterns
- Label filtering SQL
- Time range handling
- Parameter validation
- Query result building
- Window function patterns

✅ **New Components Needed**:
- MetricsQL grammar files
- AST visitor implementation
- SQL transformer hierarchy
- Function registry
- Query optimizer

This approach maximizes code reuse while maintaining consistency with the existing codebase patterns.
