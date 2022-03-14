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

import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MDGraph {

    private Map<String, List<String>> topicsByPattern;
    private final Map<String, List<Edge<MarkdownInfo>>> elementsByPattern = new HashMap<>();
    private final Map<String, List<Edge<MarkdownInfo>>> elementsByTopic = new HashMap<>();
    private final List<Edge<MarkdownInfo>> elements = new LinkedList<>();

    public void add(MarkdownInfo addingElem) {

        Edge<MarkdownInfo> edge = new Edge<>(addingElem);

        elements.add(edge);

        for (String topic : addingElem.getTopics()) {
            elementsByTopic.computeIfAbsent(topic, t -> new ArrayList<>()).add(edge);
        }

        // Always add elements to the "none" at a minimum
        if (addingElem.getTopics().size() == 0) {
            elementsByTopic.computeIfAbsent("none", t -> new ArrayList<>()).add(edge);
        }

        for (Pattern pattern : addingElem.getTopicGlobs()) {
            elementsByPattern.computeIfAbsent(pattern.pattern(),
                    p -> new ArrayList<>()).add(edge);
        }
    }

    public List<MarkdownInfo> processed() {

        if (topicsByPattern == null) {
            topicsByPattern = topicsByPattern();
        }

        LinkedList<Edge<MarkdownInfo>> resolved = new LinkedList<>(elements);

        ListIterator<Edge<MarkdownInfo>> iter = resolved.listIterator();

        while (iter.hasNext()) {
            Edge<MarkdownInfo> elementEdge = iter.next();
            MarkdownInfo element = elementEdge.get();

            List<Pattern> topicGlobs = element.getTopicGlobs();

            if (topicGlobs.size() != 0) {
                List<Edge<MarkdownInfo>> included = new ArrayList<>();
                boolean leafnodes=true;
                for (Pattern topicGlob : topicGlobs) {
                    for (String matchedTopic : topicsByPattern.get(topicGlob.pattern())) {
                        List<Edge<MarkdownInfo>> edges = elementsByTopic.get(matchedTopic);
                        for (Edge<MarkdownInfo> edge : edges) {
                            if (edge.get().getTopicGlobs().size()!=0) {
                                leafnodes=false;
                            }
                            included.add(edge);
                        }
                    }
                    if (leafnodes) {
                        CompositeMarkdownInfo mdinfo =
                                new CompositeMarkdownInfo();
                        mdinfo.add(element);
                        for (Edge<MarkdownInfo> tEdge : included) {
                            mdinfo.add(tEdge.get());
                        }
                        // TODO: Add included
                        MarkdownInfo withTopics = mdinfo.withTopics(element.getTopics());
                        elementEdge.set(withTopics);
                    } else {
                        // Move this to the end of the list.
                        iter.remove();
                        resolved.addLast(elementEdge);
                    }
                }
            }
        }

        return resolved.stream().map(Edge::get).collect(Collectors.toList());
    }

    private Map<String, List<String>> topicsByPattern() {
        Map<String, List<String>> tbp = new HashMap<>();
        for (String pattern : this.elementsByPattern.keySet()) {
            List<String> matchingTopics = tbp.computeIfAbsent(pattern, p -> new ArrayList<>());
            for (String topic : this.elementsByTopic.keySet()) {
                if (Pattern.compile(pattern).matcher(topic).matches()) {
                    matchingTopics.add(topic);
                }
            }
        }
        return tbp;
    }


    /**
     * Allow edges to point to mutable vertices
     * @param <T>
     */
    private final static class Edge<T extends MarkdownInfo> {
        private T element;

        public Edge(T element) {
            this.element = element;
        }

        public T get() {
            return element;
        }

        public void set(T element) {
            this.element = element;
        }
    }
}
