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
 * projective simulation ... TBD
 * of vector spaces
 * within which provably correct KNN relationships can be derived from affine ordinal relationships.
 * In other words, vectors in some projective  space which are addressable by some ordinal identity
 * can be constructed with procedural generation methods, and provably correct KNN neighborhoods of
 * some size can be derived on the fly in a closed form calculation.</P>
 *
 * <P>The vector spaces constructed in this way are not intended nor guaranteed to be dimensionally disperse.
 * They are meant to provide an algebraic basis for exercising vector storage systems with increasing
 * cardinality of vectors. This means that vector stores can be tested to incrementally higher limits
 * while their performance and accuracy are both measured.</P>
 *
 * <P>Each vector scheme in this method has the following properties:
 * <UL>
 *     <LI>All vectors within the space are enumerable. Each increasing ordinal value describes a new and distinct
 *     vector. The value of this vector is deterministic within the parameters of the space.</LI>
 * </UL>
 * </P>
 *
 * <P>This work is largely inspired by the DNN or "Das/Direct Nearest Neighbor" method, pioneered by
 * Shaunak Das at DataStax. Additional implementations and ideas are contributed by the vector performance
 * team and our testing community.</P>
 */
package io.nosqlbench.virtdata.lib.vectors.dnn;
