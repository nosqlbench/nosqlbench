package io.nosqlbench.engine.core.metadata;

import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class MarkdownDocInfo {
    private final static Logger logger = LogManager.getLogger(MarkdownDocInfo.class);

    public static Optional<String> forHelpTopic(String topic) {
        String help = null;
        try {
            help = new MarkdownDocInfo().forActivityInstance(topic);
            return Optional.ofNullable(help);
        } catch (Exception e) {
            logger.debug("Did not find help topic for activity instance: " + topic);
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
        Optional<Content<?>> docs = NBIO.local()
            .prefix("docs")
            .prefix(additionalSearchPaths)
            .name(s)
            .extension(".md")
            .first();

        return docs.map(Content::asString).orElse(null);
    }

    public String forActivityInstance(String s) {
        ActivityType activityType = ActivityType.FINDER.getOrThrow(s);
        return forResourceMarkdown(activityType.getName() + ".md", "docs/");
    }

}
