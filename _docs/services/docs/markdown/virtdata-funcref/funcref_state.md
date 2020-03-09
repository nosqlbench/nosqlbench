# CATEGORY state
## Clear

Clears the per-thread map which is used by the Expr function.

- java.lang.Object -> Clear() -> java.lang.Object
  - *notes:* Clear all named entries from the per-thread map.
  - *ex:* `Clear()` - *clear all thread-local variables*
- java.lang.Object -> Clear(java.lang.String[]...: names) -> java.lang.Object
  - *notes:* Clear the specified names from the per-thread map.
@param names The names to be removed from the map.
  - *ex:* `Clear('foo')` - *clear the thread-local variable 'foo'*
  - *ex:* `Clear('foo','bar')` - *clear the thread-local variables 'foo' and 'bar'*
- long -> Clear() -> long
  - *notes:* Clear all named entries from the per-thread map.
  - *ex:* `Clear()` - *clear all thread-local variables*
- long -> Clear(java.lang.String[]...: names) -> long
  - *notes:* Clear the specified names from the per-thread map.
@param names The names to be removed from the map.
  - *ex:* `Clear('foo')` - *clear the thread-local variable 'foo'*
  - *ex:* `Clear('foo','bar')` - *clear the thread-local variables 'foo' and 'bar'*


## Load

Load a named value from the per-thread state map. The previous input value will be forgotten, and the named value will replace it before the next function in the chain.

- double -> Load(java.lang.String: name) -> double
  - *ex:* `Load('foo')` - *for the current thread, load a double value from the named variable*
- double -> Load(java.lang.String: name, double: defaultValue) -> double
  - *ex:* `Load('foo',432.0D)` - *for the current thread, load a double value from the named variable, or the defaultvalue if it is not yet defined.*
- double -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> double
  - *ex:* `Load(NumberNameToString())` - *for the current thread, load a double value from the named variable, where the variablename is provided by a function.*
- double -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, double: defaultValue) -> double
  - *ex:* `Load(NumberNameToString(),1234.5D)` - *for the current thread, load a double value from the named variable, where the variablename is provided by a function, or the default value if the named value is not yet defined.*
- long -> Load(java.lang.String: name) -> long
  - *ex:* `Load('foo')` - *for the current thread, load a long value from the named variable*
- long -> Load(java.lang.String: name, long: defaultValue) -> long
  - *ex:* `Load('foo', 423L)` - *for the current thread, load a long value from the named variable, or the default value if the variable is not yet defined*
- long -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> long
  - *ex:* `Load(NumberNameToString())` - *for the current thread, load a long value from the named variable, where the variable name is provided by the provided by a function.*
- long -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, long: defaultvalue) -> long
  - *ex:* `Load(NumberNameToString(),22L)` - *for the current thread, load a long value from the named variable, where the variable name is provided by the provided by a function, or the default value if the variable is not yet defined*
- java.lang.Object -> Load(java.lang.String: name) -> java.lang.Object
  - *ex:* `Load('foo')` - *for the current thread, load an Object value from the named variable*
- java.lang.Object -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Object
  - *ex:* `Load(NumberNameToString())` - *for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function*
- java.lang.Object -> Load(java.lang.String: name, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Load('foo','testvalue')` - *for the current thread, load an Object value from the named variable, or the default value if the variable is not yet defined.*
- java.lang.Object -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Load(NumberNameToString(),'testvalue')` - *for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function, or thedefault value if the variable is not yet defined.*
- long -> Load(java.lang.String: name) -> java.lang.Object
  - *ex:* `Load('foo')` - *for the current thread, load an Object value from the named variable*
- long -> Load(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> java.lang.Object
  - *ex:* `Load(NumberNameToString())` - *for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function*
- long -> Load(java.lang.String: name, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Load('foo','testvalue')` - *for the current thread, load an Object value from the named variable, or the default value if the variable is not yet defined.*
- long -> Load(java.util.function.LongFunction<java.lang.Object>: nameFunc, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Load(NumberNameToString(),'testvalue')` - *for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function, or thedefault value if the variable is not yet defined.*
- int -> Load(java.lang.String: name) -> int
  - *ex:* `Load('foo')` - *for the current thread, load an int value from the named variable*
- int -> Load(java.lang.String: name, int: defaultValue) -> int
  - *ex:* `Load('foo',42)` - *for the current thread, load an int value from the named variable, or return the default value if it is undefined.*
- int -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> int
  - *ex:* `Load(NumberNameToString())` - *for the current thread, load an int value from the named variable, where the variable name is provided by a function.*
- int -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, int: defaultValue) -> int
  - *ex:* `Load(NumberNameToString(),42)` - *for the current thread, load an int value from the named variable, where the variable name is provided by a function, or the default value if the named variable is undefined.*
- java.lang.String -> Load(java.lang.String: name) -> java.lang.String
  - *ex:* `Load('foo')` - *for the current thread, load a String value from the named variable*
- java.lang.String -> Load(java.lang.String: name, java.lang.String: defaultvalue) -> java.lang.String
  - *ex:* `Load('foo','track05')` - *for the current thread, load a String value from the named variable, or teh default value if the variable is not yet defined.*
- java.lang.String -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.String
  - *ex:* `Load(NumberNameToString())` - *for the current thread, load a String value from the named variable, where the variable name is provided by a function*
- java.lang.String -> Load(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, java.lang.String: defaultValue) -> java.lang.String
  - *ex:* `Load(NumberNameToString(),'track05')` - *for the current thread, load a String value from the named variable, where the variable name is provided by a function, or the default value if the variable is not yet defined.*


## LoadDouble

Load a value from a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. If the named variable is not defined, then the default value is returned.

- java.lang.Object -> LoadDouble(java.lang.String: name) -> java.lang.Double
  - *ex:* `LoadDouble('foo')` - *for the current thread, load a double value from the named variable.*
- java.lang.Object -> LoadDouble(java.lang.String: name, double: defaultValue) -> java.lang.Double
  - *ex:* `LoadDouble('foo',23D)` - *for the current thread, load a double value from the named variable,or the default value if the named variable is not defined.*
- java.lang.Object -> LoadDouble(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Double
  - *ex:* `LoadDouble(NumberNameToString())` - *for the current thread, load a double value from the named variable, where the variable name is provided by a function.*
- java.lang.Object -> LoadDouble(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, double: defaultValue) -> java.lang.Double
  - *ex:* `LoadDouble(NumberNameToString(),23D)` - *for the current thread, load a double value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*
- long -> LoadDouble(java.lang.String: name) -> double
  - *ex:* `LoadDouble('foo')` - *for the current thread, load a double value from the named variable.*
- long -> LoadDouble(java.lang.String: name, double: defaultValue) -> double
  - *ex:* `LoadDouble('foo',23D)` - *for the current thread, load a double value from the named variable,or the default value if the named variable is not defined.*
- long -> LoadDouble(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> double
  - *ex:* `LoadDouble(NumberNameToString())` - *for the current thread, load a double value from the named variable, where the variable name is provided by a function.*
- long -> LoadDouble(java.util.function.LongFunction<java.lang.Object>: nameFunc, double: defaultValue) -> double
  - *ex:* `LoadDouble(NumberNameToString(),23D)` - *for the current thread, load a double value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*


## LoadFloat

Load a value from a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. If the named variable is not defined, then the default value is returned.

- java.lang.Object -> LoadFloat(java.lang.String: name) -> java.lang.Float
  - *ex:* `LoadFloat('foo')` - *for the current thread, load a float value from the named variable.*
- java.lang.Object -> LoadFloat(java.lang.String: name, float: defaultValue) -> java.lang.Float
  - *ex:* `LoadFloat('foo',23F)` - *for the current thread, load a float value from the named variable,or the default value if the named variable is not defined.*
- java.lang.Object -> LoadFloat(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Float
  - *ex:* `LoadFloat(NumberNameToString())` - *for the current thread, load a float value from the named variable,where the variable name is provided by a function.*
- java.lang.Object -> LoadFloat(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, float: defaultValue) -> java.lang.Float
  - *ex:* `LoadFloat(NumberNameToString(),23F)` - *for the current thread, load a float value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*
- long -> LoadFloat(java.lang.String: name) -> java.lang.Float
  - *ex:* `LoadFloat('foo')` - *for the current thread, load a float value from the named variable.*
- long -> LoadFloat(java.lang.String: name, float: defaultValue) -> java.lang.Float
  - *ex:* `LoadFloat('foo',23F)` - *for the current thread, load a float value from the named variable,or the default value if the named variable is not defined.*
- long -> LoadFloat(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> java.lang.Float
  - *ex:* `LoadFloat(NumberNameToString())` - *for the current thread, load a float value from the named variable,where the variable name is provided by a function.*
- long -> LoadFloat(java.util.function.LongFunction<java.lang.Object>: nameFunc, float: defaultValue) -> java.lang.Float
  - *ex:* `LoadFloat(NumberNameToString(),23F)` - *for the current thread, load a float value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*


## LoadInteger

Load a value from a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. If the named variable is not defined, then the default value is returned.

- java.lang.Object -> LoadInteger(java.lang.String: name) -> java.lang.Integer
  - *ex:* `LoadInteger('foo')` - *for the current thread, load an integer value from the named variable.*
- java.lang.Object -> LoadInteger(java.lang.String: name, int: defaultValue) -> java.lang.Integer
  - *ex:* `LoadInteger('foo',42)` - *for the current thread, load an integer value from the named variable, or the default value if the named variable is not defined.*
- java.lang.Object -> LoadInteger(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Integer
  - *ex:* `LoadInteger(NumberNameToString())` - *for the current thread, load an integer value from the named variable,where the variable name is provided by a function.*
- java.lang.Object -> LoadInteger(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, int: defaultValue) -> java.lang.Integer
  - *ex:* `LoadInteger(NumberNameToString(),42)` - *for the current thread, load an integer value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*
- long -> LoadInteger(java.lang.String: name) -> int
  - *ex:* `LoadInteger('foo')` - *for the current thread, load an integer value from the named variable.*
- long -> LoadInteger(java.lang.String: name, int: defaultValue) -> int
  - *ex:* `LoadInteger('foo',42)` - *for the current thread, load an integer value from the named variable, or the default value if the named variable is not defined.*
- long -> LoadInteger(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> int
  - *ex:* `LoadInteger(NumberNameToString())` - *for the current thread, load an integer value from the named variable,where the variable name is provided by a function.*
- long -> LoadInteger(java.util.function.LongFunction<java.lang.Object>: nameFunc, int: defaultValue) -> int
  - *ex:* `LoadInteger(NumberNameToString(),42)` - *for the current thread, load an integer value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*


## LoadLong

Load a value from a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. If the named variable is not defined, then the default value is returned.

- java.lang.Object -> LoadLong(java.lang.String: name) -> java.lang.Long
  - *ex:* `LoadLong('foo',42L)` - *for the current thread, load a long value from the named variable.*
- java.lang.Object -> LoadLong(java.lang.String: name, long: defaultValue) -> java.lang.Long
  - *ex:* `LoadLong('foo',42L)` - *for the current thread, load a long value from the named variable, or the default value if the named variable is not defined.*
- java.lang.Object -> LoadLong(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Long
  - *ex:* `LoadLong(NumberNameToString(),42L)` - *for the current thread, load a long value from the named variable,where the variable name is provided by a function.*
- java.lang.Object -> LoadLong(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, long: defaultValue) -> java.lang.Long
  - *ex:* `LoadLong(NumberNameToString(),42L)` - *for the current thread, load a long value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*
- long -> LoadLong(java.lang.String: name) -> long
  - *ex:* `LoadLong('foo',42L)` - *for the current thread, load a long value from the named variable.*
- long -> LoadLong(java.lang.String: name, long: defaultValue) -> long
  - *ex:* `LoadLong('foo',42L)` - *for the current thread, load a long value from the named variable, or the default value if the named variable is not defined.*
- long -> LoadLong(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> long
  - *ex:* `LoadLong(NumberNameToString(),42L)` - *for the current thread, load a long value from the named variable,where the variable name is provided by a function.*
- long -> LoadLong(java.util.function.LongFunction<java.lang.Object>: nameFunc, long: defaultValue) -> long
  - *ex:* `LoadLong(NumberNameToString(),42L)` - *for the current thread, load a long value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*


## LoadString

Load a value from a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. If the named variable is not defined, then the default value is returned.

- java.lang.Object -> LoadString(java.lang.String: name) -> java.lang.String
  - *ex:* `LoadString('foo','examplevalue')` - *for the current thread, load a String value from the named variable.*
- java.lang.Object -> LoadString(java.lang.String: name, java.lang.String: defaultValue) -> java.lang.String
  - *ex:* `LoadString('foo','examplevalue')` - *for the current thread, load a String value from the named variable, or the default value if the named variable is not defined.*
- java.lang.Object -> LoadString(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.String
  - *ex:* `LoadString(NumberNameToString(),'examplevalue')` - *for the current thread, load a String value from the named variable, or the default value if the named variable is not defined.*
- java.lang.Object -> LoadString(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, java.lang.String: defaultValue) -> java.lang.String
  - *ex:* `LoadString(NumberNameToString(),'examplevalue')` - *for the current thread, load a String value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*
- long -> LoadString(java.lang.String: name) -> java.lang.String
  - *ex:* `LoadString('foo','examplevalue')` - *for the current thread, load a String value from the named variable.*
- long -> LoadString(java.lang.String: name, java.lang.String: defaultValue) -> java.lang.String
  - *ex:* `LoadString('foo','examplevalue')` - *for the current thread, load a String value from the named variable, or the default value if the named variable is not defined.*
- long -> LoadString(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> java.lang.String
  - *ex:* `LoadString(NumberNameToString(),'examplevalue')` - *for the current thread, load a String value from the named variable, or the default value if the named variable is not defined.*
- long -> LoadString(java.util.function.LongFunction<java.lang.Object>: nameFunc, java.lang.String: defaultValue) -> java.lang.String
  - *ex:* `LoadString(NumberNameToString(),'examplevalue')` - *for the current thread, load a String value from the named variable,where the variable name is provided by a function, or the default value if the namedvariable is not defined.*


## NullOrLoad

Reads a long variable from the input, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return null object or a loaded value.

- long -> NullOrLoad(double: ratio, java.lang.String: varname) -> java.lang.Object


## NullOrPass

Reads a long variable from the thread local variable map, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return a null object or the input value.

- java.lang.Object -> NullOrPass(double: ratio, java.lang.String: varname) -> java.lang.Object


## Save

Save the current input value at this point in the function chain to a thread-local variable name. The input value is unchanged, and available for the next function in the chain to use as-is.

- double -> Save(java.lang.String: name) -> double
  - *ex:* `Save('foo')` - *for the current thread, save the current double value to the named variable.*
- double -> Save(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> double
  - *ex:* `Save(NumberNameToString())` - *for the current thread, save the current double value to the name 'foo' in this thread, where the variable name is provided by a function.*
- long -> Save(java.lang.String: name) -> long
  - *ex:* `Save('foo')` - *save the current long value to the name 'foo' in this thread*
- long -> Save(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> long
  - *ex:* `Save(NumberNameToString())` - *save the current long value to the name generated by the function given.*
- java.lang.Object -> Save(java.lang.String: name) -> java.lang.Object
  - *ex:* `Save('foo')` - *for the current thread, save the input object value to the named variable*
- java.lang.Object -> Save(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Object
  - *ex:* `Save(NumberNameToString())` - *for the current thread, save the current input object value to the named variable,where the variable name is provided by a function.*
- long -> Save(java.lang.String: name) -> long
  - *ex:* `Save('foo')` - *for the current thread, save the input object value to the named variable*
- long -> Save(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> long
  - *ex:* `Save(NumberNameToString())` - *for the current thread, save the current input object value to the named variable,where the variable name is provided by a function.*
- int -> Save(java.lang.String: name) -> int
  - *ex:* `Save('foo')` - *save the current int value to the name 'foo' in this thread*
- int -> Save(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> int
  - *ex:* `Save(NumberNameToString())` - *save the current int value to a named variable in this thread,where the variable name is provided by a function.*
- java.lang.String -> Save(java.lang.String: name) -> java.lang.String
  - *ex:* `Save('foo')` - *save the current String value to the name 'foo' in this thread*
- java.lang.String -> Save(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.String
  - *ex:* `Save(NumberNameToString())` - *save the current String value to a named variable in this thread, where the variable name is provided by a function*


## SaveDouble

Save a value to a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. Note that the input type is not that suitable for constructing names, so this is more likely to be used in an indirect naming pattern like

```
SaveDouble(Load('id'))
```

- double -> SaveDouble(java.lang.String: name) -> double
  - *ex:* `Save('foo')` - *save the current double value to the name 'foo' in this thread*
- double -> SaveDouble(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> double
  - *ex:* `Save(NumberNameToString())` - *save a double value to a named variable in the current thread, where the variable name is provided by a function.*
- long -> SaveDouble(java.lang.String: name) -> double
  - *ex:* `Save('foo')` - *save the current double value to the name 'foo' in this thread*
- long -> SaveDouble(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> double
  - *ex:* `Save(NumberNameToString())` - *save a double value to a named variable in the current thread, where the variable name is provided by a function.*


## SaveFloat

Save a value to a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. Note that the input type is not that suitable for constructing names, so this is more likely to be used in an indirect naming pattern like

```
SaveDouble(Load('id'))
```

- java.lang.Float -> SaveFloat(java.lang.String: name) -> java.lang.Float
  - *ex:* `SaveFloat('foo')` - *save the current float value to a named variable in this thread.*
- java.lang.Float -> SaveFloat(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Float
  - *ex:* `SaveFloat(NumberNameToString())` - *save the current float value to a named variable in this thread, where the variable name is provided by a function.*
- long -> SaveFloat(java.lang.String: name) -> java.lang.Float
  - *ex:* `SaveFloat('foo')` - *save the current float value to a named variable in this thread.*
- long -> SaveFloat(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> java.lang.Float
  - *ex:* `SaveFloat(NumberNameToString())` - *save the current float value to a named variable in this thread, where the variable name is provided by a function.*


## SaveInteger

Save a value to a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. Note that the input type is not that suitable for constructing names, so this is more likely to be used in an indirect naming pattern like

```
SaveDouble(Load('id'))
```

- int -> SaveInteger(java.lang.String: name) -> int
  - *ex:* `SaveInteger('foo')` - *save the current integer value to a named variable in this thread.*
- int -> SaveInteger(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> int
  - *ex:* `SaveInteger(NumberNameToString())` - *save the current integer value to a named variable in this thread, where the variable name is provided by a function.*
- long -> SaveInteger(java.lang.String: name) -> int
  - *ex:* `SaveInteger('foo')` - *save the current integer value to a named variable in this thread.*
- long -> SaveInteger(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> int
  - *ex:* `SaveInteger(NumberNameToString())` - *save the current integer value to a named variable in this thread, where the variable name is provided by a function.*


## SaveLong

Save a value to a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. Note that the input type is not that suitable for constructing names, so this is more likely to be used in an indirect naming pattern like

```
SaveDouble(Load('id'))
```

- long -> SaveLong(java.lang.String: name) -> long
  - *ex:* `SaveLong('foo')` - *save the current long value to a named variable in this thread.*
- long -> SaveLong(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> long
  - *ex:* `SaveLong(NumberNameToString())` - *save the current long value to a named variable in this thread, where the variable name is provided by a function.*
- long -> SaveLong(java.lang.String: name) -> long
  - *ex:* `SaveLong('foo')` - *save the current long value to a named variable in this thread.*
- long -> SaveLong(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> long
  - *ex:* `SaveLong(NumberNameToString())` - *save the current long value to a named variable in this thread, where the variable name is provided by a function.*


## SaveString

Save a value to a named thread-local variable, where the variable name is fixed or a generated variable name from a provided function. Note that the input type is not that suitable for constructing names, so this is more likely to be used in an indirect naming pattern like

```
SaveDouble(Load('id'))
```

- java.lang.String -> SaveString(java.lang.String: name) -> java.lang.String
  - *ex:* `SaveString('foo')` - *save the current String value to a named variable in this thread.*
- java.lang.String -> SaveString(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.String
  - *ex:* `SaveString(NumberNameToString())` - *save the current String value to a named variable in this thread, where the variable name is provided by a function.*
- long -> SaveString(java.lang.String: name) -> java.lang.String
  - *ex:* `SaveString('foo')` - *save the current String value to a named variable in this thread.*
- long -> SaveString(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> java.lang.String
  - *ex:* `SaveString(NumberNameToString())` - *save the current String value to a named variable in this thread, where the variable name is provided by a function.*


## Show

Show diagnostic values for the thread-local variable map.

- java.lang.Object -> Show() -> java.lang.String
  - *ex:* `Show()` - *Show all values in a json-like format*
- java.lang.Object -> Show(java.lang.String[]...: names) -> java.lang.String
  - *ex:* `Show('foo')` - *Show only the 'foo' value in a json-like format*
  - *ex:* `Show('foo','bar')` - *Show the 'foo' and 'bar' values in a json-like format*
- long -> Show() -> java.lang.String
  - *ex:* `Show()` - *Show all values in a json-like format*
- long -> Show(java.lang.String[]...: names) -> java.lang.String
  - *ex:* `Show('foo')` - *Show only the 'foo' value in a json-like format*
  - *ex:* `Show('foo','bar')` - *Show the 'foo' and 'bar' values in a json-like format*


## Swap

Load a named value from the per-thread state map. The previous input value will be stored in the named value, and the previously stored value will be returned. A default value to return may be provided in case there was no previously stored value under the given name.

- long -> Swap(java.lang.String: name) -> long
  - *ex:* `Swap('foo')` - *for the current thread, swap the input value with the named variable and returned the named variable.*
- long -> Swap(java.lang.String: name, long: defaultValue) -> long
  - *ex:* `Swap('foo',234L)` - *for the current thread, swap the input value with the named variable and returned the named variable,or the default value if the named variable is not defined.*
- long -> Swap(java.util.function.LongFunction<java.lang.String>: nameFunc) -> long
  - *ex:* `Swap(NumberNameToString())` - *for the current thread, swap the input value with the named variable and returned the named variable, where the variable name is generated by the provided function.*
- long -> Swap(java.util.function.LongFunction<java.lang.String>: nameFunc, long: defaultValue) -> long
  - *ex:* `Swap(NumberNameToString(), 234L)` - *for the current thread, swap the input value with the named variable and returned the named variable, where the variable name is generated by the provided function, or the default value if the named variable is not defined.*
- java.lang.Object -> Swap(java.lang.String: name) -> java.lang.Object
  - *ex:* `Swap('foo')` - *for the current thread, swap the input value with the named variable and returned the named variable*
- java.lang.Object -> Swap(java.lang.String: name, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Swap('foo','examplevalue')` - *for the current thread, swap the input value with the named variable and returned the named variable, or return the default value if the named value is not defined.*
- java.lang.Object -> Swap(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc) -> java.lang.Object
  - *ex:* `Swap(NumberNameToString())` - *for the current thread, swap the input value with the named variable and returned the named variable, where the variable name is generated by the provided function.*
- java.lang.Object -> Swap(java.util.function.Function<java.lang.Object,java.lang.Object>: nameFunc, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Swap(NumberNameToString(),'examplevalue')` - *for the current thread, swap the input value with the named variable and returned the named variable, where the variable name is generated by the provided function, or the default value if the named value is not defined.*
- long -> Swap(java.lang.String: name) -> java.lang.Object
  - *ex:* `Swap('foo')` - *for the current thread, swap the input value with the named variable and returned the named variable*
- long -> Swap(java.lang.String: name, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Swap('foo','examplevalue')` - *for the current thread, swap the input value with the named variable and returned the named variable, or return the default value if the named value is not defined.*
- long -> Swap(java.util.function.LongFunction<java.lang.Object>: nameFunc) -> java.lang.Object
  - *ex:* `Swap(NumberNameToString())` - *for the current thread, swap the input value with the named variable and returned the named variable, where the variable name is generated by the provided function.*
- long -> Swap(java.util.function.LongFunction<java.lang.Object>: nameFunc, java.lang.Object: defaultValue) -> java.lang.Object
  - *ex:* `Swap(NumberNameToString(),'examplevalue')` - *for the current thread, swap the input value with the named variable and returned the named variable, where the variable name is generated by the provided function, or the default value if the named value is not defined.*


## UnsetOrLoad

Reads a long variable from the input, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return UNSET.value or a loaded value.

- long -> UnsetOrLoad(double: ratio, java.lang.String: varname) -> java.lang.Object


## UnsetOrPass

Reads a long variable from the thread local variable map, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return UNSET.value or the input value.

- java.lang.Object -> UnsetOrPass(double: ratio, java.lang.String: varname) -> java.lang.Object


