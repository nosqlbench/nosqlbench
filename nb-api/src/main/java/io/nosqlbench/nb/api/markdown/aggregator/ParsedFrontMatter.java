package io.nosqlbench.nb.api.markdown.aggregator;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.nb.api.markdown.types.DocScope;
import io.nosqlbench.nb.api.markdown.types.FrontMatterInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParsedFrontMatter implements FrontMatterInfo {

    private final static Logger logger = LogManager.getLogger(ParsedFrontMatter.class);

    private final Map<String, List<String>> data;

    public ParsedFrontMatter(Map<String, List<String>> data) {
        this.data = data;
    }

    @Override
    public String getTitle() {
        List<String> titles = data.get(FrontMatterInfo.TITLE);
        if (titles==null) {
            return "";
        }
        if (titles.size()!=1) {
            throw new InvalidParameterException(FrontMatterInfo.TITLE + " can only contain a single value.");
        }
        return titles.get(0);
    }

    @Override
    public int getWeight() {
        List<String> weights = data.get(FrontMatterInfo.WEIGHT);
        if (weights==null) {
            return 0;
        }
        if (weights.size()!=1) {
            throw new InvalidParameterException(FrontMatterInfo.WEIGHT + " can only contain a single value.");
        }
        return Integer.parseInt(weights.get(0));
    }

    @Override
    public Set<String> getTopics() {
        List<String> topics = data.get(FrontMatterInfo.TOPICS);
        Set<String> topicSet = new HashSet<>();
        if (topics!=null) {
            for (String topic : topics) {
                Collections.addAll(topicSet, topic.split(", *"));
            }
//            topicSet.addAll(topics);
        }
        return topicSet;
    }

    @Override
    public List<String> getIncluded() {
        List<String> included = data.get(FrontMatterInfo.INCLUDED);
        List<String> includedList = new ArrayList<>();
        if (included!=null) {
            for (String s : included) {
                Collections.addAll(includedList, s.split(", *"));
            }
        }
        return includedList;
    }

    @Override
    public List<Pattern> getAggregations() {
        if (!data.containsKey(FrontMatterInfo.AGGREGATE)) {
            return List.of();
        }
        List<String> patterns = new ArrayList<>();
        for (String aggName : data.get(FrontMatterInfo.AGGREGATE)) {
            Collections.addAll(patterns,aggName.split(", *"));
        }
        return patterns.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    @Override
    public Set<DocScope> getDocScopes() {
        if (!data.containsKey(FrontMatterInfo.SCOPES)) {
            return Set.of(DocScope.NONE);
        }
        List<String> scopeNames = new ArrayList<>();
        for (String scopeName : data.get(FrontMatterInfo.SCOPES)) {
            Collections.addAll(scopeNames,scopeName.split(", *"));
        }
        return scopeNames.stream().map(DocScope::valueOf).collect(Collectors.toSet());
    }

    public List<String> getDiagnostics() {
        List<String> warnings = new ArrayList<>();
        for (String propname : data.keySet()) {
            if (!FrontMatterInfo.FrontMatterKeyWords.contains(propname)) {
                warnings.add("unrecognized frontm atter property " + propname);
            }
        }
        return warnings;
    }


    public void setTopics(Set<String> newTopics) {
        // TODO: allow functional version of this
//        this.data.put(FrontMatterInfo.TOPICS,newTopics);
    }

    public ParsedFrontMatter withTopics(List<String> assigning) {
        HashMap<String, List<String>> newmap = new HashMap<>();
        newmap.putAll(this.data);
        newmap.put(FrontMatterInfo.TOPICS,assigning);
        return new ParsedFrontMatter(newmap);
    }

    public ParsedFrontMatter withIncluded(List<String> included) {
        HashMap<String, List<String>> newmap = new HashMap<>();
        newmap.putAll(this.data);
        newmap.put(FrontMatterInfo.INCLUDED,included);
        return new ParsedFrontMatter(newmap);
    }

    @Override
    public String toString() {
        return "ParsedFrontMatter{" +
            "data=" + data +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedFrontMatter that = (ParsedFrontMatter) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
