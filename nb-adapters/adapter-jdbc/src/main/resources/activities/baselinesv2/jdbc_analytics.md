# JDBC Analytics Workload

## Overview

The `jdbc_analytics.yaml` workload implements TPC-H inspired analytical queries for testing complex data warehouse scenarios. This workload is designed for testing analytical query performance, multi-table joins, and aggregation operations on large datasets.

## Scenario Description

This workload simulates a data warehouse environment with TPC-H inspired schema:
- **Business Intelligence**: Complex analytical queries across multiple dimensions
- **Data Warehouse**: Star schema with fact and dimension tables
- **Reporting**: Aggregations, time-series analysis, and cross-table analytics

## Schema Design

### Tables Created

1. **region** (3 columns) - Geographic regions
   - `r_regionkey`, `r_name`, `r_comment`

2. **nation** (4 columns) - Countries within regions  
   - `n_nationkey`, `n_name`, `n_regionkey`, `n_comment`

3. **customer** (8 columns) - Customer master data
   - `c_custkey`, `c_name`, `c_address`, `c_nationkey`, `c_phone`, `c_acctbal`, `c_mktsegment`, `c_comment`

4. **supplier** (7 columns) - Supplier information
   - `s_suppkey`, `s_name`, `s_address`, `s_nationkey`, `s_phone`, `s_acctbal`, `s_comment`

5. **part** (9 columns) - Product parts catalog
   - `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment`

6. **orders** (9 columns) - Order headers
   - `o_orderkey`, `o_custkey`, `o_orderstatus`, `o_totalprice`, `o_orderdate`, `o_orderpriority`, `o_clerk`, `o_shippriority`, `o_comment`

7. **lineitem** (16 columns) - Order line items (fact table)
   - `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment`

## Operations and Phases

### Rampup Phase
**Purpose**: Load hierarchical data following TPC-H patterns

**Operations**:
- `insert_region` (ratio: 1) - Load geographic regions
- `insert_nation` (ratio: 5) - Load countries
- `insert_customer` (ratio: 30) - Load customer master data
- `insert_supplier` (ratio: 2) - Load supplier data
- `insert_part` (ratio: 40) - Load parts catalog
- `insert_orders` (ratio: 300) - Load order headers
- `insert_lineitem` (ratio: 1200) - Load order line items (largest table)

**Stride Calculation**: 1 + 5 + 30 + 2 + 40 + 300 + 1200 = **1578 operations per stride**

**Data Hierarchy**:
- Follows TPC-H scaling: 1 region → 5 nations → 30 customers → 300 orders → 1200 line items
- Creates realistic data warehouse proportions

### Analytics Phase
**Purpose**: Execute complex analytical queries

**Operations**:
- `query_pricing_summary` (ratio: 10) - TPC-H Query 1: Pricing Summary Report
- `query_customer_analysis` (ratio: 8) - Customer segmentation analysis
- `query_part_analysis` (ratio: 6) - Part performance analytics
- `query_order_trends` (ratio: 5) - Time-based order trends
- `query_customer_orders` (ratio: 4) - Customer-specific order analysis

**Stride Calculation**: 10 + 8 + 6 + 5 + 4 = **33 operations per stride**

## Key Analytical Queries

### TPC-H Query 1: Pricing Summary Report
```sql
SELECT
  l_returnflag,
  l_linestatus,
  sum(l_quantity) as sum_qty,
  sum(l_extendedprice) as sum_base_price,
  avg(l_quantity) as avg_qty,
  avg(l_extendedprice) as avg_price,
  avg(l_discount) as avg_disc,
  count(*) as count_order
FROM lineitem
WHERE l_shipdate <= date '1998-09-01'
GROUP BY l_returnflag, l_linestatus
ORDER BY l_returnflag, l_linestatus
```

### Customer Segmentation Analysis
```sql
SELECT
  c.c_mktsegment,
  count(*) as customer_count,
  avg(c.c_acctbal) as avg_balance
FROM customer c
GROUP BY c.c_mktsegment
ORDER BY customer_count desc
```

### Time-based Order Trends
```sql
SELECT
  extract(year from o.o_orderdate) as order_year,
  extract(month from o.o_orderdate) as order_month,
  count(*) as order_count,
  sum(o.o_totalprice) as total_revenue
FROM orders o
WHERE o.o_orderdate >= date '2020-01-01'
GROUP BY extract(year from o.o_orderdate), extract(month from o.o_orderdate)
ORDER BY order_year, order_month
```

## Binding Patterns

### Realistic Data Distributions
```yaml
# Customer data with realistic distributions
custkey: Uniform(1,150000)->long
customer_acctbal: Normal(5000,1000) -> double
customer_mktsegment: WeightedStrings('BUILDING:1;AUTOMOBILE:1;MACHINERY:1;HOUSEHOLD:1;FURNITURE:1')

# Order data with business patterns
order_totalprice: Normal(10000,5000) -> double
order_status: WeightedStrings('O:1;F:1;P:1')
order_priority: WeightedStrings('1-URGENT:1;2-HIGH:1;3-MEDIUM:1;4-NOT SPECIFIED:1;5-LOW:1')

# Lineitem with calculated fields
lineitem_quantity: Uniform(1,50) -> double
lineitem_extendedprice: Normal(1000,500) -> double
lineitem_discount: Uniform(0,10); Div(100) -> double
lineitem_tax: Uniform(0,8); Div(100) -> double
```

## Usage Examples

### Basic Analytics Execution
```bash
# Run complete analytics workload
nb5 jdbc_analytics.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true

# Individual phases
nb5 jdbc_analytics.yaml default.drop dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_analytics.yaml default.schema dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_analytics.yaml default.rampup dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_analytics.yaml default.analytics dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
```

### Large-Scale Analytics Testing
```bash
# High-volume data warehouse simulation
nb5 jdbc_analytics.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=8 connections=10 rampup_cycles=50000 analytics_cycles=5000

# Memory-intensive analytics
nb5 jdbc_analytics.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=auto connections=15 rampup_cycles=100000 analytics_cycles=10000
```

### Stride Optimization
```bash
# Optimal cycles (multiples of stride)
# Rampup stride = 1578, so use: 1578, 3156, 7890, 15780, etc.
nb5 jdbc_analytics.yaml default.rampup rampup_cycles=15780

# Analytics stride = 33, so use: 330, 660, 990, 3300, etc.
nb5 jdbc_analytics.yaml default.analytics analytics_cycles=3300
```

## Customization Parameters

### Template Variables
- `schemaname` (default: analytics) - Database schema name
- `connections` (default: 10) - Connection pool size
- `rampup_cycles` (default: 10000) - Number of data loading operations
- `analytics_cycles` (default: 1000) - Number of analytical queries

### Custom Configuration Example
```bash
nb5 jdbc_analytics.yaml default \
  dburl=jdbc:presto://host:8080/memory?user=testuser \
  use_hikaricp=true \
  schemaname=data_warehouse \
  connections=15 \
  rampup_cycles=31560 \
  analytics_cycles=3300 \
  threads=10
```

## Performance Characteristics

### Expected Throughput
- **Rampup Phase**: 500-2000 ops/sec (large inserts, complex schema)
- **Analytics Phase**: 50-500 ops/sec (complex aggregations and joins)

### Resource Usage
- **Memory**: High (large result sets, complex joins)
- **CPU**: High (aggregations, sorting, grouping)
- **I/O**: High (large table scans, multi-table joins)

### Scaling Behavior
- **Single Thread**: Baseline for complex query performance
- **2-4 Threads**: Good scaling for most analytical databases
- **8+ Threads**: May hit memory or CPU limits on complex queries

## Query Complexity Analysis

### Query Types
1. **Simple Aggregations**: COUNT, SUM, AVG operations
2. **Complex Aggregations**: Multi-column GROUP BY with ORDER BY
3. **Time-Series Analysis**: Date extraction and temporal grouping
4. **Multi-Table Joins**: Customer-order relationships
5. **TPC-H Patterns**: Industry-standard analytical query patterns

### Performance Considerations
- **Large Table Scans**: lineitem table is the largest (ratio: 1200)
- **Complex Joins**: Multiple foreign key relationships
- **Aggregation Memory**: GROUP BY operations on large datasets
- **Sort Operations**: ORDER BY clauses on aggregated results

## Troubleshooting

### Common Issues
1. **Memory Errors**: Reduce threads or increase database memory
2. **Long Query Times**: Complex analytics queries are expected to be slow
3. **Stride Warnings**: Use cycles that are multiples of 1578 (rampup) or 33 (analytics)
4. **Join Performance**: Ensure proper indexing on foreign keys

### Optimization Tips
```bash
# Start with smaller datasets
nb5 jdbc_analytics.yaml default.rampup rampup_cycles=1578

# Monitor query execution plans
nb5 jdbc_analytics.yaml default.analytics --log-level DEBUG

# Test individual query performance
nb5 jdbc_analytics.yaml default.analytics analytics_cycles=33
```

## Best Practices

1. **Start Small**: Begin with minimal cycles to test query correctness
2. **Monitor Resources**: Watch database memory and CPU usage closely
3. **Validate Results**: Check data counts and query results for accuracy
4. **Scale Gradually**: Increase data volume incrementally
5. **Optimize Database**: Ensure proper indexing and memory configuration
6. **Benchmark Baseline**: Establish single-threaded performance first
