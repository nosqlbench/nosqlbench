# Scripting Extensions

When a scenario runs, it executes a scenario control script. This script *
is* the scenario in programmatic terms. Even when users don't see the
scenario script as such, it is still there as an intermediary between the
user commands and the core runtime of NoSQLBench. As an executive control
layer, the scenario script doesn't directly run operations with drivers,
although it is able to observe and modify activity metrics and parameters
in real time.

## Scripting Environment

The scripting environment is a _javascript_ environment powered by
GraalJS. Initially, a set of service objects is published into this
environment that allows for the scenario, activity parameters, and
activity metrics to all be accessed directly.

## Scripting Extensions

Additional services can be published into the scripting environment when
it is initialized. These services are simply named objects. They are found
using SPI Java mechanism as long as they are published as services with
a `@Service(ScriptingPluginInfo.class)` annotation and
implement `ScriptingPluginInfo<T>`. This API is pretty basic. You can look
at the `ExamplePluginData` class for a clear example for how to build a
scripting extension.

