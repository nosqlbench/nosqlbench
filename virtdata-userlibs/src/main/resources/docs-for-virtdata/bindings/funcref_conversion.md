---
title: conversion functions
weight: 30
---

## DigestToByteBuffer

Computes the digest of the ByteBuffer on input and stores it in the output ByteBuffer. The digestTypes available are:
MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, SHA3-224, SHA3-256, SHA3-384, SHA3-512.

* java.nio.ByteBuffer -> DigestToByteBuffer(java.lang.String: digestType) -> java.nio.ByteBuffer
* long -> DigestToByteBuffer(java.lang.String: digestType) -> java.nio.ByteBuffer


## Flow

Combine functions into one.

This function allows you to combine multiple other functions into one. This is often useful
for constructing more sophisticated recipes, when you don't have the ability to use
control flow or non-functional forms.

The functions will be stitched together using the same logic that VirtData uses when
combining flows outside functions. That said, if the functions selected are not the right ones,
then it is possible to end up with the wrong data type at the end. To remedy this, be sure
to add input and output qualifiers, like `long->` or `->String` where
appropriate, to ensure that VirtData selects the right functions within the flow.

- long -> Flow(java.lang.Object[]...: funcs) -> java.lang.Object


## Format

Apply the Java String.format method to an incoming object. @see
[Java 15 String.format(...) javadoc](https://docs.oracle.com/en/java
/javase/14/docs/api/java.base/java/util/Formatter.html#syntax) Note:
This function can often be quite slow, so more direct methods are generally preferrable.

- java.lang.Object -> Format(java.lang.String: format) -> java.lang.String
  - *ex:* `Format('Y')` - *Yield the formatted year from a Java date object.*


## LongToByte

Convert the input long value to a byte, with negative values masked away.

- long -> LongToByte() -> java.lang.Byte


## LongToShort

Convert the input value from long to short.

- long -> LongToShort() -> java.lang.Short


## MD5HexString

Computes the MD5 digest of the byte image of the input long, and returns it in hexadecimal String form.

- long -> MD5HexString() -> java.lang.String
  - *ex:* `MD5String()` - *Convert a long input to an md5 digest over its bytes, and then to a hexadecimal string.*


## ModuloToBigDecimal

Return a {@code BigDecimal} value as the result of modulo division with the specified divisor.

- long -> ModuloToBigDecimal() -> java.math.BigDecimal
- long -> ModuloToBigDecimal(long: modulo) -> java.math.BigDecimal


## ModuloToBigInt

Return a {@code BigInteger} value as the result of modulo division with the specified divisor.

- long -> ModuloToBigInt() -> java.math.BigInteger
- long -> ModuloToBigInt(long: modulo) -> java.math.BigInteger


## ModuloToBoolean

Return a boolean value as the result of modulo division with the specified divisor.

- long -> ModuloToBoolean() -> java.lang.Boolean


## ModuloToByte

Return a byte value as the result of modulo division with the specified divisor.

- long -> ModuloToByte(long: modulo) -> java.lang.Byte


## ModuloToShort

Return a boolean value as the result of modulo division with the specified divisor.

- long -> ModuloToShort(long: modulo) -> java.lang.Short


## StringDateWrapper

This function wraps an epoch time in milliseconds into a String as specified in the format. The valid formatters are documented at @see [DateTimeFormat API Docs](https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html)

- long -> StringDateWrapper(java.lang.String: format) -> java.lang.String


## ToBase64String

Computes the Base64 representation of the byte image of the input long.

- long -> ToBase64String() -> java.lang.String
  - *ex:* `ToBase64String()` - *Convert the bytes of a long input into a base64 String*
- java.lang.String -> ToBase64String() -> java.lang.String
  - *ex:* `ToBase64String()` - *encode any input as Base64*


## ToBigInt

Convert the input value to a {@code BigInteger}

- long -> ToBigInt() -> java.math.BigInteger


## ToBoolean

Convert the input value to a {@code boolean}

- java.lang.Double -> ToBoolean() -> java.lang.Boolean
- java.lang.Float -> ToBoolean() -> java.lang.Boolean
- java.lang.Integer -> ToBoolean() -> java.lang.Boolean
- long -> ToBoolean() -> java.lang.Boolean


## ToByte

Convert the input value to a {@code Byte}.

- double -> ToByte() -> java.lang.Byte
- double -> ToByte(int: modulo) -> java.lang.Byte
- java.lang.Float -> ToByte() -> java.lang.Byte
- java.lang.Float -> ToByte(int: modulo) -> java.lang.Byte
- int -> ToByte() -> java.lang.Byte
- int -> ToByte(int: modulo) -> java.lang.Byte
- long -> ToByte() -> java.lang.Byte
- long -> ToByte(int: modulo) -> java.lang.Byte
- java.lang.Short -> ToByte(int: scale) -> java.lang.Byte
- java.lang.Short -> ToByte() -> java.lang.Byte
- java.lang.String -> ToByte() -> java.lang.Byte


## ToByteBuffer

Convert the input value to a {@code ByteBuffer}

- double -> ToByteBuffer() -> java.nio.ByteBuffer
- java.lang.Float -> ToByteBuffer() -> java.nio.ByteBuffer
- int -> ToByteBuffer() -> java.nio.ByteBuffer
- long -> ToByteBuffer() -> java.nio.ByteBuffer
- java.lang.Short -> ToByteBuffer() -> java.nio.ByteBuffer
- java.lang.String -> ToByteBuffer() -> java.nio.ByteBuffer


## ToDouble

Convert the input value to a double.

- long -> ToDouble() -> double


## ToFloat

Convert the input value into a float.

- double -> ToFloat(double: scale) -> java.lang.Float
- double -> ToFloat() -> java.lang.Float
- int -> ToFloat(int: scale) -> java.lang.Float
- int -> ToFloat() -> java.lang.Float
- long -> ToFloat(long: scale) -> java.lang.Float
- long -> ToFloat() -> java.lang.Float
- java.lang.Short -> ToFloat() -> java.lang.Float
- java.lang.String -> ToFloat() -> java.lang.Float


## ToHexString

Converts the input ByteBuffer to a hexadecimal String.

- java.nio.ByteBuffer -> ToHexString() -> java.lang.String
- java.nio.ByteBuffer -> ToHexString(boolean: useUpperCase) -> java.lang.String
- long -> ToHexString() -> java.lang.String


## ToInetAddress

Convert the input value to a {@code java.net.InetAddress}

- long -> ToInetAddress() -> java.net.InetAddress


## ToInt

Convert the input value to an int with long modulus remainder. If the scale is chosen, then the value is wrapped at this value. Otherwise, {@link Integer#MAX_VALUE} is used.

- double -> ToInt(int: scale) -> int
- double -> ToInt() -> int
- java.lang.Double -> ToInt(int: scale) -> java.lang.Integer
- java.lang.Double -> ToInt() -> java.lang.Integer
- long -> ToInt(int: scale) -> int
  - *ex:* `ToInt(1000)` - *converts a long input value to an int between 0 and 999, inclusive*
- long -> ToInt() -> int
  - *ex:* `ToInt()` - *converts a long input value to an int between 0 and 2147483647, inclusive *
- java.lang.String -> ToInt() -> java.lang.Integer
- long -> ToInt() -> int
- java.lang.Object -> ToInt() -> java.lang.Integer


## ToLong

Convert the input value to a long.

- double -> ToLong(long: scale) -> long
- double -> ToLong() -> long
- java.lang.Float -> ToLong(long: scale) -> java.lang.Long
- java.lang.Float -> ToLong() -> java.lang.Long
- java.lang.String -> ToLong() -> java.lang.Long


## ToMD5ByteBuffer

Converts the byte image of the input long to a MD5 digest in ByteBuffer form.

- long -> ToMD5ByteBuffer() -> java.nio.ByteBuffer
  - *ex:* `MD5ByteBuffer()` - *convert the a input to an md5 digest of its bytes*


## ToShort

Convert the input value to a short.

- double -> ToShort() -> java.lang.Short
- double -> ToShort(int: modulo) -> java.lang.Short
- java.lang.Float -> ToShort() -> java.lang.Short
- java.lang.Float -> ToShort(int: modulo) -> java.lang.Short
- int -> ToShort() -> java.lang.Short
- int -> ToShort(int: scale) -> java.lang.Short
- long -> ToShort() -> java.lang.Short
- long -> ToShort(int: wrapat) -> java.lang.Short
  - *notes:* This form allows for limiting the short values at a lower limit than Short.MAX_VALUE.
@param wrapat The maximum value to return.
- java.lang.String -> ToShort() -> java.lang.Short


## ToString

Converts the input to the most obvious string representation with String.valueOf(...). Forms which accept a function will evaluate that function first and then apply String.valueOf() to the result.

- double -> ToString() -> java.lang.String
  - *ex:* `ToString()` - *map the double input value to a String*
- double -> ToString(java.util.function.DoubleUnaryOperator: df) -> java.lang.String
  - *ex:* `ToString(Add(5.7D))` - *map the double input value X to X+5.7D and then to a String*
- double -> ToString(java.util.function.DoubleFunction<java.lang.Double>: df) -> java.lang.String
- double -> ToString(java.util.function.Function<java.lang.Double,java.lang.Double>: df) -> java.lang.String
- java.lang.Float -> ToString() -> java.lang.String
- int -> ToString() -> java.lang.String
- long -> ToString() -> java.lang.String
- long -> ToString(java.util.function.LongUnaryOperator: f) -> java.lang.String
- long -> ToString(java.util.function.LongFunction<?>: f) -> java.lang.String
- long -> ToString(java.util.function.Function<java.lang.Long,?>: f) -> java.lang.String
- long -> ToString(java.util.function.LongToIntFunction: f) -> java.lang.String
- long -> ToString(java.util.function.LongToDoubleFunction: f) -> java.lang.String
- long -> ToString(io.virtdata.libbasics.shared.from_long.to_byte.LongToByte: f) -> java.lang.String
- long -> ToString(io.virtdata.libbasics.shared.from_long.to_short.LongToShort: f) -> java.lang.String
- java.lang.Object -> ToString() -> java.lang.String


