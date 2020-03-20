---
title: 08 Template Params
weight: 08
---

# Template Params

All nosqlbench YAML formats support a parameter macro format that applies before YAML processing starts. It is a basic macro facility that allows named anchors to be placed in the document as a whole:

```text
<<varname:defaultval>>
# or
TEMPLATE(varname,defaultval)
```

In this example, the name of the parameter is `varname`. It is given a default value of `defaultval`. If an activity parameter named *varname* is provided, as in `varname=barbaz`, then this whole expression will be replaced with `barbaz`. If none is provided then the default value will be used instead. For example:

```text
[test]$ cat > stdout-test.yaml
statements:
 - "<<linetoprint:MISSING>>\n"
# EOF (control-D in your terminal)

[test]$ ./nb run driver=stdout workload=stdout-test cycles=1
MISSING

[test]$ ./nb run driver=stdout workload=stdout-test cycles=1 linetoprint="THIS IS IT"
THIS IS IT
```

If an empty value is desired by default, then simply use an empty string in your template, like `<<varname:>>` or `TEMPLATE(varname,)`.


