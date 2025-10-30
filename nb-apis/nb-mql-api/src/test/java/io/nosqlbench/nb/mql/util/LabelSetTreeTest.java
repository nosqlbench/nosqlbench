/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LabelSetTreeTest {

    @Test
    void testEmptyTree() {
        LabelSetTree tree = new LabelSetTree();
        assertNotNull(tree.root());
        assertTrue(tree.root().labels().isEmpty());
        assertTrue(tree.root().metrics().isEmpty());
        assertTrue(tree.root().children().isEmpty());
    }

    @Test
    void testSingleLabelSet() {
        LabelSetTree tree = new LabelSetTree();

        Map<String, String> labels = Map.of("session", "abc123");
        tree.addLabelSet(labels, List.of("metric1", "metric2"));

        assertEquals(1, tree.root().children().size());
        LabelSetTree.Node child = tree.root().children().values().iterator().next();
        assertEquals(labels, child.labels());
        assertEquals(Set.of("metric1", "metric2"), child.metrics());
    }

    @Test
    void testContainmentRelationship() {
        LabelSetTree tree = new LabelSetTree();

        // Add parent label set first
        Map<String, String> parent = Map.of("session", "abc");
        tree.addLabelSet(parent, List.of("memory_max", "memory_used"));

        // Add child label set (contains parent + more)
        Map<String, String> child1 = Map.of("session", "abc", "activity", "read");
        tree.addLabelSet(child1, List.of("ops_complete", "ops_pending"));

        Map<String, String> child2 = Map.of("session", "abc", "activity", "write");
        tree.addLabelSet(child2, List.of("ops_complete", "ops_pending"));

        // Verify structure: root -> parent -> two children
        assertEquals(1, tree.root().children().size());

        LabelSetTree.Node parentNode = tree.root().children().values().iterator().next();
        assertEquals(parent, parentNode.labels());
        assertEquals(Set.of("memory_max", "memory_used"), parentNode.metrics());
        assertEquals(2, parentNode.children().size());

        // Verify children
        List<LabelSetTree.Node> children = new ArrayList<>(parentNode.children().values());
        boolean foundRead = false, foundWrite = false;

        for (LabelSetTree.Node child : children) {
            if (child.labels().get("activity").equals("read")) {
                foundRead = true;
                assertEquals(Set.of("ops_complete", "ops_pending"), child.metrics());
            } else if (child.labels().get("activity").equals("write")) {
                foundWrite = true;
                assertEquals(Set.of("ops_complete", "ops_pending"), child.metrics());
            }
        }

        assertTrue(foundRead && foundWrite, "Should have both read and write children");
    }

    @Test
    void testAddInReverseOrder() {
        LabelSetTree tree = new LabelSetTree();

        // Add child first, then parent
        Map<String, String> child = Map.of("session", "abc", "activity", "read");
        tree.addLabelSet(child, List.of("ops_complete"));

        Map<String, String> parent = Map.of("session", "abc");
        tree.addLabelSet(parent, List.of("memory_max"));

        // Structure should still be: root -> parent -> child
        assertEquals(1, tree.root().children().size());

        LabelSetTree.Node parentNode = tree.root().children().values().iterator().next();
        assertEquals(parent, parentNode.labels());
        assertEquals(Set.of("memory_max"), parentNode.metrics());
        assertEquals(1, parentNode.children().size());

        LabelSetTree.Node childNode = parentNode.children().values().iterator().next();
        assertEquals(child, childNode.labels());
        assertEquals(Set.of("ops_complete"), childNode.metrics());
    }

    @Test
    void testDisjointLabelSets() {
        LabelSetTree tree = new LabelSetTree();

        // Add two label sets with no overlap
        Map<String, String> labels1 = Map.of("session", "abc");
        tree.addLabelSet(labels1, List.of("metric1"));

        Map<String, String> labels2 = Map.of("host", "server1");
        tree.addLabelSet(labels2, List.of("metric2"));

        // Both should be direct children of root
        assertEquals(2, tree.root().children().size());

        for (LabelSetTree.Node child : tree.root().children().values()) {
            if (child.labels().containsKey("session")) {
                assertEquals(Set.of("metric1"), child.metrics());
            } else if (child.labels().containsKey("host")) {
                assertEquals(Set.of("metric2"), child.metrics());
            }
        }
    }

    @Test
    void testMergeTrees() {
        LabelSetTree tree1 = new LabelSetTree();
        tree1.addLabelSet(Map.of("session", "abc"), List.of("metric1"));

        LabelSetTree tree2 = new LabelSetTree();
        tree2.addLabelSet(Map.of("session", "abc"), List.of("metric2"));
        tree2.addLabelSet(Map.of("session", "abc", "activity", "read"), List.of("metric3"));

        tree1.merge(tree2);

        // Should have structure: root -> session=abc (metric1, metric2) -> activity=read (metric3)
        assertEquals(1, tree1.root().children().size());

        LabelSetTree.Node sessionNode = tree1.root().children().values().iterator().next();
        assertEquals(Map.of("session", "abc"), sessionNode.labels());
        assertEquals(Set.of("metric1", "metric2"), sessionNode.metrics());
        assertEquals(1, sessionNode.children().size());

        LabelSetTree.Node activityNode = sessionNode.children().values().iterator().next();
        assertEquals(Set.of("metric3"), activityNode.metrics());
    }

    @Test
    void testRender() {
        LabelSetTree tree = new LabelSetTree();

        tree.addLabelSet(Map.of("session", "abc"), List.of("memory_max", "memory_used"));
        tree.addLabelSet(Map.of("session", "abc", "activity", "read"), List.of("ops_complete", "ops_pending"));
        tree.addLabelSet(Map.of("session", "abc", "activity", "write"), List.of("ops_complete"));

        List<String> lines = tree.render(false);

        // Verify basic structure is present
        assertFalse(lines.isEmpty());
        System.out.println("=== Rendered Tree ===");
        lines.forEach(System.out::println);

        // Should contain session label, activities, and metrics
        String rendered = String.join("\n", lines);
        assertTrue(rendered.contains("session=abc"));
        assertTrue(rendered.contains("activity=read"));
        assertTrue(rendered.contains("activity=write"));
        assertTrue(rendered.contains("ops_complete"));
    }

    @Test
    void testComplexThreeLevelHierarchy() {
        LabelSetTree tree = new LabelSetTree();

        // Level 1: Just session
        tree.addLabelSet(Map.of("session", "xyz"), List.of("on_heap_memory_max"));

        // Level 2: session + activity
        tree.addLabelSet(
            Map.of("session", "xyz", "activity", "UNNAMEDACTIVITY"),
            Collections.emptyList()
        );

        // Level 3: session + activity + container
        tree.addLabelSet(
            Map.of("session", "xyz", "activity", "UNNAMEDACTIVITY", "container", "default"),
            List.of("ops_active", "ops_complete", "ops_pending", "pending_ops_total")
        );

        System.out.println("\n=== Three Level Hierarchy ===");
        tree.render(false).forEach(System.out::println);

        // Verify structure
        assertEquals(1, tree.root().children().size());

        LabelSetTree.Node level1 = tree.root().children().values().iterator().next();
        assertEquals(1, level1.labels().size());
        assertEquals("xyz", level1.labels().get("session"));
        assertEquals(1, level1.children().size());

        LabelSetTree.Node level2 = level1.children().values().iterator().next();
        assertEquals(2, level2.labels().size());
        assertEquals(1, level2.children().size());

        LabelSetTree.Node level3 = level2.children().values().iterator().next();
        assertEquals(3, level3.labels().size());
        assertEquals(4, level3.metrics().size());
    }

    @Test
    void testNodeSignature() {
        LabelSetTree.Node node1 = new LabelSetTree.Node(
            new LinkedHashMap<>(Map.of("b", "2", "a", "1"))
        );
        LabelSetTree.Node node2 = new LabelSetTree.Node(
            new LinkedHashMap<>(Map.of("a", "1", "b", "2"))
        );

        // Signatures should be the same regardless of insertion order
        assertEquals(node1.signature(), node2.signature());
        assertEquals("a=1,b=2", node1.signature());
    }

    @Test
    void testIsProperSubsetOf() {
        LabelSetTree.Node parent = new LabelSetTree.Node(Map.of("session", "abc"));

        Map<String, String> childLabels = Map.of("session", "abc", "activity", "read");
        assertTrue(parent.isProperSubsetOf(childLabels));

        Map<String, String> sameLabels = Map.of("session", "abc");
        assertFalse(parent.isProperSubsetOf(sameLabels));

        Map<String, String> differentLabels = Map.of("session", "xyz");
        assertFalse(parent.isProperSubsetOf(differentLabels));

        Map<String, String> disjointLabels = Map.of("host", "server1");
        assertFalse(parent.isProperSubsetOf(disjointLabels));
    }

    @Test
    void testCondenseOption() {
        LabelSetTree tree = new LabelSetTree();

        // Add label sets that would benefit from condensation
        tree.addLabelSet(Map.of("session", "abc", "activity", "read"), List.of("ops_complete"));
        tree.addLabelSet(Map.of("session", "abc", "activity", "write"), List.of("ops_complete"));
        tree.addLabelSet(Map.of("session", "abc", "activity", "delete"), List.of("ops_complete"));

        // Test with condensation enabled (default)
        DisplayTree condensedTree = DisplayTree.fromLabelSetTree(tree, true);
        List<String> condensedLines = condensedTree.render(false);

        // Test with condensation disabled
        DisplayTree nonCondensedTree = DisplayTree.fromLabelSetTree(tree, false);
        List<String> nonCondensedLines = nonCondensedTree.render(false);

        System.out.println("\n=== Condensed Tree (condense=true) ===");
        condensedLines.forEach(System.out::println);

        System.out.println("\n=== Non-Condensed Tree (condense=false) ===");
        nonCondensedLines.forEach(System.out::println);

        // Verify that condensed tree is more compact
        // (it should have fewer lines due to condensation)
        assertTrue(condensedLines.size() <= nonCondensedLines.size(),
            "Condensed tree should have fewer or equal lines than non-condensed tree");
    }
}
