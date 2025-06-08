# OpenSearch Bulk Operations Workload

A high-throughput workload designed for testing bulk indexing performance and large-scale data loading scenarios.

## Purpose

This workload demonstrates:
- High-performance bulk document indexing
- Optimized index settings for bulk operations
- Configurable batch sizes and threading
- Performance verification through search operations
- Best practices for large-scale data loading

## Scenarios

### default
Complete bulk workflow: pre-cleanup → schema → bulk loading → verification → cleanup

*Note: Includes automatic cleanup at start to handle existing indices*

### schema_only
Creates only the optimized index for bulk operations

### bulk_only
Performs bulk indexing operations (assumes index exists)

### verify_only
Runs verification searches (assumes data has been loaded)

## Template Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `index_name` | `bulk_index` | Name of the bulk index |
| `doc_count` | `100000` | Total number of documents to index |
| `bulk_size` | `1000` | Documents per bulk request |
| `bulk_threads` | `5` | Threads for bulk operations |
| `refresh_interval` | `30s` | Index refresh interval during loading |

## Operations

### Pre-Cleanup Phase
- **delete_bulk_index** - Automatically removes any existing index to ensure clean start
  - Uses `errors=count,warn` to gracefully handle non-existent indices
  - Prevents "index already exists" errors on repeated runs

### Schema Phase
- **create_bulk_index** - Creates an index optimized for bulk operations with:
  - Multiple shards for parallel processing
  - Increased refresh interval
  - Optimized mappings for the data model

### Bulk Phase
- **bulk_index** - Performs bulk indexing operations with configurable batch sizes

### Verify Phase
- **search_by_user** - Verifies data by searching for user-specific documents
- **search_by_category** - Verifies data distribution across categories

### Cleanup Phase
- **delete_bulk_index** - Removes the bulk test index

## Index Optimization

The workload creates an index optimized for bulk operations:

```yaml
settings:
  number_of_shards: 3        # Parallel processing
  number_of_replicas: 1      # Reduced during loading
  refresh_interval: 30s      # Delayed refresh for performance
  index:
    max_result_window: 50000 # Support large result sets
```

## Data Model

The workload simulates an e-commerce event tracking system:

### Document Structure
- **user_id** - User identifier (keyword)
- **product_id** - Product identifier (keyword)  
- **event_type** - Event type (view, click, purchase, add_to_cart)
- **timestamp** - Event timestamp
- **session_id** - Session identifier (UUID)
- **price** - Product price (float)
- **quantity** - Quantity involved (integer)
- **category** - Product category (keyword)

### Data Distribution
- **Users**: 1-10,000 user IDs
- **Products**: 1-1,000 product IDs
- **Events**: Weighted distribution (view:5, click:3, add_to_cart:2, purchase:1)
- **Categories**: electronics, clothing, books, home, sports

## Usage Examples

### Default Bulk Loading
```bash
nb5 opensearch_bulk region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com
```

### High-Volume Testing
```bash
nb5 opensearch_bulk region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  doc_count=10000000 bulk_size=5000 bulk_threads=20
```

### Small Batch Testing
```bash
nb5 opensearch_bulk region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  doc_count=10000 bulk_size=100 bulk_threads=2
```

### Schema Setup Only
```bash
nb5 opensearch_bulk region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  --scenario schema_only
```

## Performance Tuning

### Bulk Size Optimization
- **Small batches (100-500)**: Lower memory usage, more HTTP overhead
- **Medium batches (1000-2000)**: Good balance for most use cases
- **Large batches (5000+)**: Higher throughput, more memory usage

### Thread Count Guidelines
- **Conservative**: 2-5 threads for small clusters
- **Moderate**: 5-10 threads for medium clusters  
- **Aggressive**: 10-20+ threads for large clusters
- **Rule**: Monitor cluster CPU/memory, don't exceed 70-80% utilization

### Index Settings
- **Shards**: 1 shard per 10-50GB of data
- **Replicas**: Set to 0 during bulk loading, increase afterward
- **Refresh interval**: 30s-60s during loading, 1s for real-time needs

## Monitoring

### Key Metrics to Watch
- **Indexing rate** (docs/second)
- **Bulk request latency** (ms)
- **Cluster CPU utilization** (%)
- **JVM heap usage** (%)
- **Disk I/O** (IOPS, throughput)

### Performance Indicators
- **Good**: Consistent indexing rate, low error rate
- **Warning**: Increasing latency, high CPU/memory usage
- **Critical**: Bulk rejections, out of memory errors

## Expected Results

### Throughput Expectations
- **Small cluster** (3 nodes): 5,000-15,000 docs/sec
- **Medium cluster** (6 nodes): 15,000-50,000 docs/sec  
- **Large cluster** (12+ nodes): 50,000+ docs/sec

*Actual performance depends on document size, cluster configuration, and hardware.*

### Verification Results
- Search operations should return expected document counts
- Data should be evenly distributed across categories
- User-specific searches should return relevant events

## Troubleshooting

### Bulk Rejections
- **Cause**: Queue full, cluster overwhelmed
- **Solution**: Reduce thread count or bulk size
- **Prevention**: Monitor queue sizes and cluster health

### Out of Memory Errors
- **Cause**: Bulk size too large, insufficient heap
- **Solution**: Reduce bulk_size parameter
- **Prevention**: Monitor JVM heap usage

### Slow Performance
- **Cause**: Suboptimal settings, resource constraints
- **Solution**: Tune bulk_size, threads, and index settings
- **Investigation**: Check cluster metrics and logs

### Uneven Data Distribution
- **Cause**: Poor shard allocation, routing issues
- **Solution**: Verify shard count and cluster balance
- **Prevention**: Use appropriate shard count for data size

## Best Practices

### Before Bulk Loading
1. Set replicas to 0
2. Increase refresh interval (30s+)
3. Disable index.translog.durability (if acceptable)
4. Ensure sufficient cluster resources

### During Bulk Loading
1. Monitor cluster health continuously
2. Watch for bulk rejections and errors
3. Adjust thread count based on performance
4. Use consistent bulk sizes

### After Bulk Loading
1. Reset refresh interval to 1s
2. Increase replica count as needed
3. Force merge to optimize segments
4. Verify data integrity with searches

## Advanced Configuration

### Custom Refresh Strategy
```yaml
settings:
  refresh_interval: -1  # Disable automatic refresh
```
Then manually refresh after bulk loading.

### Translog Optimization
```yaml
settings:
  index.translog.durability: async
  index.translog.sync_interval: 30s
```

### Memory Circuit Breaker
Monitor and adjust circuit breaker settings if needed:
- `indices.breaker.total.limit`
- `indices.breaker.request.limit`

## Next Steps

- Optimize bulk parameters for your specific cluster
- Test with your actual document structure and size
- Implement monitoring and alerting for production bulk loading
- Consider using the Index State Management (ISM) plugin for lifecycle management
