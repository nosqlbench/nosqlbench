# Project Structure

nosqlbench is packaged as a
[Maven Reactor](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
project.

## Defaults and Dependencies

Maven reactor projects often confuse developers. In this document, we'll
explain the basic structure of the nosqlbench project and the reasons for
it.

Firstly, there is a parent for each of the modules. In Maven parlance, you
can think of a parent project as a defaults template for projects that
reference it. One of the reasons you would do this is to supply common
build or dependency settings across many maven projects or modules. That
is exactly why we do that here. The 'parent' project for all nosqlbench
modules is aptly named 'mvn-defaults', as that is exactly what we use it
for.

As well, there is a "root" project, which is simply the project at the
project's base directory. It pulls in the modules of the project
explicitly as in:

~~~
    <modules>
        <module>mvn-defaults</module>
        <module>nb</module>
        <module>nb-api</module>
        <module>nb-annotations</module>
        ...
~~~

This means that when you build the root project, it will build all the
modules included, but only after linearizing the build order around the
inter-module dependencies. This is an important detail, as it is often
overlooked that this is the purpose of a reactor-style project.

The dependencies between the modules is not implicit. Each module listed
in the root pom.xml has its own explicit dependencies to other modules in
the project. We could cause them to have a common set of dependencies by
adding those dependencies to the 'mvn-defaults' module, but this would
mostly prevent us from making the dependencies for each as lean and
specific as we like. That is why the dependencies in the mvn-defaults **
module** module are very limited. Only those modules which are to be taken
for granted as dependencies everywhere in the project should be added to
the mvn-defaults module.

The mvn-defaults module contains build, locale, and project identity
settings. You can consider these cross-cutting aspects of all of the other
modules in the project. If you want to put something in the mvn-defaults
module, and it is not strictly cross-cutting across the other modules,
then don't. That's how you keep maven reactor projects functioning and
maintainable.

To be clear, cross-cutting build behavior and per-module dependencies are
two separate axes of build management. Try to keep this in mind when
thinking about modular projects.

## Inter-module Dependencies

Modularity at runtime is enabled via the
[Java ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
.

The engine-core module uses the engine-api module to know the loadable
activity types. Driver implementations use the engine-api module to
implement the loadable activity types. In this way, they both depend on
the engine-api module to provide the common types needed for this to work.
In this way, multiple modules depending on a single API allows them to
speak together using the language of that API. This also means that the
API must be external to each of the participating modules which use it as
a communication layer.

The nb module allows the separate implementations of the core and the
activity type implementations to exist together in the same classpath.
This occurs specifically because the nb module is an _aggregator_
module which depends on multiple other modules. When the artifact for the
nb module is built, it has all these dependencies together. Since the nb
module is built in a way to include all dependencies, a jar is built that
contains them all in one single file.

## Module Naming

A consistent naming scheme is prescribed for all modules within
nosqlbench, as described in [MODULES](../../MODULES.md)

