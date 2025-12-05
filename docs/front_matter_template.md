---
title: Front Matter Template
tags:
  - documentation
  - templates
audience: meta
diataxis: howto
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Front Matter Template

Copy, paste, and adjust this template at the top of every Markdown document.
Update the values inside `<>` and remove optional fields if they do not apply.

```yaml
---
title: "<Concise title shown in navigation>"
description: "<One-sentence summary>"
audience: user|operator|developer|internal|meta
diataxis: tutorial|howto|explanation|reference
tags:
  - key-topic
  - secondary-topic
component: core|drivers|virtdata|nbr|nb5|docsys|site|community
topic: workloads|drivers|bindings|metrics|architecture|releases|contributing|ops|api|docops
status: draft|review|live|deprecated
owner: "<GitHub team or individual>"
source: "<Path to canonical source if generated>"
generated: false
related:
  - ../path/to/related-doc.md
labels:
  dimension: value
---
```

## Usage Guidance

- `audience`, `diataxis`, `component`, `topic`, and `status` are required for lint
  compliance.
- Set `generated: true` only for files created programmatically; include the
  `source` path pointing back to the authoritative copy.
- Use lower-case, hyphenated keywords for `tags` to ease filtering.
- `owner` should match a resolvable GitHub team (e.g., `@nosqlbench/docs`) or a
  specific maintainer.
- Delete unused optional keys once a document is finalized to keep metadata
  concise.
