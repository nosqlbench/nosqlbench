# Invokable Operations

The op execution interface between native drivers and NoSQLBench
drivers needs to be simplified as well as generalized. These are
generally competing goals, as the simplest interfaces are more often
the more specific ones.  This poses a few challenges in terms of
design, particularly because there are a few execution patterns that
need to be supported:


- Runnable
- Callable
- Functional
- Futures/async
- Driver methods

To clarify the differences, here are some basic examples:

    # runnable
    op.run()
    pulsarOp.run(ctx::close);

    # callable
    R = op.call()

    # functional
    O = op.apply(I)

    # Futures async
    Future<R> f= executor.executeAsync(op)
    R = f.get

    # Driver methods
    cqlActivity.getSession().executeAsync(statement);
    dsegraph.getSession().executeGraphAsync(simpleGraphStatement);
    httpFuture = client.sendAsync(httpOp.request, this.bodyreader);
    jdbcConn.createStatement().execute(sql)
    jmxOp.execute();
    kafkaSequencer.get(cycle).write(cycle);
    mongoResultDoc = activity.getDatabase().runCommand(queryBson, rms.getReadPreference());
    WebDriverVerbs.execute(cycle, commandTemplate, context, dryrun);


ActivityType<A extends Activity>


