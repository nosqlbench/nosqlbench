---
title: "Branch Plans"
description: "Catalog of in-flight branch objectives and required changes."
audience: internal
diataxis: explanation
tags:
  - planning
  - branches
component: core
topic: architecture
status: draft
owner: "@nosqlbench/architecture"
generated: false
---

<!--
  ~ Copyright (c) 2025 nosqlbench
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

# Branch Plans

## CLI Improvements

### --copy command enhancement

**Current behavior:**
- The `--copy` command only accepts a resource name argument: `--copy <resource>`
- It always copies to the current directory with the original filename
- If users try to specify a destination (e.g., `--copy workload.yaml .`), the extra argument is treated as an unrecognized command, resulting in a confusing error message

**Required improvements:**
1. Add support for an optional destination argument: `--copy <resource> [destination]`
   - If destination is not provided, use current directory (maintain backward compatibility)
   - If destination is a directory, copy to that directory with original filename
   - If destination is a file path, copy to that specific file path
2. Improve error handling to provide clear messages when destination-related errors occur (e.g., file already exists, permission denied, invalid path)

**Affected files:**
- `nb-engine/nb-engine-cli/src/main/java/io/nosqlbench/engine/cli/NBCLIOptions.java` - Parse optional destination argument
- `nb-engine/nb-engine-cli/src/main/java/io/nosqlbench/engine/cli/NBCLI.java` - Handle destination path logic
