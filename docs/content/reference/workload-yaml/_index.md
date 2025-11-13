+++
title = "Workload YAML Specification"
description = "Complete specification for NoSQLBench workload definition format"
weight = 40
sort_by = "weight"
template = "pages.html"

[extra]
quadrant = "reference"
topic = "workload-yaml"
category = "specification"
tags = ["yaml", "workload", "specification", "living-documentation", "tested"]
testable = true
+++

# Workload YAML Specification

Complete, testable specification for NoSQLBench workload definition format.

This section contains the **living documentation** for workload definitions. All content blocks have been validated with the latest NoSQLBench build, ensuring accuracy and reliability.

## Specification Documents

### [00 - Workload Specification](00_workload_specification.md)
Overview of the workload specification format and structure

### [01 - Spectest Formatting](01_spectest_formatting.md)
How specification tests are formatted and validated

### [02 - Workload Structure](02_workload_structure.md)
Overall structure of workload YAML files

### [04 - Op Template Basics](04_op_template_basics.md)
Basic operation template syntax and usage

### [05 - Op Template Payloads](05_op_template_payloads.md)
Defining payloads in operation templates

### [06 - Op Template Variations](06_op_template_variations.md)
Creating template variations for different scenarios

### [07 - Template Variables](07_template_variables.md)
Using variables and bindings in templates

### [08 - Parsed Op API](08_parsed_op_api.md)
Programmatic access to parsed operations

## Living Documentation

These specification documents are **tested automatically**. Code examples and YAML snippets are validated against the NoSQLBench implementation to ensure they work exactly as documented.

This guarantees that:
- Examples are always current
- Syntax is correct
- Features work as described
- Documentation evolves with the code

## Related Documentation

- **[Binding Functions](../bindings/)** - Data generation functions used in workload bindings
- **[Drivers](../drivers/)** - Driver-specific operation formats and options
- **[Tutorials](/tutorials/)** - Step-by-step workload creation guides
