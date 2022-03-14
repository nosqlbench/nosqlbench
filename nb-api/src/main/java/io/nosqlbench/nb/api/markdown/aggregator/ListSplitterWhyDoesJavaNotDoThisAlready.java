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

package io.nosqlbench.nb.api.markdown.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public class ListSplitterWhyDoesJavaNotDoThisAlready {
    public static <T> List<? extends T> partition(List<? extends T> source, Predicate<T> filterout) {
        ArrayList<T> filtered = new ArrayList<>();
        ListIterator<? extends T> it = source.listIterator();
        while (it.hasNext()) {
            T element = it.next();
            if (filterout.test(element)) {
                it.remove();
                filtered.add(element);
            }
        }
        return filtered;
    }
}
