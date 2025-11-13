+++
title = "Reference"
description = "Technical specifications and API documentation"
weight = 30
sort_by = "weight"
template = "pages.html"

[extra]
quadrant = "reference"
topic = "specifications"
+++

# Reference Documentation

**Technical specifications** for precise, detailed information about NoSQLBench components, APIs, and features.

Reference documentation is information-oriented. It describes the machinery and provides accurate technical descriptions without explanation or instruction.

## Reference Sections

### [CLI Commands](cli/)
- [Command Line Options](cli/options/)
- [CLI Scripting](cli/scripting/)
- [Core Activity Parameters](cli/core-activity-params/)
- [SSL Options](cli/ssl-options/)

### [Binding Functions](bindings/)
Auto-generated reference for all virtdata binding functions:
- [State Functions](bindings/funcref-state/)
- [Functional Functions](bindings/funcref-functional/)
- [Collections](bindings/funcref-collections/)
- [Distributions](bindings/funcref-distributions/)
- [Datetime Functions](bindings/funcref-datetime/)
- [And many more...](bindings/)

### [Drivers](drivers/)
Driver-specific documentation for all supported systems:
- [CQL (Cassandra)](drivers/cqld4/)
- [HTTP](drivers/http/)
- [Kafka](drivers/kafka/)
- [MongoDB (S4J)](drivers/s4j/)
- [DynamoDB](drivers/dynamodb/)
- [See all drivers...](drivers/)

### [Workload YAML](workload-yaml/)
Complete workload specification reference:
- [Workload Specification](workload-yaml/00-workload-specification/)
- [Workload Structure](workload-yaml/02-workload-structure/)
- [Op Template Basics](workload-yaml/04-op-template-basics/)
- [Core Op Fields](workload-yaml/core-op-fields/)
- [See all YAML docs...](workload-yaml/)

### [Apps](apps/)
Built-in applications reference:
- [VirtData](apps/virtdata/)
- [MQL (MetricsQL)](apps/mql/)
- [CQLGen](apps/cqlgen/)
- [Export Docs](apps/export-docs/)

### [Concepts](concepts/)
- [Timing Terminology](concepts/timing-terms/)
