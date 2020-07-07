---
title: 05 Statement Blocks
weight: 05
---

# Statement Blocks

All the basic primitives described above (names, statements, bindings, params, tags) can be used to describe and
parameterize a set of statements in a yaml document. In some scenarios, however, you may need to structure your
statements in a more sophisticated way. You might want to do this if you have a set of common statement forms or
parameters that need to apply to many statements, or perhaps if you have several *different* groups of statements that
need to be configured independently.

This is where blocks become useful:

```text
[test]$ cat > stdout-test.yaml
bindings:
 alpha: Identity()
 beta: Combinations('u;n;u;s;e;d;')
blocks:
 - statements:
   - "{alpha},{beta}\n"
   bindings:
    beta: Combinations('b;l;o;c;k;1;-;COMBINATIONS;')
 - statements:
   - "{alpha},{beta}\n"
   bindings:
    beta: Combinations('b;l;o;c;k;2;-;COMBINATIONS;')
# EOF (control-D in your terminal)

[test]$ ./nb run driver=stdout workload=stdout-test cycles=10
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

This shows a couple of important features of blocks. All blocks inherit defaults for bindings, params, and tags from the
root document level. Any of these values that are defined at the base document level apply to all blocks contained in
that document, unless specifically overridden within a given block.

