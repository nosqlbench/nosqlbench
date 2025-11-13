+++
title = "Core Concepts"
description = "Fundamental concepts underlying NoSQLBench design and execution model"
weight = 1
template = "docs-page.html"

[extra]
quadrant = "explanations"
topic = "concepts"
category = "fundamentals"
tags = ["concepts", "architecture", "design", "cycles", "activities", "scenarios"]
+++

# Core Concepts

NoSQLBench is built on core concepts that have been scrutinized, replaced, refined, and hardened through several years of use by a diverse set of users.

## Why Concepts Matter

This level of refinement is important when trying to express common patterns in what is often a highly fragmented practice. Testing is hard. Scale testing is hard. Distributed testing is hard. Combined, the challenge of executing realistic tests is often quite daunting to all but seasoned test engineers.

Existing tools have only skirmished with this problem enough to make dents, but none has tackled the fundamental lack of conceptual building blocks.

**This has to change.** We need a set of testing concepts that can span across workloads and system types, with machinery to put these concepts to use. Finding these building blocks is often one of the most difficult challenges in systems design. Once you find and validate a useful set of concepts, everything else gets easier.

We believe that NoSQLBench's success has been strongly tied to these core concepts.

## The Cycle

**Cycles** in NoSQLBench are whole numbers on a number line. Each operation in a NoSQLBench scenario is derived from a single cycle - a `long` value representing a seed.

The cycle determines:
- Which operation is selected for execution
- What data will be generated and attached to it

### Cycle Ranges

Cycles are specified as a **closed-open** `[min,max)` interval (known as _slices_ in some languages):
- The min value **is included** in the range
- The max value **is not included**

**Example:** `cycles=5` means cycles 0,1,2,3,4 (not 5)

### Benefits

**Stackable ranges:** You can stack slices using common numeric reference points without overlaps or gaps.

**Determinism:** You have exact awareness of what data is in your dataset, even incrementally.

**Coordinate system:** Think of a cycle as a single-valued coordinate system for data that lives adjacent to that number on the number line. Virtual dataset functions convert coordinates into data.

**Repeatability:** Using the same cycle range specifies the exact same operations. Tests are completely deterministic (pseudo-random) and repeatable, even when they appear random or are shaped by density curves.

## The Activity

An **activity** is a multithreaded flywheel of statements in some sequence and ratio.

- Each activity runs over the numbers in a cycle range
- Specified as a series of op templates in some ratio and order
- Executes an efficient loop over specific operations with its own thread pool
- Multiple activities can run concurrently within a scenario

## The Op Template

Each possible operation in an activity is provided by the user in a YAML or data structure driven template.

Op templates are used to create efficient **op dispensers** in the runtime according to the mapping rules for a given driver.

**Template → Dispenser → Operation**

## The Driver Adapter

A **driver adapter** is a high-level driver for a protocol that interfaces a native driver to the runtime machinery of NoSQLBench.

Think of it as a **statement-aware cartridge** that knows how to:
- Take a basic op template
- Turn it into an executable operation
- Execute it for a given cycle using the native driver

**Examples:**
- `cqld4` - CQL driver adapter using DataStax Java Driver 4.x
- `http` - HTTP/REST driver adapter
- `kafka` - Kafka producer/consumer adapter

## The Scenario

The **scenario** is a runtime session that holds activities while they run.

A NoSQLBench scenario is responsible for:
- Aggregating global runtime settings
- Managing metrics reporting channels
- Coordinating log files
- Controlling activity lifecycle

All activities run within a scenario, under the control of the scenario script.

## The Scenario Script

Each scenario is governed by a central **scenario script**.

### Characteristics

- Runs in a single-threaded manner, asynchronous from the activities
- Maintains control over all activities
- Automatically created if not provided by the user
- Available for advanced testing requirements

### Completion

The scenario completes when:
1. The script exits **AND**
2. All activities are complete

### Convenience Forms

Shortcut forms of scripting are provided on the command line to address common variations without writing explicit scripts.

**Example:**
```bash
./nb5 <workload> <scenario> ...
```

This automatically generates a scenario script that runs the named scenario's steps in order.

## Putting It Together

**Conceptual Flow:**

```
Scenario (Session)
  ├─ Scenario Script (Control)
  ├─ Activity 1 (Thread Pool)
  │   ├─ Driver Adapter (e.g., cqld4)
  │   ├─ Op Templates (YAML)
  │   └─ Cycle Range (e.g., 0..100000)
  │       └─ Bindings (Data Generation)
  └─ Activity 2 (Thread Pool)
      └─ ...
```

### Execution Model

1. **Scenario script** starts and creates activities
2. Each **activity** spins up its thread pool
3. Threads loop over **cycle ranges**
4. For each cycle, **driver adapter** uses **op templates** to create operations
5. **Bindings** generate data from cycle numbers
6. Operations execute against target system
7. **Metrics** are collected and reported
8. Scenario completes when script and all activities finish

## Why This Design?

### Determinism

Using cycles as seeds means:
- Same cycle range = same exact operations
- Tests are reproducible
- Data generation is predictable
- Debugging is easier

### Flexibility

- Mix and match drivers
- Run multiple activities concurrently
- Scale from single-machine to distributed
- Compose complex scenarios from simple building blocks

### Performance

- Efficient multi-threaded execution
- Lock-free cycle distribution
- Native driver integration
- Minimal overhead

## Next Steps

Now that you understand the concepts:

- **[Activities](activities.md)** - Deep dive into activity lifecycle and configuration
- **[Scenarios](scenarios.md)** - Scenario composition and scripting
- **[Bindings](bindings.md)** - Data generation system explained
- **[Workload Templates](../../reference/workload-yaml/)** - YAML specification

## Related Documentation

- **[Getting Started](../../tutorials/getting-started/)** - Apply these concepts practically
- **[Workload Design Guides](../../guides/workload-design/)** - Best practices
- **[Architecture Overview](../architecture/)** - System design details
