/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * @Library
 * Collection utilities for NoSQLBench expressions.
 * Provides functions for working with lists, maps, and sets including
 * filtering, transformation, aggregation, and utility operations.
 */

import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec
import io.nosqlbench.nb.api.expr.annotations.ExprExample

@ExprFunctionSpec(
    synopsis = "take(list, n)",
    description = "Get the first n elements of a list"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "3"], expect = "[1, 2, 3]")
def take(list, n) {
    list.take(n)
}

@ExprFunctionSpec(
    synopsis = "takeLast(list, n)",
    description = "Get the last n elements of a list"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "2"], expect = "[4, 5]")
def takeLast(list, n) {
    def size = list.size()
    if (n >= size) return list
    list.drop(size - n)
}

@ExprFunctionSpec(
    synopsis = "skip(list, n)",
    description = "Skip the first n elements of a list"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "2"], expect = "[3, 4, 5]")
def skip(list, n) {
    list.drop(n)
}

@ExprFunctionSpec(
    synopsis = "chunk(list, size)",
    description = "Chunk a list into sublists of specified size"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "2"], expect = "[[1, 2], [3, 4], [5]]")
def chunk(list, size) {
    list.collate(size)
}

@ExprFunctionSpec(
    synopsis = "flattenList(list)",
    description = "Flatten a nested list structure"
)
@ExprExample(args = ["[[1, 2], [3, [4, 5]]]"], expect = "[1, 2, 3, 4, 5]")
def flattenList(list) {
    list.flatten()
}

@ExprFunctionSpec(
    synopsis = "unique(list)",
    description = "Get unique elements from a list"
)
@ExprExample(args = ["[1, 2, 2, 3, 3, 3]"], expect = "[1, 2, 3]")
def unique(list) {
    list.unique()
}

@ExprFunctionSpec(
    synopsis = "reverseList(list)",
    description = "Reverse a list"
)
@ExprExample(args = ["[1, 2, 3]"], expect = "[3, 2, 1]")
def reverseList(list) {
    list.reverse()
}

@ExprFunctionSpec(
    synopsis = "sortList(list)",
    description = "Sort a list in ascending order"
)
@ExprExample(args = ["[3, 1, 4, 1, 5]"], expect = "[1, 1, 3, 4, 5]")
def sortList(list) {
    list.sort()
}

@ExprFunctionSpec(
    synopsis = "sortDescending(list)",
    description = "Sort a list in descending order"
)
@ExprExample(args = ["[3, 1, 4, 1, 5]"], expect = "[5, 4, 3, 1, 1]")
def sortDescending(list) {
    list.sort().reverse()
}

@ExprFunctionSpec(
    synopsis = "intersect(list1, list2)",
    description = "Get the intersection of two lists"
)
@ExprExample(args = ["[1, 2, 3]", "[2, 3, 4]"], expect = "[2, 3]")
def intersect(list1, list2) {
    list1.intersect(list2)
}

@ExprFunctionSpec(
    synopsis = "union(list1, list2)",
    description = "Get the union of two lists (unique elements from both)"
)
@ExprExample(args = ["[1, 2]", "[2, 3]"], expect = "[1, 2, 3]")
def union(list1, list2) {
    (list1 + list2).unique()
}

@ExprFunctionSpec(
    synopsis = "difference(list1, list2)",
    description = "Get elements in first list but not in second"
)
@ExprExample(args = ["[1, 2, 3]", "[2, 3, 4]"], expect = "[1]")
def difference(list1, list2) {
    list1 - list2
}

@ExprFunctionSpec(
    synopsis = "contains(list, element)",
    description = "Check if a list contains an element"
)
@ExprExample(args = ["[1, 2, 3]", "2"], expect = "true")
def contains(list, element) {
    list.contains(element)
}

@ExprFunctionSpec(
    synopsis = "allMatch(list, condition)",
    description = "Check if all elements in a list match a condition"
)
@ExprExample(args = ["[2, 4, 6]", "{ it % 2 == 0 }"], expect = "true")
def allMatch(list, condition) {
    list.every(condition)
}

@ExprFunctionSpec(
    synopsis = "anyMatch(list, condition)",
    description = "Check if any element in a list matches a condition"
)
@ExprExample(args = ["[1, 2, 3]", "{ it > 2 }"], expect = "true")
def anyMatch(list, condition) {
    list.any(condition)
}

@ExprFunctionSpec(
    synopsis = "countMatching(list, condition)",
    description = "Count elements in a list that match a condition"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "{ it > 2 }"], expect = "3")
def countMatching(list, condition) {
    list.count(condition)
}

@ExprFunctionSpec(
    synopsis = "groupBy(list, keyFunc)",
    description = "Group elements by a key function"
)
@ExprExample(args = ["[1, 2, 3, 4]", "{ it % 2 }"], expect = "[0: [2, 4], 1: [1, 3]]")
def groupBy(list, keyFunc) {
    list.groupBy(keyFunc)
}

@ExprFunctionSpec(
    synopsis = "partition(list, condition)",
    description = "Partition a list into two lists based on a condition"
)
@ExprExample(args = ["[1, 2, 3, 4]", "{ it % 2 == 0 }"], expect = "[[2, 4], [1, 3]]")
def partition(list, condition) {
    def result = list.split(condition)
    [result[0], result[1]]
}

@ExprFunctionSpec(
    synopsis = "frequency(list)",
    description = "Get the frequency of each element in a list"
)
@ExprExample(args = ["['a', 'b', 'a', 'c', 'a']"], expect = "[a: 3, b: 1, c: 1]")
def frequency(list) {
    list.countBy { it }
}

@ExprFunctionSpec(
    synopsis = "zipLists(list1, list2)",
    description = "Zip two lists together into pairs"
)
@ExprExample(args = ["[1, 2, 3]", "['a', 'b', 'c']"], expect = "[[1, 'a'], [2, 'b'], [3, 'c']]")
def zipLists(list1, list2) {
    [list1, list2].transpose()
}

@ExprFunctionSpec(
    synopsis = "joinList(list, delimiter)",
    description = "Join list elements into a string with delimiter"
)
@ExprExample(args = ["[1, 2, 3]", "', '"], expect = "'1, 2, 3'")
def joinList(list, delimiter) {
    list.join(delimiter)
}

@ExprFunctionSpec(
    synopsis = "mapKeys(map)",
    description = "Get map keys as a list"
)
@ExprExample(args = ["[a: 1, b: 2]"], expect = "['a', 'b']")
def mapKeys(map) {
    map.keySet() as List
}

@ExprFunctionSpec(
    synopsis = "mapValues(map)",
    description = "Get map values as a list"
)
@ExprExample(args = ["[a: 1, b: 2]"], expect = "[1, 2]")
def mapValues(map) {
    map.values() as List
}

@ExprFunctionSpec(
    synopsis = "mergeMaps(map1, map2)",
    description = "Merge two maps (second map wins on conflicts)"
)
@ExprExample(args = ["[a: 1, b: 2]", "[b: 3, c: 4]"], expect = "[a: 1, b: 3, c: 4]")
def mergeMaps(map1, map2) {
    map1 + map2
}

@ExprFunctionSpec(
    synopsis = "filterMapKeys(map, keys)",
    description = "Filter a map by keys"
)
@ExprExample(args = ["[a: 1, b: 2, c: 3]", "['a', 'c']"], expect = "[a: 1, c: 3]")
def filterMapKeys(map, keys) {
    map.subMap(keys)
}

@ExprFunctionSpec(
    synopsis = "invertMap(map)",
    description = "Invert a map (swap keys and values)"
)
@ExprExample(args = ["[a: 1, b: 2]"], expect = "[1: 'a', 2: 'b']")
def invertMap(map) {
    map.collectEntries { [(it.value): it.key] }
}

@ExprFunctionSpec(
    synopsis = "rangeInts(start, end)",
    description = "Create a range of integers"
)
@ExprExample(args = ["1", "5"], expect = "[1, 2, 3, 4, 5]")
def rangeInts(start, end) {
    (start..end).collect()
}

@ExprFunctionSpec(
    synopsis = "repeatElement(element, times)",
    description = "Create a list by repeating an element n times"
)
@ExprExample(args = ["'x'", "3"], expect = "['x', 'x', 'x']")
def repeatElement(element, times) {
    [element] * times
}

@ExprFunctionSpec(
    synopsis = "rotateLeft(list, n)",
    description = "Rotate a list left by n positions"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "2"], expect = "[3, 4, 5, 1, 2]")
def rotateLeft(list, n) {
    def size = list.size()
    if (size == 0) return list
    n = n % size
    list.drop(n) + list.take(n)
}

@ExprFunctionSpec(
    synopsis = "rotateRight(list, n)",
    description = "Rotate a list right by n positions"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]", "2"], expect = "[4, 5, 1, 2, 3]")
def rotateRight(list, n) {
    def size = list.size()
    if (size == 0) return list
    n = n % size
    list.drop(size - n) + list.take(size - n)
}

@ExprFunctionSpec(
    synopsis = "sample(list, n)",
    description = "Sample n random elements from a list without replacement"
)
@ExprExample(expectNotNull = true)
def sample(list, n) {
    def shuffled = new ArrayList(list)
    Collections.shuffle(shuffled)
    shuffled.take(Math.min(n, list.size()))
}
