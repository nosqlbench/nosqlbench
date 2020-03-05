---
title: Statement Params
weight: 15
---

Statement parameters apply to the defined operations for an activity. Statement
parameters are always configurable as part of a `params` block in YAML, for
activities that use the [Standard YAML](/user-guide/standard_yaml) format.

In some cases, an [Activity Parameter](/parameters/activity_params) of the same
name can be used to establish a default value. In that case, it will be
documented here with the parameter description. 

### ratio

`ratio: <ratio>`

Determines the frequency of the affected statements in the operational sequence.
This means, in effect, the number of times a given statement will be executed
within the planned sequence before it starts over at the beginning. When using ratio,
it is important to be aware of *how* these statements are sequenced according
to the ratio. That is controlled by [seq](/parameters/activity_params#seq).






