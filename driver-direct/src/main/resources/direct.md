# Direct Driver

This is a unique type of NoSQLBench driver which assumes no particular
runtime API, instead relying on runtime reflection to find and invoke
the methods as specified in the op template.

```yaml
statements:
    - "java.lang.System.out.println(\"Testing\");"
    - "System.out.println(\"Testing\");"
    - op: "System.out.println(\"Testing\");"
    - op:
          class: java.lang.System
          field: out
          method: println
          arg0: Testing
    - op:
          class: java.lang.System
          field: out
          method: println
          _x: Testing
    - op:
          object: myobj
    - myobj=System.out.println("testing");
```
