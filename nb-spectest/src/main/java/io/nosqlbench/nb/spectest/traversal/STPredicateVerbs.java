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

package io.nosqlbench.nb.spectest.traversal;

public class STPredicateVerbs {

    /**
     * see {@link STBreadthFirstPredicate} for details.
     */
    public static STBreadthFirstPredicate breadth(Object... specs) {
        return new STBreadthFirstPredicate(specs);
    }

    public static STDepthFirstPredicate depth(Object... specs) {
        return new STDepthFirstPredicate(specs);
    }

    public static STPairWisePredicate pairwise(Object... specs) {
        return new STPairWisePredicate(specs);
    }

    public static STAndPredicate and(Object... specs) {
        return new STAndPredicate(specs);
    }

    public static STDeepMatchAnyPredicate deepany(Object... specs) {
        return new STDeepMatchAnyPredicate(specs);
    }

    public static STArgumentRef ref(int ref) {
        return new STArgumentRef(ref);
    }
}
