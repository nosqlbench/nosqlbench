# Developing with NoSQLBench

This is an overview of how to develop within the NoSQLBench ecosystem.
This guide

## Welcome All

The NoSQLBench project welcomes all contributors, so long as you are
willing to abide by the code
of [CODE OF CONDUCT](../../CODE_OF_CONDUCT.md). Further, you must be
willing to license your contributions under the
[APLv2](../../LICENSE.txt).

## Basics

Herein, and in other docs, you may see NoSQLBench abbreviated as NB
(the project) or nb (the command).

This dev guide covers detailed topics which can help you get bootstrapped
as a contributor. It does not try to cover all the topics which are
included in the main documentation. What you see in this guide wil assume
you have some familiarity with NoSQLBench as a user.

## Topics

- [Design Principles](design_principles.md) - This is a short treatise
  that explains the architectural principles behind NoSQLBench.
- [Project Structure](project_structure.md) - An explanation of the module
  naming and dependency structure.
- [Dynamic Endpoints](apis/dynamic_endpoints.md) - An introduction to the
  appserver mode and how to write new webservices for it.
- [Driver How-To](drivers/README.md) - An introduction for new driver
  designers, including API guides and driver standards.
- [NBUI Development](nbui/README.md) - An introduction to the UI
  subsystem (NBUI), and tricks for effective development.
- [Java versions](java_versions.md) - A map of where java versions are
  configured in the project.
- [Scripting Extensions](scripting_extensions.md) - A guide on what
  scripting extensions are, how they are used, and how to build one.
- [Documentation Sources](nb_docs.md) - Where docs are located
- [Adding Scenarios](adding_scenarios.md) - How to add built-in scenarios

### API Naming

In general, internal APIs which are suggested for use throughout
NoSQLBench will start with the *NB* prefix, such as NBIO, or NBErrors.
This makes these APIs easier to find and reference when building new
drivers or core functionality.

New contributors should familiarize themselves with these APIs so that
they can employ them when appropriate. These APIs should also have
sufficient documenation here and in Javadoc to explain their usage.

### API Guides

- [NBIO - File and System IO](nbio_api.md) - A standard way to get files
  and other resources from a running NB process.

