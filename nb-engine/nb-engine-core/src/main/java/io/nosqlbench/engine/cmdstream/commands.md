---
title: "API notes; Commands"
description: "Doc for commands."
tags:
  - nb-engine
  - docs
audience: developer
diataxis: reference
component: core
topic: architecture
status: live
owner: "@nosqlbench/devrel"
generated: false
---

# API notes; Commands

Commands have a very basic type model which facilitates parsing,
configuring, overriding, and basic type checking.

* Cmd <-- This is a configured command, including:
  * CmdType <-- the type model for the command:
    * It's verb name, AKA a CmdType enum value.
    * It's valid parameters, their names and types, aka CmdArgs.
  * CmdArg <-- A named assignment as parameter name = argument value:
    * referencing a valid CmdArg from the command
    * assignment locks for template overrides and restrictions
  * The name of the context in which the command should be run

In short:

* cmd
  * CmdType enum type
    * String name
    * CmdParam[] params
  * arguments map <name, CmdArg>,
    * [param] reference
    * assigned value (the actual argument value to the related parameter)
      * mutability (unlocked | silent locked | verbose locked)
  * activity context name

A command stream consists of a sequence of commands, which is the basis for
setting the logic for a session. NBSession is responsible for taking a sequence of commands
and mapping them into NBInvokableCommands.

Prior to handing a sequence of commands over to a session, the named scenario preprocessor
can evaluate a command line against a specified named scenario template. NBInvokableCommand
resolution does not occur until the session has the command sequence. Thus, the work done by
the named scenario preprocessor is limited to the exposed command types shown above.

