---
title: Auto Apply License in IntelliJ
description: Auto-Apply-License-Intellij
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 55
---

# Assumptions

- You are using IntelliJ to work with the NoSQLBench code base.
- You want IntelliJ to automatically apply the license files when needed on files you edit.

# Steps

1. Enable the bundled `copyright` plugin
2. Under File | Settings | Editor | Copyright | Copyright Profiles, add one named 'aplv2'.

- Set the copyright text (velocity template) to:
```
Copyright (c) $originalComment.match("Copyright \(c\) (\d+)", 1, "-", "$today.year")$today.year nosqlbench

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

- Set the `Regex to detect copyright in comments` to:
```
.*Licensed under the Apache License, Version 2.0.*
```

You can click the validate button to confirm that the velocity template works.

# 
