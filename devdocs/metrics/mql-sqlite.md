---
title: "MetricsQL to SQLite Implementation Plan"
description: "Execution plan for delivering MetricsQL support atop the SQLite metrics store."
audience: developer
diataxis: explanation
tags:
  - metricsql
  - planning
component: core
topic: metrics
status: live
owner: "@nosqlbench/metrics"
generated: false
---

# MetricsQL to SQLite Implementation Plan

## Executive Summary

This document outlines a systematic approach for implementing VictoriaMetrics MetricsQL support using SQLite as the backend database. The plan focuses on creating reusable, DRY (Don't Repeat Yourself) implementations through careful categorization of MetricsQL features and a modular parser/transformer architecture.

**📋 Related Documents:**
- `mql-feature-categories.md` - Detailed feature categorization and SQL mappings
- `mql-reusable-patterns.md` - Existing patterns to leverage from current codebase
- `mql-implementation-robustness.md` - Reliability, testing, and performance guidelines

## 1. MetricsQL Feature Categorization

Based on analysis of MetricsQL documentation and implementation patterns, features are grouped into the following categories for implementation efficiency:

### 1.1 Time-Series Selection Functions
**Characteristics**: Functions that select and filter time series data
**Implementation Pattern**: SQL WHERE clauses with label and metric filtering

- `metric{label="value"}` → Basic metric selector
- `metric{label=~"regex"}` → Regex label matching
- `metric{label!="value"}` → Negative label matching
- `metric{label!~"regex"}` → Negative regex matching

**SQL Mapping Strategy**:
```sql
-- Basic selector: metric{job="api", status="200"}
SELECT ... FROM sample_value sv
JOIN ... ON ...
WHERE sn.sample = 'metric'
  AND label_filters...
```

### 1.2 Rollup/Range Functions
**Characteristics**: Functions that aggregate data over time windows
**Implementation Pattern**: Window functions with GROUP BY time buckets

Core functions:
- `rate(m[5m])` → Per-second rate calculation
- `increase(m[5m])` → Total increase over window
- `avg_over_time(m[5m])` → Average over window
- `sum_over_time(m[5m])` → Sum over window
- `min_over_time(m[5m])` → Minimum over window
- `max_over_time(m[5m])` → Maximum over window
- `count_over_time(m[5m])` → Count of samples
- `stddev_over_time(m[5m])` → Standard deviation
- `quantile_over_time(0.95, m[5m])` → Quantile calculation

**SQL Mapping Strategy**:
```sql
-- rate(metric[5m])
WITH window_data AS (
  SELECT timestamp_ms, value,
         LAG(value) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_value,
         LAG(timestamp_ms) OVER (...) AS prev_timestamp
  FROM ...
  WHERE timestamp_ms >= (current_time - interval)
)
SELECT (value - prev_value) / ((timestamp_ms - prev_timestamp) / 1000.0) AS rate_per_sec
```

### 1.3 Aggregation Functions
**Characteristics**: Functions that aggregate across multiple time series at a single point in time
**Implementation Pattern**: GROUP BY with aggregate SQL functions

Core functions:
- `sum(m) by (label)` → Sum grouped by label
- `avg(m) by (label)` → Average grouped by label
- `min(m) by (label)` → Minimum grouped by label
- `max(m) by (label)` → Maximum grouped by label
- `count(m) by (label)` → Count grouped by label
- `stddev(m) by (label)` → Standard deviation
- `quantile(0.95, m) by (label)` → Quantile grouped

**SQL Mapping Strategy**:
```sql
-- sum(metric) by (job)
SELECT job_label, SUM(value) as sum_value
FROM ...
GROUP BY job_label
```

### 1.4 Transform Functions
**Characteristics**: Functions that transform values without changing cardinality
**Implementation Pattern**: SQL expressions in SELECT clause

Core functions:
- `abs(m)` → Absolute value
- `ceil(m)` → Ceiling
- `floor(m)` → Floor
- `round(m)` → Round
- `ln(m)` → Natural logarithm
- `log2(m)` → Log base 2
- `log10(m)` → Log base 10
- `sqrt(m)` → Square root
- `exp(m)` → Exponential

**SQL Mapping Strategy**:
```sql
-- abs(metric)
SELECT ABS(value) AS transformed_value
FROM ...
```

### 1.5 Label Manipulation Functions
**Characteristics**: Functions that modify label sets
**Implementation Pattern**: String manipulation in SELECT/GROUP BY

Core functions:
- `label_set(m, "label", "value")` → Add/modify label
- `label_del(m, "label1", ...)` → Remove labels
- `label_keep(m, "label1", ...)` → Keep only specified labels
- `label_copy(m, "src", "dst")` → Copy label value
- `label_move(m, "src", "dst")` → Move label
- `label_transform(m, "label", "regex", "replacement")` → Transform label values

**SQL Mapping Strategy**:
```sql
-- Complex label manipulation requires CTEs
WITH labeled_data AS (
  SELECT ... label constructions ...
),
transformed AS (
  SELECT ... label transformations ...
)
SELECT ...
```

### 1.6 Binary Operators
**Characteristics**: Operations between two metrics or metric and scalar
**Implementation Pattern**: JOIN operations or scalar operations

Core operators:
- `+`, `-`, `*`, `/`, `%` → Arithmetic
- `==`, `!=`, `>`, `<`, `>=`, `<=` → Comparison
- `and`, `or`, `unless` → Set operations

**SQL Mapping Strategy**:
```sql
-- metric1 + metric2
SELECT m1.timestamp, m1.value + m2.value AS result
FROM (SELECT ... FROM ... WHERE metric='metric1') m1
JOIN (SELECT ... FROM ... WHERE metric='metric2') m2
ON m1.timestamp = m2.timestamp AND m1.label_set_id = m2.label_set_id
```

## 2. Implementation Architecture

### 2.1 Parser Design

Create a modular ANTLR4-based parser with the following components:

```
MetricsQLParser
├── MetricsQLLexer.g4        # Token definitions
├── MetricsQLParser.g4       # Grammar rules
└── MetricsQLVisitor.java    # AST visitor for SQL generation
```

Grammar structure outline:
```antlr
grammar MetricsQL;

query: expression EOF;

expression
    : functionCall
    | selector
    | binaryOp
    | aggregation
    ;

selector: IDENTIFIER '{' labelMatchers? '}' ('[' duration ']')?;

functionCall: IDENTIFIER '(' arguments ')';

aggregation: AGG_FUNCTION '(' expression ')' ('by' '(' labelList ')')?;
```

**🛡️ Robustness Notes:**
- Implement custom error listener for meaningful parse errors
- Add error recovery rules to handle malformed input gracefully
- Use explicit precedence rules to avoid ambiguity
- Include line/column tracking for error reporting
- Test with malformed input early and often

### 2.2 SQL Transformer Architecture

Create a transformer hierarchy that converts AST nodes to SQL:

```java
interface SQLTransformer {
    SQLFragment transform(ASTNode node, TransformContext context);
}

class SelectorTransformer implements SQLTransformer { }
class RollupTransformer implements SQLTransformer { }
class AggregationTransformer implements SQLTransformer { }
class BinaryOpTransformer implements SQLTransformer { }
```

**🛡️ Robustness Notes:**
- **CRITICAL**: Always use parameterized queries - never concatenate user input
- Validate all label names against regex pattern `^[a-zA-Z_][a-zA-Z0-9_]*$`
- Implement query timeout (30s default) to prevent runaway queries
- Add result size limits to prevent OOM (10K rows default)
- Use read-only connections with `PRAGMA query_only = ON`

### 2.3 Query Builder Pattern

Implement a fluent SQL builder for clean query construction:

```java
class MetricsSQLBuilder {
    public MetricsSQLBuilder withTimeRange(long start, long end);
    public MetricsSQLBuilder withMetric(String metric);
    public MetricsSQLBuilder withLabelFilters(Map<String, String> filters);
    public MetricsSQLBuilder withAggregation(AggregationType type, String... groupBy);
    public MetricsSQLBuilder withRollup(RollupType type, Duration window);
    public String build();
}
```

## 2A. Existing Libraries and Features

### Already Available in Project

The following libraries are already included in the project dependencies and should be leveraged:

#### Parser Infrastructure
- **ANTLR 4.13.x** - Already used in `nb-virtdata/virtdata-lang` module
  - `antlr4-runtime:4.13.1`
  - `antlr4-maven-plugin:4.13.2`
  - No need to add ANTLR dependencies, use existing configuration pattern

#### Data Processing
- **Apache Commons**
  - `commons-lang3:3.18.0` - String manipulation, utilities
  - `commons-text:1.13.1` - Advanced string processing
  - `commons-csv` - Already used in MQL module for CSV output
  - `commons-math4-*` - Statistical functions available for quantile calculations

#### JSON/Serialization
- **Gson 2.13.1** - Already used in MQL module for JSON output
- **Jackson 2.18.3** - Available via jackson-jaxrs-json-provider

#### Database
- **SQLite JDBC 3.46.0.0** - Already configured in MQL module
- **JDBC Connection pooling** - Check if HikariCP or similar is available

#### CLI Framework
- **Picocli 4.7.6** - Already used extensively for CLI parsing

#### Logging
- **SLF4J 2.0.16** - API already configured
- **Log4j2 2.25.2** - Implementation already configured

#### Testing
- **JUnit Jupiter 5.9.3** - Test framework
- **TestContainers 1.20.6** - Available for integration testing

### Utilities to Leverage

#### From Existing MQL Module
- `TimeWindowParser.java` - Parse time durations ("5m", "1h", etc.)
- `LabelSetResolver.java` - Handle label set operations
- `MetricsDatabaseReader.java` - Database access patterns
- `ResultFormatter.java` hierarchy - Output formatting infrastructure
- `MetricsSchema.java` - Schema constants and SQL fragments

#### From nb-api Module
- Metrics interfaces and implementations
- Label handling utilities
- Existing metric types and patterns

### Libraries NOT to Import (Already Available)

Do not add these to pom.xml as they're already managed:
- ✗ ANTLR (use existing 4.13.x)
- ✗ Gson (use existing 2.13.1)
- ✗ Apache Commons Lang/Text/CSV/Math (use existing versions)
- ✗ Picocli (use existing 4.7.6)
- ✗ SQLite JDBC (use existing 3.46.0.0)
- ✗ SLF4J/Log4j2 (use existing logging setup)
- ✗ JUnit Jupiter (use existing 5.9.3)

### New Dependencies Potentially Needed

These may need to be added if not available:
- **None identified** - All required functionality appears to be available through existing dependencies

### Maven Configuration Pattern

Follow the existing pattern from virtdata-lang for ANTLR:

```xml
<plugin>
    <groupId>org.antlr</groupId>
    <artifactId>antlr4-maven-plugin</artifactId>
    <version>4.13.2</version>
    <configuration>
        <sourceDirectory>${basedir}/src/main/antlr4</sourceDirectory>
        <visitor>true</visitor>
    </configuration>
    <executions>
        <execution>
            <id>antlr</id>
            <goals><goal>antlr4</goal></goals>
            <phase>generate-sources</phase>
        </execution>
    </executions>
</plugin>
```

## 3. Implementation Phases

### Phase 1: Core Infrastructure (Week 1)
1. ~~Set up ANTLR4 parser generator~~ Use existing ANTLR 4.13.x configuration
2. Create basic lexer and parser grammar in `src/main/antlr4/`
3. Implement AST visitor framework using ANTLR visitor pattern
4. Extend existing SQL builder patterns from current commands
5. Leverage existing test infrastructure and TestDataGenerator

**Deliverables**:
- `src/main/antlr4/io/nosqlbench/nb/mql/parser/MetricsQLLexer.g4`
- `src/main/antlr4/io/nosqlbench/nb/mql/parser/MetricsQLParser.g4`
- `MetricsQLBaseVisitor.java` (auto-generated)
- `MetricsSQLBuilder.java` (extend existing patterns)
- Test framework using existing `TestDatabaseGenerator.java`

### Phase 2: Basic Selectors (Week 1-2)
1. Implement metric selector parsing
2. Add label matcher support (exact, regex, negative)
3. Create SelectorTransformer
4. Implement time range filtering

**Commands to implement**:
- Basic selector: `metric{label="value"}`
- Regex selector: `metric{label=~"pattern"}`
- Multiple labels: `metric{label1="value1", label2="value2"}`

**Test Cases**:
```java
@Test testBasicSelector()
@Test testRegexSelector()
@Test testMultiLabelSelector()
@Test testNegativeSelector()
```

### Phase 3: Rollup Functions (Week 2-3)
1. Parse time window syntax `[5m]`, `[1h]`, etc.
2. Implement core rollup functions one by one
3. Create RollupTransformer with window function SQL

**Priority Order**:
1. `rate()` - Most commonly used
2. `avg_over_time()`
3. `sum_over_time()`
4. `increase()`
5. `min_over_time()`, `max_over_time()`
6. `count_over_time()`
7. `quantile_over_time()`
8. `stddev_over_time()`

**Test Database Requirements**:
- Counter metrics (monotonic increase)
- Gauge metrics (fluctuating values)
- Multiple label sets per metric
- Sufficient time range (at least 24h of data)

### Phase 4: Aggregation Functions (Week 3-4)
1. Parse aggregation syntax with `by` and `without` modifiers
2. Implement aggregation transformers
3. Handle label grouping logic

**Priority Order**:
1. `sum() by (label)`
2. `avg() by (label)`
3. `max() by (label)`, `min() by (label)`
4. `count() by (label)`
5. `quantile() by (label)`
6. `stddev() by (label)`

### Phase 5: Transform Functions (Week 4)
1. Implement mathematical transform functions
2. Create TransformTransformer
3. Handle function composition

**Functions**: `abs()`, `ceil()`, `floor()`, `round()`, `ln()`, `log2()`, `log10()`, `sqrt()`, `exp()`

### Phase 6: Label Manipulation (Week 5)
1. Implement label manipulation functions
2. Create complex label transformation logic
3. Handle label set modifications

### Phase 7: Binary Operations (Week 5-6)
1. Parse binary operator syntax
2. Implement metric-to-metric operations
3. Handle scalar operations
4. Implement set operations (and, or, unless)

### Phase 8: Advanced Features (Week 6+)
1. Subqueries
2. Function composition
3. Complex aggregations
4. Optimization passes

## 4. Testing Strategy

### 4.1 Test Database Generation

Create reusable test data generators:

```java
class MetricsTestDataGenerator {
    // Generate counter metrics
    public void generateCounter(String name, Map<String, String> labels,
                                int points, Duration interval);

    // Generate gauge metrics
    public void generateGauge(String name, Map<String, String> labels,
                              int points, double min, double max);

    // Generate histogram data
    public void generateHistogram(String name, Map<String, String> labels,
                                  int points, int buckets);
}
```

**🛡️ Robustness Notes:**
- Include edge cases: empty data, single point, counter resets
- Test with malicious input (SQL injection attempts)
- Generate datasets with >1M points for performance testing
- Create data with NULL values and missing labels
- Test timezone handling and timestamp boundaries

### 4.2 Test Categories

1. **Parser Tests**: Validate AST generation
   - Valid expressions (positive tests)
   - Malformed input (negative tests with expected errors)
   - Ambiguous cases (precedence verification)

2. **Transformer Tests**: Verify SQL generation
   - SQL injection prevention tests
   - Parameter binding verification
   - Query plan analysis (no full table scans)

3. **Integration Tests**: End-to-end query execution
   - Timeout enforcement (<5s for complex queries)
   - Memory limits (result size capping)
   - Connection pool behavior under load

4. **Performance Tests**: Query optimization validation
   - Benchmark against direct SQL equivalents (target: <2x overhead)
   - Profile memory usage (target: <100MB for typical queries)

5. **Compatibility Tests**: PromQL compatibility verification

### 4.3 Test File Organization

```
test/
├── parser/
│   ├── SelectorParserTest.java
│   ├── FunctionParserTest.java
│   └── OperatorParserTest.java
├── transformer/
│   ├── SelectorTransformerTest.java
│   ├── RollupTransformerTest.java
│   └── AggregationTransformerTest.java
├── integration/
│   ├── BasicQueriesTest.java
│   ├── ComplexQueriesTest.java
│   └── PerformanceTest.java
└── data/
    ├── TestDataGenerator.java
    └── test-metrics.db (ephemeral)
```

## 5. SQL Optimization Strategies

### 5.1 Index Usage
Ensure queries utilize existing indexes:
- `metric_instance(sample_name_id, label_set_id)`
- `sample_value(metric_instance_id, timestamp_ms)`

### 5.2 CTE (Common Table Expression) Usage
Use CTEs for complex queries to improve readability and potentially performance:

```sql
WITH
  filtered_metrics AS (...),
  windowed_data AS (...),
  aggregated AS (...)
SELECT ... FROM aggregated
```

### 5.3 Query Plan Analysis
Implement query plan analysis for optimization:

```java
class QueryOptimizer {
    public String analyzeQueryPlan(String sql);
    public String optimizeQuery(String sql);
}
```

## 6. Implementation Utilities

### 6.1 Time Window Parser
Already exists: `TimeWindowParser.java`
- Handles duration parsing: "5m", "1h", "30s", etc.

### 6.2 Label Set Resolver
Already exists: `LabelSetResolver.java`
- Handles label set matching and filtering

### 6.3 New Utilities Needed

```java
// Pattern matching for label values
class LabelPatternMatcher {
    boolean matches(String pattern, String value, MatchType type);
}

// SQL fragment builder for reuse
class SQLFragmentBuilder {
    String buildLabelFilter(Map<String, LabelMatcher> matchers);
    String buildTimeRangeFilter(long start, long end);
    String buildAggregation(AggregationType type, List<String> groupBy);
}

// Result set transformer
class ResultTransformer {
    QueryResult transformToMetricsQL(ResultSet rs, QueryMetadata metadata);
}
```

## 7. Command Line Interface Extension

Extend existing CLI to support MetricsQL syntax:

```bash
# Direct MetricsQL query
nb5 mql query 'rate(http_requests_total[5m])'

# With output format
nb5 mql query 'sum(rate(http_requests_total[5m])) by (status)' --format json

# With time range
nb5 mql query 'avg_over_time(cpu_usage[1h])' --start 2024-01-01T00:00:00Z --end 2024-01-02T00:00:00Z
```

## 8. Error Handling and Validation

### 8.1 Parser Errors
- Syntax errors with line/column information
- Helpful error messages with suggestions

### 8.2 Semantic Validation
- Type checking (counter vs gauge functions)
- Label existence validation
- Time range validation

### 8.3 Runtime Errors
- Missing metrics handling
- Empty result set handling
- Query timeout management

## 9. Documentation Requirements

### 9.1 User Documentation
- MetricsQL syntax guide
- Function reference
- Migration guide from existing commands

### 9.2 Developer Documentation
- Parser architecture
- Transformer patterns
- Extension guide for new functions

### 9.3 Test Documentation
- Test data schema
- Test scenario descriptions
- Performance benchmarks

## 10. Success Criteria

1. **Functionality**: Support 80% of common MetricsQL queries
2. **Performance**: Queries complete within 2x of direct SQL equivalents
3. **Compatibility**: Pass PromQL compatibility test suite
4. **Maintainability**: Clear separation of concerns, DRY principles
5. **Testability**: >90% code coverage with automated tests
6. **Documentation**: Complete user and developer guides

## 11. Risk Mitigation

### 11.1 Technical Risks
- **Complex grammar**: Start with subset, iterate
- **Performance issues**: Profile early, optimize continuously
- **SQLite limitations**: Document unsupported features

### 11.2 Mitigation Strategies
- Incremental implementation with working features at each phase
- Continuous integration testing
- Performance regression tests
- Clear documentation of limitations

## 12. Next Steps

1. Review and approve this plan
2. Set up development environment with ANTLR4
3. Create initial parser grammar
4. Implement Phase 1 core infrastructure
5. Begin iterative implementation of function categories

## 13. Critical Implementation Guidelines

### 🔴 Non-Negotiable Safety Requirements

1. **SQL Injection Prevention**
   - ✅ ALWAYS use parameterized queries with `?` placeholders
   - ❌ NEVER concatenate user input into SQL strings
   - ✅ Validate all label names with regex before use
   - ✅ Use `PRAGMA query_only = ON` for all connections

2. **Resource Protection**
   - ✅ Set query timeout to 30 seconds maximum
   - ✅ Limit result sets to 10,000 rows by default
   - ✅ Use connection pooling (max 10 connections)
   - ✅ Monitor memory usage and fail fast on OOM risk

3. **Error Handling**
   - ✅ Provide meaningful error messages with context
   - ✅ Include suggestions for common syntax errors
   - ✅ Log all errors with sufficient detail for debugging
   - ✅ Never expose internal SQL errors to users

### 🟡 Performance Requirements

1. **Query Performance Targets**
   - Simple selectors: <100ms
   - Rollup functions: <500ms
   - Complex aggregations: <1s
   - Large datasets (1M points): <5s

2. **Optimization Checklist**
   - ✅ Verify index usage with `EXPLAIN QUERY PLAN`
   - ✅ Use CTEs for complex queries
   - ✅ Implement query result caching (1min TTL)
   - ✅ Profile before optimizing

### 🟢 Quality Assurance

1. **Test Coverage Requirements**
   - Core functions: >90% coverage
   - Error paths: 100% coverage
   - SQL injection tests: Mandatory for each transformer
   - Performance regression tests: Run on CI

2. **Documentation Requirements**
   - Every public function must have Javadoc
   - Include usage examples for each MetricsQL function
   - Document limitations and unsupported features
   - Maintain compatibility matrix with PromQL

### 📊 Monitoring and Observability

1. **Metrics to Track**
   - Query execution time (p50, p95, p99)
   - Error rates by type
   - Function usage frequency
   - Result set sizes
   - Cache hit rates

2. **Logging Strategy**
   - DEBUG: MetricsQL → SQL transformation
   - INFO: Slow queries (>1s)
   - WARN: Unsupported functions, deprecated usage
   - ERROR: Parse failures, SQL errors, timeouts

This plan provides a structured, DRY approach to implementing MetricsQL support while maintaining compatibility with the existing SQLite schema and command structure, with strong emphasis on reliability, security, and performance.
