/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.content;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NBIOSets {

    /**
     * Combine overlapping sets or create new ones with no overlap
     * @param setsData Existing sets
     * @param newSets Additional sets
     * @return combined sets
     * @param <T>
     */
    public static <T extends Comparable<T>> List<Set<T>> combine(List<Set<T>> setsData, Set<T>... newSets) {
        for (Set<T> coset : newSets) {
            Set<T> addTo = null;
            for (Set<T> extensionSet : setsData) {
                Set<T> union = new LinkedHashSet<>(coset);
                for (T entry : coset) {
                    if (extensionSet.contains(entry)) {
                        addTo = extensionSet;
                        break;
                    }
                }
                if (addTo != null) {
                    break;
                }
            }
            if (addTo==null) {
                addTo=new LinkedHashSet<>();
                setsData.add(addTo);
            }
            addTo.addAll(coset);
        }
        return setsData;
    }
}
