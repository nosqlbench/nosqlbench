---
title: collection functions
weight: 40
---

Collection functions allow you to construct Java Lists, Maps or Sets.
These functions often take the form of a higher-order function, where
the inner function definitions are called to determine the size of
the collection, the individual values to be added, etc.

For each type of collection, there exists multiple forms which allow you to control how the provided
function arguments are used to set the values into the collection.

## Sized or Pair-wise functions

Any function in this category with `Sized` occuring in its name must be initialized with a sizing
function as an argument. For example, `ListSized(Mod(5),NumberNameToString())` will create a list
which is sized by the first function -- a list between 0 and 4 elements in this case. With an input
value of `3L`, the resulting List will contain 3 elements. With an input of `7L`, it will contain 2
elements.

Alternately, when a function does *not* contain `Sized` in its name, the arguments provided are used
as pair-wise mapping functions to the elements in the resulting collection.

Simply put, a Sized function will always require a sizing function as the first argument.

## Stepped or Hashed or Same

Any function in this category which contains `Stepped` in its name will automatically increment the
input value used for each element in the collection. For example
`ListStepped(NumberNameToString(),NumberNameToString())` will always creat a two-element List, but
the inputs to the provided functions will be i+0, i+1, where i is the input value to the ListStepped
function.

Alternately, any function in this category which contains `Hashed` in its name will automatically
hash the input value used for each element. This is useful when you want to create values within a
collection that vary significantly with respect of their common seed value. For example,
`ListHashed(NumberNameToString(),NumberNameToString(),NumberNameToString())` will always provide a
three element List with values that are not obviously related to each other. For each additional
element added to the collection, the previous input is hashed, so there is a relationship, but it
will not be obvious nor discernable for most testing purposes.

If neither `Stepped` nor `Hashed` occurs in the function name, then every element function
gets the exact value given to the main function.

## Overview of functions

All of the useful collection binding functions follow the same basic patterns above.

###  List Functions**

|               |  Same Input | Stepped Input| Hashed Input |
|---------------|----------|--------|----------|
| **Pair-wise** | ListFunctions(...)             | ListStepped(...)       | ListHashed(...)      |
| **Sized**     | ListSized(...)                 | ListSizedStepped(...)  | ListSizedHashed(...) |

The name `ListFunctions(...)` was chosen to avoid clashing with the existing `List(...)` function.

### Set Functions

The values produced by the provided element functions for Sets do not check for duplicate values.
This means that you must ensure that your element functions yield distinct values to insert into
the collection as it is being built if you want to have a particular cardinality of values in your
collection.  Overwrites are allowed, although they may not be intended in most cases.

|               | Same Input                    | Stepped Input        | Hashed Input        |
| ---           | ---                           | ---                  | ---                 |
| **Pair-wise** | SetFunctions(...)             | SetStepped(...)      | SetHashed(...)      |
| **Sized**     | SetSized(...)                 | SetSizedStepped(...) | SetSizedHashed(...) |

The name `SetFunctions(...)` was chosen to avoid clashing with the existing `Set(...)` function.

### Map Functions

The values produced by the provided element functions for Maps do not check for duplicate values.
This means that you must ensure that your element functions yield distinct keys to insert into
the collection as it is being built if you want to have a particular cardinality of values in your
collection.  Overwrites are allowed, although they may not be intended in most cases.

|               | Same Input                    | Stepped Input        | Hashed Input        |
| ---           | ---                           | ---                  | ---                 |
| **Pair-wise** | MapFunctions(...)             | MapStepped(...)      | MapHashed(...)      |
| **Sized**     | MapSized(...)                 | MapSizedStepped(...) | MapSizedHashed(...) |

The name `MapFunctions(...)` was chosen to avoid clashing with the existing `Map(...)` function.

For the key and value functions provided to a Map function, they are taken as even-odd pairs (starting
 at zero). For sized functions, the last defined key function will be used for elements past
 the size of the _key_ functions provided. The same is true for the value functions. For example,
 a call to `MapSized(3,f(...),g(...),h(...))` will use `f(...)` and `g(...)` for the first key and value,
 but from that point forward will use `h(...)` for all keys and `g(...)` for all values.
