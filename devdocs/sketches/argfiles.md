# Args Files

An argsfile (Command Line Arguments File) is a simple text file which
 contains defaults for command-line arguments. You can use an args
  file to contain a set of global defaults that you want to use by
   default and automatically.

A command, `--argsfile <path>` is used to specify an args file. You can
 use it like an instant import statement in the middle of a command
  line. Notice that this option uses only a single dash. This
   distinguishes  the argsfile options from the others in general. These are meta
   options which can modify how options are loaded, so it is important
    that the look distinctive from everything else.

## Default argsfile

The default args file location is `$HOME/.nosqlbench/argsfile`. If this
file is present, it is loaded by nosqlbench when it starts even if you
 don't ask it to. That is, nosqlbench behaves as if your first set of
  command line arguments is `--argsfile "$HOME/.nosqlbench/argsfile
  `. However, unlike when you specify `--argsfile ...` explicitly on
   your command line, this form will not throw an error if the file is
    missing. This means that when you explicitly ask for an args file
     to be loaded, and it does not exist, an error is thrown. If you
      don't ask for it, but the default one does exist, it is loaded
       automatically before other options are processed.

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

As a special case, if the `--pin` is the last option of

## Unpinning options.

To reverse the effect of pinning an option, you simply use
 `--unpin ...`.

The behavior of --unpin is slightly different than --pin. Specifically,
an option which is unpinned will be removed from the arg list, and will
 not be used in the current invocation of nosqlbench after removal.

Further, you can specify `--unpin --grafana-baseurl` to unpin an option
 which
 normally has an argument, and all instances of that argument will be
  removed. If you want to unpin a specific instance of a multi-valued
   option, or one that can be specified more than once with different
    parameter values, then you must provide the value as well, as in
     `--unpin --log-histograms 'histodata.log:.*:1m'`

# Setting defaults, the simple way

To simply set global defaults, you can run nosqlbench with a command
 line like this:

   ./nb --pin --docker-metrics-at metricsnode --pin --annotate all
