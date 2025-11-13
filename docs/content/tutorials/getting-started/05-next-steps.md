+++
title = "Next Steps"
description = "Choose your path forward with NoSQLBench"
weight = 105
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "learning-paths"
tags = ["next-steps", "learning", "workloads", "scenarios"]
+++

# Next Steps

Now that you've run NoSQLBench for the first time and seen what it does, choose your customization level for further testing.

## Essential Reading

Everyone using NoSQLBench should read:

**[Core Concepts](../../explanations/concepts/)** - Essential understanding for new and experienced testers alike. Explains activities, scenarios, bindings, and the NoSQLBench execution model.

## Learning Paths

### High-Level Users

**Goal:** Use pre-built workloads without customization

Several canonical workloads are built into NoSQLBench for immediate use:

```bash
# List all built-in workloads
./nb5 --list-workloads

# List with examples included
./nb5 --list-workloads --include examples

# Copy a workload to customize
./nb5 --copy cql-keyvalue
```

**Next:** Explore built-in workloads and their configuration options.

### Workload Builders

**Goal:** Create tailored workloads emulating specific applications

Build self-contained, portable YAML files specifying:
- Access patterns
- Data distributions
- Operation mixes
- Custom bindings

**Start here:**
- **[CQL Quickstart](../workloads/cql-quickstart.md)** - Learn by doing
- **[HTTP Quickstart](../workloads/http-quickstart.md)** - REST API testing
- **[Workload YAML Specification](../../reference/workload-yaml/)** - Complete format reference
- **[Workload Design Guides](../../guides/workload-design/)** - Best practices

**Built-in Sources:** Use `--list-workloads` and `--copy <name>` to access built-in examples as templates.

### Scenario Developers (Advanced)

**Goal:** Advanced scenario designs, iterative testing, custom analysis

Use ECMAScript to control scenarios programmatically from start to finish. This enables:
- Complex multi-phase testing
- Dynamic parameter adjustment
- Custom metrics collection
- Iterative optimization workflows

**Not recommended for first-time users.** If you need this feature, join us on [GitHub Discussions](https://github.com/nosqlbench/nosqlbench/discussions).

## Common Next Steps by Role

### Performance Engineers

Focus on metrics and analysis:
- [Understanding Metrics](04-reading-metrics.md)
- [Metrics and Analysis Guides](../../guides/metrics/)
- [MetricsQL](../../reference/apps/mql.md)

### Test Automation Engineers

Focus on workload building:
- [Workload Design](../../guides/workload-design/)
- [Testing Strategies](../../guides/testing/)
- [Built-in Scenarios](02-scenarios.md)

### Database Administrators

Focus on specific drivers and deployment:
- [CQL Driver](../../reference/drivers/cqld4.md)
- [Driver Documentation](../../reference/drivers/)
- [Load Testing Best Practices](../../guides/testing/load-testing.md)

### Developers

Focus on data generation and binding functions:
- [Binding Functions Reference](../../reference/bindings/)
- [Data Generation Guides](../../guides/data-generation/)
- [Creating Custom Functions](../../development/guides/binding-functions.md)

## Resources

- **[Documentation Home](../../_index.md)** - Full documentation index
- **[GitHub Repository](https://github.com/nosqlbench/nosqlbench)** - Source code, issues, discussions
- **[Release Notes](../../release-notes/)** - Latest features and changes

## Get Help

- **Questions:** [GitHub Discussions](https://github.com/nosqlbench/nosqlbench/discussions)
- **Bugs:** [GitHub Issues](https://github.com/nosqlbench/nosqlbench/issues)
- **Contributing:** [Contributing Guide](../../development/contributing.md)

---

**Congratulations!** You've completed the Getting Started tutorial series. Choose your path above and dive deeper into NoSQLBench.
