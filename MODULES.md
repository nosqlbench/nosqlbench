## Contributing

We are actively looking for contributors to help make NoSQLBench better.
This is an ambitious project that is just finding its stride. If you want
to be part of the next chapter in NoSQLBench development please look at
[CONTRIBUTING](CONTRIBUTING.md) for ideas, and jump in where you feel comfortable.

All contributors are expected to abide by the [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md).

## License

All of the code in this repository is licensed under the APL version 2. If you contribute
to this project, then you must agree to license all of your construbutions under
this license.

# Project Structure

NoSQLBench is an ambitious project. It has lots of advanced functionality
built-in that you won't find in any other performance testing tool. That means that
there is also a lot of code.

To make a testing runtime that can be expanded according to different protocols and
scripting requirements, modularity is absolutely essential. Thus, you will find many
nodules, each focusing on one specific type of responsibility. The build system
bundles all the modules together into a cohesive whole using SPI and auto-discovery
mechanisms at run-time.

To make it easier to find the module you are looking for, the following strict
naming conventions have been adopted:

- All packages within a module start with `io.nosqlbench`, followed by the module name,
  with hyphens converted to dots. For example, the 'engine-api' module contains
  packages in `io.nosqlbench.engine.api`.
- Modules which implement activity types (high-level protocol drivers) are named `driver-...`.
- Modules which provide procedural data generation support are named `virtdata-...`.
- Modules which provide core runtime logic are named `engine-...`.
- Project-wide maven defaults are contained in the mvn-defaults module.

All runtime packaging and bundling is done in the `nb` module. It produces a Linux binary
  in target/eb. If you are doing testing and need to have a classpath that includes all th
  bundled modules together, then use the `nb` module as your classpath module.


