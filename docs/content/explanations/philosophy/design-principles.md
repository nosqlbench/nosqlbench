+++
title = "Design Principles"
description = "Core principles guiding NoSQLBench technical design and development"
weight = 10
template = "page.html"

[extra]
quadrant = "explanations"
topic = "philosophy"
category = "design"
tags = ["principles", "design", "philosophy", "values", "architecture"]
+++

# Design Principles

Core design principles that steer the technical design of NoSQLBench. These principles help others understand what NoSQLBench is all about and guide development decisions.

## Respect for Users

While this sounds like a conduct-level aspect, the focus here is on what this means in terms of design. (Respect is absolutely part of the [code of conduct](../../development/contributing/) governing this project as well.)

### In Design Terms

**Respect means:**
- Building systems that respect users in general and specific ways
- Thoughtful design that values user time
- Making powerful capabilities accessible
- Providing clear upgrade paths without breaking existing work
- Minimizing cognitive load through consistent patterns

### Manifestations

- **Sane defaults:** Work out of the box without configuration
- **Progressive disclosure:** Advanced features don't clutter simple use cases
- **Clear error messages:** When something fails, tell users why and how to fix it
- **Backward compatibility:** Respect investment in existing workloads

## Durable Concepts

We focus on core concepts that stand the test of time and give time back to users.

### Criteria

We look for concepts that:
- **Give back more** in clarity and reuse than they take in indirection
- **Bring users together** in common practice and understanding
- **Move the ecosystem forward** as a whole
- **Remain relevant** as systems and practices evolve

### Examples

**The Cycle:** A universal coordinate system for data generation that works across all workloads and drivers.

**Activities:** Self-contained execution units that compose cleanly into complex scenarios.

**Bindings:** Declarative data generation that separates "what data" from "what operations."

See [Core Concepts](../concepts/core-concepts/) for detailed explanations.

## Composable Systems

We build composable systems that serve users at multiple levels simultaneously.

### Pre-Built High-Level Forms

**Quick results with minimal effort:**
- Built-in workload templates ready to run
- Standard scenarios for common patterns
- Sensible defaults throughout

```bash
# Just works, no configuration needed
./nb5 cql-keyvalue host=myhost
```

### Reconfigurable Components

**Customization when needed:**
- Override parameters
- Modify templates
- Add custom bindings
- Compose new scenarios from existing pieces

```yaml
# Customize without rebuilding from scratch
scenarios:
  my-test: default.schema default.rampup my-custom-main
```

### Deep Extensibility

**Full control for advanced users:**
- Custom driver adapters
- New binding functions
- Programmatic scenario scripts
- Plugin architecture

### Sliding Scale of Effort

```
Low Investment     →  Built-in workloads, standard scenarios
Medium Investment  →  YAML customization, parameter tuning
High Investment    →  Custom code, advanced scripting
```

Users choose their investment level based on needs. Each level builds on the previous without requiring mastery of everything below.

## High Fidelity

We build high-fidelity measurement tools and instruments into NoSQLBench.

### Measurement Quality

Results are:
- **Useful:** Provide actionable insights
- **Repeatable:** Same test produces consistent results
- **Reproducible:** Others can replicate your findings
- **Accurate:** Measurements reflect reality, not artifacts

### Implementation

**High-resolution timing:**
- HDR Histogram for latency tracking
- Microsecond precision
- Correct percentile calculation

**Efficiency:**
- Minimal client-side overhead
- Lock-free algorithms where possible
- Native driver integration
- Headroom for accurate measurement at speed

**Deterministic execution:**
- Cycle-based data generation
- Reproducible operation sequences
- Controlled concurrency

### Why It Matters

**Testing tools should not be the bottleneck.** Measurements must reflect the target system's behavior, not the test harness's limitations.

NoSQLBench maintains sufficient performance headroom to measure systems accurately even under extreme load.

## Principles in Action

These aren't just aspirational statements - they guide every design decision:

| Principle | Example Feature | User Benefit |
|-----------|----------------|--------------|
| Respect for Users | Auto-generated docs from code | Always current documentation |
| Durable Concepts | Cycle-based data generation | Tests work the same way 5 years later |
| Composable Systems | Scenario composition | Build complex tests from simple parts |
| High Fidelity | HDR Histogram metrics | Accurate tail latency measurement |

## Related Documentation

- **[Core Concepts](../concepts/core-concepts/)** - Concepts stemming from these principles
- **[Architecture](../architecture/)** - How principles guide implementation
- **[Contributing](../../development/contributing/)** - Apply these principles in contributions

---

*These principles aren't dogma - they're the result of years of real-world use and continuous refinement based on user feedback and testing experience.*
