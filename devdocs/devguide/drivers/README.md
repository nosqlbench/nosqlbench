# Driver Development

This section should have everything you need to know to build a successful
driver in NoSQLBench.

If you are new to NoSQLBench concepts, you may want to
read [about drivers](drivers_overview.md).

You'll want to be generally familiar with the current NoSQLBench
[driver standards](driver_standards.md). This document explains what a
well-behaved driver can do. Much of what is requested in driver standards
is directly supported by a set of supporting APIs which are provided to
all driver implementations. It may seem like a high bar to request this of
developers, but without such guidelines, a bar is set nonetheless. The aim
of this is to help define and clarify how to make a
_good_ driver.

## Driver APIs

- [NBOpTemplate](optemplate_api.md) - Op Templating - This is the
  recommended way to map user-level semantics to driver-specific
  operations.
- [NBErrors](nberrors_api.md) - Uniform error handling - A modular and
  configurable error handler which new drivers should use by default.
