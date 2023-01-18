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

import io.nosqlbench.api.markdown.aggregator.MutableMarkdown;

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
        if (parsedMarkdown.getFrontmatter().getWeight()<=0) {
            String title = parsedMarkdown.getFrontmatter().getTitle();
            if (parsedMarkdown.getPath()!=null && parsedMarkdown.getPath().endsWith("README.md")) {
                parsedMarkdown.getFrontmatter().setWeight(readmeWeight);
            } else {
                parsedMarkdown.getFrontmatter().setWeight(floorWeight +alphaWeightOf(title));
            }
        }
        return parsedMarkdown;
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
