# State Directory

In order to maintain state for a NoSQLBench client instance,
a directory is used. The default directory will be auto-created
for you if you do not specify one.

You can always override the state directory location by providing
an option like `--statedir=/tmp/testdir`, or `--statedir=$HOME/.nosqlbench`.

Within the --statedir parameter, the following values will be expanded
automatically:

- $HOME: the current user's home directory
- $USER: the current user's name
- $varname: Any other environment variable

`$NBSTATEDIR` is a mechanism for setting and finding the local state
directory for NoSQLBench. It is a search path, delimited by
the ':' character. It allows both Java system properties
and shell environment variables.

Multiple values may be specified, like with the PATH environment variable,
separated by colons. When none of the directories is found,
the last one in the list will be created. This is based on the convention
that more specific "override" directories are searched first, whereas more
global state is allowed as the fall-through case. Generally users will
want to keep their state in a single and uniform location, like
`$HOME/.nosqlbench`, but they will want the option of localizing configs for
directory-based test management. Thus, the default value for
--statedir is '$NBSTATEDIR:$PWD/.nosqlbench:$HOME/.nosqlbench'.

Once NoSQLBench is past the CLI processing stage, the NBSTATEDIR becomes
a valid system property, and any internal access to environment variables
can also use this property.

NoSQLBench developers should take care to use the
NBEnvironment class as the method to access environment variables.
(TODO: Add this to the developer guide)
