package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.markdown.types.BasicFrontMatterInfo;
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

    private final Map<String, List<String>> data = new LinkedHashMap<>();

    public ParsedFrontMatter(Map<String, List<String>> data) {
        this.data.putAll(data);
    }

    @Override
    public String getTitle() {
        List<String> titles = data.get(BasicFrontMatterInfo.TITLE);
        if (titles==null) {
            return "";
        }
        if (titles.size()>1) {
            throw new InvalidParameterException(BasicFrontMatterInfo.TITLE + " can only contain a single value.");
        }
        if (titles.size()==1) {
            return titles.get(0);
        }
        return "";
    }

    @Override
    public int getWeight() {
        List<String> weights = data.get(BasicFrontMatterInfo.WEIGHT);
        if (weights==null) {
            return 0;
        }
        if (weights.size()>1) {
            throw new InvalidParameterException(BasicFrontMatterInfo.WEIGHT + " can only contain a single value.");
        }
        if (weights.size()==1) {
            return Integer.parseInt(weights.get(0));
        }
        return 0;
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

    @Override
    public List<String> getDiagnostics(List<String> buffer) {
        for (String propname : data.keySet()) {
            if (!FrontMatterInfo.FrontMatterKeyWords.contains(propname)) {
                buffer.add("unrecognized frontmatter property " + propname);
            }
        }
        return buffer;
    }

    public List<String> getDiagnostics() {
        return getDiagnostics(new ArrayList<>());
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

    public void setTitle(String title) {
        this.data.put(FrontMatterInfo.TITLE,List.of(title));
    }

    public void setWeight(int weight) {
        data.put(FrontMatterInfo.WEIGHT,List.of(String.valueOf(weight)));
    }

}
