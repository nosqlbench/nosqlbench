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

package io.nosqlbench.api.docsapi.docexporter;

import io.nosqlbench.nb.api.markdown.aggregator.MutableMarkdown;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BundledFrontmatterInjector implements BundledMarkdownProcessor {

    private final long floorWeight;
    private final long readmeWeight;

    public BundledFrontmatterInjector(int floorWeight, int readmeWeight) {
        this.floorWeight = floorWeight;
        this.readmeWeight = readmeWeight;
    }

    @Override
    public MutableMarkdown apply(MutableMarkdown parsedMarkdown) {
        // Set weight if not already set
        if (parsedMarkdown.getFrontmatter().getWeight()<=0) {
            String title = parsedMarkdown.getFrontmatter().getTitle();
            if (parsedMarkdown.getPath()!=null && parsedMarkdown.getPath().endsWith("README.md")) {
                parsedMarkdown.getFrontmatter().setWeight(readmeWeight);
            } else {
                parsedMarkdown.getFrontmatter().setWeight(floorWeight +alphaWeightOf(title));
            }
        }

        // Add compositional metadata for Zola/docs site organization
        addCompositionalMetadata(parsedMarkdown);

        return parsedMarkdown;
    }

    /**
     * Add compositional metadata to enable flexible site organization.
     * This metadata allows the static site generator to organize content
     * independent of file location.
     */
    private void addCompositionalMetadata(MutableMarkdown parsedMarkdown) {
        // Determine document type from title and source
        String title = parsedMarkdown.getFrontmatter().getTitle();
        String source = parsedMarkdown.getFrontmatter().getSource();
        boolean isBindingFunction = title != null && title.toLowerCase().contains("functions");
        boolean isDriverDoc = source != null && source.contains("adapter-");
        boolean isAppDoc = source != null && source.contains("/apps/");
        boolean isWorkloadSpec = source != null && source.contains("workload_definition");

        // Set quadrant (all auto-generated docs are reference material)
        if (parsedMarkdown.getFrontmatter().getQuadrant() == null) {
            parsedMarkdown.getFrontmatter().setQuadrant("reference");
        }

        // Set topic based on document type
        if (parsedMarkdown.getFrontmatter().getTopic() == null) {
            if (isBindingFunction) {
                parsedMarkdown.getFrontmatter().setTopic("bindings");
            } else if (isDriverDoc) {
                parsedMarkdown.getFrontmatter().setTopic("drivers");
            } else if (isAppDoc) {
                parsedMarkdown.getFrontmatter().setTopic("apps");
            } else if (isWorkloadSpec) {
                parsedMarkdown.getFrontmatter().setTopic("workload-yaml");
            } else {
                parsedMarkdown.getFrontmatter().setTopic("general");
            }
        }

        // Extract category from title (e.g., "state functions" -> "state")
        if (parsedMarkdown.getFrontmatter().getCategory() == null && isBindingFunction) {
            String category = title.toLowerCase()
                .replace(" functions", "")
                .replace("functions", "")
                .trim();
            if (!category.isEmpty()) {
                parsedMarkdown.getFrontmatter().setCategory(category);
            }
        }

        // Add tags for searchability
        if (parsedMarkdown.getFrontmatter().getTags().isEmpty()) {
            List<String> tags = new ArrayList<>();
            tags.add("auto-generated");

            if (isBindingFunction) {
                tags.addAll(List.of("virtdata", "data-generation", "binding-functions"));
            } else if (isDriverDoc) {
                tags.addAll(List.of("driver", "adapter", "database"));
            } else if (isAppDoc) {
                tags.addAll(List.of("built-in-app", "command"));
            } else if (isWorkloadSpec) {
                tags.addAll(List.of("yaml", "specification", "workload"));
            }

            parsedMarkdown.getFrontmatter().setTags(tags);
        }

        // Mark as auto-generated and non-testable (pure reference)
        if (parsedMarkdown.getFrontmatter().getGenerator() == null) {
            parsedMarkdown.getFrontmatter().setGenerator("BundledMarkdownExporter");
        }

        parsedMarkdown.getFrontmatter().setTestable(false);

        // Set template for docs pages
        if (parsedMarkdown.getFrontmatter().getTemplate() == null) {
            parsedMarkdown.getFrontmatter().setTemplate("docs-page.html");
        }

        // Set date
        if (parsedMarkdown.getFrontmatter().getDate() == null) {
            parsedMarkdown.getFrontmatter().setDate(LocalDate.now().toString());
        }

        // Set description if not present
        if (parsedMarkdown.getFrontmatter().getDescription() == null) {
            parsedMarkdown.getFrontmatter().setDescription(
                "Auto-generated reference documentation for " + title
            );
        }
    }

    private long alphaWeightOf(String name) {
        name=name.toLowerCase(Locale.ROOT);
        long sum=0;
        long pow=26;
        for (int i = 0; i < 6; i++) {
            if (name.length()>i) {
                int ord=name.charAt(i);

                if (48 <= ord && ord <= 57) {
                    ord-=48;
                } else if (65 <= ord && ord <= 90) {
                    ord-=65;
                } else if (97 <= ord && ord <= 122) {
                    ord-=97;
                } else {
                    ord=1;
                }
                double addendDouble = Math.pow(pow, i) * ord;
                long addend = (long)addendDouble;
                if (addend>(Long.MAX_VALUE-sum)) {
                    throw new RuntimeException("overflow on doc weight with value of " + addend + " with sum of " + sum + " for '" + name+"'");
                }
                sum += addend;
            } else {
                break;
            }
        }
        sum = sum>0 ? sum : 1;
        return sum;
    }
}
