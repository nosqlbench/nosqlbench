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

/**
 * <P>This package and module provides a set of modular testing tools which enable
 * specifications, documentation, and tests to be one and the same. This is a form
 * of literate programming, but more focused on providing a clearly documented
 * and tested specification, including detailed examples and corner cases.</P>
 *
 * <H2>Motivation</H2>
 * <P>There is a common problem in many OSS projects and code bases which has no
 * simple and definitive solution so far: Many APIs, type systems, protocols, and other
 * foundational design elements which are the bedrock of stable systems are themselves
 * unstable in practice. While it is true that this has much to do with management
 * style and project hygiene, and thus varies wildly from project to project, it is
 * still true that there exists no generally accepted method of ensconcing those
 * for users in a way that remains correct, provable, and current. In other words,
 * identifying the foundational elements of a system which should be held up as
 * key concepts and so reliable that they can be taken for granted is crucial to
 * the longevity and utility of any non-trivial OSS project. This module and package
 * are simply a support system to enable some degree of that within the NoSQLBench
 * project.</P>
 *
 * <H2>Concepts</H2>
 * <p>
 * Specifications to be used with this system are expected to be woven into a set
 * of user-appropriate markdown documentation (or other type of documentation system,
 * if you have an need for another, jump in and help!). Any documentation form is
 * allowed as long as there is some regular structure within the documentation that can be
 * used to extract stanzas of specification. These stanzas will generally be a sequence
 * of parsed nodes which fit a sequence pattern. During test phases, provided paths
 * are scanned for matching markdown content, that content is scanned for specific
 * node patterns, and those sequences are extracted into self-contained test definitions,
 * known as {@link io.nosqlbench.nb.spectest.core.STNodeAssembly}. Since all of the wiring
 * for these tests operates on the AST of a parsed markdown file, the
 * {@link com.vladsch.flexmark.util.ast.Node} nomenclature is retained in all the class names.
 * Those test definitions are then fed to the validators which are provided in test code.
 * </p>
 *
 * <H2>Validation Methods</H2>
 * <P>All of the validations are meant to check the correctness of a type of operation. This can be some
 * thing as simple as loading a data structure into memory and verifying that it is equal to a known value. It is important
 * to bear in mind that no specification for a system stands alone. The whole point of having the
 * specification is to capture, at an implementable, testable, mechanical level of detail, how
 * the system in question should behave. More specifically, the specification is meant to say what
 * the system should do with inputs of any kind, and how you can know that it is doing that faithfully.
 * As such, you should endeavor to capture these behaviors within your spec, such that the stanzas
 * hold enough information to actually exercise parts of a system which can prove or disprove it's
 * conformance to a specification. Any tests which do not do this are merely documenting a format and
 * are of little use above and beyond documentation.</p>
 *
 * <H3>Fenced Data</H3>
 * <p>Generally speaking, you will provide fenced code within your documentation that can be presented
 * in a friendly format to readers of your documentation system. These fenced code sections are the
 * easiest place to put data which needs to be verified. Within a stanza, adjacent fenced code sections
 * can represent testable tuples, like (program, output), or (input, program, output). It is up
 * to you to decide what the structure should be. Further, it is useful to look at the fenced code
 * </p>
 *
 * <H3>Output Assertions</H3>
 */
package io.nosqlbench.nb.spectest;
