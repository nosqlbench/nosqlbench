# External Bindings

NOTE: This is a sketch: a work in progress that is incomplete at this
 time. Feedback is welcome.


A binding which is part of any named scenario available to the runtime
should be usable in any scenario without having to define them again.

These are simply known as _external bindings_. This includes bindings
from every named scenario at the root level. The syntax for using external
bindings is simply the double curly brace, as in `{{name}}`, with the
name being a valid binding name as you might add to any bindings section.

## Structure

TBD: This section to describe naming, parameters, and the use of basic
 or extended binding properties.

Any accessible binding within any named scenario can be referenced as
a valid external binding. Specifically, this includes any binding that
 is defined at the root level within any local (in
 filesystem) or bundled (in the nb runtime)...

## Scope

TBD: This section to describe the file or resource paths which are
 included by default, and how to extend this otherwise.

## Parameterizing

TBD: This section to describe default parameters, naming them, and then
overriding them in references.

## Documenting

Since YAML doesn't really care much about comments, it is non-trivial to
port documentation features into the existing format. Instead, the
parsing conventions used by statement forms will be adopted in order to
allow some structural elaboration for individual binding recipes.

Examples of proposed formats:

```yaml
bindings:
 b1:
  func: MyTestFunction(p=wow); ToString()
  desc: This is an example of a binding description
  examples:
   - desc: Run my test function with parameter woo
     func: MyTestFunction('woo')
   - desc: Run my test function with parameter whee
     func: MyTestFunction('wee')

```

## Enabling

TBD: This section to describe how to avoid ambiguity when implementing
external binding references.

The syntax for enabling use of external bindings must be mutually
 exclusive with respect to accessing local bindings. This is important
 in order to avoid confusing namespace clashes in cases where it is
 not clear to a user where a binding is coming from. A remedy for
 localizing binding definitions is below under _importing.

Bindings from existing examples should be made available to be pulled in
to any workload.

## Examples

## Globally accessible bindings
```yaml
statements:
 s1: "this is my statement with binding {{id}}
```

## Scoped binding refs

This is an example of using a binding from a specific workload:


```yaml
statements:
 s1: "this is my statement with binding {{filefoo:id}}

```
## Importing Bindings as a new name

Scenario builders will not always want to be required to modify the op
templates when they want to update the bindings. Instead, they may have
meaningful and symbolic names which are familiar, and defined as locally
resolvable bindings. To facilitate this workflow, binding imports should
be supported. This would allow local indirection of a binding to point
to an external binding, to be changed in the bindings as needed without
touching affected statements directly.

Example:

```yaml
bindings:
 myname: {{id}}
```
