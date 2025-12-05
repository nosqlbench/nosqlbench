---
title: Labeling Controls
description: Labeling Standards
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
weight: 26
---

👉 NOTE: The labeling control features are new within NoSQLBench and are subject to changes.

## Labeling Standards

In order to make sure that your metrics and annotations have the necessary labels
so that the data is traceable, identifiable, and contextual, you may want to set
some label validation rules. These rules simply say which label names are required
and which are not allowed. If you have a shared system of record for your metrics with
other engineers or teams, then some discipline about how these metrics are reported is
always required, but not often adhered to, even if there is a shared idea of what the
metrics should look like. The net effect is that some of your most useful data gets lost
among and indistinguishable from a trove of other metrics data which is extraneous,
or worse, your data has this effect on others'.

It is recommended that you have a labeling standard, and that is is somewhere that is easy
to share, verify, and enforce. For lack of this, NoSQLBench provides a client-side
mechanism to make this possible in practice, although it is opt-in and not enforced from
any consuming system. It is expected that NoSQLBench will support loading any provided labeling
standards from consuming system automatically in the future.

Some specific examples for why this is important:

* You want to look the results of a test which were run 6 months ago, there are 37 other sets of
  metrics data from the same time frame. Either your data is labeled so you can distinguish the
  studies, or you're just out of luck. You could have simply required a single label with a
  unique identifier in your standard to avoid this.
* You want to aggregate results across a set of nodes. Either your data is distinguished by node,
  or you have bad data. You could simply require a node label to be provided, within the context
  of your other labels. The validity of your aggregating metrics depends directly on this being
  stable and distinctly addressable.


### Filtering and Validation

example label:

    alpha=one,beta=two,gamma=three,delta=four

#### Labeling Specifications

There are two ways to ensure that labels you send are valid:
1) Adjust the provided labels to conform to the standard before sending.
2) Validate that the labels provided already conform to the standard before sending.

These two are distinct methods which are complimentary, and thus NB does both. This is because
there are label sets which can be aligned to a standard simply by filtering, and there are other
cases where this is not possible due to missing data.

### Label Specs

These two method are combined in practice into one configuration parameter (for now at least),
since the expression of validation and pre-filtering for that validation is effectively one and the
same. This is known as the `labelspec`, and it is simply a one-line pattern which specifies which
labels are required, which labels are disallowed, and so on.

### Label Filtering

- Included labels are indicated as `+<labelname>` or simply `<labelname>`.
- Excluded labels are indicated as `-<labelname>`.
- The labelname can be any valid regular expression, and is always treated as such. Bare words
  are simply literal expressions.
- If a provided label set includes label names which are not indicated as either, then they are
  Included by default.

So why would you have a bare label name in lieu of `+` or vice-versa if they mean the same thing?
The reason is that the labelname filter operates as a tri-state filter, going from left to right
over the labelspec. You can include, or exclude any label name, and you can also use _wildcard_
patterns simply by using regular expressions. Thus, a spec of `lioness,-lion.*` indicates that you
want to allow `lioness` labels but disallow any other labels which start with `lion`. However,
this does not say that you _require_ a `lioness` label, which looks like this `+lioness,-lion.*`.
In both cases, any label which is not mentioned will pass through the filter unchanged. If you
wanted to reject all others, you can always end the labelspec with a `-.*`, such as `+lioness,
-lion.*,-.*`, which is also duplicitous and can simply be reduced to `+lioness,-.*`.

#### Label Filtering Effects

The label filter is applied at the point of label usage on outbound signals. Metrics and
Annotations both have the filter applied. The underlying label sources are not affected.
When a label set is passed through the label filter, some fields may be removed. Otherwise label
filtering is entirely passive. The order of labels is preserved if they came from an ordered map.

### Label Validation

Label validation occurs on a label set immediately after filtering. The specification is identical,
although it weighs a little more heavily in terms of side-effects for validation.

- Required label names are indicated with `+<labelname>`
- Disallowed label names are indicated with `-<labelname>`
- Bare words are disregarded by validation.
- Patterns are still applied with the expected behavior.

#### Label Validation Effects

Label validation occurs immediately after label filtering, and will throw an error in your test,
with an error that indicates why the label set could not be validated.

This is because invalid labels will cause pain downstream. In most cases, this will manifest
early in a test so that you don't have any wasted bake time. It is recommended that you always run
a full end-to-end functional validation of all your test workflows before running the more
intensive versions. This helps you shake out any issues with late-validation, such as a bad
label set in a later stage of testing.

## TBD: conditional validation

There is a very clear need to make the validation work a bit more reasonably with regard to
labeling context. For example, requiring an `activity` label at the top level does not work well 
for the session annotation, nor does requiring and `activity` label when the `scenario` label is 
not present. There are some simple forms which can support these coherently and these will be 
added shortly. 

### sketch on conditional validation

```
+appname,+instance:\w+,+session,+node,activity->k,activity->dataset,activity->dimensions
```
1. appname is a required label name
2. instance is a required label name and its value must match regex pattern `\w+`, word chars only
3. session is a required label
4. node is a required label
5. given activity is present as a label name, then k must also be present
6. given activity is present as a label name, then dataset must also be present
7. given activity is present as a label name, then dimension must also be present

if a then b
a -> b
if b then c
b -> c
if a then b and c
a -> b -> c
A, thus b! and c!

A! ->b ->c
a->b->c
+a->b->c

## Special cases

* assertions:
* conjunctions like tag filtering: none(), any(), all()
* 
* require a named label
* require a named label with a specific value
* require a named label with a pattern
* require a pattern matching label name with a pattern matching value pattern
* conditional

