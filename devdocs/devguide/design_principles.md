# NoSQLBench Design Principles

This is a basic readout on the design principles that inform the core
NoSQLBench design. They can be taken as a set of non-feature requirements
for a serious testing instrument. These principles do not represent the
actual state of NoSQLBench. In fact, some of them may appear more
aspirational than descriptive. Nonetheless, they describe a set of guiding
principles that any contributor should strive to keep in mind as they
build on NoSQLBench.

Ideally, any feature or component will have the best of all of these
attributes. When there is tension between them, designs should always
defer to what a reasonable user would want. This list may adapt and change
over time with more contributions and discussion.

The rest of this guide is rather detailed and delves into design
philosphy.

## Concept Reuse

NoSQLBench aims to be a power tool that is safe for casual users while
also being a trusted tool by serious users. As such, it needs to provide a
set of useful concepts which apply in an axiomatic way across all
NoSQLBench capabilities. This means that once you know what a `cycle` is,
you know that the purpose and usage of it will be consistent no matter
where, when, or how you find it. As such, it is a material part of the
user experience. Concepts like this are often disregarded in tool design.
This puts users as a disadvantage when they want to do anything
non-trivial. Designing robust concepts that are tangible and composable is
one of the hardest aspects of design, and one that is often kicked down
the road. Not attacking this problem directly is one of the deepest forms
of accidental complexity and one that is difficult for a desing to recover
from.

In NoSQLBench, users should take for granted that an understanding of
concepts like _activity_, _cycle_, and _binding_
is directly empowering, and worthy of their attention. Such an
understanding gives them access to additional layers of capability and
expression, and allows all users to speak the same language to each other
with little concern for ambiguity.

## Modularity

Modularity is used throughout NoSQLBench. The reasons are many, but the
practical effect is that once you understand the mechanisms used to
achieve modularity, you can add your own modules of a given type to the
runtime with little fanfare.

Modularity in a project is also a form of applying the Liskov substitution
principle at a higher level. It encourages healthy design boundaries at
key points of re-use. This means that both service providers and service
consumers can depend on the notions of responsibility based design as well
as design-by-contract.

Many runtime elements of NoSQLBench are provided as bundled plugins. Any
time you see a `@Service` annotation, it describes a service entry for
something that is defined by an interface.

## Efficiency

A serious testing tool must be an effective measurement instrument. In the
end all testing tools are merely measurement tools of some sort. With a
load-generating tool, the business of driving a workload can not interfere
with the need to measure accurately. There are multiple paths to achieving
an effective separation of these concerns, but they are all non-trivial. (
load bank orchestration, local resource marshaling, etc.)

One of the greatest simplifications we can provide in a testing tool is to
allow it to do meaningful work without requiring the same level of
orchestration, coordination, and aggregation that many other tools require
for basic workloads. Optimization for performance is absolutely critical
in a testing tool. It doesn't matter what your testing system can do if it
is always the slowest part of the composed system. Thus, many constructs
found within NoSQLBench are purpose built for efficiency. It is "maturely
optimized" because that is what makes NoSQLBench suited for its purpose.

## Flexibility

A testing system which can only do one thing or be used in on particular
way will not be suitable for many common testing tasks. This lack of fit
for many simple purposes would make a system less reusable overall. In the
world of systems testing, requirements are extremely varied, and
circumstances are always changing. Thus, it is uncommon for users to
invest much time in one trick ponies. Such tools have their purposes, and
serve a need at times. However, they do not generally reward users with
incremental knowledge, nor help them to adapt their investment of testing
effort to new or more nuanced needs. As such, they are often used and then
disregarded for further consideration. Even more costly is that these
tools often over-simplify the testing surface area to such a degree that
users are left without even basic answers to the questions they are
asking, or worse, wrong or inaccurate answers to specific questions.

NoSQLBench should provide the building blocks that it has with clarity and
purposes. It should allow the user to recombine and reconfigure these
building blocks as needed for whatever level of testing they require.

### Scalable Experience

For common tasks, or simple usage patterns, it should be possible to use
NoSQLBench quickly. A user should be able to ask a simple question and get
a simple answer without having to debate or wrestle with the system to get
it running. These capabilities should simply be packaged in a pre-baked
form. Each time a user asks more from the testing tool, it should be able
to

### Discoverability

Another element of flexibility, in practical terms, is discoverability. A
tool which can do both simple and sophisticated things is wasted if users
are unable to find and understand the incremental levels of capability it
offers. While the initial surface area of the tool may be intentionally
simplified, there must be some obvious way for a user to opt-in to more
knowledge, more tooling, and more sophistication if and when they want to.

## Interoperability

Where possible, common standard and modern conventions should be used for
interfacing with external systems. This means that most basic tooling in
the modern development ecosystem should be easy to integrate with
NoSQLBench when needed. This includes aspects such as configuration
formats, logging tools, web endpoints, and so on.
