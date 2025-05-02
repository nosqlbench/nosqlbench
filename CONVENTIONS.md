These conventions are to be followed by any person or agent acting on this project. These directions
apply to all files, directories, modules, or sub-projects contained within this directory.

# Basic Requirements and Conventions

* Do not add convenience methods when a fully-parameterized method signature is sufficient.
    * This includes additional constructors.

## Java Language Specification

* Use the latest supported Java language features and assume the latest Java LTS bytecode version.
* Do not use implied classes.
* Do not use the old format for javadoc.
* Variables that are used in closures should be effectively final.
* Unsafe methods are not allowed.
* Using JVM hacks like modifying the accessibility of a method is not allowed.

* Use Java language spec 23 and newer.

## Libraries and Dependencies

* Do NOT add guava or guava-dependent libraries to a project unless it already has them.
* Assume that maven is being used.
* Assume that the latest version of any dependency is being used. Do not use old or stale
  information when determining how to use a version of a library dependency.

## Functional Style

* For methods which compute raw values or well-defined value types and records, early exit is
  allowed. Early exit is disallowed for any other function returning a value.
* When reading requirements, do not assume that special features which have not been asked for
  should be added. You may suggest these and confirm if more sweeping changes are appropriate, but
  these must be confirmed before being added to a change set.
* Conversely, I specify requirements in an abstract or high-level way, I want you to envision the
  form of a solution according to modern and sustainable coding practices, without strict limits on
  requirements or edge cases. I want you to do this before writing code, so that I may review the
  form and function of the design sketch first.

# Documentation

* When generating new code, always document it with javadoc using the modern markdown javadoc format
  with three triple slashes starting each line.
* The format of markdown java should be idiomatic Java.
* Where possible, include diagrammatic documentation with unicode drawing symbols in a fenced code
  block in this documentation.

# Modifying Existing Code

* When I tell you to update a class or method, it means to update it with respect to other recent
  code changes. You may suggest other changes to make updates better, but the focus of updating
  should be to make the specific request work with the existing classes first.

# Requirements

* When you see any comment starting with the word REQUIREMENT, interpret it as a hard and absolute
  requirement. Do not implement any other code or test which violates the requirement. You may ask
  me to relax or change the requirement if it is necessary.
* Requirements for each section of the project shall be kept in the package level in the
  package-info.java javadocs. The scope of changes should be tightly coupled to these file and the
  requirements therein.
* You should always read the contents of package-info files for any resources you are working with.
  They include essential details about requirements.
* Changes to multiple packages may be done so long as the requirements to make different packages or
  modules work together are understood first.
* Pay special attention to documented requirements with heading REQUIREMENTS or similar. Any docs
  which fall logically within the scope of REQUIREMENTS heading or similar should be considered
  fully as requirements too.
* Do not remove my javadocs from package-info.java files. You can add more to these as long as it is
  marked as ADDITIONAL REQUIREMENTS, IMPLEMENTATION NOTES, or AMBIGUOUS REQUIREMENTS. You can add
  these sections where appropriate, just do not remove any of my docs under the basic REQUIREMENTS
  headings.
    * ADDITIONAL REQUIREMENTS is where you should put your understanding of the requirements as I
      have provided them. If I give you chat instructions, this is where additional requirements
      from those instructions can be stored.
    * IMPLEMENTATION NOTES is where you should put your explanation for non-obvious implementation
      details, or assumptions which should be asserted for clarity.
    * AMBIGUOUS REQUIREMENTS is where you should put details about requirements which were not
      distinctly well written, or which could be easily interpreted in a number of ways.

## Concurrency

* Use modern java concurrency types when working on multi-threaded code. Use virtual threads by
  default.
* When buffers or threads or other resources are used which could cause a resource leak, ensure that
  these resources are tracked consistently in non-blocking data structures like atomic refs or
  longs. Care should be taken to ensure that resources are properly managed and released when they
  are no longer needed.
* When implementing concurrent solutions, never leave the non-concurrent path in place. The
  non-concurrent path should simply be the same as the concurrent path, just with a concurrency of
  1.

## Instrumentation

* Log4j2 should be used by default, with no accomodations made for slf4j. Slf4j is banal in its
  present-day form.
* When Log4j2 is not provided, you can create a basic logging facade which emulates the log4j2 API.
  Do NOT include slf4j.

# Testing

* Do not add special code to non-test classes to make a test pass. This is in violation of basic
  testing principles. You should never have code paths which are only activated during tests, unless
  the purpose of doing this is to ensure that other code paths are used during tests which would
  otherwise be missed. When doing this, the special code paths need to be very well documented.
* Use the latest junit jupiter API and assertions for unit tests.
* Implement test for every requirement. Strive for good coverage in unit test.
* Favor building native test fixtures in the code base rather than pulling in Mockito for tests.
  Only use mockito for things which are challenging to test because fixtures require remote system
  views, like wire protocols or other complex testing scenarios. Avoid including mockito in general
  unless absolutely necessary.
* Changing non-test code to make a test pass is only valid if it constitutes a design change or
  implementation fix. Any major changes to make a test pass should be confirmed with me first.
* When a file needs to be created as a testing asset, it should always go under the
  src/test/resource path of the respective module. Any tests associated with it should presume that
  as the logical root of tests. Access to these files should be granted via the system class loader
  as a resource stream during tests.

