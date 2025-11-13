+++
title = "Framing Performance Analysis"
description = "How to frame and approach performance factoring questions"
weight = 5
template = "page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "analysis"
tags = ["performance", "methodology", "analysis", "framework"]
+++

If you are using NoSQLBench to perform advanced testing or performance analysis, then this
section is for you. It is meant to be a reference point and idea percolator for advanced testing methods,
particularly those which can be streamlined or automated with NoSQLBench.

The key objective of performance factoring is to identify and measure factors in system
performance using repeatable and reliable methods. These methods are often implied in
common practice, but there are few places where they are explained in a useful way for new
distributed systems engineers. They may involve numerical or analytical methods, but a cohesive
view includes the basic workflow, orchestration and staging elements which make results valid.

One of the key strategic elements of NoSQLBench is the ability to codify those workflows and methods
which have historically been unspecified and thus unreliable from one effort to the next.
Methods and workflows which are useful will be demonstrated, with the express purpose of
validating them with our user community before building them into NoSQLBench capabilities.

Consider this section as an on-ramp, an incremental sandbox, and an attempt to close the gap between
advanced methods and automation for performance factoring.
