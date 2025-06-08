# JDBC Key-Value Workload

## Overview

The `jdbc_keyvalue.yaml` workload provides a simple key-value storage pattern for testing basic read/write operations. This workload is ideal for baseline performance testing, connection pool validation, and simple throughput measurements.

## Scenario Description

This workload simulates a basic key-value store with:
- **Simple Schema**: Two-column table (key, value)
- **Basic Operations**: Insert and point lookup operations
- **High Throughput**: Optimized for maximum ops/sec testing
- **Minimal Complexity**: No joins, aggregations, or complex queries

## Schema Design

### Table Created

**baselines** (2 columns)
- `key` (varchar): String key identifier
- `value` (varchar): String value data

This minimal schema provides:
- **Fast Inserts**: Simple two-column structure
- **Efficient Lookups**: Single-key point queries
- **Low Overhead**: Minimal storage and processing requirements

## Operations and Phases

### Rampup Phase
**Purpose**: Load key-value pairs into the table

**Operations**:
- `main_insert` (ratio: 1) - Insert key-value pairs

**Stride Calculation**: 1 = **1 operation per stride**

**Data Pattern**:
- Each operation inserts one key-value pair
- Keys are generated as strings from Identity() function
- Values are generated as strings from hash ranges

### Select Phase
**Purpose**: Execute point lookups by key

**Operations**:
- `select` (ratio: 1) - Point lookup by key

**Stride Calculation**: 1 = **1 operation per stride**

**Query Pattern**:
- Each operation performs: `SELECT * FROM table WHERE key=?`
- Uses prepared statements for optimal performance
- Targets keys that were inserted during rampup phase

## Binding Patterns

### Simple Key-Value Generation
```yaml
bindings:
  rw_key: ToString()                    # String key from cycle identity
  rw_value: HashRange(0L,10000L); ToString()  # String value from hash range
```

**Key Characteristics**:
- **rw_key**: Generates unique string keys based on cycle number
- **rw_value**: Generates pseudo-random string values from hash function
- **Deterministic**: Same cycle produces same key-value pair
- **Reusable**: Select phase can target keys from rampup phase

## Usage Examples

### Basic Key-Value Testing
```bash
# Run complete key-value workload
nb5 jdbc_keyvalue.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true

# Individual phases
nb5 jdbc_keyvalue.yaml default.drop dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_keyvalue.yaml default.schema dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_keyvalue.yaml default.rampup dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_keyvalue.yaml default.select dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
```

### High-Throughput Testing
```bash
# Maximum throughput testing
nb5 jdbc_keyvalue.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=10 connections=10 rampupsize=100000 select_cycles=50000

# Connection pool stress testing
nb5 jdbc_keyvalue.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=20 connections=20 rampupsize=1000000 select_cycles=500000

# Single-threaded baseline
nb5 jdbc_keyvalue.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=1 connections=1 rampupsize=10000 select_cycles=10000
```

### Performance Profiling
```bash
# Write-heavy workload
nb5 jdbc_keyvalue.yaml default.rampup dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=auto rampupsize=1000000

# Read-heavy workload  
nb5 jdbc_keyvalue.yaml default.select dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=auto select_cycles=1000000

# Mixed read/write (run both phases with threads)
nb5 jdbc_keyvalue.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=auto rampupsize=500000 select_cycles=500000
```

## Customization Parameters

### Template Variables
- `schemaname` (default: public) - Database schema name
- `tablename` (default: baselines) - Table name for key-value storage
- `rampupsize` (default: 1000) - Number of key-value pairs to insert
- `connections` (default: 10) - Connection pool size
- `select_cycles` (default: 1000) - Number of select operations

### Custom Configuration Example
```bash
nb5 jdbc_keyvalue.yaml default \
  dburl=jdbc:presto://host:8080/memory?user=testuser \
  use_hikaricp=true \
  schemaname=test_schema \
  tablename=kv_store \
  connections=15 \
  rampupsize=1000000 \
  select_cycles=500000 \
  threads=12
```

## Performance Characteristics

### Expected Throughput
- **Rampup Phase**: 5000-20000 ops/sec (simple inserts)
- **Select Phase**: 10000-50000 ops/sec (point lookups)

### Resource Usage
- **Memory**: Very Low (minimal data structure)
- **CPU**: Low (simple operations)
- **I/O**: Low to Moderate (depends on volume)

### Scaling Behavior
- **Linear Scaling**: Excellent scaling with thread count
- **Connection Limited**: Performance plateaus at connection pool limit
- **Database Limited**: May hit database-specific limits at high concurrency

## Use Cases

### Performance Baseline
- **Throughput Measurement**: Maximum ops/sec capability
- **Latency Testing**: Minimum response time measurement
- **Connection Pool Testing**: Validate pool configuration
- **Database Limits**: Find maximum concurrent connection limits

### Stress Testing
- **High Concurrency**: Test with many threads and connections
- **Sustained Load**: Long-running tests for stability
- **Resource Exhaustion**: Push database to resource limits
- **Recovery Testing**: Test database recovery after overload

### Comparative Analysis
- **Database Comparison**: Compare different database engines
- **Configuration Tuning**: Test different database settings
- **Hardware Scaling**: Test performance across different hardware
- **Network Impact**: Test performance across network configurations

## Troubleshooting

### Common Issues
1. **Connection Pool Exhaustion**: Reduce threads or increase connections
2. **Database Locks**: Key-value operations should be lock-free
3. **Memory Issues**: Reduce rampupsize for memory-constrained environments
4. **Network Timeouts**: Reduce concurrency for network-limited scenarios

### Performance Optimization
```bash
# Find optimal thread count
for threads in 1 2 4 8 16; do
  echo "Testing with $threads threads"
  nb5 jdbc_keyvalue.yaml default.select threads=$threads select_cycles=10000
done

# Test connection pool sizing
for conns in 5 10 20 50; do
  echo "Testing with $conns connections"
  nb5 jdbc_keyvalue.yaml default connections=$conns threads=10 select_cycles=10000
done
```

### Debug Commands
```bash
# Validate workload syntax
nb5 jdbc_keyvalue.yaml --dry-run

# Monitor detailed performance
nb5 jdbc_keyvalue.yaml default --log-level DEBUG

# Test single operation
nb5 jdbc_keyvalue.yaml default.select select_cycles=1 --log-level TRACE
```

## Best Practices

1. **Start Simple**: Begin with single-threaded execution
2. **Scale Incrementally**: Double thread count to find optimal concurrency
3. **Monitor Resources**: Watch database CPU, memory, and connection usage
4. **Validate Results**: Ensure select operations return expected data
5. **Baseline First**: Establish single-threaded performance before scaling
6. **Connection Tuning**: Match connection pool size to thread count
7. **Sustained Testing**: Run longer tests to identify stability issues

## Advanced Usage

### Custom Key Patterns
```yaml
# Sequential keys
rw_key: Identity(); ToString()

# UUID-like keys  
rw_key: Template('{}-{}-{}', Uniform(1000,9999), Uniform(1000,9999), Uniform(1000,9999))

# Timestamp-based keys
rw_key: Template('key_{}', EpochMillis())
```

### Value Variations
```yaml
# Fixed-size values
rw_value: FixedValue('fixed_value_content')

# Variable-size values
rw_value: Template('value_{}', HashRange(0L,1000000L))

# Large values for throughput testing
rw_value: Template('large_value_{}', Repeat('x', 1000))
```
