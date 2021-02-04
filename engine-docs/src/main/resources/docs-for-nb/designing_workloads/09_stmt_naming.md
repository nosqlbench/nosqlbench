---
title: 09 Statement Naming
weight: 09
---

# Statement Naming

Docs, Blocks, and Statements can all have names:

```yaml
name: doc1
blocks:
    - name: block1
      statements:
          - stmt1: statement1
          - name: st2
            stmt: statement2
---
name: doc2
...
```

This provides a layered naming scheme for the statements themselves. It is
not usually important to name things except for documentation or metric
naming purposes.

If no names are provided, then names are automatically created for blocks
and statements. Statements assigned at the document level are assigned
to "block0". All other statements are named with the
format `doc#--block#--stmt#`.

For example, the full name of statement1 above would
be `doc1--block1--stmt1`.

**NOTE:**
If you anticipate wanting to get metrics for a specific statement in
addition to the other metrics, then you will want to adopt the habit of
naming all your statements something basic and descriptive.

