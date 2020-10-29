# Args Files

An argsfile (Command Line Arguments File) is a simple text file which
contains defaults for command-line arguments. You can use an args
file to contain a set of global defaults that you want to use by
default and automatically.

A command, `--argsfile <path>` is used to specify an args file. You can
use it like an instant import statement in the middle of a command
line. There are three variants of this option. The `--argsfile <path>`
variant will warn you if the file doesn't exist. If you want to load
an argsfile if it exist, but avoid warnings if it doesn't, then use
the `--argsfile-optional <path>` form. If you want to throw an error if
the argsfile doesn't exist, then use the `--argsfile-required <path>`
form.

## Default argsfile

The default args file location is `$NBSTATEDIR/argsfile`.

After the NBSTATEDIR environment variable or default is resolved,
the default argsfile will be searched for in that directory.

`$NBSTATEDIR` is a mechanism for setting and finding the local state
directory for NoSQLBench. It is a search path, delimited by
colons, and allowing Java system properties and shell environment
variables. When the NBSTATEDIR location is first needed,
the paths are checked in order, and the first one found is used.
If one is not found on the filesystem, the first expanded value
is used to create the state directory.
 
 
If the default argsfile is is present, it is loaded by nosqlbench when
it starts even if you don't ask it to. That is, nosqlbench behaves as
 if your first set of command line arguments is
 
    --argsfile-optional "$NBSTATEDIR/argsfile    

Just as with the NBSTATEDIR location, the argsfile can also be used
like a search path. That is, if the value provided is a colon-delimited
set of possible paths, the first one found (after variable expansion)
will be used. If needed, the first expanded path will be used to create
an argsfile when pinning options are used.

## Args file format

An args file simply contains an argument on each line, like this:

    --docker-metrics
    --annotate all
    --grafana-baseurl http://localhost:3000/

## Pinning options

It is possible to pin an option to the default args file by use of the
`--pin` meta-option. This option will take the following command line
argument and add it to the currently active args file. That means, if
you use `--pin --docker-metrics`, then `--docker-metrics` is added to
the args file. If there is an exact duplicate of the same option
and value, then it is skipped, but if the option name is the same
with a different value, then it is added at the end. This allows
for options which may be called multiple times normally.

If the `--pin` option occurs after an explicit use of `--argsfile
<filename>`, then the filename used in this argument is the one that
is modified.

After the `--pin` option, the following argument is taken as any global
option (--with-double-dashes) and any non-option values after it which
are not commands (reserved words)

When the `--pin` option is used, it does not cause the pinned option
to be excluded from the current command line call. The effects of the
pinned option will take place in the current nosqlbench invocation
just as they would without the `--pin`. However, when pinning global
options when there are no commands on the command line, nosqlbench
will not run a scenario, so this form is suitable for setting
arguments.

## Unpinning options.

To reverse the effect of pinning an option, you simply use
 `--unpin ...`.

The behavior of --unpin is slightly different than --pin. Specifically,
an option which is unpinned will be removed from the arg list, and will
not be used in the current invocation of nosqlbench after removal.

Further, you can specify `--unpin --grafana-baseurl` to unpin an option
which normally has an argument, and all instances of that argument will be
removed. If you want to unpin a specific instance of a multi-valued
option, or one that can be specified more than once with different
parameter values, then you must provide the value as well, as in
`--unpin --log-histograms 'histodata.log:.*:1m'`

# Setting defaults, the simple way

To simply set global defaults, you can run nosqlbench with a command
line like this:

    ./nb --pin --docker-metrics-at metricsnode --pin --annotate all
