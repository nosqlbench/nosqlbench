# Metrics Labeling

All metrics flowing from NoSQLBench should come with a useful set of labels which
are presented in a self-consistent manner. These labels serve to identify a given metric
not only within a given study or deployment, but across time with macr-level identifiers.

Those identifiers which are nominal for the study or deployment should also be provided
in the annotations which can be queried later to find the original set of related metrics.

# Naming Context

In order to simplify the naming methods, all metrics instruments are created through
a helper type called ActivityMetrics. (This name might change).
It contains factory methods for all the metric types you may use within the NoSQLBench runtime.

Each factory method must start with an NBLabeledElement, which provides the naming context
for the _thing to which the metric pertains_, *separate* from the actual metric family name.
The metric family name is provided separately. This means that the factory methods have,
injected at the construction site, all the identifying labels needed by the metric for
reporting to the metrics collector.

However, the appropriate set of labels which should be provided might vary by caller, as sometimes
the caller is an Activity, sometimes an OpDispenser within an activity, sometimes a user script,
etc.

This section describes the different caller (instrumented element, AKA NBLabeledElement)
contexts and what labels are expected to be provided for each. Each level is considered
a nested layer below some other element, which implicitly includes all labeling data from
above.

# Labeling Contexts

- NoSQLBench Process
  - "appname": "nosqlbench"
  - Scenario Context (calling as Scenario)
    - IFF Named Scenario Mode:
      - "workload": "..." # from the file
      - "scenario": "..." # from the scenario name
      - "usermode": "named_scenario"
    - IFF Run Mode:
      - "workload": "..." # from the file
      - "scenario": "..." # from the (auto) scenario name
      - "usermode": "adhoc_activity"
  - Activity Context (calling as Activity)
    - includes above labels
    - IFF Named Scenario Mode
      - "step": "..."
      - "alias": "${workload}_${scenario}_${step}"
    - ELSE
    - "alias": "..." # just the activity alias
  - Op Template Context (calling as OpDispenser)
    - includes above labels
    - "op": "<name of the parsed op>"

