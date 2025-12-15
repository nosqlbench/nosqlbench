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
import java.util.stream.Collectors;

/**
 * A display-optimized tree derived from a canonical LabelSetTree.
 *
 * <p>While LabelSetTree maintains strict containment relationships,
 * DisplayTree condenses multiple label sets with common prefixes for
 * more concise display.
 *
 * <p>Example transformation:
 * <pre>
 * Canonical (flat siblings):
 * - {activity=read, env=prod, host=server1, region=us-east}
 * - {activity=read, env=prod, host=server1, region=us-west}
 * - {activity=read, env=prod, host=server2, region=us-east}
 *
 * Display (condensed hierarchy):
 * activity=read, env=prod
 * ├── host=server1
 * │   ├── region=us-east
 * │   └── region=us-west
 * └── host=server2
 *     └── region=us-east
 * </pre>
 */
public class DisplayTree {

    /**
     * Display node that may represent multiple canonical label sets.
     * Contains differential labels and metrics.
     */
    public record DisplayNode(
        Map<String, String> labels,
        Set<String> metrics,
        Map<String, DisplayNode> children
    ) {
        public DisplayNode(Map<String, String> labels) {
            this(
                Collections.unmodifiableMap(new LinkedHashMap<>(labels)),
                new LinkedHashSet<>(),
                new LinkedHashMap<>()
            );
        }
    }

    private final DisplayNode root;
    private final boolean condenseTree;

    /**
     * Create a DisplayTree from a canonical LabelSetTree with default condensation enabled.
     */
    public static DisplayTree fromLabelSetTree(LabelSetTree canonicalTree) {
        return fromLabelSetTree(canonicalTree, true);
    }

    /**
     * Create a DisplayTree from a canonical LabelSetTree.
     *
     * @param canonicalTree The canonical tree to convert
     * @param condenseTree If true (default), apply condensation algorithms to merge
     *                     siblings with common patterns. If false, preserve the original
     *                     structure without condensation, showing each metric instance
     *                     separately with full label sets.
     */
    public static DisplayTree fromLabelSetTree(LabelSetTree canonicalTree, boolean condenseTree) {
        DisplayTree displayTree = new DisplayTree(condenseTree);
        displayTree.buildFrom(canonicalTree.root());

        if (condenseTree) {
            // Post-process: condense siblings with same metrics but different single label value
            displayTree.condenseSiblingsWithSameMetrics(displayTree.root);

            // Final consolidation: merge siblings with identical label sets
            displayTree.consolidateIdenticalLabelSets(displayTree.root);
        }

        return displayTree;
    }

    private DisplayTree() {
        this(true);
    }

    private DisplayTree(boolean condenseTree) {
        this.root = new DisplayNode(Collections.emptyMap());
        this.condenseTree = condenseTree;
    }

    public DisplayNode root() {
        return root;
    }

    /**
     * Build display tree from canonical tree by condensing siblings with common prefixes.
     */
    private void buildFrom(LabelSetTree.Node canonicalNode) {
        buildDisplayNode(root, canonicalNode, Collections.emptyMap());
    }

    /**
     * Recursively build display nodes from canonical nodes.
     *
     * @param displayParent The parent display node
     * @param canonicalNode The canonical node to process
     * @param parentLabels The accumulated labels from parent nodes
     */
    private void buildDisplayNode(DisplayNode displayParent, LabelSetTree.Node canonicalNode,
                                   Map<String, String> parentLabels) {
        // Add metrics from this canonical node to the display parent
        displayParent.metrics.addAll(canonicalNode.metrics());

        // If no children, we're done
        if (canonicalNode.children().isEmpty()) {
            return;
        }

        // Collect all child canonical nodes
        List<LabelSetTree.Node> children = new ArrayList<>(canonicalNode.children().values());

        // Try to condense children by finding common label prefixes
        condenseAndBuildChildren(displayParent, children, canonicalNode.labels());
    }

    /**
     * Condense children by finding common label prefixes and building a hierarchy.
     */
    private void condenseAndBuildChildren(DisplayNode displayParent,
                                          List<LabelSetTree.Node> canonicalChildren,
                                          Map<String, String> parentLabels) {
        if (canonicalChildren.isEmpty()) {
            return;
        }

        // If only one child, just add it directly with all its differential labels
        if (canonicalChildren.size() == 1) {
            LabelSetTree.Node singleChild = canonicalChildren.get(0);
            Map<String, String> diffLabels = computeDifferential(singleChild.labels(), parentLabels);

            // Only create a node if there are differential labels
            if (!diffLabels.isEmpty()) {
                DisplayNode displayChild = new DisplayNode(diffLabels);
                displayParent.children.put(formatLabels(diffLabels), displayChild);

                // Add metrics and process grandchildren
                displayChild.metrics.addAll(singleChild.metrics());
                if (!singleChild.children().isEmpty()) {
                    List<LabelSetTree.Node> grandchildren = new ArrayList<>(singleChild.children().values());
                    condenseAndBuildChildren(displayChild, grandchildren, singleChild.labels());
                }
            } else {
                // No differential labels - just add metrics to parent
                displayParent.metrics.addAll(singleChild.metrics());
                if (!singleChild.children().isEmpty()) {
                    List<LabelSetTree.Node> grandchildren = new ArrayList<>(singleChild.children().values());
                    condenseAndBuildChildren(displayParent, grandchildren, singleChild.labels());
                }
            }
            return;
        }

        // Find the longest common prefix across all children
        Map<String, String> commonPrefix = findCommonPrefix(canonicalChildren, parentLabels);

        if (!commonPrefix.isEmpty()) {
            // Create display node for common prefix
            DisplayNode prefixNode = new DisplayNode(commonPrefix);
            displayParent.children.put(formatLabels(commonPrefix), prefixNode);

            // Update parent labels and recursively condense remaining labels
            Map<String, String> newParentLabels = new LinkedHashMap<>(parentLabels);
            newParentLabels.putAll(commonPrefix);
            condenseAndBuildChildren(prefixNode, canonicalChildren, newParentLabels);

        } else {
            // No common prefix - group by first differential label key-value pair
            Map<String, List<LabelSetTree.Node>> grouped = groupByFirstDifferentialLabelPair(
                canonicalChildren, parentLabels
            );

            // Create a node for each group
            for (Map.Entry<String, List<LabelSetTree.Node>> entry : grouped.entrySet()) {
                List<LabelSetTree.Node> group = entry.getValue();

                // Recursively process this group
                condenseAndBuildChildren(displayParent, group, parentLabels);
            }
        }
    }

    /**
     * Group canonical nodes by their first differential label key=value pair.
     */
    private Map<String, List<LabelSetTree.Node>> groupByFirstDifferentialLabelPair(
            List<LabelSetTree.Node> nodes, Map<String, String> parentLabels) {

        Map<String, List<LabelSetTree.Node>> grouped = new LinkedHashMap<>();

        for (LabelSetTree.Node node : nodes) {
            Map<String, String> diff = computeDifferential(node.labels(), parentLabels);
            if (!diff.isEmpty()) {
                // Get first label key=value pair (they're sorted in the map)
                Map.Entry<String, String> firstEntry = diff.entrySet().iterator().next();
                String firstPair = firstEntry.getKey() + "=" + firstEntry.getValue();
                grouped.computeIfAbsent(firstPair, k -> new ArrayList<>()).add(node);
            }
        }

        return grouped;
    }

    /**
     * Find common label prefix across multiple nodes.
     * Returns labels that are present with the same value in all nodes.
     */
    private Map<String, String> findCommonPrefix(List<LabelSetTree.Node> nodes,
                                                  Map<String, String> parentLabels) {
        if (nodes.isEmpty()) {
            return Collections.emptyMap();
        }

        // Start with differential of first node
        Map<String, String> common = new LinkedHashMap<>(
            computeDifferential(nodes.get(0).labels(), parentLabels)
        );

        // Intersect with all other nodes
        for (int i = 1; i < nodes.size(); i++) {
            Map<String, String> nodeDiff = computeDifferential(nodes.get(i).labels(), parentLabels);
            common.entrySet().removeIf(entry ->
                !nodeDiff.containsKey(entry.getKey()) ||
                !nodeDiff.get(entry.getKey()).equals(entry.getValue())
            );

            if (common.isEmpty()) {
                break;
            }
        }

        return common;
    }

    /**
     * Compute differential labels: labels in node but not in parent.
     */
    private Map<String, String> computeDifferential(Map<String, String> nodeLabels,
                                                     Map<String, String> parentLabels) {
        Map<String, String> diff = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : nodeLabels.entrySet()) {
            if (!parentLabels.containsKey(entry.getKey()) ||
                !parentLabels.get(entry.getKey()).equals(entry.getValue())) {
                diff.put(entry.getKey(), entry.getValue());
            }
        }
        return diff;
    }

    /**
     * Format labels as a display string.
     */
    private String formatLabels(Map<String, String> labels) {
        if (labels.isEmpty()) {
            return "{}";
        }
        return labels.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(", "));
    }

    /**
     * Condense sibling nodes that have identical child metrics but differ by only one label.
     * Recursively processes the entire tree.
     */
    private void condenseSiblingsWithSameMetrics(DisplayNode node) {
        // First, recursively process all children
        for (DisplayNode child : new ArrayList<>(node.children.values())) {
            condenseSiblingsWithSameMetrics(child);
        }

        // Now condense siblings at this level
        if (node.children.size() < 2) {
            return; // Need at least 2 siblings to condense
        }

        // Group children by their structure signature (metrics + grandchildren structure)
        Map<String, List<DisplayNode>> structureGroups = new LinkedHashMap<>();
        for (DisplayNode child : node.children.values()) {
            String signature = computeStructureSignature(child);
            structureGroups.computeIfAbsent(signature, k -> new ArrayList<>()).add(child);
        }

        // For each group with multiple members, check if they can be condensed
        for (List<DisplayNode> group : structureGroups.values()) {
            if (group.size() < 2) {
                continue;
            }

            // Check if all nodes in group differ by exactly one label
            String differingLabelKey = findSingleDifferingLabel(group);
            if (differingLabelKey != null) {
                // Can condense! Create new node with comma-separated values
                condenseGroup(node, group, differingLabelKey);
            }
        }
    }

    /**
     * Compute a signature for a node's structure (metrics + child structure).
     */
    private String computeStructureSignature(DisplayNode node) {
        StringBuilder sig = new StringBuilder();

        // Add sorted metrics
        sig.append("M:");
        node.metrics.stream().sorted().forEach(m -> sig.append(m).append(","));

        // Add child keys (labels)
        sig.append("|C:");
        node.children.keySet().stream().sorted().forEach(k -> sig.append(k).append(","));

        return sig.toString();
    }

    /**
     * Find the label key that differs across all nodes in the group.
     * Returns the key if exactly one label differs, null otherwise.
     */
    private String findSingleDifferingLabel(List<DisplayNode> group) {
        if (group.size() < 2) {
            return null;
        }

        // All nodes must have the same label keys
        Set<String> firstKeys = group.get(0).labels.keySet();
        for (int i = 1; i < group.size(); i++) {
            if (!group.get(i).labels.keySet().equals(firstKeys)) {
                return null; // Different label keys
            }
        }

        // Find which label has different values
        String differingKey = null;
        for (String key : firstKeys) {
            Set<String> values = new LinkedHashSet<>();
            for (DisplayNode node : group) {
                values.add(node.labels.get(key));
            }

            if (values.size() > 1) {
                // This label has different values
                if (differingKey != null) {
                    return null; // More than one label differs
                }
                differingKey = key;
            }
        }

        return differingKey;
    }

    /**
     * Condense a group of nodes that differ by a single label into one node
     * with comma-separated values.
     */
    private void condenseGroup(DisplayNode parent, List<DisplayNode> group, String differingKey) {
        // Collect all values for the differing label
        List<String> values = group.stream()
            .map(n -> n.labels.get(differingKey))
            .sorted()
            .collect(Collectors.toList());

        // Create new labels with comma-separated values
        Map<String, String> newLabels = new LinkedHashMap<>(group.get(0).labels);
        newLabels.put(differingKey, String.join(",", values));

        // Create new consolidated node
        DisplayNode consolidated = new DisplayNode(newLabels);

        // Copy children and metrics from first node (they're all the same structure)
        consolidated.children.putAll(group.get(0).children);
        consolidated.metrics.addAll(group.get(0).metrics);

        // Remove old nodes and add new consolidated node
        for (DisplayNode oldNode : group) {
            parent.children.remove(formatLabels(oldNode.labels));
        }
        parent.children.put(formatLabels(newLabels), consolidated);
    }

    /**
     * Consolidate siblings that have identical label sets.
     * After condensation, multiple nodes may have the same labels but different metrics/children.
     * This merges them into a single node.
     */
    private void consolidateIdenticalLabelSets(DisplayNode node) {
        // First, recursively process all children
        for (DisplayNode child : new ArrayList<>(node.children.values())) {
            consolidateIdenticalLabelSets(child);
        }

        // Build a new children map with consolidated nodes
        Map<String, DisplayNode> newChildren = new LinkedHashMap<>();

        // Group children by their exact label set (formatted string)
        Map<String, List<DisplayNode>> labelGroups = new LinkedHashMap<>();
        for (DisplayNode child : node.children.values()) {
            String labelKey = formatLabels(child.labels);
            labelGroups.computeIfAbsent(labelKey, k -> new ArrayList<>()).add(child);
        }

        // Process each group
        for (Map.Entry<String, List<DisplayNode>> entry : labelGroups.entrySet()) {
            String labelKey = entry.getKey();
            List<DisplayNode> group = entry.getValue();

            if (group.size() == 1) {
                // Single node - just keep it
                newChildren.put(labelKey, group.get(0));
            } else {
                // Multiple nodes with same label - consolidate them
                DisplayNode consolidated = consolidateNodeGroup(group);
                newChildren.put(labelKey, consolidated);
            }
        }

        // Replace the children map
        node.children.clear();
        node.children.putAll(newChildren);
    }

    /**
     * Consolidate a group of nodes with identical labels into a single node.
     */
    private DisplayNode consolidateNodeGroup(List<DisplayNode> group) {
        DisplayNode consolidated = new DisplayNode(group.get(0).labels);

        // Merge all metrics from all nodes in the group
        for (DisplayNode groupNode : group) {
            consolidated.metrics.addAll(groupNode.metrics);
        }

        // Merge all children from all nodes in the group
        for (DisplayNode groupNode : group) {
            for (Map.Entry<String, DisplayNode> childEntry : groupNode.children.entrySet()) {
                String childKey = childEntry.getKey();
                DisplayNode childNode = childEntry.getValue();

                if (consolidated.children.containsKey(childKey)) {
                    // Child with same key already exists - need to merge them recursively
                    DisplayNode existingChild = consolidated.children.get(childKey);
                    // Merge metrics
                    existingChild.metrics.addAll(childNode.metrics);
                    // Recursively merge grandchildren
                    mergeChildrenRecursive(existingChild, childNode);
                } else {
                    // New child - just add it
                    consolidated.children.put(childKey, childNode);
                }
            }
        }

        return consolidated;
    }

    /**
     * Recursively merge children from source into target.
     */
    private void mergeChildrenRecursive(DisplayNode target, DisplayNode source) {
        for (Map.Entry<String, DisplayNode> entry : source.children.entrySet()) {
            String childKey = entry.getKey();
            DisplayNode sourceChild = entry.getValue();

            if (target.children.containsKey(childKey)) {
                DisplayNode targetChild = target.children.get(childKey);
                targetChild.metrics.addAll(sourceChild.metrics);
                mergeChildrenRecursive(targetChild, sourceChild);
            } else {
                target.children.put(childKey, sourceChild);
            }
        }
    }

    /**
     * Render the display tree with colorization.
     */
    public List<String> render(boolean colorize) {
        List<String> lines = new ArrayList<>();
        renderNode(root, "", true, true, lines, colorize);
        return lines;
    }

    /**
     * Colorize tree branch characters in a prefix string while leaving spaces unchanged.
     */
    private String colorizePrefixBranches(String prefix) {
        // Replace branch patterns with colorized versions
        String result = prefix;
        result = result.replace("└── ", AnsiColors.colorizeTreeBranch("└── "));
        result = result.replace("├── ", AnsiColors.colorizeTreeBranch("├── "));
        result = result.replace("│   ", AnsiColors.colorizeTreeBranch("│   "));
        return result;
    }

    /**
     * Recursively render a display node.
     */
    private void renderNode(DisplayNode node, String prefix, boolean isLast, boolean isRoot,
                           List<String> lines, boolean colorize) {
        // Render this node's labels (skip for root)
        if (!isRoot) {
            String labelStr = formatLabels(node.labels);
            // Colorize both prefix (which may contain branch chars) and label
            String prefixStr = colorize ? colorizePrefixBranches(prefix) : prefix;
            String colorizedLabel = colorize ? AnsiColors.colorizeLabel(labelStr) : labelStr;
            lines.add(prefixStr + colorizedLabel);
        }

        // Calculate prefix for children - this is the indentation WITHOUT the branch character
        String childPrefix;
        if (isRoot) {
            childPrefix = "";
        } else {
            String connector = isLast ? "    " : "│   ";
            // Remove the last branch characters (with trailing space) and replace with connector
            if (prefix.endsWith("└── ")) {
                childPrefix = prefix.substring(0, prefix.length() - 4) + connector;
            } else if (prefix.endsWith("├── ")) {
                childPrefix = prefix.substring(0, prefix.length() - 4) + connector;
            } else {
                childPrefix = prefix + connector;
            }
        }

        // Render metrics and children
        List<String> sortedMetrics = new ArrayList<>(node.metrics);
        Collections.sort(sortedMetrics);

        List<DisplayNode> children = new ArrayList<>(node.children.values());
        int totalItems = sortedMetrics.size() + children.size();

        // Metrics first
        for (int i = 0; i < sortedMetrics.size(); i++) {
            boolean isLastItem = (i == totalItems - 1);
            String branch = isRoot ? "" : (isLastItem ? "└── " : "├── ");
            String branchStr = colorize ? AnsiColors.colorizeTreeBranch(branch) : branch;
            String metricStr = colorize ? AnsiColors.colorizeMetric(sortedMetrics.get(i)) : sortedMetrics.get(i);
            String prefixStr = colorize ? colorizePrefixBranches(childPrefix) : childPrefix;
            lines.add(prefixStr + branchStr + metricStr);
        }

        // Then children
        for (int i = 0; i < children.size(); i++) {
            int itemIndex = sortedMetrics.size() + i;
            boolean isLastItem = (itemIndex == totalItems - 1);
            String branch = isRoot ? "" : (isLastItem ? "└── " : "├── ");
            // Pass uncolorized branch to child (colors applied when rendering label)
            renderNode(children.get(i), childPrefix + branch, isLastItem, false, lines, colorize);
        }
    }
}
