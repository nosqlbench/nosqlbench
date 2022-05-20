package io.nosqlbench.engine.core.metadata;

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


import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.core.lifecycle.ActivityTypeLoader;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
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
        ActivityType activityType = new ActivityTypeLoader().load(ActivityDef.parseActivityDef("driver="+s)).orElseThrow(
            () -> new BasicError("Unable to find driver for '" + s + "'")
        );
        return forResourceMarkdown(activityType.getClass().getAnnotation(Service.class)
            .selector() + ".md", "docs/");
    }

}
