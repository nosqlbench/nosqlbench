---
title: "MetricsQL Implementation Robustness"
description: "Guidelines for building resilient MetricsQL parsing and execution."
audience: developer
diataxis: explanation
tags:
  - metricsql
  - robustness
component: core
topic: metrics
status: live
owner: "@nosqlbench/metrics"
generated: false
---

# MetricsQL Implementation Robustness Guidelines

## 🛡️ Defensive Programming Practices

### 1. Parser Robustness

#### Grammar Design Principles
```antlr
// GOOD: Explicit error recovery rules
expression
    : functionCall
    | selector
    | binaryOp
    | aggregation
    | error { notifyError("Invalid expression at line " + $line); }
    ;

// GOOD: Clear precedence rules to avoid ambiguity
expression
    : expression ('*'|'/'|'%') expression  # MulDivMod
    | expression ('+'|'-') expression      # AddSub
    | expression comparison expression     # Compare
    | '(' expression ')'                   # Parens
    ;
```

#### Parser Error Handling
- **Implement custom error listener** to provide helpful error messages
- **Track parsing context** for better error reporting
- **Provide suggestions** for common syntax errors
- **Fail fast** on unrecoverable errors

```java
public class MetricsQLErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                           int line, int charPositionInLine, String msg, RecognitionException e) {
        // Provide context-aware error messages
        String sourceName = recognizer.getInputStream().getSourceName();
        throw new ParseException(String.format(
            "Syntax error at %s:%d:%d - %s\nDid you mean: %s?",
            sourceName, line, charPositionInLine, msg,
            getSuggestion(offendingSymbol, msg)
        ));
    }
}
```

### 2. SQL Injection Prevention

#### Parameter Binding Strategy
```java
// NEVER: String concatenation for user input
String sql = "WHERE metric = '" + userInput + "'";  // ❌ VULNERABLE

// ALWAYS: Parameterized queries
String sql = "WHERE metric = ?";  // ✅ SAFE
ps.setString(1, userInput);
```

#### Label Name Validation
```java
public class LabelValidator {
    private static final Pattern VALID_LABEL = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public static void validateLabelName(String label) {
        if (!VALID_LABEL.matcher(label).matches()) {
            throw new InvalidQueryException(
                "Invalid label name: " + label +
                ". Labels must start with letter/underscore and contain only alphanumeric/underscore"
            );
        }
    }
}
```

### 3. Resource Management

#### Connection Pool Configuration
```java
public class MetricsQLConnectionManager {
    private static final int MAX_CONNECTIONS = 10;
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    public Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);  // Read-only queries
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        // Set query timeout to prevent runaway queries
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 5000");  // 5 second busy timeout
            stmt.execute("PRAGMA query_only = ON");      // Enforce read-only
        }
        return conn;
    }
}
```

#### Memory Management for Large Results
```java
public class StreamingQueryResult extends QueryResult {
    private static final int MAX_ROWS_IN_MEMORY = 10000;

    @Override
    public List<Map<String, Object>> getRows() {
        if (rowCount > MAX_ROWS_IN_MEMORY) {
            throw new QueryException(
                "Result set too large (" + rowCount + " rows). " +
                "Use pagination or more specific filters."
            );
        }
        return rows;
    }
}
```

## 🧪 Testing Strategy for Reliability

### 1. Grammar Testing

#### Positive Test Cases
```java
@ParameterizedTest
@ValueSource(strings = {
    "metric",
    "metric{label=\"value\"}",
    "rate(metric[5m])",
    "sum(metric) by (job)",
    "metric1 + metric2"
})
void testValidExpressions(String expression) {
    assertDoesNotThrow(() -> parser.parse(expression));
}
```

#### Negative Test Cases (Malformed Input)
```java
@ParameterizedTest
@ValueSource(strings = {
    "metric{",                    // Unclosed brace
    "rate(metric[5m)",           // Unclosed parenthesis
    "metric{label=}",            // Missing value
    "sum() by (job)",            // Missing metric
    "metric + ",                 // Incomplete expression
    "rate(metric[invalid])",     // Invalid duration
})
void testInvalidExpressions(String expression) {
    ParseException ex = assertThrows(ParseException.class,
        () -> parser.parse(expression));
    assertTrue(ex.getMessage().contains("Syntax error"));
}
```

### 2. SQL Generation Testing

#### SQL Injection Tests
```java
@Test
void testSQLInjectionPrevention() {
    String maliciousInput = "metric'; DROP TABLE sample_value; --";

    Map<String, Object> params = Map.of("metric", maliciousInput);
    SQLQuery query = transformer.transform(params);

    // Verify parameters are bound, not concatenated
    assertFalse(query.getSql().contains("DROP TABLE"));
    assertEquals(1, query.getParameters().size());
    assertEquals(maliciousInput, query.getParameters().get(0));
}
```

#### Edge Case Testing
```java
@Test
void testEmptyDatabase() {
    // Test with no data
    QueryResult result = command.execute(emptyConn, params);
    assertNotNull(result);
    assertTrue(result.getRows().isEmpty());
}

@Test
void testSingleDataPoint() {
    // Test with exactly one data point (edge case for rate/delta)
    generateSinglePoint();
    QueryResult result = rateCommand.execute(conn, params);
    assertTrue(result.getRows().isEmpty()); // Rate needs at least 2 points
}

@Test
void testCounterReset() {
    // Test counter reset detection
    generateCounterWithReset();
    QueryResult result = rateCommand.execute(conn, params);
    // Verify counter reset is handled correctly
    assertNoNegativeRates(result);
}
```

### 3. Performance Testing

```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void testLargeDatasetPerformance() {
    // Generate 1M data points
    generateLargeDataset(1_000_000);

    long start = System.currentTimeMillis();
    QueryResult result = command.execute(conn, complexQuery);
    long duration = System.currentTimeMillis() - start;

    assertTrue(duration < 5000, "Query took " + duration + "ms");
    assertNotNull(result);
}
```

## ⚡ Performance Optimization Guidelines

### 1. Query Optimization

#### Index Utilization Check
```java
public class QueryOptimizer {
    public void validateQueryPlan(String sql) throws SQLException {
        String explainSql = "EXPLAIN QUERY PLAN " + sql;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(explainSql)) {

            while (rs.next()) {
                String detail = rs.getString("detail");
                if (detail.contains("SCAN TABLE")) {
                    logger.warn("Potential performance issue: Full table scan detected");
                    // Consider adding index or optimizing query
                }
            }
        }
    }
}
```

#### Query Result Caching
```java
public class QueryCache {
    private final Cache<String, QueryResult> cache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public QueryResult execute(String sql, List<Object> params) {
        String cacheKey = sql + params.toString();

        return cache.get(cacheKey, () -> {
            // Execute query only if not cached
            return executeQuery(sql, params);
        });
    }
}
```

### 2. Batch Processing

```java
public class BatchQueryProcessor {
    private static final int BATCH_SIZE = 1000;

    public void processBatch(List<String> metrics) {
        // Process in batches to avoid overwhelming the database
        for (int i = 0; i < metrics.size(); i += BATCH_SIZE) {
            List<String> batch = metrics.subList(i,
                Math.min(i + BATCH_SIZE, metrics.size()));
            processSingleBatch(batch);
        }
    }
}
```

## 🔍 Debugging and Observability

### 1. Query Logging

```java
public class QueryLogger {
    private static final Logger logger = LoggerFactory.getLogger(QueryLogger.class);

    public void logQuery(String metricsQL, String generatedSQL, long executionTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("MetricsQL: {}", metricsQL);
            logger.debug("Generated SQL: {}", generatedSQL);
            logger.debug("Execution time: {}ms", executionTime);
        }

        if (executionTime > 1000) {
            logger.warn("Slow query detected ({}ms): {}", executionTime, metricsQL);
        }
    }
}
```

### 2. Metrics Collection

```java
public class QueryMetrics {
    private final Timer queryTimer;
    private final Counter errorCounter;
    private final Histogram resultSizeHistogram;

    public void recordQuery(String functionType, long duration, int resultSize) {
        queryTimer.record(duration, TimeUnit.MILLISECONDS);
        resultSizeHistogram.update(resultSize);

        // Track function usage patterns
        Metrics.counter("metricsql.function", "type", functionType).increment();
    }

    public void recordError(String errorType) {
        errorCounter.increment();
        Metrics.counter("metricsql.error", "type", errorType).increment();
    }
}
```

## 🏗️ Maintainability Guidelines

### 1. Version Compatibility

```java
public class SchemaVersionChecker {
    private static final int MINIMUM_SCHEMA_VERSION = 1;
    private static final int MAXIMUM_SCHEMA_VERSION = 2;

    public void checkCompatibility(Connection conn) throws SQLException {
        int version = getSchemaVersion(conn);

        if (version < MINIMUM_SCHEMA_VERSION) {
            throw new IncompatibleSchemaException(
                "Schema version " + version + " is too old. " +
                "Minimum supported version is " + MINIMUM_SCHEMA_VERSION
            );
        }

        if (version > MAXIMUM_SCHEMA_VERSION) {
            logger.warn("Schema version {} is newer than tested version {}. " +
                       "Some features may not work correctly.",
                       version, MAXIMUM_SCHEMA_VERSION);
        }
    }
}
```

### 2. Feature Flags

```java
public enum MetricsQLFeature {
    REGEX_MATCHING("metricsql.regex.enabled", true),
    HISTOGRAM_FUNCTIONS("metricsql.histogram.enabled", false),
    PREDICTION_FUNCTIONS("metricsql.prediction.enabled", false);

    private final String property;
    private final boolean defaultValue;

    public boolean isEnabled() {
        return Boolean.parseBoolean(
            System.getProperty(property, String.valueOf(defaultValue))
        );
    }
}

// Usage
if (MetricsQLFeature.REGEX_MATCHING.isEnabled()) {
    // Process regex
} else {
    throw new UnsupportedOperationException("Regex matching is disabled");
}
```

### 3. Graceful Degradation

```java
public class FunctionRegistry {
    private final Map<String, FunctionTransformer> functions = new HashMap<>();
    private final FunctionTransformer defaultTransformer = new UnsupportedFunctionTransformer();

    public FunctionTransformer getTransformer(String functionName) {
        FunctionTransformer transformer = functions.get(functionName);

        if (transformer == null) {
            logger.warn("Function '{}' not implemented, using default handler", functionName);
            return defaultTransformer;
        }

        return transformer;
    }
}

class UnsupportedFunctionTransformer implements FunctionTransformer {
    @Override
    public SQLFragment transform(ASTNode node) {
        throw new UnsupportedOperationException(
            "Function '" + node.getFunctionName() + "' is not yet supported. " +
            "Supported functions: " + String.join(", ", getSupportedFunctions())
        );
    }
}
```

## 📋 Implementation Checklist

### For Each Phase/Function Implementation

- [ ] **Parser Rules**
  - [ ] Grammar rule defined
  - [ ] Error recovery rules added
  - [ ] Ambiguity resolved
  - [ ] Test cases for valid syntax
  - [ ] Test cases for invalid syntax

- [ ] **SQL Generation**
  - [ ] Parameterized queries used
  - [ ] SQL injection tests written
  - [ ] Query plan analyzed
  - [ ] Indexes utilized properly
  - [ ] Edge cases handled (empty data, single point, nulls)

- [ ] **Error Handling**
  - [ ] Input validation implemented
  - [ ] Meaningful error messages
  - [ ] Error recovery strategy defined
  - [ ] Resource cleanup in finally blocks
  - [ ] Timeout handling

- [ ] **Testing**
  - [ ] Unit tests (>90% coverage)
  - [ ] Integration tests
  - [ ] Performance tests
  - [ ] Edge case tests
  - [ ] Error condition tests
  - [ ] Compatibility tests

- [ ] **Documentation**
  - [ ] Function documented in reference
  - [ ] Examples provided
  - [ ] Limitations documented
  - [ ] Performance characteristics noted

- [ ] **Monitoring**
  - [ ] Metrics collected
  - [ ] Slow query logging
  - [ ] Error tracking
  - [ ] Usage patterns tracked

## 🚨 Common Pitfalls to Avoid

### 1. Parser Pitfalls
- **Left recursion** in grammar rules → Use right recursion or precedence climbing
- **Ambiguous grammar** → Add explicit precedence rules
- **Poor error messages** → Implement custom error listener

### 2. SQL Generation Pitfalls
- **String concatenation** of user input → Always use parameters
- **Missing indexes** → Profile queries, add appropriate indexes
- **N+1 query problem** → Use joins and batch operations

### 3. Performance Pitfalls
- **Loading entire result set** → Implement pagination/streaming
- **No query timeout** → Set reasonable timeouts
- **No connection pooling** → Reuse connections properly

### 4. Testing Pitfalls
- **Testing only happy path** → Test error conditions thoroughly
- **No performance tests** → Add timeout tests and load tests
- **Missing edge cases** → Test empty data, single points, boundaries

### 5. Maintenance Pitfalls
- **Hard-coded assumptions** → Use configuration and constants
- **No version checking** → Implement schema compatibility checks
- **Missing feature flags** → Allow disabling experimental features

## 📊 Quality Metrics Targets

### Code Quality
- **Test Coverage**: >90% for core functionality
- **Cyclomatic Complexity**: <10 per method
- **Code Duplication**: <5%
- **Documentation Coverage**: 100% for public APIs

### Performance Targets
- **Simple queries**: <100ms
- **Complex aggregations**: <1s
- **Large dataset queries**: <5s
- **Memory usage**: <100MB for typical queries

### Reliability Targets
- **Error rate**: <0.1%
- **Query timeout rate**: <0.01%
- **Recovery from errors**: 100% (no crashes)

## 🔄 Continuous Improvement Process

### 1. Telemetry Collection
```java
public class UsageTracker {
    public void trackFunctionUsage() {
        // Collect which functions are most used
        // Identify unsupported functions users are trying
        // Track performance patterns
    }
}
```

### 2. Performance Profiling
- Regular profiling of slow queries
- Query plan analysis for new functions
- Memory usage monitoring

### 3. User Feedback Loop
- Log unsupported function attempts
- Track error messages users encounter
- Monitor which features are most/least used

### 4. Incremental Optimization
- Start with correct implementation
- Profile to identify bottlenecks
- Optimize only measured problems
- Maintain regression tests

This robustness guide ensures that each component is built with reliability, performance, and maintainability in mind from the start.
