# OpenSearch Getting Started Workload

This is the simplest possible workload to test OpenSearch connectivity and basic operations.

## Purpose

The getting started workload is designed to:
- Verify OpenSearch adapter connectivity
- Test basic authentication
- Demonstrate minimal index and document operations
- Provide a quick smoke test for your OpenSearch setup

## Operations

1. **create_test_index** - Creates a simple test index with basic mappings
2. **index_test_doc** - Indexes a test document
3. **search_test** - Performs a match_all search to verify the document was indexed
4. **delete_test_index** - Cleans up by deleting the test index

## Required Parameters

- `region` - AWS region (e.g., `us-east-1`)
- `host` - OpenSearch endpoint hostname (without https://)

## Optional Parameters

- `profile` - AWS profile name for authentication
- `svctype` - Service type: `es` for OpenSearch domains (default), `aoss` for Serverless
- `getinfo` - Whether to call cluster info on connect (default: `true`)

## Usage Examples

### Basic Usage
```bash
nb5 opensearch_getting_started region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com
```

### With AWS Profile
```bash
nb5 opensearch_getting_started region=us-east-1 host=search-mydomain.us-east-1.es.amazonaws.com profile=myprofile
```

### OpenSearch Serverless
```bash
nb5 opensearch_getting_started region=us-east-1 host=abc123.us-east-1.aoss.amazonaws.com svctype=aoss
```

## Expected Output

The workload will:
1. Create an index called `test_index`
2. Index 5 test documents (cycles=5)
3. Search for all documents in the index
4. Delete the test index

## Troubleshooting

### Connection Errors
- Verify your AWS credentials are configured
- Check that the OpenSearch endpoint is correct and accessible
- Ensure your security group allows HTTPS access (port 443)

### Authentication Errors
- Verify your AWS profile has OpenSearch permissions
- Check that you're using the correct service type (`es` vs `aoss`)

### Permission Errors
- Ensure your AWS credentials have the following permissions:
  - `es:ESHttpPost`
  - `es:ESHttpPut`
  - `es:ESHttpGet`
  - `es:ESHttpDelete`

## Next Steps

Once this workload runs successfully, you can try:
- `opensearch_basic` - More comprehensive CRUD operations
- `opensearch_vector_search` - Vector similarity search testing
- `opensearch_bulk` - High-throughput bulk operations
