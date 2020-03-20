---
title: 07 Multi-Docs
weight: 07
---

# Multi-Docs

The YAML spec allows for multiple yaml documents to be concatenated in the
same file with a separator:

```yaml
---
```

This offers an additional convenience when configuring activities. If you want to parameterize or tag some a set of statements with their own bindings, params, or tags, but alongside another set of uniquely configured statements, you need only put them in separate logical documents, separated by a triple-dash.

For example:

```text
[test]$ cat > stdout-test.yaml
bindings:
 docval: WeightedStrings('doc1.1:1;doc1.2:2;')
statements:
 - "doc1.form1 {docval}\n"
 - "doc1.form2 {docval}\n"
---
bindings:
 numname: NumberNameToString()
statements:
 - "doc2.number {numname}\n"
# EOF (control-D in your terminal)
[test]$ ./nb run driver=stdout workload=stdout-test cycles=10
doc1.form1 doc1.1
doc1.form2 doc1.2
doc2.number two
doc1.form1 doc1.2
doc1.form2 doc1.1
doc2.number five
doc1.form1 doc1.2
doc1.form2 doc1.2
doc2.number eight
doc1.form1 doc1.1
```

This shows that you can use the power of blocks and tags together at one level and also allow statements to be broken apart into a whole other level of partitioning if desired.

:::warning
The multi-doc support is there as a ripcord when you need it. However, it is strongly advised that you keep your YAML workloads simple to start and only use features like the multi-doc when you absolutely need it. For this, blocks are generally a better choice. See examples in the standard workloads.
:::
