/*
 * Copyright (c) 2023-2024 nosqlbench
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
 * <P>This package contains experimental support for new methods for testing vector stores.
 * The primary method employed is functional mapping of ordinal spaces to vector spaces.
 * In this way, closed-form functions can be used to synthesize vectors and provably correct neighborhoods
 * as if they were defined in a static dataset. This allows for arbitrary testing scenarios to be
 * created and used immediately and with no need to regenerate or compute any data beforehand.</P>
 *
 * <P>The original concept for this was derived by Shaunak Das, in the form of (Das) Direct Nearest Neighbor.
 * Additional methods have been implemented using this technique to include additional space mappings
 * for other vector distance functions.</P>
 *
 * <P>The testing methods enabled by this approach include:
 * <OL>
 * <LI>Generation of a population of vectors which are enumerable and stable with respect to their
 * ordinal addresses.</LI>
 * <LI>Generation of ordered subsets of this population which maintain a unique local ordering in
 * terms of the selected distance function, otherwise known as rank for KNN queries.</LI>
 * <LI>Validation of results for nearest neighborhood queries, using synthetic results computed on the fly as the
 * basis for correctness.</LI>
 * </OL>
 * </P>
 *
 * <P>The vector spaces constructed in this way are not intended nor guaranteed to be dimensionally disperse.
 * They are meant to provide an algebraic basis for exercising vector storage systems with increasing
 * cardinality of vectors. This means that vector stores can be tested to incrementally higher limits
 * while their performance and accuracy are both measured.</P>
 *
 * <P>Each vector scheme in this method has the following properties:
 * <UL>
 * <LI>Each virtual vector space is defined by a set of parameters which are used as inputs to the
 * mapping functions. The space, and the definition of valid vectors in a neighborhood depend on these
 * for stability and correctness. Thus each space is explicitly defined by and inseparable from its parameters.</LI>
 * <LI>All vectors within the space are enumerable. Each increasing ordinal value describes a new and distinct
 * vector. The value of this vector is deterministic within the parameters of the space.</LI>
 * <LI>Each vector within a space is a valid query vector which implies a correct set of distance-ranked
 * neighbors up to some neighborhood size for the related distance function.</LI>
 * <LI>Nearest neighbors may have equal distance in some cases, for which ties are accommodated in testing
 * assertions. Suppose the distance from v<sub>10</sub> to v<sub>5</sub> is the same as the distance from
 * v<sub>10</sub> to v<sub>15</sub>, then both v<sub>5</sub> and v<sub>15</sub> should be interchangeable as
 * correct elements in any KNN results for query vector v<sub>10</sub>, provided that their distances are within
 * the top K results as otherwise expected.
 * </LI>
 * </UL>
 *
 * <P>TBD: Explain the above in terms of specific implementations and parameters.</P>
 * </P>
 */
package io.nosqlbench.virtdata.lib.vectors.dnn;
