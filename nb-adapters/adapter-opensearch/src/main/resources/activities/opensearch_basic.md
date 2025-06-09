# OpenSearch Basic CRUD Workload

A comprehensive workload demonstrating all basic OpenSearch operations including index management, document operations, and search functionality.

## Purpose

This workload demonstrates:
- Index creation with custom mappings
- Document indexing with various data types
- Search operations with different query types
- Proper cleanup procedures

## Scenarios

### default
Runs the complete workflow: pre-cleanup → schema creation → document indexing → search operations → cleanup

*Note: Includes automatic cleanup at start to handle existing indices*

### schema_only
Creates only the index schema (useful for setup)

### index_only
Indexes documents without creating schema or searching (assumes index exists)

### search_only
Performs search operations only (assumes index and documents exist)

## Template Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `index_name` | `test_index` | Name of the index to create |
| `doc_count` | `1000` | Number of documents to index |
| `search_count` | `100` | Number of search operations |
| `rampup_threads` | `10` | Threads for indexing phase |
| `search_threads` | `5` | Threads for search phase |

## Operations

### Pre-Cleanup Phase
- **delete_index** - Automatically removes any existing index to ensure clean start
  - Uses `errors=count,warn` to gracefully handle non-existent indices
  - Prevents "index already exists" errors on repeated runs

### Schema Phase
- **create_index** - Creates an index with mappings for text, keyword, date, and numeric fields

### Rampup Phase  
- **index_doc** - Indexes documents with realistic test data including:
  - Text fields (title, content)
  - Keyword fields (category)
  - Date fields (timestamp)
  - Numeric fields (price)

### Search Phase
- **search_by_term** - Full-text search on content field
- **search_by_category** - Exact match search on category field

### Cleanup Phase
- **delete_index** - Removes the test index

## Data Generation

The workload uses realistic data bindings:
- **doc_id** - Sequential document IDs
- **title** - Template-generated document titles
- **content** - Lorem ipsum text (50-100 words)
- **category** - Weighted categories (tech, science, business)
- **timestamp** - Current epoch time
- **price** - Random prices between $10-$1000

## Usage Examples

### Default Run
```bash
nb5 opensearch_basic region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com
```

### Custom Parameters
```bash
nb5 opensearch_basic region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  index_name=my_test_index doc_count=5000 search_count=500
```

### Schema Only
```bash
nb5 opensearch_basic region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  --scenario schema_only
```

### High Volume Testing
```bash
nb5 opensearch_basic region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  doc_count=100000 rampup_threads=20 search_threads=10
```

## Performance Considerations

### Indexing Performance
- Increase `rampup_threads` for faster indexing
- Consider bulk operations for very high document counts
- Monitor cluster CPU and memory usage

### Search Performance
- Adjust `search_threads` based on cluster capacity
- Monitor search latency and throughput
- Consider index warming for consistent performance

## Index Structure

The workload creates an index with the following mapping:

```json
{
  "mappings": {
    "properties": {
      "title": {"type": "text"},
      "content": {"type": "text"},
      "category": {"type": "keyword"},
      "timestamp": {"type": "date"},
      "price": {"type": "float"}
    }
  }
}
```

## Expected Results

- **Indexing**: Should achieve consistent document indexing rates
- **Search**: Should return relevant results for both text and keyword searches
- **Cleanup**: Should successfully remove all test data

## Troubleshooting

### Slow Indexing
- Reduce thread count if cluster is overwhelmed
- Check cluster health and resource utilization
- Consider increasing refresh interval during bulk loading

### Search Errors
- Verify index exists before running search-only scenario
- Check that documents were successfully indexed
- Ensure search queries match the index mapping

### Memory Issues
- Reduce `doc_count` for smaller clusters
- Monitor JVM heap usage on OpenSearch nodes
- Consider using bulk operations for large datasets

## Next Steps

After running this workload successfully:
- Try `opensearch_vector_search` for vector similarity testing
- Use `opensearch_bulk` for high-throughput scenarios
- Customize the workload for your specific use case
