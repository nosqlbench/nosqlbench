# SpecTest Formatting

The specifications and examples follow a pattern:

1. Some or part of a templated workload in yaml format.
2. The JSON equivalent as it would be loaded. This is cross-checked against the result of parsing
   the yaml into data.
3. The Workload API view of the same data rendered as a JSON data structure. This is cross-checked
   against the workload API's rendering of the loaded data.

To be matched by the testing layer, you must prefix each section with a format marker with emphasis,
like this:

*format:*

```text
body of example
```

Further, to match the pattern above, these must occur in sequences like the following, with no other
intervening content. If the second fenced code section is a JSON array, then each object within
it is compared pair-wise with the yaml structure as in a multi-doc scenario. The following
example is actually tested along with the non-empty templates. It is valid because the second
block is in array form, and thus compares 0 pair-wise elements.

*yaml:*
```yaml
# some yaml here
```

*json:*
```json5
[]
```

*ops:*
```json5
[]
```

The above sequence of 6 contiguous markdown elements follows a recognizable pattern to the
specification testing harness. The names above the sections are required to match and fenced
code sections are required to follow each.

All the markdown files in this directory are loaded and scanned for this pattern, and all
such sequences are verified each time NoSQLBench is built.
