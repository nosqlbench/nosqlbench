/*
 * Copyright (c) 2022 nosqlbench
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

    @Override
    public MutableMarkdown apply(MutableMarkdown parsedMarkdown) {
        if (parsedMarkdown.getFrontmatter().getWeight()<0) {
            String title = parsedMarkdown.getFrontmatter().getTitle();
            parsedMarkdown.getFrontmatter().setWeight(alphaWeightOf(title));
        }
        return parsedMarkdown;
    }

    private int alphaWeightOf(String name) {
        name=name.toLowerCase(Locale.ROOT);
        int sum=0;
        int pow=26;
        for (int i = 0; i < 6; i++) {
            if (name.length()>i) {
                int ord = name.charAt(i) - 'a';
                double addend = Math.pow(pow, i) * ord;
                sum += addend;
            } else {
                break;
            }
        }
        return sum;
    }
}
