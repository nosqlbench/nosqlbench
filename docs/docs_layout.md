# NoSQLBench Documentation Layout

This doc defines the intentional layout of the core docs of the nosqlbench project.

There are multiple sources of docs:
* The built-in markdown files which accompany various SPI and modular types of the nb (nosqlbench) project:
  * bundled apps, implemented as io.nosqlbench.nb.api.apps.BundledApp
  * driver adapters, implemented as DriverAdapter
* Programmatically documented types, using annotations, for example:
  * expression functions, implemented in ExprFunctionProvider with associated io.nosqlbench.nb.api.expr.annotations
  * binding functions, implemented as io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper, with io.nosqlbench.virtdata.api.annotations
    * including: associated auto-generated io.nosqlbench.virtdata.api.processors.DocFuncData docs classes, and runtime discovery and traversal utils
* Original hand-written documentation from the doc site, cached at local/nosqlbench-build-docs in the site/content directory.
  * This is in zola-compatible format, but that is not important for this
* Living examples of docs which are tested by unit tests like UniformWorkloadSpecificationTest

# Docs Location
The set of maintained docs shall be comprised of all the markdown files an structure in the docs directory in addition to all the modular, programmatic, or other docs sources which are associated with implementations in the repo.

# Docs Principles
The principles below should apply to how the docs are maintained and used.

## Live Docs
Some docs, like the Uniform Workload Specification are a living form of docs which are also exercised by unit tests during build.
This is a key principle for ensuring that all examples are meaningful and valid.

Live docs should be maintained and should continue to work during builds.

## Modular Docs
There are also certain rules about how docs are intended to be bundled closely with the thing they document.
A good example of this is where each DriverAdapter is required to have an associated markdown file in its module or a build error is thrown.

Modular docs should be maintained and should continue to work during builds.

## Labeled Docs
Each markdown file should contain a front matter section in yaml that provides tags to identify it in a taxonomic way.

Dimensional labels and tags should be added using a consistent set of terms or categories so that all docs can be related to a set of cohesive concepts.

## Linked Docs
The docs should have valid relative links between them.

## Compatible Docs
The docs should be maintained in markdown format using commonmark and github compatible syntax.

## Structured Docs
The docs should be organized in the Diátaxis framework structure.

## Projected Docs
It should be possible to take the set of maintained docs, including those which are live example docs, annotated methods and classes, and other sources, and through a programmatic organization process, assemble them into a well-structured copy of the docs, with the metadata in the from matter being used to apply topological layout to taxonomic or other cues for consistent organization.
