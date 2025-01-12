# Command Line Scripting

Each NoSQLBench session runs a series of commands. Some of these commands
may run asynchronously, like a background process, while other commands
run in the foreground. This allows some commands to act as orchestration
controls over others which act more like persistent processes. This occurs
entirely within the NoSQLBench process.

Each command runs within a specific container, which encapsulates configuration,
state, and visibility of results into episodes or partitions. This is useful
for testing scenarios which may have separately named stages or similar.

The primary way to feed commands to a NoSQLBench session is via command line scripting.
All this means is that you are providing a series of valid commands which are
implemented internally by nb5, and which execute in the order presented.

Apart from the activity related commands, all commands are synchronous, as if they
were simply a script. Activities can be *run* synchronously with respect to other
commands, or they can be *start*ed asynchronously and then managed by other commands
afterwards.

## Conventions

Any argument in name=value format serves as a parameter to the script or activity that precedes it.

Commands can be specified one after another in a continuous stream of _command_
_paramname_=_paramvalue_ ... without ambiguity.

## Examples
To run a single activity:
```
${PROG} run <param>=<value> [...]
```

To run multiple activities serially:
```
${PROG} run <param>=<value> [...] run <param>=<value> [...]
```

## Available Commands

To see the list of available commands, simply:
```
${PROG} --list-commands
```

To get help on any one of these commands, simply:
```
${PROG} help <command>
```

These can all be used on the command line in any order. By combining these activity commands on
the command line, you can construct a non-trivial testing scenarios, like control activity
sequencing and concurrency, etc.

## Time & Size Units

Anywhere you need to specify a time, you can use standard unit suffixes,
like "1 day", "1m", etc. Both long names and short names work in any
case. The valid time suffixes are ns, us, ms, s, m, h, d, w, y, for
nanoseconds, microseconds, milliseconds, seconds, minutes, hours,
days, weeks, or years, respectively.

As well, when specifying sizes, standard SI and IEC units apply for suffixes like
KB, KiB. For more details, see [The Wikipedia Page](https://en.wikipedia.org/wiki/Binary_prefix).

