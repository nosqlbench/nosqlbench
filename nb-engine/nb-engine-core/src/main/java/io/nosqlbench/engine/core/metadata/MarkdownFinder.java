/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.core.metadata;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.ServiceLoader;

public class MarkdownFinder {
    private static final Logger logger = LogManager.getLogger(MarkdownFinder.class);

    public static Optional<String> forHelpTopic(final String topic) {
        String help = null;
        try {
            help = new MarkdownFinder().forDriverAdapterType(topic);
            return Optional.ofNullable(help);
        } catch (final Exception e) {
            MarkdownFinder.logger.debug("Did not find help topic for activity instance: {}", topic);
        }

        try {
            help = new MarkdownFinder().forResourceMarkdown(topic, "docs/");
            return Optional.ofNullable(help);
        } catch (final Exception e) {
            MarkdownFinder.logger.debug("Did not find help topic for generic markdown file: {}(.md)", topic);
        }

        return Optional.empty();

    }

    public String forResourceMarkdown(final String s, final String... additionalSearchPaths) {
        final Optional<Content<?>> docs = NBIO.local()
            .searchPrefixes("docs")
            .searchPrefixes(additionalSearchPaths)
            .pathname(s)
            .extensionSet(".md")
            .first();

        return docs.map(Content::asString).orElse(null);
    }

    public String forDriverAdapterType(final String selector) {

        ServiceSelector.of(selector, ServiceLoader.load(DriverAdapterLoader.class)).get()
            .orElseThrow(() -> new BasicError("Unable to find driver for '" + selector + "'"));
        return this.forResourceMarkdown(selector + ".md", "docs/");
    }

}
