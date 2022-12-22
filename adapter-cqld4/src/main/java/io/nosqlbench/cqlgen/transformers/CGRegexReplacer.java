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

package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.cqlgen.api.CGTransformerConfigurable;
import io.nosqlbench.cqlgen.api.CGTextTransformer;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CGRegexReplacer implements CGTextTransformer, CGTransformerConfigurable {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/replacer");
    private List<Replacer> replacers;
    private String prefix = "";
    private String suffix = "";
    private String outfile;

    @Override
    public String apply(String text) {
        String previous = "";
        while (!previous.equals(text)) {
            previous=text;

            long original_size = text.length();
            int steps = 0;
            int replacements = 0;
            for (Replacer replacer : replacers) {
                logger.info("applying regex replacer #" + ++steps);
                text = replacer.apply(text);
                replacements += replacer.replacements;
            }
            logger.info(steps + " replacers applied. " + replacements + " replacements found total.");
            if (outfile != null) {
                try {
                    if (outfile.startsWith("_")) {
                        Files.write(Path.of(outfile), text.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        Files.write(Path.of(outfile), text.getBytes(), StandardOpenOption.CREATE_NEW);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return text;
    }

    @Override
    public void accept(Object configObject) {
        if (configObject instanceof Map stringMap) {
            this.prefix = stringMap.containsKey("prefix") ? stringMap.get("prefix").toString() : "";
            this.suffix = stringMap.containsKey("suffix") ? stringMap.get("suffix").toString() : "";
            this.outfile = stringMap.containsKey("outfile") ? stringMap.get("outfile").toString() : null;
            Object replacersObject = stringMap.get("replacers");
            if (replacersObject instanceof List list) {
                List<List<String>> replacers = (List<List<String>>) stringMap.get("replacers");
                this.replacers = replacers.stream()
                    .map(l -> new Replacer(l.get(0), l.get(1).replaceAll("PREFIX", prefix).replaceAll("SUFFIX", suffix)))
                    .toList();
            } else {
                throw new RuntimeException("regex replacer needs a list of lists for its replacers field, with each list consisting" +
                    " of a single regex matcher and a single regex replacer.");
            }
        } else {
            throw new RuntimeException("regex replacer requires a Map for its config value, with a replacer field.");
        }
    }

    private final static class Replacer implements Function<String, String> {
        private final Pattern pattern;
        private final String replacement;
        public int replacements = 0;

        Replacer(String from, String to) {
            this.pattern = Pattern.compile("(?m)(?i)" + from);
            this.replacement = to;
        }

        @Override
        public String apply(String s) {
            Matcher matcher = pattern.matcher(s);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, replacement);
//                if (matcher.end() - matcher.start() > 10000) {
//                    logger.info(()-> "whoops");
//                }
//                logger.info("matcher:[" + matcher.group(0) + "][" + matcher.group(1) + "][" + matcher.group(2) + "][" + matcher.group(3));
//                logger.info(String.format("\tat %2.2f%%", (float) ((float) matcher.start() / (float) s.length())));
                CharSequence replaced = sb.subSequence(matcher.start(), matcher.end());
//                logger.info("replaced:" + replaced);
                this.replacements++;
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

    }


}
