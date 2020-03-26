---
title: Grafana Metrics
weight: 2
---

# Grafana Metrics

NoSQLBench comes with a built-in helper to get you up and running quickly with client-side testing metrics. This
functionality is based on docker, and a built-in method for bringing up a docker stack, automated by NoSQLBench.

:::warning
This feature requires that you have docker running on the local system and that your user is in a group that
is allowed to manage docker. Using the `--docker-metrics` command *will* attempt to manage docker on your local system.
:::

To ask nosqlbench to stand up your metrics infrastructure using a local docker runtime, use this command line option
with any other nosqlbench commands:

    --docker-metrics

When this option is set, nosqlbench will start graphite, prometheus, and grafana automatically on your local docker,
configure them to work together, and to send metrics the system automatically. It also imports a base dashboard for
nosqlbench and configures grafana snapshot export to share with a central DataStax grafana instance (grafana can be
found on localhost:3000 with the default credentials admin/admin).
