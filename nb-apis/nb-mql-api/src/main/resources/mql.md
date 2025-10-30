# MetricsQL Query Engine

MetricsQL is a query engine for analyzing NoSQLBench SQLite metrics databases. It provides a MetricsQL/PromQL-inspired interface for querying time-series metrics data.

## Quick Start

```bash
# Show session information
nb5 mql session

# List available metrics with tree view
nb5 mql summary

# Explore metrics with their label sets
nb5 mql metrics

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
- **metrics** - List all metrics with their label sets in tree view
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

## Tree Display Features

The `summary` and `metrics` commands use intelligent tree display to organize metrics hierarchically:

### Automatic Condensation (Default)
By default, metrics with common label patterns are condensed into a tree structure:

```bash
# Shows condensed tree view
nb5 mql metrics

# Example output:
activity=read, env=prod
├── host=server1
│   ├── region=us-east
│   └── region=us-west
└── host=server2
    └── region=us-east
```

### Tree Display Options

- **`--no-condense`** - Disable condensation, show each metric instance separately with full label sets
  ```bash
  nb5 mql summary --no-condense
  ```

- **`--group-by=<mode>`** - Control grouping strategy (metrics command only)
  - `name` (default) - Group by metric name
  - `labelset` - Group by label set structure
  ```bash
  nb5 mql metrics --group-by labelset
  ```

- **`--keep-labels=<labels>`** - Preserve specific labels from elision (default: activity,session)
  ```bash
  # Keep all labels visible
  nb5 mql summary --keep-labels '*'

  # Keep specific labels
  nb5 mql summary --keep-labels activity,session,region
  ```

### How Tree Condensation Works

The tree display uses a two-phase approach:

1. **Canonical Layer (LabelSetTree)**: Maintains strict label set containment relationships
2. **Display Layer (DisplayTree)**: Applies three condensation algorithms:
   - **Differential labels**: Show only labels differing from parent
   - **Sibling condensation**: Merge siblings differing by single label (comma-separated values)
   - **Identical consolidation**: Combine nodes with same label sets

This makes large metric sets easier to navigate while preserving the full relational structure.

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

### Discovery & Navigation
```bash
# Quick overview of all metrics
nb5 mql summary

# Explore label structure grouped by name
nb5 mql metrics --group-by name

# See all label combinations (no condensation)
nb5 mql metrics --no-condense

# Group by label set structure instead of metric name
nb5 mql metrics --group-by labelset
```

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
  JOIN metric_instance mi ON sv.metric_instance_id = mi.id
  JOIN sample_name sn ON mi.sample_name_id = sn.id
  GROUP BY sn.sample
"

# Schema inspection
nb5 mql sql --query "PRAGMA table_info(metric_instance)"
```

### Query Debugging

The `--explain` flag shows the generated SQL query instead of executing it. Available on all query and calculation commands:

```bash
# See SQL for instant query
nb5 mql instant --metric ops_total --explain

# See SQL for rate calculation
nb5 mql rate --metric requests_total --window 5m --explain

# See SQL for aggregation with grouping
nb5 mql agg --metric api_requests_total --func sum --by endpoint --explain
```

This is useful for:
- Understanding how MetricsQL translates to SQL
- Debugging unexpected query results
- Learning the schema structure
- Building custom queries with the `sql` command

## Learn More

Run `nb5 help mql` to see this documentation.

For detailed examples, see the [examples README](docs/mql/README.md).
