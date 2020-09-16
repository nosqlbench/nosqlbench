---
title: Script Parameters
weight: 20
---

# Script Parameters

When running a script, it is sometimes necessary to pass parameters to it in the same way
that you would for an activity. For example, you might have a scenario script like this:

    # myscript.js
    scenario.run({
      driver:   'stdout',
      workload: 'test.yaml',
      cycles:   '1000'
    });

This is what the script form of starting an activity might look like. It is
simply passing a parameter map with the activity parameters to the scenario controller.

You might invoke it like this:

    nb script myscript

Suppose that you want to allow the user to run such an activity by calling the script directly,
but you also want them to allow them to add their own parameters specifically to the
activity.

NoSQLBench supports this type of flexibility by providing any command-line arguments to the
script as a script object. It is possible to then combine the parameters that a user provides
with any templated parameters in your script. You can make either one the primary, while allowing
the other to backfill values. In either case, it's a matter of using helper methods that are
baked into the command line parameters object.

To force parameters to specific values while allowing user command line parameters to backfill,
use a pattern like this:

    myparams = params.withOverrides(
      {
        myparam: 'myvalue'
      }
    );

This will force 'myparam' to the specified values irrespective of what the user has provided for
that value, and will add the value if it is not present already.

To force _unset_ a parameter, use a similar pattern, but with the value `UNSET` instead:


    myparams = params.withOverrides(
      {
        myparam: 'UNSET'
      }
    );

If this form is used, then any parameter which has already been provided for `myparam` will be
removed from the resulting map.


