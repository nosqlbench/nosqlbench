package io.nosqlbench.nb.api.markdown.aggregator;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MutableFrontMatter implements FrontMatter {
    private final Map<String, List<String>> data;

    public MutableFrontMatter(Map<String, List<String>> data) {
        this.data = data;
    }

    @Override
    public String getTitle() {
        List<String> titles = data.get(FrontMatter.TITLE);
        if (titles==null) {
            return "";
        }
        if (titles.size()!=1) {
            throw new InvalidParameterException(FrontMatter.TITLE + " can only contain a single value.");
        }
        return titles.get(0);
    }

    @Override
    public int getWeight() {
        List<String> weights = data.get(FrontMatter.WEIGHT);
        if (weights==null) {
            return 0;
        }
        if (weights.size()!=1) {
            throw new InvalidParameterException(FrontMatter.WEIGHT + " can only contain a single value.");
        }
        return Integer.parseInt(weights.get(0));
    }

    @Override
    public Set<String> getTopics() {

        List<String> topics = data.get(FrontMatter.TOPICS);
        List<String> topic = data.get(FrontMatter.TOPIC);

        if (topics==null && topic==null) {
            return Set.of();
        }
        Set<String> topicSet = new HashSet<>();
        if (topics!=null) {
            topicSet.addAll(topics);
        }
        if (topic!=null) {
            topicSet.addAll(topic);
        }
        return topicSet;
    }

    @Override
    public List<Pattern> getAggregations() {
        if (!data.containsKey(FrontMatter.AGGREGATIONS)) {
            return List.of();
        }
        return data.get(FrontMatter.AGGREGATIONS).stream().map(Pattern::compile).collect(Collectors.toList());
    }

    @Override
    public Set<DocScope> getDocScopes() {
        if (!data.containsKey(FrontMatter.SCOPE)) {
            return Set.of(DocScope.NONE);
        }
        return data.get(FrontMatter.SCOPE).stream().map(DocScope::valueOf).collect(Collectors.toSet());
    }
}
