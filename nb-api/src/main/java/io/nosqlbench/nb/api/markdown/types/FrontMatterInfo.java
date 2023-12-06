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

package io.nosqlbench.nb.api.markdown.types;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * FrontMatter services provided within NoSQLBench are required to return the following types.
 * If the markdown source file does not contain the metadata requested, then reasonable non-null
 * defaults must be provided.
 */
public interface FrontMatterInfo extends BasicFrontMatterInfo, HasDiagnostics {

    String SCOPES = "scopes";
    String AGGREGATE = "aggregate";
    String TOPICS = "topics";
    String INCLUDED = "included";

    Set<String> FrontMatterKeyWords =
            Set.of(SCOPES, AGGREGATE,TOPICS,WEIGHT, TITLE,INCLUDED);


    /**
     * @return A title for the given markdown source file.
     */
    String getTitle();

    /**
     * @return A weight for the given markdown source file.
     */
    int getWeight();

    /**
     * <p>
     * Topics in this service are taxonomically hierarchical, and use the same naming convention
     * as relative file paths. (In a *NIX system, meaning forward slashes as path separators).
     * </p>
     *
     * <p>
     * The end name on a topic is considered the local topic name. The leading names before this
     * local topic name are considered nested categories. Taken together, these categories
     * (in hierarchic ordered form) are the <i>general category</i>.
     * </p>
     *
     * <p>
     * A topic and the location of the markdown content it is part of are <EM>NOT</EM> bound
     * together.
     * </p>
     *
     * @return A list of categories
     */
    Set<String> getTopics();

    /**
     * <p>If content is included in an item from another topic, then the
     * topic name with which the additional content was added is in the
     * inclueded list of topics.</p>
     *
     * <p>This is distinct from {@link #getTopics()}, which is not modified
     * by the included topic names.</p>
     *
     * @return A list of included topics.
     */
    List<String> getIncluded();

    /**
     * <p>
     * Aggregation patterns coalesce all the topics that they match into a seamless logical
     * section of content. All source markdown files which have a topic which is matched
     * by at least one of the aggregation patterns is included in the order of their weight.
     * </p>
     *
     * <P>
     * Aggregation patterns are simple regexes. It is possible to have multiple patterns as well
     * as content that is referenced by multiple aggregations, from the same or different source
     * aggregating topics.
     * </P>
     *
     * @return A list of aggregation patterns
     */
    List<Pattern> getAggregations();

    /**
     * If a markdown source is flagged for use in a specific doc scope, then you can filter for
     * that scope when you ask for markdown.
     * <p>
     * Markdown content which contains zero scopes should explicitly return {@link DocScope#NONE}.
     * Markdown content which contains one or more scopes should explicitly add
     * {@link DocScope#ANY} to the returned set.
     * Markdown should never be tagged with ANY or NONE in the source content. Readers should throw
     * an error if this is detected.
     *
     * @return A list of DocScopes for which this markdown should be used.
     */
    Set<DocScope> getDocScopes();

}
