---
title: null functions
weight: 40
---

## NullIfCloseTo

Returns null if the input value is within range of the specified value.

- double -> NullIfCloseTo(double: compareto, double: sigma) -> java.lang.Double


## NullIfEmpty

Yields a null if the input String is empty. Throws an error if the input String is null.

- java.lang.String -> NullIfEmpty() -> java.lang.String


## NullIfEq

Yeilds a null if the input value is equal to the specified value.

- double -> NullIfEq(double: compareto) -> java.lang.Double
- long -> NullIfEq(long: compareto) -> java.lang.Long


## NullIfGe

Yields a null if the input value is greater tha or equal to the specified value.

- double -> NullIfGe(double: compareto) -> java.lang.Double
- long -> NullIfGe(long: compareto) -> java.lang.Long


## NullIfGt

Yields a null if the input value is greater than the specified value.

- double -> NullIfGt(double: compareto) -> java.lang.Double
- long -> NullIfGt(long: compareto) -> java.lang.Long


## NullIfLe

Yields a null if the input value is less than or equal to the specified value.

- double -> NullIfLe(double: compareto) -> java.lang.Double
- long -> NullIfLe(long: compareto) -> java.lang.Long


## NullIfLt

Yields a null if the input value is equal to the specified value.

- double -> NullIfLt(double: compareto) -> java.lang.Double
- long -> NullIfLt(long: compareto) -> java.lang.Long


## NullIfNullOrEmpty

Yield a null value if the input String is either null or empty.

- java.lang.String -> NullIfNullOrEmpty() -> java.lang.String


## NullIfWithin

Yields a null if the input value is within the specified range, inclusive.

- double -> NullIfWithin(double: min, double: max) -> java.lang.Double


## NullOrLoad

Reads a long variable from the input, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return null object or a loaded value.

- long -> NullOrLoad(double: ratio, java.lang.String: varname) -> java.lang.Object


## NullOrPass

Reads a long variable from the thread local variable map, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return a null object or the input value.

- java.lang.Object -> NullOrPass(double: ratio, java.lang.String: varname) -> java.lang.Object


## Unset

Always yields the VALUE.unset value, which signals to any consumers that the value provided should be considered undefined for any operation. This is distinct from functions which return a null, which is considered an actual value to be acted upon. It is deemed an error for any downstream user of this library to do anything with VALUE.unset besides explicitly acting like it wasn't provided. That is the point of VALUE.unset. The purpose of having such a value in this library is to provide a value type to help bridge between functional flows and imperative run-times. Without such a value, it would be difficult to simulate value streams in which some of the time values are set and other times they are not.

- long -> Unset() -> java.lang.Object


## UnsetIfCloseTo

Yield VALUE.unset if the input value is close to the specified value by the sigma threshold. Otherwise, pass the input value along.

- double -> UnsetIfCloseTo(double: compareto, double: sigma) -> java.lang.Object


## UnsetIfEmpty

Yield VALUE.unset if the input String is empty. Throws an error if the input value is null. Otherwise, passes the original value along.

- java.lang.String -> UnsetIfEmpty() -> java.lang.Object


## UnsetIfEq

Yield UNSET.vale if the input value is equal to the specified value. Otherwise, pass the input value along.

- double -> UnsetIfEq(double: compareto) -> java.lang.Double
- long -> UnsetIfEq(long: compareto) -> java.lang.Object


## UnsetIfGe

Yield VALUE.unset if the input value is greater than or equal to the specified value. Otherwise, pass the input value along.

- double -> UnsetIfGe(double: compareto) -> java.lang.Object
- long -> UnsetIfGe(long: compareto) -> java.lang.Object


## UnsetIfGt

Yield UNSET.value if the input value is greater than the specified value. Otherwise, pass the input value along.

- double -> UnsetIfGt(double: compareto) -> java.lang.Object
- long -> UnsetIfGt(long: compareto) -> java.lang.Object


## UnsetIfLe

Yield VALUE.unset if the input value is less than or equal to the specified value. Otherwise, pass the value along.

- double -> UnsetIfLe(double: compareto) -> java.lang.Object
- long -> UnsetIfLe(long: compareto) -> java.lang.Object


## UnsetIfLt

Yield VALUE.unset if the provided value is less than the specified value, otherwise, pass the original value;

- double -> UnsetIfLt(double: compareto) -> java.lang.Object
- long -> UnsetIfLt(long: compareto) -> java.lang.Object


## UnsetIfNullOrEmpty

Yields UNSET.value if the input value is null or empty. Otherwise, passes the original value along.

- java.lang.String -> UnsetIfNullOrEmpty() -> java.lang.Object


## UnsetIfStringEq

Yields UNSET.value if the input value is equal to the specified value. Throws an error if the input value is null. Otherwise, passes the original value along.

- java.lang.String -> UnsetIfStringEq(java.lang.String: compareto) -> java.lang.Object


## UnsetIfStringNe

Yields UNSET.value if the input String is not equal to the specified String value. Throws an error if the input value is null. Otherwise, passes the original value along.

- java.lang.String -> UnsetIfStringNe(java.lang.String: compareto) -> java.lang.Object


## UnsetIfWithin

Yields UNSET.value if the input value is within the specified range, inclusive. Otherwise, passes the original value along.

- double -> UnsetIfWithin(double: min, double: max) -> java.lang.Object


## UnsetOrLoad

Reads a long variable from the input, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return UNSET.value or a loaded value.

- long -> UnsetOrLoad(double: ratio, java.lang.String: varname) -> java.lang.Object


## UnsetOrPass

Reads a long variable from the thread local variable map, hashes and scales it to the unit interval 0.0d - 1.0d, then uses the result to determine whether to return UNSET.value or the input value.

- java.lang.Object -> UnsetOrPass(double: ratio, java.lang.String: varname) -> java.lang.Object


