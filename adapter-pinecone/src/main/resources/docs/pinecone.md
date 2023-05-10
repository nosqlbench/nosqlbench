# pinecone driver

```yaml
ops:

# A pinecone query op
 query-example:
  query: myindex
  vector: use bindings to generate an array of floats
  namespace: mynamespace
  filter:
    field:
    operator:
    comparator:


# A delete op
 delete-example:
  delete: indexfoo
  ... additional fields ...

# A describe index stats op
 describe-index-stats-example:
  describe-index-stats: indexbar

# A pinecone fetch op

# A pinecone update op

# A pinecone upsert op

```
