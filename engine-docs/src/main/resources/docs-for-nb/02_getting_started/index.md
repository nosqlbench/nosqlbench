---
title: Quick Start Example
weight: 20
---

If you are on Linux, you should be able to start getting useful results immediately with these commands:

1. `/nb  run type=cql yaml=baselines/cql-iot tags=phase:schema host=dsehost`
2. `./nb run type=cql yaml=baselines/cql-iot tags=phase:main host=dsehost cycles=1M threads=auto host=dsehost`

You can put your own contact points in place of `dsehost`.

Alternately, if you have docker installed on your Linux system, and you have permissions to use it, you
can use `--docker-metrics` to stand up a live metrics dashboard at port 3000.

This example doesn't go into much detail about what it is doing. It is here to show you how quickly you can
start running real workloads without having to learn much about the machinery that makes it happen.

The rest of this section has a more elaborate example that exposes some of the basic options you may want to
adjust for your first serious test.
