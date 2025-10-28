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

import java.util.*;

/**
 * An algebraic tree structure for organizing metrics by their label sets.
 *
 * <p>The tree represents label set containment relationships:
 * <ul>
 *   <li>Root node has empty label set</li>
 *   <li>Parent nodes have fewer labels than their children</li>
 *   <li>Child label sets fully contain all parent labels plus additional ones</li>
 *   <li>Metrics are attached to nodes with their exact label set</li>
 * </ul>
 *
 * <p>This structure is algebraic - two trees can be merged together to produce
 * a combined tree with consistent structure.
 *
 * <p>Example structure:
 * <pre>
 * {} (root)
 * ├── {session=abc}
 * │   ├── {session=abc, activity=read}
 * │   │   └── metrics: [ops_complete, ops_pending]
 * │   └── {session=abc, activity=write}
 * │       └── metrics: [ops_complete, ops_pending]
 * └── {host=server1}
 *     └── metrics: [cpu_usage, memory_usage]
 * </pre>
 */
public class LabelSetTree {

    private final Node root;

    /**
     * Node in the label set tree.
     * Each node represents a unique label set and can have:
     * - Child nodes with more elaborate label sets (containment relationship)
     * - Metrics that have exactly this label set
     *
     * @param labels The label set for this node (immutable)
     * @param metrics Metrics that have exactly this label set (mutable)
     * @param children Child nodes with more elaborate label sets (mutable)
     */
    public record Node(
        Map<String, String> labels,
        Set<String> metrics,
        Map<String, Node> children
    ) {
        /**
         * Create a new node with the given labels.
         */
        public Node(Map<String, String> labels) {
            this(
                Collections.unmodifiableMap(new LinkedHashMap<>(labels)),
                new LinkedHashSet<>(),
                new LinkedHashMap<>()
            );
        }

        /**
         * Get a signature string for this label set for use as a map key.
         */
        public String signature() {
            if (labels.isEmpty()) {
                return "";
            }
            return labels.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        }

        /**
         * Check if this node's label set is a proper subset of another label set.
         * Returns true if all labels in this node are present in otherLabels with the same values,
         * and otherLabels has at least one additional label.
         */
        public boolean isProperSubsetOf(Map<String, String> otherLabels) {
            if (labels.size() >= otherLabels.size()) {
                return false;
            }
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                if (!otherLabels.containsKey(entry.getKey()) ||
                    !otherLabels.get(entry.getKey()).equals(entry.getValue())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Check if this node's label set exactly equals another label set.
         */
        public boolean labelsEqual(Map<String, String> otherLabels) {
            return labels.equals(otherLabels);
        }
    }

    /**
     * Create a new empty label set tree.
     */
    public LabelSetTree() {
        this.root = new Node(Collections.emptyMap());
    }

    /**
     * Get the root node of the tree.
     */
    public Node root() {
        return root;
    }

    /**
     * Add a label set with its associated metrics to the tree.
     * The label set will be placed in the tree according to its containment relationships.
     *
     * @param labels The label set
     * @param metrics The metrics that have this exact label set
     */
    public void addLabelSet(Map<String, String> labels, Collection<String> metrics) {
        Node node = findOrCreateNode(labels);
        node.metrics.addAll(metrics);
    }

    /**
     * Find or create a node for the given label set.
     * This method ensures proper tree structure based on label set containment.
     */
    private Node findOrCreateNode(Map<String, String> labels) {
        if (labels.isEmpty()) {
            return root;
        }

        // Find the node by traversing from root
        return findOrCreateNodeRecursive(root, labels);
    }

    /**
     * Recursively find or create a node, maintaining containment relationships.
     */
    private Node findOrCreateNodeRecursive(Node current, Map<String, String> targetLabels) {
        // Check if we've found an exact match
        if (current.labelsEqual(targetLabels)) {
            return current;
        }

        // Look for a child that matches or is a subset of target
        for (Node child : current.children.values()) {
            if (child.labelsEqual(targetLabels)) {
                return child;
            }
            // If child is a proper subset of target, recurse into it
            if (child.isProperSubsetOf(targetLabels)) {
                return findOrCreateNodeRecursive(child, targetLabels);
            }
        }

        // No matching child found, create new node under current
        Node newNode = new Node(targetLabels);
        current.children.put(newNode.signature(), newNode);

        // Check if any existing children should be moved under this new node
        List<Node> childrenToMove = new ArrayList<>();
        for (Node child : current.children.values()) {
            if (child != newNode && newNode.isProperSubsetOf(child.labels)) {
                childrenToMove.add(child);
            }
        }

        // Move children under the new node
        for (Node child : childrenToMove) {
            current.children.remove(child.signature());
            newNode.children.put(child.signature(), child);
        }

        return newNode;
    }

    /**
     * Merge another tree into this tree.
     * This is an algebraic operation that combines two trees consistently.
     *
     * @param other The tree to merge into this one
     */
    public void merge(LabelSetTree other) {
        mergeNodes(this.root, other.root);
    }

    /**
     * Recursively merge nodes from another tree.
     */
    private void mergeNodes(Node target, Node source) {
        // Merge metrics
        target.metrics.addAll(source.metrics);

        // Merge children
        for (Node sourceChild : source.children.values()) {
            Node targetChild = target.children.get(sourceChild.signature());
            if (targetChild != null) {
                // Child exists, merge recursively
                mergeNodes(targetChild, sourceChild);
            } else {
                // Child doesn't exist, need to add it
                // But we need to ensure proper containment relationships
                addSubtree(target, sourceChild);
            }
        }
    }

    /**
     * Add an entire subtree from another tree, maintaining proper structure.
     */
    private void addSubtree(Node target, Node subtreeRoot) {
        // Find where this subtree should be inserted
        Node insertionPoint = findOrCreateNode(subtreeRoot.labels);

        // Copy metrics
        insertionPoint.metrics.addAll(subtreeRoot.metrics);

        // Recursively copy children
        for (Node child : subtreeRoot.children.values()) {
            addSubtree(insertionPoint, child);
        }
    }

    /**
     * Render the tree as a hierarchical string representation.
     * Each node displays only the labels that differ from its parent (differential labels).
     *
     * @param colorize Whether to apply ANSI color codes
     * @return List of rendered lines
     */
    public List<String> render(boolean colorize) {
        List<String> lines = new ArrayList<>();
        renderNode(root, Collections.emptyMap(), "", true, true, lines, colorize);
        return lines;
    }

    /**
     * Recursively render a node and its children.
     * @param node The node to render
     * @param parentLabels The labels from the parent node
     * @param prefix The indentation prefix for this line
     * @param isLast Whether this is the last child of its parent
     * @param isRoot Whether this is the root node
     * @param lines Output list of rendered lines
     * @param colorize Whether to apply ANSI colors
     */
    private void renderNode(Node node, Map<String, String> parentLabels, String prefix,
                           boolean isLast, boolean isRoot, List<String> lines, boolean colorize) {
        // Don't render the root node itself
        if (!isRoot) {
            // Compute differential labels (labels in this node but not in parent)
            Map<String, String> diffLabels = computeDifferentialLabels(node.labels, parentLabels);
            String labelStr = formatLabels(diffLabels);
            if (colorize) {
                labelStr = AnsiColors.colorizeLabel(labelStr);
            }
            lines.add(prefix + labelStr);
        }

        // Calculate prefix for children
        String childPrefix;
        if (isRoot) {
            childPrefix = "";
        } else {
            String connector = isLast ? "    " : "│   ";
            childPrefix = prefix.replaceFirst("└── $", connector)
                               .replaceFirst("├── $", connector);
        }

        // Render metrics first (as leaves)
        List<String> sortedMetrics = new ArrayList<>(node.metrics);
        Collections.sort(sortedMetrics);

        // Render child nodes
        List<Node> childNodes = new ArrayList<>(node.children.values());

        int totalChildren = sortedMetrics.size() + childNodes.size();
        int index = 0;

        // Metrics first
        for (String metric : sortedMetrics) {
            boolean isLastChild = (index == totalChildren - 1);
            String branch = isRoot ? "" : (isLastChild ? "└── " : "├── ");
            String branchStr = colorize ? AnsiColors.colorizeTreeBranch(branch) : branch;
            String metricStr = colorize ? AnsiColors.colorizeMetric(metric) : metric;
            lines.add(childPrefix + branchStr + metricStr);
            index++;
        }

        // Then child label sets
        for (Node child : childNodes) {
            boolean isLastChild = (index == totalChildren - 1);
            String branch = isRoot ? "" : (isLastChild ? "└── " : "├── ");
            String branchStr = colorize ? AnsiColors.colorizeTreeBranch(branch) : branch;
            renderNode(child, node.labels, childPrefix + branchStr, isLastChild, false, lines, colorize);
            index++;
        }
    }

    /**
     * Compute the differential labels between a node and its parent.
     * Returns only the labels that are in node but not in parent (or have different values).
     */
    private Map<String, String> computeDifferentialLabels(Map<String, String> nodeLabels,
                                                           Map<String, String> parentLabels) {
        Map<String, String> diff = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : nodeLabels.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // Include if not in parent, or if value is different
            if (!parentLabels.containsKey(key) || !parentLabels.get(key).equals(value)) {
                diff.put(key, value);
            }
        }
        return diff;
    }

    /**
     * Format a label map as a display string.
     */
    private String formatLabels(Map<String, String> labels) {
        if (labels.isEmpty()) {
            return "{}";
        }
        return labels.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("{}");
    }

    /**
     * Count the total number of metrics across all nodes in the tree.
     */
    public int countMetrics() {
        return countMetricsRecursive(root);
    }

    private int countMetricsRecursive(Node node) {
        int count = node.metrics.size();
        for (Node child : node.children.values()) {
            count += countMetricsRecursive(child);
        }
        return count;
    }

    /**
     * Count the total number of nodes (label sets) in the tree, excluding root.
     */
    public int countNodes() {
        return countNodesRecursive(root) - 1; // Exclude root
    }

    private int countNodesRecursive(Node node) {
        int count = 1;
        for (Node child : node.children.values()) {
            count += countNodesRecursive(child);
        }
        return count;
    }
}
