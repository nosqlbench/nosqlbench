---
title: Scripting Environment
weight: 3
---

# Scripting Environment

The ability to write open-ended testing simulations is provided in
EngineBlock by means of a scripted runtime, where each scenario is
driven from a control script that can do anything the user wants.

## Dynamic Parameters

Some configuration parameters of activities are designed to be
assignable while a workload is running. This makes things like
threads, rates, and other workload dynamics pseudo real-time.
The internal APIs work with the scripting environment to expose
these parameters directly to scenario scripts.

## Scripting Automatons

When a NoSQLBench scenario is running, it is under the control of a
single-threaded script. Each activity that is started by this script
is run within its own threadpool, asynchronously.

The control script has executive control of the activities, as well
as full visibility into the metrics that are provided by each activity.
The way these two parts of the runtime meet is through the service
objects which are installed into the scripting runtime. These service
objects provide a named access point for each running activity and its
metrics.

This means that the scenario script can do something simple, like start
activities and wait for them to complete, OR, it can do something
more sophisticated like dynamically and interative scrutinize the metrics
and make realtime adjustments to the workload while it runs.

## Analysis Methods

Scripting automatons that do feedback-oriented analysis of a target system
are called analysis methods in NoSQLBench. We have prototypes a couple of
these already, but there is nothing keeping the adventurous from coming up
with their own.

## Command Line Scripting

The command line has the form of basic test commands and parameters.
These command get converted directly into scenario control script
in the order they appear. The user can choose whether to stay in
high level executive mode, with simple commands like "run workload=...",
or to drop down directly into script design. They can look at the
equivalent script for any command line by running --show-script.
If you take the script that is dumped to console and run it, it should
do exactly the same thing as if you hadn't even looked at it and just
the standard commands.

There are even ways to combine script fragments, full commands, and calls
to scripts on the command line. Since each variant is merely a way of
constructing scenario script, they all get composited together before
the scenario script is run.

New introductions to NoSQLBench should focus on the command line. Once
a user is familiar with this, it is up to them whether to tap into the
deeper functionality. If they don't need to know about scenario scripting,
then they shouldn't have to learn about it to be effective.

## Compared to DSLs

Other tools may claim that their DSL makes scenario "simulation" easier.
In practice, any DSL is generally dependent on a development tool to
lay the language out in front of a user in a fluent way. This means that
DSLs are almost always developer-targeted tools, and mostly useless for
casual users who don't want to break out an IDE.

One of the things a DSL proponent may tell you is that it tells you
"all the things you can do!". This is de-facto the same thing as it
telling you "all the things you can't do" because it's not part of the
DSL. This is not a win for the user. For DSL-based systems, the user
has to use the DSL whether or not it enhances their creative control,
while in fact, most DSL aren't rich enough to do much that is interesting
from a simulation perspective.

In NoSQLBench, we don't force the user to use the programming abstractions
except at a very surface level -- the CLI. It is up to the user whether
or not to open the secret access panel for the more advance functionality.
If they decide to do this, we give them a commodity language (ECMAScript),
and we wire it into all the things they were already using. We don't take
away their expressivity by telling them what they can't do. This way,
users can pick their level of investment and reward as best fits thir individual
needs, as it should be.

## Scripting Extensions

Also mentioned under the section on modularity, it is relatively easy
for a developer to add their own scripting extensions into NoSQLBench.
