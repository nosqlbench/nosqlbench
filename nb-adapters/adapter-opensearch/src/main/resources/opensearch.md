# OpenSearch NoSQLBench Adapter

The OpenSearch adapter allows you to test both open source OpenSearch and AWS OpenSearch Service/Serverless using NoSQLBench. This unified adapter automatically detects the connection type and uses the appropriate transport mechanism.

## Features

- **Index Management**: Create and delete indices with custom mappings
- **Document Operations**: Index, update, and delete documents
- **Search Operations**: Full-text search and vector similarity search (KNN)
- **Bulk Operations**: High-performance bulk indexing
- **Vector Search**: Support for KNN vector search with HNSW algorithm
- **Automatic Cleanup**: Pre-cleanup functionality prevents "index already exists" errors
- **Dual Transport Support**:
  - HTTP transport for open source OpenSearch
  - AWS SDK transport for AWS OpenSearch Service and OpenSearch Serverless

## Connection Types

The adapter automatically detects whether to use AWS or HTTP transport based on the configuration parameters provided.

### Open Source OpenSearch Configuration

For connecting to open source OpenSearch clusters:

```yaml
params:
  driver: opensearch
  host: localhost          # or your OpenSearch host
  port: 9200              # optional, defaults to 9200
  username: admin         # optional, for basic auth
  password: admin         # optional, for basic auth
  ssl: true              # optional, enables HTTPS
  truststore: /path/to/truststore.jks  # optional, for SSL
  truststore_password: password        # optional, for SSL
```

### AWS OpenSearch Service Configuration

For connecting to AWS OpenSearch Service:

```yaml
params:
  driver: opensearch
  host: search-domain.us-west-2.es.amazonaws.com
  region: us-west-2
  profile: default        # optional, AWS profile
  svctype: es            # optional, 'es' for OpenSearch Service, 'aoss' for Serverless
```

## Configuration Parameters

### Required Parameters

- `region` - AWS region where your OpenSearch cluster is located
- `host` - OpenSearch endpoint hostname (without https://)

### Optional Parameters

- `profile` - AWS profile name for authentication (if not using default credentials)
- `svctype` - Service type: `es` for OpenSearch domains (default), `aoss` for OpenSearch Serverless
- `getinfo` - Whether to call cluster info on connect (`true`/`false`, default: `false`)

### Example Configuration

```bash
# For OpenSearch Service
nb5 opensearch_getting_started region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com

# For OpenSearch Serverless
nb5 opensearch_getting_started region=us-east-1 host=abc123.us-east-1.aoss.amazonaws.com svctype=aoss

# With specific AWS profile
nb5 opensearch_getting_started region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com profile=myprofile
```

## Operation Types

### Index Management

#### create_index
Creates a new index with optional mappings and settings.

```yaml
create_index: my_index
mappings:
  properties:
    title:
      type: text
    timestamp:
      type: date
```

#### delete_index
Deletes an existing index.

```yaml
delete_index: my_index
```

### Document Operations

#### index
Indexes a document into the specified index.

```yaml
index: my_index
document:
  title: "Sample Document"
  content: "This is a test document"
  timestamp: "2024-01-01T00:00:00Z"
```

#### update
Updates an existing document.

```yaml
update: my_index
document:
  title: "Updated Document"
```

#### delete
Deletes a document by ID.

```yaml
delete: my_index
id: "document_id"
```

### Search Operations

#### search
Performs regular text and keyword search queries.

```yaml
search: my_index
query:
  match:
    content: "search term"
size: 10
schema: java.lang.Object  # For flexible document handling
```

#### knn_search
Performs vector similarity search (requires KNN plugin).

```yaml
knn_search: vector_index
k: 10
vector: [0.1, 0.2, 0.3, ...]
field: vector_field
size: 10
```

### Bulk Operations

#### bulk
Performs bulk indexing operations for high throughput.

```yaml
bulk: my_index
op_template:
  repeat: 1000
  index: my_index
  document:
    field1: "value1"
    field2: "value2"
```

## Available Workloads

All workloads include automatic pre-cleanup to handle existing indices and prevent "index already exists" errors on repeated runs.

### opensearch_getting_started
A minimal workload to test basic connectivity and operations.
- **Status**: ✅ Ready to use
- **Operations**: Basic CRUD operations
- **Use Case**: Connectivity testing and quick validation

### opensearch_basic
Comprehensive CRUD operations workload including:
- **Status**: ✅ Ready to use
- **Workflow**: pre-cleanup → schema → indexing → search → cleanup
- **Operations**: Index creation, document indexing, text search, cleanup
- **Use Case**: General-purpose testing and development

### opensearch_bulk
High-throughput bulk operations workload:
- **Status**: ✅ Ready to use
- **Workflow**: pre-cleanup → schema → bulk indexing → verification → cleanup
- **Operations**: Optimized bulk indexing, search verification
- **Use Case**: Performance testing and high-throughput scenarios

### opensearch_vector_search
Vector search workload for testing KNN capabilities:
- **Status**: ✅ Ready to use (requires KNN plugin for search operations)
- **Workflow**: pre-cleanup → schema → vector indexing → KNN search → cleanup
- **Operations**: Vector index creation with HNSW, vector document indexing, KNN search
- **Use Case**: Vector similarity search and AI/ML applications

### osvectors_advancedsearch
Advanced vector search workload with real datasets and relevancy metrics:
- **Status**: ✅ Ready to use (requires HDF5 datasets and KNN plugin)
- **Workflow**: pre-cleanup → schema → bulk vector indexing → search with verification
- **Operations**: HDF5 dataset loading, bulk vector indexing, relevancy measurement
- **Use Case**: Production-grade vector search testing with quality metrics

## Authentication

The adapter uses the AWS SDK for authentication. You can authenticate using:

1. **AWS Credentials File** (`~/.aws/credentials`)
2. **AWS Profile** (specify with `profile` parameter)
3. **IAM Roles** (when running on EC2/ECS/Lambda)
4. **Environment Variables** (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)

## Automatic Cleanup Feature

All OpenSearch workloads include automatic pre-cleanup functionality that:

- **Prevents Errors**: Eliminates "index already exists" errors on repeated runs
- **Graceful Handling**: Uses `errors=count,warn` to handle non-existent indices
- **No Manual Intervention**: Automatically runs before each workload execution
- **Safe Operation**: Only removes indices that would conflict with the workload

### How It Works

Each workload scenario includes a pre-cleanup step:
```yaml
scenarios:
  default:
    pre_cleanup: run driver=opensearch tags==block:"cleanup.*" threads=1 cycles=1 errors=count,warn
    schema: run driver=opensearch tags==block:"schema.*" threads=1 cycles=1
    # ... rest of workflow
```

This allows you to run workloads repeatedly during development and testing without manual cleanup.

## Quick Start Examples

### Basic Testing
```bash
# Test connectivity and basic operations
nb5 opensearch_getting_started driver=opensearch host=localhost port=9200

# Comprehensive CRUD testing
nb5 opensearch_basic driver=opensearch host=localhost port=9200 doc_count=1000 search_count=100

# High-throughput bulk testing
nb5 opensearch_bulk driver=opensearch host=localhost port=9200 doc_count=10000 bulk_size=100
```

### AWS OpenSearch Service
```bash
# Basic workload on AWS
nb5 opensearch_basic region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com

# Vector search (requires KNN plugin)
nb5 opensearch_vector_search region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com doc_count=1000 dimensions=128
```

### Vector Search with HDF5 Datasets
```bash
# Advanced vector testing with real datasets
nb5 osvectors_advancedsearch vectors_brief region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com dataset=sift trainsize=10000 testsize=1000
```

## Vector Search Configuration

For vector search workloads, you need to configure the index with KNN settings:

```yaml
create_index: vector_index
dimensions: 128
ef_construction: 512
m: 16
mappings:
  properties:
    vector_field:
      type: knn_vector
      dimension: 128
      method:
        name: hnsw
        space_type: l2
        engine: faiss
        parameters:
          ef_construction: 512
          m: 16
```

## Performance Tips

1. **Bulk Operations**: Use bulk operations for high-throughput indexing
2. **Refresh Interval**: Increase refresh interval during bulk loading
3. **Replica Count**: Reduce replicas during initial loading, increase for search workloads
4. **Thread Count**: Tune thread counts based on your cluster size and capacity
5. **Vector Dimensions**: Choose appropriate vector dimensions for your use case

## Workload Requirements

### Basic Workloads
- **opensearch_getting_started**: No special requirements
- **opensearch_basic**: No special requirements
- **opensearch_bulk**: No special requirements

### Vector Search Workloads
- **opensearch_vector_search**: Requires OpenSearch KNN plugin for search operations
- **osvectors_advancedsearch**: Requires OpenSearch KNN plugin and HDF5 dataset files

### Installing KNN Plugin
```bash
# For OpenSearch 2.x
sudo bin/opensearch-plugin install https://github.com/opensearch-project/k-NN/releases/download/2.11.0.0/opensearch-knn-2.11.0.0.zip

# Restart OpenSearch after installation
sudo systemctl restart opensearch
```

## Troubleshooting

### Connection Issues
- Verify your AWS credentials are configured correctly
- Check that the OpenSearch endpoint is accessible from your network
- Ensure the security group allows access on port 443

### Authentication Errors
- Verify your AWS profile has the necessary OpenSearch permissions
- Check that the service type (`es` vs `aoss`) matches your cluster type

### Index Already Exists Errors
- **Cause**: Running workloads multiple times without cleanup
- **Solution**: Automatic pre-cleanup handles this - no action needed
- **Manual Fix**: Delete indices manually if needed: `curl -X DELETE "host:9200/index_name"`

### Vector Search Issues
- **Error**: "search_phase_execution_exception" or "all shards failed"
- **Cause**: KNN plugin not installed or vector index misconfigured
- **Solution**: Install KNN plugin and verify vector field mappings

### Performance Issues
- Monitor OpenSearch cluster metrics during testing
- Adjust thread counts and batch sizes based on cluster capacity
- Consider using bulk operations for high-throughput scenarios
- For vector workloads, ensure sufficient memory for vector indices
