---
title: Project Structure
description: Project-Structure
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 40
---

NoSQLBench is a large project. It has a few basic layers of functionality which nest together
like a system of modules and interfaces:


* Core APIs
  * Annotations and Processors
* Core Runtime
* Virtual Data Subsystem
* Extensions and Plugins
* Driver Adapters

## SPI and Modules

Nearly all of the functionality in NoSQLBench is wired together at runtime through a service 
discovery mechanism. This allows modules to be bundled within the project and packaged together 
as needed. At runtime, when a particular set of components is called up to be used together, the 
runtime uses Java's SPI mechanism to discover and map them by a name, called a *selector*.

You will see this throughout the project in the form of a `@Service(...)` annotation. Any 
component withing the code base which needs to be realized at runtime must have this. The 
built-in annotation processors do the packing work to put these into the standard service manifest 
format.

## Driver Adapters

If you are building a driver adapter, you don't need to understand the whole project structure. 
You simply need to implement the DriverAdapter interface. You only need the *adapters-api* 
module as an upstream dependency.

## Insert Module Graph Here

This will be a visual of all the core modules.

## (Java) Package Naming

All of the modules in the project follow a minimum structural standard. The internal package 
names *must* have the prefix `io.nosqlbench.[module-name]` where the module name has its 
non-words replaced with package boundaries.

This means that module named `adapter-diag` lives in a directory under the main project of the 
same name. It has a package named `io.nosqlbench.adapter.diag` in which all of its driver code 
resides.

This makes it easy to know what module any package is part of simply from the name.
