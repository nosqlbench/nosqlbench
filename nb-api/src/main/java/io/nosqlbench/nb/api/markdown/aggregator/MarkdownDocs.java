package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.markdown.providers.RawMarkdownSources;
import io.nosqlbench.nb.api.markdown.types.Diagnostics;
import io.nosqlbench.nb.api.markdown.types.DocScope;
import io.nosqlbench.nb.api.markdown.types.FrontMatterInfo;
import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MarkdownDocs {

    private final static Logger logger = LogManager.getLogger(MarkdownDocs.class);

    public static List<MarkdownInfo> find(DocScope... scopes) {
        return find(".*", scopes);
    }

    public static List<MarkdownInfo> findAll() {
        return find(DocScope.ANY);
    }

    public static List<MarkdownInfo> find(String name, DocScope... scopes) {
        List<MarkdownInfo> aggregated = new ArrayList<>();

        List<Content<?>> markdownContent = RawMarkdownSources.getAllMarkdown();

        // Find all topics and aggregators
        List<String> aggregators = new ArrayList<>();

        List<MarkdownInfo> markdownInfos = markdownContent
            .stream()
            .map(ParsedMarkdown::new)
            .collect(Collectors.toList());

        List<String> diagBuffer = new ArrayList<>();
        markdownInfos.forEach(i -> Diagnostics.getDiagnostics(i,diagBuffer));
        diagBuffer.forEach(logger::warn);


        List<Set<String>> topicSets =
            markdownInfos.stream().map(m -> m.getFrontmatter().getTopics()).collect(Collectors.toList());

        // find non-glob topics
        List<String> nonGlobTopics = markdownInfos.stream()
            .map(MarkdownInfo::getFrontmatter)
            .map(FrontMatterInfo::getTopics).flatMap(Collection::stream)
            .filter(s -> !isPattern(s))
            .collect(Collectors.toList());

        List<? extends MarkdownInfo> markdownWithTopicGlobs =
            ListSplitterWhyDoesJavaNotDoThisAlready.partition(markdownInfos, MarkdownInfo::hasTopicGlobs);
        List<? extends MarkdownInfo> markdownWithOnlyTopicGlobs =
            ListSplitterWhyDoesJavaNotDoThisAlready.partition(markdownWithTopicGlobs, m -> m.getTopics().size()==0);

        List<MarkdownInfo> ordered = new ArrayList<>();


        // At this point, we have three set of markdown infos
        // a) with only globs
        // b) with globs and literals
        // c) with only literals
        // We can do an O((n/2)^2) association check, which is better than O(n^2)

        ordered.addAll(markdownWithOnlyTopicGlobs);
        ordered.addAll(markdownWithTopicGlobs);
        ordered.addAll(markdownInfos);

        MDGraph mdgraph = new MDGraph();
        ordered.forEach(mdgraph::add);

        return mdgraph.processed();
//
//
//
//        List<Edge<List<String>>> edges = new ArrayList<>();
//        List<String> matchedtopics = null;
//
//        for (int i = 0; i < ordered.size()-1; i++) {
//            MarkdownInfo mdHavingGlobs = ordered.get(i);
//            List<Pattern> topicGlobs = mdHavingGlobs.getTopicGlobs();
//
//            for (Pattern topicGlob : topicGlobs) {
//                for (int matchidx = i+1; matchidx < ordered.size(); matchidx++) {
//                    MarkdownInfo matchableContent = ordered.get(matchidx);
//                    List<String> matchableTopics = matchableContent.getTopics();
//                    for (String matchableTopic : matchableTopics) {
//                        if (topicGlob.matcher(matchableTopic).matches()) {
//                            matchedtopics=matchedtopics==null ? new ArrayList<>() : matchedtopics;
//                            matchedtopics.add(matchableTopic);
//                            logger.debug("added topic=" + matchableTopic + " to " + i + "->" + matchidx + " with " + topicGlob);
//                        }
//                    }
//                    if (matchedtopics!=null) {
//                        matchedtopics.addAll(mdHavingGlobs.getTopics());
//                        ordered.set(i,mdHavingGlobs.withTopics(matchedtopics));
//                        logger.debug("assigned new mdinfo");
//                        matchedtopics=null;
//                    }
//                }
//            }
//
//            // TODO track and warn if a glob doesn't match anything
//            for (int j = i+1; j < ordered.size(); j++) {
//
//                MarkdownInfo mdHavingTopics = ordered.get(j);
//                List<String> topics = mdHavingTopics.getTopics();
//
//                for (Pattern topicGlob : topicGlobs) {
//
//                    for (String topic : topics) {
//                        if (topicGlob.matcher(topic).matches()) {
//                            matchedtopics=matchedtopics==null ? new ArrayList<>() : matchedtopics;
//                            matchedtopics.add(topic);
//                            logger.debug("added topic=" + topic + " to " + i + "->" + j + " with " + topicGlob);
//                        }
//                    }
//                    if (matchedtopics!=null) {
//                        matchedtopics.addAll(mdHavingGlobs.getTopics());
//                        ordered.set(i,mdHavingGlobs.withTopics(matchedtopics));
//                        logger.debug("assigned new mdinfo");
//                    }
//                }
//            }
//        }
//
//        int loopsremaining=100;
//
//        // Assign glob topics to non-glob topics that match
//
////        for (MarkdownInfo parsedMarkdown : markdownInfos) {
////            FrontMatterInfo fm = parsedMarkdown.getFrontmatter();
////            Set<String> topics = fm.getTopics();
////            Set<String> newTopics = new HashSet<>();
////            for (String topic : topics) {
////                if (isPattern(topic)) {
////                    Pattern p = Pattern.compile(topic);
////                    for (String nonGlobTopic : nonGlobTopics) {
////                        if (p.matcher(nonGlobTopic).matches()) {
////                            newTopics.add(topic);
////                        }
////                    }
////                } else {
////                    newTopics.add(topic);
////                }
////            }
////            fm.setTopics(newTopics);
////        }
////
////        // create topic to content map
////        HashMap<String,List<ParsedMarkdown>> contentByTopic = new HashMap<>();
////        for (ParsedMarkdown parsedMarkdown : markdownInfos) {
////            for (String topic : parsedMarkdown.getFrontmatter().getTopics()) {
////                contentByTopic.computeIfAbsent(topic, t -> new ArrayList<>()).add(parsedMarkdown);
////            }
////        }
////
////        ListIterator<? extends MarkdownInfo> lit = markdownInfos.listIterator();
////        while (lit.hasNext()) {
////            MarkdownInfo mif = lit.next();
////            if (mif.hasAggregations()) {
////                lit.remove();
////                mif = new CompositeMarkdownInfo().add(mif);
////                lit.add(mif);
////            }
////        }
////
////        // combine aggregate targets
////        for (ParsedMarkdown parsedMarkdown : markdownInfos) {
////            List<Pattern> aggregations = parsedMarkdown.getFrontmatter().getAggregations();
////            if (aggregations.size()>0) {
////                for (Pattern aggregation : aggregations) {
////
////                }
////            }
////        }
////
////        // Assign glob topics
////
////        // Assign content aggregates
////        System.out.println("topics: " + topicSets);
////
////        aggregated.addAll(markdownInfos);
//        return aggregated;


    }

    private static boolean isPattern(String srcTopic) {
        return
            srcTopic.startsWith("^")
                || srcTopic.contains(".*")
                || srcTopic.contains(".+")
                || srcTopic.endsWith("$");
    }

    private static class Edge<T> {

        private final int from;
        private final int to;
        private final T edgeProps;

        public Edge(int from, int to, Supplier<T> forT) {
            this.from = from;
            this.to = to;
            edgeProps = forT.get();
        }

        public int from() {
            return from;
        }
        public int to() {
            return to;
        }
        public T props() {
            return edgeProps;
        }

    }

}
