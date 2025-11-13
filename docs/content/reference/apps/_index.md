+++
title = "Built-in Applications"
description = "Reference documentation for NoSQLBench built-in command-line applications"
weight = 30
sort_by = "weight"
template = "section.html"

[extra]
quadrant = "reference"
topic = "apps"
category = "commands"
tags = ["built-in", "cli", "apps", "commands"]
+++

# Built-in Applications

NoSQLBench includes several built-in applications accessible via the command line. These apps provide utility functions for working with workloads, data generation, metrics, and more.

## Available Apps

### [virtdata](virtdata.md)
Command-line tool for testing and validating binding functions. Useful for verifying data generation logic and getting performance baselines.

### [cqlgen](cqlgen.md)
Generate CQL schema definitions and data from templates.

### [mql](mql.md)
MetricsQL query interface for analyzing metrics stored in SQLite.

### [export-docs](export-docs.md)
Export bundled documentation to ZIP or directory format.

## Usage

Built-in apps are invoked using the app name as the first argument:

```bash
# Run virtdata app
nb5 virtdata --help

# Run mql app
nb5 mql --help
```

## Auto-Generated Content

Application documentation is auto-generated from the source code and maintained alongside application implementations.
