# NoSQLBench Driver Standards

This is a work in progress...

This is the document to read if you want to know if your NoSQLBench driver is complete.
Within this document, the phrase `conformant` will be taken to mean that a driver or feature
is implemented according to the design intent and standards of the NoSQLBench driver API.

While it may be possible to partially implement a driver for basic use, following the guidelines
in this document will ensure that contributed drivers for NoSQLBench work in a familiar and
reliable way for users from one driver to another.

Over time, the standards in this guide will be programmatically enforced by the NoSQLBench
driver API.

## Metrics

At a minimum, a conformant driver should provide the following metrics:

- bind (timer) - A timer around the code that prepares an executable form of a statement
- execute (timer) - A timer around the code that submits work to a native driver
- result (timer) - A timer around the code that awaits and processes results from a native driver. This
  timer should be included around all operations, successful ones and errors too.
- result-success (timer) - A timer around the code that awaits and processes results from a native driver.
  This timer should only be updated for successful operations.
- errorcounts-... (counters)- Each uniquely named exception or error type that is known to the native driver
  should be counted.
- tries (histogram) - The number of tries for a given operation. This number is incremented before each
  execution of a native operation, and when the result timer is updated, this value should be updated
  as well (for all operations). This includes errored operations.

## Error Handling

Users often want to control what level of sensitivity their tests have to errors. Testing requirements
vary from the basic "shutdown the test when any error occurs" to the more advanced "tell me when the
error rate exceeds some threshold", and so on.

Configurable error handling is essential.

## Naming Conventions

## Documentation

Each activity is required to have a set of markdown documentation in its resource directory.
The name of the driver should also be used as the name of the documentation for that driver.

Additional documentation can be added beyond this file. However, all documentation for a given driver
must start with the drivers name and a hyphen.

## Named Scenarios

Confirmant driver implementations should come with one or more examples of a workload under the activities directory path.

Complete driver implementations should also come with a set of eaxmples under the examples directory path.

## Examples

## Testing and Docs

Testing Examples in Docs vs Testing Doc Examples, practices and intentions

## Usage of the Op Template

Structural variations and conventions.
