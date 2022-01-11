package io.nosqlbench.docexporter;

import io.nosqlbench.nb.api.markdown.aggregator.MutableMarkdown;

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
