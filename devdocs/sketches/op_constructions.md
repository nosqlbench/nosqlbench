## Testing Patterns

This is a work in progress...

Drivers have different _creational_ patterns which guide others down a particular path
of development. These are described here to help explain to users what errors they may see
if the mix and match sessions and operations incongruently.

### Implicitly Session-Bound Operations

Some drivers will require that a logical session is _attached_ to an operation for it to work.
This can be done implicitly through a builder pattern that starts with the native driver object.
When the build() method is invoked, the operation that is returned will be connected internally
to the session or state it needs in order to execute successfully.

__Implicitly Session-Bound Operations operations should originate and execute entirely within the
provided cycle and session provided.__ Any other behavior should be considered a programming error
in the associated driver adapter.

### Explicitly Session-Bound Operations

Other drivers will require that a logical session is _used with_ an operation which was created
within that session, without providing any simple way to keep them together automatically.
In these cases, using an operation with a session which it was not intended to be used with can
have undefined (at worst) results and (slightly better) throw an error.

__Explicitly Session-Bound Operations should explicitly retain the space or driver instance needed
to execute correctly by the driver adapter.__ Any other behavior should be considered a programming
error in the associated driver adapter.

### Explicitly-And-Implicitly Session-Bound Operations

There are some drivers which will use a root driver object to create an associated operation, and
then require the user to apply the operation explicitly with the help of that original driver
object. This is the best and worst of both worlds. On one hand, you may have some safeties to
prevent invalid usage of the operation, but on the other hand, why make the user worry about this?

### Session-Agnostic Operations

Some drivers will allow an operations, once defined, to be executed on any compatible client or
driver.

### Independent Operations

It is quite possible to create operations which do not need any explicitly associated driver or
similar object. These provide the simplest user experience, but may also need robust error
handling behavior built-in which does reasonable things for a user when the unexpected occurs.

