---
title: Diag ActivityType    
weight: 32
menu:
  main:
    parent: Dev Guide
    identifier: Diag ActivityType
    weight: 12
---

{{< warning >}}
This section is out of date, and will be updated after the next major release
with details on building async activity types.
{{< /warning >}}

If you take all the code chunks from this document and concatenate them
together, you'll have 'diag', one of the in-build activity types for
nosqlbench.

All activity types are annotated for inclusion in META-INF/services/ActivityType
to be found at runtime by the ServiceLoader API. This is done by an
upstream annotation _io.virtdata.annotations.Service_, since this avoids hoisting
in the popular but overly heavy AutoServer (it has a dependency on Guava).

~~~
@Service(ActivityType.class)
~~~

### DiagActivityType is an ActivityType

Let's implement an ActivityType. Actually, let's make it useful for something
besides default behavior. Let's also implement ActionDispenserProvider.

~~~
public class DiagActivityType implements ActivityType {
~~~

The ActivityType interface uses default methods for the *...DispenserProvider*
methods. Most ActivityType implementations will only need ActionDispenser(...).

### DiagActivityType has a name

Each ActivityType implementation must provide a simple name by which it is known
at runtime. When the available activity types are discovered at runtime, it is
an error for more than one to have the same name.

~~~

    @Override
    public String getName() {
        return "diag";
    }

~~~

### ActionDispenser method

We need to provide our own type of action for _diag_ in order to make it useful.
getActionDispenser(...) will be called exactly once per activity instance. The
ActionDispenser that we provide here (per activity instance) will be used to
create the per-thread actions for each per-thread Runnable (aka Motor). The
ActivityDef is also our first chance to specialize the behavior of the
ActivityType. This means that your primary input into the control of an
activity's behavior is this activity definition. If you want your ActivityType
to do something configurable, this is how you do it.

~~~
    @Override
    public ActionDispenser getActionDispenser(ActivityDef activity) {
        return new DiagActionDispenser(activity);
    }
}
~~~

Now, on to the implementation of the DiagActionDispenser we just saw above.

This implementation does little, but what it does is important. First, it
remembers the ActivityDef that goes with the activity instance. This is intended
to be the initializer data for the activity instance. Second, it simply provides
an Action when asked. Now we see the second opportunity to specialize the
behavior of the Action, around the slot number.

Whether we want it or not, the slot number is available to us. Notice that the
DiagAction itself is taking the slot number and the activity. We'll explain why
further down.


~~~
public class DiagActionDispenser implements ActionDispenser {
    private ActivityDef activity;

    public DiagActionDispenser(ActivityDef activity) {
        this.activity = activity;
    }

    @Override
    public Action getAction(int slot) {
        return new DiagAction(slot, activity);
    }
}
~~~

#### A note on instances and scoping

It may be the case that your Action implementation is thread-safe, and that you
want to just share the same instance across all Runnables in your running
activity. In that case, you'd keep a local instance of a DiagAction and simply
initialize and return it as needed. However, most often you'll want thread to
have some thread-local state, and you'll simply use the slot number for
diagnostic and logging purposes. This implementation does the latter.

The picture is different when you are talking about Inputs. It is often useful
to have a common stream of input for many threads, such as when you want to
meter the rate of processing over some number of inputs. In this case, a simple
atomically accessed and incremented long does the job well. But, in order to
meter or rate-limit the set of threads, you need them to use the same input. The
input is your control and measurement point. In this case, you would simply
re-use the same input for all slots.

Motors work exactly the same way. The naming of the interfaces for Motors,
Inputs, and Actions is consistent throughout, so hopefully that makes the API
easier to understand and use.

This allows for a degree of flexibility in mapping motors, inputs, and action to
slot numbers. You can create a ActivityType that shares none of these components
between threads for an activity, or one that shares all of the across threads
for an activity, or anything in between. The most common recipe is: one input
per activity and one motor and action per thread reading from this input.

### DiagAction

Now, on to the substance of this activity type, the Action implementation.

~~~
public class DiagAction implements Action, ActivityDefObserver {
    private final static Logger logger = LoggerFactory.getLogger(DiagAction.class);
~~~

DiagAction is also an ActivityDefObserver. This is how an activity is able to be
informed when any of it's parameters are modified while it is running. We also
have the usual Logger in play.

Now, the local state:

~~~
    private ActivityDef activity;
    private int slot;
    private long lastUpdate;
    private long quantizedInterval;
~~~

ActivityDef and slot number are remembered. lastUpdate and quantizedInterval are
used by DiagAction in order to know when to report.

The basic purpose of diag is to provide a simple ActivityType that can be used
for testing and diagnostics. To do that, its action simply logs the input value
at some configured interval. It also demonstrates a couple of basic nosqlbench
patterns:

1. Sharing work across threads
2. Dynamically adjusting when the activity definition is modified.

A more detailed explaination of diag's behavior goes like this:

A logline for the input value is reported at every configured 'interval', in
milliseconds. The time between the scheduled reporting time and the actual
reporting time is also reported as 'delay'. All threads take a turn reporting
the interval.

In order to support this behavior, when the activity (and its actions) are
initialized, each Action computes a time interval which would put it in the
right place on the reporting schedule. This method is updateReportTime(), as
seen in the constructor:

~~~
    public DiagAction(int slot, ActivityDef activity) {
        this.activity = activity;
        this.slot = slot;

        updateReportTime();
    }

~~~

Some helper methods make updateReportTime and the math around time offsets easier to read.

~~~
    private void updateReportTime() {
        lastUpdate = System.currentTimeMillis() - calculateOffset(slot, activity);
        quantizedInterval = calculateInterval(activity);
        logger.debug("updating report time for slot:" + slot + ", def:" + activity + " to " + quantizedInterval);
    }

    private long calculateOffset(long timeslot, ActivityDef activity) {
        long updateInterval = activity.getParams().getLongOrDefault("interval", 100L);
        long offset = calculateInterval(activity) - (updateInterval * timeslot);
        return offset;
    }

    private long calculateInterval(ActivityDef activity) {
        long updateInterval = activity.getParams().getLongOrDefault("interval", 100L);
        int threads = activity.getThreads();
        return updateInterval * threads;
    }
~~~

This is where we read the activity def values. For diag, *interval* is a useful
parameter in the activity definition. The default is 100, of unset.

It is true that the code could be optimized more around performance or
terseness, but clarity and correctness are more important here.


### DiagAction implements Action

As an Action, we must accept input:

~~~
    @Override
    public void accept(long value) {
        long now = System.currentTimeMillis();
        if ((now - lastUpdate) > quantizedInterval) {

            logger.info("diag action, input=" + value + ", report delay=" + ((now - lastUpdate) - quantizedInterval));
            lastUpdate += quantizedInterval;
        }
    }
~~~

This is simply a loop that reads input and throws it away unless it is time to
report. If it is time to report, we mark the time in lastUpdate.

### DiagAction implements ActivityDefObserver
~~~
    @Override
    public void onActivityDefUpdate(ActivityDef activity) {
        updateReportTime();
    }
}
~~~

This is all there is to making an activity react to real-time changes in the activity definition.

    