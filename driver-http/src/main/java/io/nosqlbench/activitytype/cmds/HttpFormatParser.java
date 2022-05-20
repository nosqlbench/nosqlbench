package io.nosqlbench.activitytype.cmds;

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


import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.BindPointParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpFormatParser {

    public static Map<String, String> parseUrl(String uri) {
        if (uri.matches("http.+")) {
            return Map.of("uri", rewriteExplicitSections(uri));
        }
        return null;
    }

    public static Map<String, String> parseInline(String command) {
        if (command == null) {
            return null;
        }

        // Only attempt to match this if it starts with a bare word
        if (!command.matches("(?m)(?s)^\\{?[a-zA-Z]+}? .+")) {
            return null;
        }
        Map<String, String> props = new HashMap<>();

        String[] headAndBody = command.trim().split("\n\n", 2);
        if (headAndBody.length == 2) {
            props.put("body", headAndBody[1]);
        }

        String[] methodAndHeaders = headAndBody[0].split("\n", 2);
        if (methodAndHeaders.length > 1) {
            for (String header : methodAndHeaders[1].split("\n")) {
                String[] headerNameAndVal = header.split(": *", 2);
                if (headerNameAndVal.length != 2) {
                    throw new BasicError("Headers must be in 'Name: value form");
                }
                if (!headerNameAndVal[0].substring(0, 1).toUpperCase().equals(headerNameAndVal[0].substring(0, 1))) {
                    throw new BasicError("Headers must be capitalized to avoid ambiguity with other request parameters:'" + headerNameAndVal[0]);
                }
                props.put(headerNameAndVal[0], headerNameAndVal[1]);

            }
        }

        String[] methodLine = methodAndHeaders[0].split(" ", 3);
        if (methodLine.length < 2) {
            throw new BasicError("Request template must have at least a method and a uri: " + methodAndHeaders[0]);
        }
        props.put("method", methodLine[0]);
        props.put("uri", rewriteExplicitSections(methodLine[1]));

        if (methodLine.length == 3) {
            String actualVersion = methodLine[2];
            String symbolicVersion = actualVersion
                    .replaceAll("/1.1", "_1_1")
                    .replaceAll("/2.0", "_2")
                .replaceAll("/2", "_2");

            props.put("version", symbolicVersion);
        }

        return props;
    }

    private final static Pattern DOENCODE = Pattern.compile("(URLENCODE|E)\\[\\[(?<data>.+?)\\]\\]");

    public static String rewriteExplicitSections(String template) {

        StringBuilder sb = new StringBuilder();
        Matcher matcher = DOENCODE.matcher(template);
        while (matcher.find()) {
            String rewrite = matcher.group("data");
            String encoded = rewriteStaticsOnly(rewrite);
            matcher.appendReplacement(sb, encoded);
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    public static String rewriteStaticsOnly(String template) {

        StringBuilder sb = new StringBuilder();
        String input = template;
        Matcher matcher = BindPointParser.BINDPOINT_ANCHOR.matcher(input);
        int idx = 0;
        while (matcher.find()) {
            String pre = input.substring(idx, matcher.start());
            sb.append(URLEncoder.encode(pre, StandardCharsets.UTF_8));
            sb.append(matcher.group());
            idx = matcher.end();
//                matcher.appendReplacement(sb, "test-value" + idx);
        }
        sb.append(URLEncoder.encode(input.substring(idx), StandardCharsets.UTF_8));
        return sb.toString();
    }

}
