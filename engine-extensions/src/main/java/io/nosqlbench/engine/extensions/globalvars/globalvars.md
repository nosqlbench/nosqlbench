globalvars extension
===================

Allows access to the global object map from SharedState.gl_ObjectMap, which allows
for cross-binding and cross-thread data sharing.

```
var result = globalvars.get("result");
```
