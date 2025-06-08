# OpenSearch NoSQLBench Adapter

The OpenSearch adapter allows you to test AWS OpenSearch Service and OpenSearch Serverless using NoSQLBench. This adapter supports both traditional document operations and vector search capabilities.

## Features

- **Index Management**: Create and delete indices with custom mappings
- **Document Operations**: Index, update, and delete documents
- **Search Operations**: Full-text search and vector similarity search (KNN)
- **Bulk Operations**: High-performance bulk indexing
- **Vector Search**: Support for KNN vector search with HNSW algorithm
- **AWS Integration**: Native support for AWS OpenSearch Service and OpenSearch Serverless

## Configuration

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
nb5 opensearch-getting-started region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com

# For OpenSearch Serverless
nb5 opensearch-getting-started region=us-east-1 host=abc123.us-east-1.aoss.amazonaws.com svctype=aoss

# With specific AWS profile
nb5 opensearch-getting-started region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com profile=myprofile
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

#### knn_search
Performs vector similarity search or regular search queries.

```yaml
# Regular search
knn_search: my_index
query:
  match:
    content: "search term"
size: 10

# Vector search
knn_search: vector_index
k: 10
vector: [0.1, 0.2, 0.3, ...]
field: vector_field
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

### opensearch-getting-started
A minimal workload to test basic connectivity and operations.

### opensearch-basic
Comprehensive CRUD operations workload including:
- Index creation with mappings
- Document indexing
- Search operations
- Cleanup

### opensearch-vector-search
Vector search workload for testing KNN capabilities:
- Vector index creation with HNSW configuration
- Vector document indexing
- KNN search operations
- Filtered vector search

### opensearch-bulk
High-throughput bulk operations workload:
- Optimized index settings for bulk operations
- Bulk document indexing
- Performance verification

## Authentication

The adapter uses the AWS SDK for authentication. You can authenticate using:

1. **AWS Credentials File** (`~/.aws/credentials`)
2. **AWS Profile** (specify with `profile` parameter)
3. **IAM Roles** (when running on EC2/ECS/Lambda)
4. **Environment Variables** (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)

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

## Troubleshooting

### Connection Issues
- Verify your AWS credentials are configured correctly
- Check that the OpenSearch endpoint is accessible from your network
- Ensure the security group allows access on port 443

### Authentication Errors
- Verify your AWS profile has the necessary OpenSearch permissions
- Check that the service type (`es` vs `aoss`) matches your cluster type

### Performance Issues
- Monitor OpenSearch cluster metrics during testing
- Adjust thread counts and batch sizes based on cluster capacity
- Consider using bulk operations for high-throughput scenarios
