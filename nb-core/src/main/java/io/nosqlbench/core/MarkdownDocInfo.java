package io.nosqlbench.core;

import io.nosqlbench.activityapi.core.ActivityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class MarkdownDocInfo {
    private final static Logger logger = LoggerFactory.getLogger(MarkdownDocInfo.class);

    public static Optional<String> forHelpTopic(String topic) {
        String help = null;
        try {
            help = new MarkdownDocInfo().forActivityInstance(topic);
            return Optional.ofNullable(help);
        } catch (Exception e) {
            logger.debug("Did not find help topic for activity instance: "  + topic);
        }

        try {
            help = new MarkdownDocInfo().forResourceMarkdown(topic, "docs/");
            return Optional.ofNullable(help);
        } catch (Exception e) {
            logger.debug("Did not find help topic for generic markdown file: " + topic + "(.md)");
        }

        return Optional.empty();

    }

    public String forResourceMarkdown(String s, String... additionalSearchPaths) {
        String docFileName = s + ".md";

        List<String> searchIn = new ArrayList<>();
        searchIn.add(docFileName);
        Arrays.stream(additionalSearchPaths).map(path -> path + docFileName).forEach(searchIn::add);

        logger.info("loading doc file for topic:" + docFileName);

        Optional<InputStream> found = searchIn.stream().map(
                getClass().getClassLoader()::getResourceAsStream)
                .filter(Objects::nonNull)
                .findFirst();

        InputStream stream = found.orElseThrow(
                () -> new RuntimeException("Unable to find docstream in classpath: " + docFileName)
        );

        String docInfo = "";
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            docInfo = buffer.lines().collect(Collectors.joining("\n"));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to buffer data from docstream: " + docFileName + ":" + t);
        }

        return docInfo;
    }

    public String forActivityInstance(String s) {
        ActivityType activityType = ActivityType.FINDER.getOrThrow(s);
        return forResourceMarkdown(activityType.getName()+".md", "docs/");
    }

}
