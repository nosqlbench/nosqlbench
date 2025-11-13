+++
title = "Pull Request Guidelines"
description = "How to submit and what to expect from pull requests"
weight = 30
template = "docs-page.html"

[extra]
quadrant = "development"
topic = "contributing"
category = "process"
tags = ["contributing", "pull-requests", "code-review"]
+++

All new changes to the project should be made in the form of a pull request. In certain cases,
project maintainers may push directly in order to fix a CI/CD issue or similar, but otherwise
_everyone_ should expect to submit pull requests and get at least 1 maintainer approval before
having their code merged.

Maintainers may make suggestions to your PR before it can be approved. This is done via the
conversations feature directly in github. If there are required changes before approval, they
will be described clearly with a request to make the changes before further review. If you are
asked to make changes, all you have to do is refine the branch you submitted the PR from and
push the changes up.

*note:* If you are unsure of your branch, and want to work on it further before review or merge,
please submit it as a *draft* PR. This helps set expectations so that reviewers aren't studying
incomplete submissions.

In order to make sure related issues are closed, you can add
[closing terms](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue)
to the description.

When you are ready for review, move your PR from draft to "ready for review", and request one of
the project maintainers for a review.

The rest of this page tells you what to expect from the maintainers during code review.

# What is Accepted?

Generally speaking, any change which is non-trivial should already be discussed within the
project in order to make cooperation between contributors harmonious. Very trivial change which
are quick to review and very self-evident as to what they fix or improve may be accepted with
little pushback. Still, it is a courtesy to the rest of the developer community to document what
you are working on in an issue and assign yourself to it first.

## Licensing

All code submitted should have the APLv2 license header at the top, with the copyright set as

```
/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

If this is not the case, then the build will fail. There is a handy way to fix this.
After this happens, the license checker will automatically create some prototype files
which include the changes needed. You can simply run a utility script in
`scripts/accept_license.sh` to write the updated files to the orignal names.

# Coding Standards

## Static Analysis

You should use a static analyzer if you have one. There is one that is used by the project for
every build. It may flag significant issues in your PR, which may also keep it from being merged.
It is always a good idea to leave your PR in draft mode until you get green light, as the CodeQL
feedback will already give you something to fix if it finds anything.

## Test Coverage

Any contribution of code should have an appropriate amount of built-in testing. This can be
either as unit tests or as integrated tests. It is up to the judgement of the contributor what
constitutes a sufficient level of testing, although more may be requested by code review.

The code coverage in unit and integrated tests will be improved over time. There may be
build-time checks which warn you if the code coverage goes down with your added changes. This
could cause it to fail during merge checks, so try to have a reasonable amount of test coverage
in new code.

## Dependencies

Be cautious about adding even more dependencies to the project. NoSQLBench is a rather large
project due to the scope of what it does, including many driver modes. If you add dependencies
without scanning for a suitable capability that is already in the project, it will cause
dependency creep. This is bad. Try to help us keep the dependency tree well trimmed.

# What is (generally) Accepted?

## Incremental Improvements

Changes which are complimentary or incremental to core NoSQLBench functionality are always good.
As long as a change doesn't compromise existing functionality or otherwise compromise the
integrity of the project it will generally be welcome.

## Trivial Improvements

Changes which have already been discussed or are of such a trivial nature that they are
self-describing don't require substantial ceremony. Use your best judgement here. If you are
fixing a typo on the website, just do it. If you are adding a better description to some CI/CD
wiring, just do it. Again, just use your best judgement about what should be discussed or agreed
to beforehand.

## Subsystem Enhancements

Changes which make the core of NoSQLBench easier, better, or more powerful for any user in the
NoSQL ecosystem are awesome. New driver are awesome. This includes drivers from vendors. All
changes will be subject to the platform standards and community guidelines, of course.

# What is NOT Accepted?

## Bad Dependencies

The platform standards are enforced in PRs.

## Large Surprises

Large, surprising changes with no previous consensus or discussion may not be approved with
a request for discussion. This type of change is an unreasonable burden to any maintainer. If
you want to make large changes, you must work together with the NoSQLBench builder community to
make sure it fits well within the project, and is on-mission with the core outcomes of the project.

## Untested or Untestable Code

You must design your contributions in a way that allows for testing.
