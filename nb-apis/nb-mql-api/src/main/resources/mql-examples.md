# MetricsQL Query Examples

This directory contains executable examples demonstrating all query commands in the MetricsQL query engine. Each example is validated by automated tests to ensure the documentation stays accurate.

## Example Files by Theme

### Basic Queries
- **[INSTANT_QUERIES.md](INSTANT_QUERIES.md)** - Get current/latest values
  - Basic instant queries
  - Label filtering
  - Error metrics
  - Multi-dimensional filtering

- **[RANGE_QUERIES.md](RANGE_QUERIES.md)** - Time-series data retrieval
  - Time-series over windows
  - Historical data analysis
  - Error tracking over time

### Analysis Queries (Coming Soon)
- **RATE_QUERIES.md** - Calculate per-second rates
  - Counter rate calculations
  - Change detection
  - Throughput analysis

- **AGGREGATION_QUERIES.md** - Sum, average, min, max operations
  - Grouping by labels
  - Cross-dimensional aggregations
  - Statistical summaries

- **QUANTILE_QUERIES.md** - Percentile analysis
  - Latency percentiles (p50, p95, p99)
  - Timer statistics
  - Performance analysis

### Advanced Queries (Future)
- **RATIO_QUERIES.md** - Calculate error rates and ratios
- **TOPK_QUERIES.md** - Find top N values
- **BINARY_OPERATIONS.md** - Arithmetic between metrics

## Test Database: examples.db

All examples use `examples.db`, which contains metrics from a simplified e-commerce API:

**Metrics:**
- `api_requests_total` - Request counters by method, endpoint, and status
- `api_latency_ms` - API latency timers with percentiles

**Timeline:**
- 5 snapshots spanning 10 minutes (2025-10-23 10:00:00Z to 10:10:00Z)
- Simulates normal traffic → increased load → high load → recovery

**Label Dimensions:**
- `method`: {GET, POST}
- `endpoint`: {/users, /products}
- `status`: {200, 404, 500}

See [TEST_DATABASES.md](../TEST_DATABASES.md) for detailed schema information.

## How Examples Work

Each example follows this format:

```markdown
## Example N: Description

**Command:**
```
command --param value --other param
```

**Expected Output:**
```
[exact formatted table output]
```

**What it shows:** Explanation of the use case
```

The `ExampleDocumentationTest` class automatically:
1. Parses all example markdown files
2. Extracts commands and expected results
3. Executes each command against examples.db
4. Validates the actual output matches expectations

**This means documentation is always accurate and tested!**

## Running Examples Manually

Once the CLI is implemented, you can run these examples:

```bash
# Point to the examples database
export EXAMPLES_DB=src/test/resources/testdata/examples.db

# Run any example command
nb-metricsql instant --db $EXAMPLES_DB --metric api_requests_total

# With different output formats
nb-metricsql instant --db $EXAMPLES_DB --metric api_requests_total --format json
nb-metricsql instant --db $EXAMPLES_DB --metric api_requests_total --format csv
```

## Regenerating examples.db

If you modify the schema or need to update the example data:

```bash
mvn exec:java -pl nb-apis/nb-mql-api \
  -Dexec.mainClass="io.nosqlbench.nb.mql.testdata.ExampleDataGenerator" \
  -Dexec.classpathScope=test
```

## Contributing New Examples

When adding a new query command:

1. Add examples to the appropriate themed markdown file
2. Follow the established format with command, expected output, and explanation
3. Run the tests to verify: `mvn test -Dtest=ExampleDocumentationTest`
4. The tests will automatically validate your examples

---

*These examples are automatically tested and validated with every build.*
