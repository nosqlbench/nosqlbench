package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class HttpFormatParser {

    public static Map<String, String> parseUrl(String uri) {
        if (uri.matches("http.+")) {
            return Map.of("uri", rewriteUriWithStaticsEncoded(uri));
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
        props.put("uri", rewriteUriWithStaticsEncoded(methodLine[1]));

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

    private static String rewriteUriWithStaticsEncoded(String template) {
        String[] parts = template.split("\\?", 2);

        if (parts.length == 2) {
            StringBuilder sb = new StringBuilder();
            String input = parts[1];
            Matcher matcher = ParsedTemplate.STANDARD_ANCHOR.matcher(input);
            int idx = 0;
            while (matcher.find()) {
                String pre = input.substring(0, matcher.start());
                sb.append(URLEncoder.encode(pre, StandardCharsets.UTF_8));
                sb.append(matcher.group());
                idx = matcher.end();
//                matcher.appendReplacement(sb, "test-value" + idx);
            }
            sb.append(URLEncoder.encode(input.substring(idx), StandardCharsets.UTF_8));
            return parts[0] + "?" + sb.toString();
        } else {
            return template;
        }

    }

}
