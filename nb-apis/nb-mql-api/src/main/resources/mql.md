# MetricsQL Query Engine

MetricsQL is a query engine for analyzing NoSQLBench SQLite metrics databases. It provides a MetricsQL/PromQL-inspired interface for querying time-series metrics data.

## Quick Start

```bash
# Show session information
nb5 mql session

# List available metrics
nb5 mql summary

# Get current metric values
nb5 mql instant --metric api_requests_total

# Query time-series data
nb5 mql range --metric ops_total --window 5m

# Calculate request rate
nb5 mql rate --metric requests_total --window 5m
```

## Available Commands

### Discovery Commands
- **session** - Display session timeline and snapshot information
- **summary** - List all metrics with types and sample counts
- **sql** - Execute raw SQLite queries for advanced analysis

### Query Commands
- **instant** - Get the most recent value for a metric
- **range** - Retrieve time-series data over a time window
- **topk** - Find top N label sets by metric value

### Calculation Commands
- **rate** - Calculate per-second rate of change for counters
- **increase** - Calculate total increase in counter over time window
- **quantile** - Extract percentile values from timer/summary metrics
- **agg** - Perform aggregations (sum, avg, max, min, count) with grouping
- **ratio** - Calculate ratio between two metrics (e.g., error rate)

## Output Formats

All commands support multiple output formats via `--format`:

- **table** (default) - ASCII tables for console
- **json** - Pretty-printed JSON for programmatic use
- **jsonl** - JSON Lines for streaming
- **csv** - Comma-separated values for spreadsheets
- **tsv** - Tab-separated values for Unix tools
- **markdown** - GitHub-flavored markdown tables

## Examples

See detailed examples organized by topic:
- [Examples Overview](mql-examples.md) - Navigation and all examples
- [Instant Queries](mql-instant-queries.md) - Point-in-time queries
- [Range Queries](mql-range-queries.md) - Time-series queries
- More examples coming for rate, aggregation, and quantile queries

## Database Location

By default, commands query `logs/metrics.db` which is a symlink to the current session's metrics database.

To query a specific session:
```bash
nb5 mql instant --db logs/session_20251023_100000_metrics.db --metric ops_total
```

## Common Patterns

### Monitoring
```bash
# Current error count
nb5 mql instant --metric api_requests_total --labels status=500

# Request rate last 5 minutes
nb5 mql rate --metric api_requests_total --window 5m --labels status=200

# p95 latency
nb5 mql quantile --metric api_latency --p 0.95
```

### Analysis
```bash
# Total requests by endpoint
nb5 mql agg --metric api_requests_total --func sum --by endpoint

# Top 10 busiest endpoints
nb5 mql topk --metric api_requests_total --n 10

# Error rate
nb5 mql ratio --numerator errors_total --denominator requests_total
```

### Exporting Data
```bash
# CSV for Excel
nb5 mql range --metric ops_total --window 1h --format csv > metrics.csv

# JSON for analysis
nb5 mql summary --format json > metrics_inventory.json

# Streaming JSONL
nb5 mql range --metric ops_total --window 1h --format jsonl | jq '.value'
```

## Advanced Usage

### SQL Queries
```bash
# Custom aggregation
nb5 mql sql --query "
  SELECT sn.sample, AVG(sv.value) as avg_value
  FROM sample_value sv
  JOIN sample_name sn ON sv.sample_name_id = sn.id
  GROUP BY sn.sample
"

# Schema inspection
nb5 mql sql --query "PRAGMA table_info(sample_value)"
```

### Debug Mode
```bash
# Show generated SQL
nb5 mql instant --metric ops_total --explain
```

## Learn More

Run `nb5 help mql` to see this documentation.

For detailed examples, see the [examples README](docs/mql/README.md).
