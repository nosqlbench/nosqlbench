+++
title = "Op Blocks"
description = "Learn how to organize operations into blocks with shared configuration"
weight = 70
template = "docs-page.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
category = "organization"
tags = ["workloads", "yaml", "blocks", "structure"]
+++

## Op Blocks Defined


All the basic primitives described above (names, ops, bindings, params, tags) can be used to
describe and parameterize a set of op templates in a yaml document. In some scenarios, however, you
may need to structure your op templates in a more sophisticated way. You might want to do this if
you have a set of common operational forms or op params that need to apply to many statements, or
perhaps if you have several *different* groups of operations that need to be configured
independently.

This is where blocks become useful:

## Op Blocks Example

```yaml
# stdout-test.yaml
bindings:
 alpha: Identity()
 beta: Combinations('u;n;u;s;e;d;')
blocks:
 - ops:
   - "{alpha},{beta}\n"
   bindings:
    beta: Combinations('b;l;o;c;k;1;-;COMBINATIONS;')
 - ops:
   - "{alpha},{beta}\n"
   bindings:
    beta: Combinations('b;l;o;c;k;2;-;COMBINATIONS;')
```

```shell
[test]$ ./nb5 run driver=stdout workload=stdout-test cycles=10
0,block1-C
1,block2-O
2,block1-M
3,block2-B
4,block1-I
5,block2-N
6,block1-A
7,block2-T
8,block1-I
9,block2-O
```

This shows a couple of important features of blocks. All blocks inherit defaults for bindings,
params, and tags from the root document level. Any of these values that are defined at the base
document level apply to all blocks contained in that document, unless specifically overridden within
a given block.
