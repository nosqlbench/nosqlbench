# OpenSearch Advanced Vector Search Workload

An advanced vector search workload designed for testing KNN capabilities with real vector datasets and comprehensive relevancy metrics.

## Purpose

This workload demonstrates:
- Advanced KNN search with real vector datasets from HDF5 files
- Relevancy measurement and verification
- Bulk vector indexing for large datasets
- Custom schema handling for vector operations
- Performance metrics including recall, precision, F1, and average precision

## Scenarios

### vectors_brief
Streamlined workflow: pre-cleanup → bulk indexing → search with verification

*Note: Includes automatic cleanup at start to handle existing indices*

### vectors
Complete workflow: pre-cleanup → schema → indexing → search with verification

*Note: Includes automatic cleanup at start to handle existing indices*

## Template Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `indexname` | `vectors_index` | Name of the vector index |
| `dataset` | Required | HDF5 dataset name (without .hdf5 extension) |
| `trainsize` | Required | Number of vectors to index |
| `testsize` | Required | Number of search operations |
| `rampup_threads` | `10` | Threads for indexing phase |
| `search_threads` | `10` | Threads for search phase |
| `bulk_repeat` | `100` | Documents per bulk request |

## Operations

### Pre-Cleanup Phase
- **drop_index** - Automatically removes any existing vector index to ensure clean start
  - Uses `errors=count,warn` to gracefully handle non-existent indices
  - Prevents "index already exists" errors on repeated runs

### Schema Phase
- **create_index** - Creates a vector index with basic configuration

### Indexing Phases
- **bulk_index** - High-performance bulk indexing of vectors
- **index** - Individual document indexing (alternative to bulk)

### Search Phases
- **search** - Basic KNN vector search
- **search_specify_schema** - KNN search with custom schema and filtering
- **search_and_verify** - KNN search with comprehensive relevancy verification

## Data Requirements

This workload requires HDF5 files containing vector datasets in the `testdata/` directory:

### Required HDF5 Structure
```
testdata/{dataset}.hdf5
├── /test      - Query vectors for search
├── /train     - Training vectors for indexing  
├── /neighbors - Ground truth nearest neighbor indices
└── /distance  - Ground truth distances
```

### Example Datasets
- **SIFT**: Computer vision feature vectors
- **GloVe**: Word embedding vectors
- **Deep1B**: Large-scale deep learning vectors

## Vector Data Bindings

The workload uses HDF5-based data bindings:
- **test_floatlist** - Query vectors from HDF5 test dataset
- **train_floatlist** - Training vectors from HDF5 train dataset
- **relevant_indices** - Ground truth neighbor indices for verification
- **distance_floatlist** - Ground truth distances for relevancy metrics

## Relevancy Metrics

The workload calculates comprehensive search quality metrics:

### Supported Metrics
- **Recall@k** - Fraction of relevant items retrieved
- **Precision@k** - Fraction of retrieved items that are relevant
- **F1@k** - Harmonic mean of precision and recall
- **Reciprocal Rank (RR)** - Reciprocal of rank of first relevant item
- **Average Precision (AP)** - Area under precision-recall curve

### Verification Process
1. Execute KNN search with query vector
2. Extract document indices from search results
3. Compare against ground truth neighbor indices
4. Calculate relevancy metrics for quality assessment

## Usage Examples

### Basic Vector Search Testing
```bash
nb5 osvectors_advancedsearch vectors_brief \
  region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  dataset=sift trainsize=10000 testsize=1000
```

### Large Scale Performance Testing
```bash
nb5 osvectors_advancedsearch vectors \
  region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  dataset=deep1b trainsize=1000000 testsize=10000 \
  rampup_threads=50 search_threads=20
```

### Custom Bulk Configuration
```bash
nb5 osvectors_advancedsearch vectors_brief \
  region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com \
  dataset=glove trainsize=100000 testsize=5000 \
  bulk_repeat=500 rampup_threads=20
```

## Performance Considerations

### Indexing Performance
- Increase `bulk_repeat` for higher throughput bulk operations
- Adjust `rampup_threads` based on cluster capacity
- Monitor memory usage as vectors consume significant RAM

### Search Performance  
- Higher thread counts improve throughput but may overwhelm cluster
- Vector search is CPU-intensive on OpenSearch nodes
- Consider index warming before performance testing

### Memory Requirements
- Vector datasets can be very large (GB to TB scale)
- HDF5 files are memory-mapped for efficient access
- Ensure sufficient cluster memory for vector indices

## Expected Results

### Indexing Metrics
- Bulk indexing rate (vectors/second)
- Index build time and memory consumption
- Successful document count verification

### Search Metrics
- Search latency (p50, p95, p99)
- Search throughput (queries/second)
- Relevancy quality metrics (recall, precision, F1)

### Quality Benchmarks
- **Good Recall@10**: >0.8 for most datasets
- **Good Precision@10**: >0.7 for most datasets
- **Acceptable Latency**: <100ms p95 for real-time applications

## Troubleshooting

### Missing HDF5 Files
- **Error**: "Unable to find source for testdata/{dataset}.hdf5"
- **Solution**: Ensure HDF5 files are in the correct testdata/ directory
- **Alternative**: Use synthetic vector generation workloads instead

### Memory Issues
- **Symptoms**: OutOfMemoryError, slow indexing
- **Solutions**: Reduce trainsize, increase cluster memory, use fewer threads
- **Monitoring**: Watch JVM heap usage on OpenSearch nodes

### Poor Relevancy Scores
- **Causes**: Incorrect vector normalization, wrong distance metric
- **Investigation**: Verify HDF5 data format and ground truth accuracy
- **Tuning**: Adjust KNN algorithm parameters (ef_construction, m)

### Search Failures
- **Error**: "search_phase_execution_exception"
- **Cause**: KNN plugin not installed or configured
- **Solution**: Install OpenSearch KNN plugin and restart cluster

## Advanced Configuration

### Custom Schema Usage
The workload supports custom schema classes for specialized vector handling:
```yaml
schema: io.nosqlbench.adapter.opensearch.pojos.UserDefinedSchema
```

### Filtering Support
Combine vector search with metadata filtering:
```yaml
filter:
  field: "type"
  comparator: "eq" 
  value: "experimental"
```

### Bulk Optimization
Tune bulk operations for your cluster:
- Increase `bulk_repeat` for larger batches
- Adjust thread counts based on cluster size
- Monitor bulk rejection rates

## Dataset Preparation

### Converting Datasets to HDF5
Use tools like h5py (Python) to convert vector datasets:
```python
import h5py
import numpy as np

with h5py.File('dataset.hdf5', 'w') as f:
    f.create_dataset('/train', data=train_vectors)
    f.create_dataset('/test', data=test_vectors)
    f.create_dataset('/neighbors', data=ground_truth_neighbors)
    f.create_dataset('/distance', data=ground_truth_distances)
```

### Supported Vector Formats
- **Float32**: Most common, good balance of precision and memory
- **Float64**: Higher precision, more memory usage
- **Int16/Int8**: Quantized vectors for memory efficiency

## Next Steps

- Prepare HDF5 vector datasets for your specific use case
- Install and configure OpenSearch KNN plugin
- Tune HNSW parameters based on relevancy requirements
- Implement custom relevancy metrics for domain-specific evaluation
- Scale testing to production-sized datasets and query loads
