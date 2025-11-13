+++
title = "Binding Functions Reference"
description = "Complete reference documentation for all virtdata binding functions"
weight = 10
sort_by = "weight"
template = "pages.html"

[extra]
quadrant = "reference"
topic = "bindings"
category = "virtdata"
tags = ["data-generation", "virtdata", "functions"]
+++

# Binding Functions Reference

Complete reference documentation for all NoSQLBench virtdata binding functions.

Binding functions are used to generate synthetic data for your workloads. They transform input values (typically long integers from the activity sequence) into realistic test data.

## Function Categories

The binding functions are organized by category:

- **State Functions** - Side-effect functions for managing thread-local state
- **Functional Functions** - Pure data transformation functions
- **Premade Functions** - Pre-configured function chains for common patterns
- **Reader Functions** - Functions for reading data from external sources
- **And more** - See the individual category pages below

## Auto-Generated Documentation

All binding function documentation on this page and its children is auto-generated from the Java source code annotations and Javadoc comments. This ensures the documentation stays synchronized with the actual implementation.

**Generation Source:** virtdata-lib-* modules
**Generator:** BundledMarkdownExporter
**Last Updated:** Auto-generated on build

## Usage Example

```yaml
bindings:
  user_id: HashRange(1,1000000)
  username: Template("user_{}")
  email: Template("{}@example.com")
  created_at: ToEpochTimeUUID()
```

See individual function pages for detailed parameters and examples.
