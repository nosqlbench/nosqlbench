# CATEGORY diagnostics
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


## ToLongFunction

Adapts any compatible {@link FunctionalInterface} type to a LongFunction, for use with higher-order functions, when they require a LongFunction as an argument. Some of the higher-order functions within this library specifically require a LongFunction as an argument, while some of the other functions are provided in semantically equivalent forms with compatible types which can't be converted directly or automatically by Java. In such cases, those types of functions can be wrapped with the forms described here in order to allow the inner and outer functions to work together.

- long -> ToLongFunction(java.util.function.LongUnaryOperator: op) -> java.lang.Object
- long -> ToLongFunction(java.util.function.Function<java.lang.Long,java.lang.Long>: op) -> java.lang.Object
- long -> ToLongFunction(java.util.function.LongToIntFunction: op) -> java.lang.Object
- long -> ToLongFunction(java.util.function.LongToDoubleFunction: op) -> java.lang.Object
- long -> ToLongFunction(java.util.function.LongFunction<?>: func) -> java.lang.Object


## ToLongUnaryOperator

Adapts any compatible {@link FunctionalInterface} type to a LongUnaryOperator, for use with higher-order functions, when they require a LongUnaryOperator as an argument. Some of the higher-order functions within this library specifically require a LongUnaryOperator as an argument, while some of the other functions are provided in semantically equivalent forms with compatible types which can't be converted directly or automatically by Java. In such cases, those types of functions can be wrapped with the forms described here in order to allow the inner and outer functions to work together.

- long -> ToLongUnaryOperator(java.util.function.LongFunction<java.lang.Long>: f) -> long
- long -> ToLongUnaryOperator(java.util.function.Function<java.lang.Long,java.lang.Long>: f) -> long
- long -> ToLongUnaryOperator(java.util.function.LongUnaryOperator: f) -> long


## TypeOf

Yields the class of the resulting type in String form.

- java.lang.Object -> TypeOf() -> java.lang.String


