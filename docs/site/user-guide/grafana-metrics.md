---
title: Grafana-Metrics
description: Grafana-Metrics
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 40
---

---
title: "Built-In Grafana Dashboard"
weight: 40
---
```
NOTE: the built-in local dashboard feature has been removed and is no longer available. 
A new grafana dashboard metrics reporting tutorial will be available soon.
```

--- 


[//]: # (## Built-In Local Dashboard)

[//]: # ()
[//]: # (NoSQLBench comes with a built-in helper to get you up and running quickly with client-side testing)

[//]: # (metrics. This functionality is based on docker, and a built-in method for bringing up a docker)

[//]: # (stack, automated by NoSQLBench.)

[//]: # ()
[//]: # (**WARNING:**)

[//]: # (This feature requires that you have docker running on the local system and that your user is in a)

[//]: # (group that is allowed to manage docker. Using the `--docker-metrics` command *will* attempt to)

[//]: # (manage docker on your local system.)

[//]: # ()
[//]: # (To ask NoSQLBench to stand up your metrics infrastructure using a local docker runtime, use this)

[//]: # (command line option with any other NoSQLBench commands:)

[//]: # ()
[//]: # (    --docker-metrics)

[//]: # ()
[//]: # (When this option is set, NoSQLBench will start graphite, prometheus, and grafana automatically on)

[//]: # (your local docker, configure them to work together, and to send metrics the system automatically.)

[//]: # ()
[//]: # (## Annotations)

[//]: # ()
[//]: # (As part of this integration, the internal annotation facility for NoSQLBench is also pointed at the)

[//]: # (grafana instance. Several life-cycle events are reported, in both instant and span form. For)

[//]: # (example, when an activity is stopped, an annotation is recorded with its parameters, start time,)

[//]: # (end time, and so on. The built-in dashboards have support for toggling these annotations as a)

[//]: # (way to provide traceability to test scenarios and events.)

[//]: # ()
[//]: # (## Using a Remote Dashboard)

[//]: # ()
[//]: # (If you have started a dashboard docker stack as described above, then you can also run clients )

[//]: # (in a mode where the metrics and annotations are forwarded to it. In order to do that, simply add )

[//]: # (this too your command line:)

[//]: # ()
[//]: # (    --docker-metrics-at <host>)

[//]: # ()
