# OpenSearch Vector Search Workload

A specialized workload for testing OpenSearch vector search capabilities using the k-nearest neighbors (KNN) algorithm with HNSW (Hierarchical Navigable Small World) indexing.

## Purpose

This workload demonstrates:
- Vector index creation with KNN configuration
- High-dimensional vector document indexing
- KNN vector similarity search
- Filtered vector search operations
- HNSW algorithm parameter tuning

## Scenarios

### default
Complete vector search workflow: pre-cleanup → schema → indexing → search → cleanup

*Note: Includes automatic cleanup at start to handle existing indices*

### schema_only
Creates only the vector index with KNN configuration

### index_only
Indexes vector documents (assumes vector index exists)

### search_only
Performs vector search operations (assumes index and vectors exist)

## Template Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `index_name` | `vector_index` | Name of the vector index |
| `dimensions` | `128` | Vector embedding dimensions |
| `doc_count` | `10000` | Number of vector documents to index |
| `search_count` | `1000` | Number of search operations |
| `k` | `10` | Number of nearest neighbors to return |
| `ef_construction` | `512` | HNSW ef_construction parameter |
| `m` | `16` | HNSW m parameter (max connections) |
| `rampup_threads` | `20` | Threads for indexing phase |
| `search_threads` | `10` | Threads for search phase |

## Operations

### Pre-Cleanup Phase
- **delete_vector_index** - Automatically removes any existing vector index to ensure clean start
  - Uses `errors=count,warn` to gracefully handle non-existent indices
  - Prevents "index already exists" errors on repeated runs

### Schema Phase
- **create_vector_index** - Creates a vector index with:
  - KNN vector field configuration
  - HNSW algorithm parameters
  - Faiss engine settings
  - L2 distance metric

### Rampup Phase
- **index_vector** - Indexes documents containing:
  - High-dimensional vector embeddings
  - Metadata fields for filtering
  - Category information

### Search Phase
- **knn_search** - Pure vector similarity search
- **knn_search_with_filter** - Vector search with metadata filtering

### Cleanup Phase
- **delete_vector_index** - Removes the vector index

## Vector Configuration

The workload creates vectors with configurable dimensions and uses the HNSW algorithm:

```yaml
mappings:
  properties:
    vector_field:
      type: knn_vector
      dimension: 128  # Configurable
      method:
        name: hnsw
        space_type: l2
        engine: faiss
        parameters:
          ef_construction: 512  # Build-time parameter
          m: 16                 # Max connections per node
```

## HNSW Parameters

### ef_construction
- **Purpose**: Controls index build quality vs speed
- **Range**: 100-1000+ (higher = better quality, slower build)
- **Default**: 512
- **Recommendation**: Start with 512, increase for better recall

### m
- **Purpose**: Maximum connections per node in the graph
- **Range**: 4-64 (higher = better recall, more memory)
- **Default**: 16
- **Recommendation**: 16 for most use cases, 32-48 for high recall needs

## Usage Examples

### Default Vector Search
```bash
nb5 opensearch_vector_search region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com
```

### High-Dimensional Vectors
```bash
nb5 opensearch_vector_search region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  dimensions=512 ef_construction=1024 m=32
```

### Large Scale Testing
```bash
nb5 opensearch_vector_search region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  doc_count=1000000 search_count=10000 rampup_threads=50
```

### Schema Only (for manual testing)
```bash
nb5 opensearch_vector_search region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  --scenario schema_only dimensions=256
```

## Performance Considerations

### Indexing Performance
- Higher `ef_construction` = slower indexing, better search quality
- More threads can speed up indexing but may overwhelm cluster
- Monitor memory usage as vectors consume significant RAM

### Search Performance
- Higher `m` values improve recall but increase memory usage
- Vector search is CPU-intensive
- Consider warming the index before performance testing

### Memory Requirements
Vector indices require substantial memory:
- **Formula**: `dimensions × doc_count × 4 bytes` (for float32)
- **Example**: 128D × 1M docs = ~512MB just for vectors
- **HNSW overhead**: Additional 20-50% for graph structure

## Distance Metrics

The workload uses L2 (Euclidean) distance by default. OpenSearch supports:
- **l2** - Euclidean distance (default)
- **cosinesimil** - Cosine similarity
- **l1** - Manhattan distance

## Data Generation

### Vector Generation
- Vectors are randomly generated with uniform distribution
- Each dimension ranges from 0.0 to 1.0
- Query vectors are generated independently for realistic search

### Metadata
- Category fields for filtered search testing
- Timestamp information
- JSON metadata for complex filtering scenarios

## Expected Results

### Indexing Metrics
- Document indexing rate (docs/second)
- Index build time
- Memory consumption growth

### Search Metrics
- Search latency (p50, p95, p99)
- Search throughput (queries/second)
- Recall quality (if ground truth available)

## Troubleshooting

### Out of Memory Errors
- Reduce `doc_count` or `dimensions`
- Increase OpenSearch heap size
- Use fewer indexing threads

### Slow Search Performance
- Reduce `ef_construction` for faster (but lower quality) index
- Increase cluster resources
- Consider using fewer search threads

### Poor Recall
- Increase `ef_construction` parameter
- Increase `m` parameter
- Verify vector normalization if using cosine similarity

## Advanced Usage

### Custom Vector Data
Replace the random vector generation with your own embeddings:

```yaml
bindings:
  vector: YourCustomVectorBinding()
  query_vector: YourCustomQueryVectorBinding()
```

### Multiple Vector Fields
Modify the schema to include multiple vector fields for different embedding models.

### Hybrid Search
Combine vector search with traditional text search for hybrid retrieval.

## Next Steps

- Experiment with different HNSW parameters for your use case
- Test with real embedding models (BERT, Sentence Transformers, etc.)
- Implement hybrid search combining vector and text queries
- Benchmark against your specific recall and latency requirements
