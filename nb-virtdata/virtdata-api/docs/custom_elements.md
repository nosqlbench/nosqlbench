# Custom Elements (rename to VirtData Config Elements)

## Status

The current progress of custom elements includes:
- ConfigData config type
- a proof of concept for "customConfig" values

The next step should be to introduce ConfigData to elements that support it via one of:
- automatic injection into constructors by parameter type
- Annotation support
- ThreadLocal configuration types
- ConfigAware injection

## Purpose

Functions may sometimes need to access environmental configuration data. When this is the case, it is not usually
reasonable to impose on the user to pass this into a function as initializer data within the siganature of each function
recipe.

Custom elements are a way to support this. A custom element is simply an object that can be injected into the virtdata's
resolver environment before function resolution continues. This is allows any function which may need custom elements to
ask the virtdata environment for them.

## Custom Elements Structure

Custom Elements are provided to callers as a <String,Object> map. Internally, elements may be structured as a list of
maps, so that layers of config can be added with precedence or overrides.

All access to the custom elements service by accessors should require a typed getter, with a type check in the call for
assignability to the target type. It is expected that functions which use these elements will use this typed accessor in
order to assert that:

1. a (potentially) required element is provided
2. it is of the type required for the caller.

## Documentation

It is important to document what every custom element does, and where it will be used. Specifically, functions which use
custom functions must provide additional details for users that specify what names, types, and effects custom elemements
may have on them.

### Functional Impact

Custom elements may change the semantics of function use. Specifically, when the effective value of custom elements
changes, functions will cease to act as pure functions in some way.


## Using Custom Elements

When you implement a function which uses custom elements, the function should *only* access the elements for the
purposes of instance and type checking in the constructor. Some custom elements may trigger additional initialization
logic before a function can ultimately return a value, but when possible, building any cacheable values should be
deferred to a lazy property to be used in the main apply method.

