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

        int loopsremaining=100;
        // TODO: add logic to deal with leaf nodes and kick intermediate nodes to the end of the processing list.
        // TODO: Double check exit conditions and warn user
        while (markdownWithTopicGlobs.size()>0 && loopsremaining>0) {
            for (MarkdownInfo markdownWithTopicGlob : markdownWithTopicGlobs) {
                markdownWithTopicGlob.getTopicGlobs();
                for (MarkdownInfo allInfo : markdownInfos) {
//                    allInfo.getTopics()
                }
            }
            loopsremaining--;
        }
        if (markdownWithTopicGlobs.size()>0) {
            throw new RuntimeException("Non-terminal condition in markdown graph processing, unable to resolve all " +
                "topic globs, " + markdownWithTopicGlobs.size() + " remaining: " + markdownWithTopicGlobs);
        }


        // Assign glob topics to non-glob topics that match

//        for (MarkdownInfo parsedMarkdown : markdownInfos) {
//            FrontMatterInfo fm = parsedMarkdown.getFrontmatter();
//            Set<String> topics = fm.getTopics();
//            Set<String> newTopics = new HashSet<>();
//            for (String topic : topics) {
//                if (isPattern(topic)) {
//                    Pattern p = Pattern.compile(topic);
//                    for (String nonGlobTopic : nonGlobTopics) {
//                        if (p.matcher(nonGlobTopic).matches()) {
//                            newTopics.add(topic);
//                        }
//                    }
//                } else {
//                    newTopics.add(topic);
//                }
//            }
//            fm.setTopics(newTopics);
//        }
//
//        // create topic to content map
//        HashMap<String,List<ParsedMarkdown>> contentByTopic = new HashMap<>();
//        for (ParsedMarkdown parsedMarkdown : markdownInfos) {
//            for (String topic : parsedMarkdown.getFrontmatter().getTopics()) {
//                contentByTopic.computeIfAbsent(topic, t -> new ArrayList<>()).add(parsedMarkdown);
//            }
//        }
//
//        ListIterator<? extends MarkdownInfo> lit = markdownInfos.listIterator();
//        while (lit.hasNext()) {
//            MarkdownInfo mif = lit.next();
//            if (mif.hasAggregations()) {
//                lit.remove();
//                mif = new CompositeMarkdownInfo().add(mif);
//                lit.add(mif);
//            }
//        }
//
//        // combine aggregate targets
//        for (ParsedMarkdown parsedMarkdown : markdownInfos) {
//            List<Pattern> aggregations = parsedMarkdown.getFrontmatter().getAggregations();
//            if (aggregations.size()>0) {
//                for (Pattern aggregation : aggregations) {
//
//                }
//            }
//        }
//
//        // Assign glob topics
//
//        // Assign content aggregates
//        System.out.println("topics: " + topicSets);
//
//        aggregated.addAll(markdownInfos);
        return aggregated;


    }

    private static boolean isPattern(String srcTopic) {
        return
            srcTopic.startsWith("^")
                || srcTopic.contains(".*")
                || srcTopic.contains(".+")
                || srcTopic.endsWith("$");
    }

}
