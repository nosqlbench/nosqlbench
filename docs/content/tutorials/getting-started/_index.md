+++
title = "Getting Started"
description = "Your first steps with NoSQLBench - installation, setup, and running your first workload"
weight = 1
sort_by = "weight"
template = "pages.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "basics"
tags = ["installation", "quickstart", "first-steps"]
+++

# Getting Started with NoSQLBench

Step-by-step guides to get you up and running with NoSQLBench quickly.

This section walks you through:
1. Downloading and installing NoSQLBench
2. Setting up a test target database
3. Running your first scenario
4. Understanding the output and metrics
5. Next steps for deeper learning

## Tutorial Path

Follow these guides in order for the best learning experience:

1. **[Download NoSQLBench](00-installation.md)** - Get NoSQLBench installed on your system
2. **[Set Up Test Target](01-test-target.md)** - Start a database to test against
3. **[Built-in Scenarios](02-scenarios.md)** - Explore pre-built workloads
4. **[Example Results](03-example-results.md)** - Understand what NoSQLBench outputs
5. **[Reading Metrics](04-reading-metrics.md)** - Interpret performance metrics
6. **[Next Steps](05-next-steps.md)** - Where to go from here

## Quick Start

For the impatient, here's the fastest path:

```bash
# 1. Download NoSQLBench
curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5
chmod +x nb5

# 2. Run a simple workload
./nb5 examples/bindings-basics

# 3. Explore built-in workloads
./nb5 --list-workloads
```

Then read the guides above to understand what just happened and how to customize for your needs.
