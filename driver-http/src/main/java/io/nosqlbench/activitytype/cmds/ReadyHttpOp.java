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


import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import io.nosqlbench.nb.api.errors.BasicError;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class ReadyHttpOp implements OpDispenser<HttpOp> {

    private final CommandTemplate propertyTemplate;
    public static final String DEFAULT_OK_BODY = ".+?";
    public static final String DEFAULT_OK_STATUS = "2..";

    // only populated if there is no value which is an actual bindings template
    private final HttpOp cachedOp;

    public ReadyHttpOp(OpTemplate stmtDef) {
        propertyTemplate = new CommandTemplate(stmtDef,
            List.of(
                HttpFormatParser::parseUrl,
                HttpFormatParser::parseInline
            )
        );

        sanityCheckUri();
        if (propertyTemplate.isStatic()) {
            cachedOp = apply(0);
        } else {
            cachedOp = null;
        }

    }

    // :/?#[]@ !$&'()*+,;=

    /**
     * Try to catch situations in which the user put invalid characters in some part of the URI.
     * In this case, the only safe thing to try seems to be to automatically urldecode
     */
    private void sanityCheckUri() {
        Map<String, String> command = propertyTemplate.getCommand(0L);
        if (command.containsKey("uri")) {
            String uriSpec = command.get("uri");
            URI uri = null;
            try {
                uri = new URI(uriSpec);
            } catch (URISyntaxException e) {
                throw new BasicError(e.getMessage() + ", either use URLEncode in your bindings for values which could " +
                        "contain invalid URI characters, or modify the static portions of your op template to use the" +
                        " appropriate encodings.");
            }
        }

    }

    @Override
    public HttpOp apply(long value) {

        // If the request is invariant, simply return it, since it is thread-safe
        if (this.cachedOp != null) {
            return this.cachedOp;
        }

        Map<String, String> cmd = propertyTemplate.getCommand(value);

        HttpRequest.Builder builder = HttpRequest.newBuilder();

        HttpRequest.BodyPublisher bodyPublisher = cmd.containsKey("body") ?
                HttpRequest.BodyPublishers.ofString(cmd.remove("body"))
                : HttpRequest.BodyPublishers.noBody();

        String method = cmd.containsKey("method") ? cmd.remove("method") : "GET";

        builder.method(method, bodyPublisher);

        if (cmd.containsKey("version")) {
            String versionName = cmd.remove("version")
                    .replaceAll("/1.1", "_1_1")
                .replaceAll("/2.0", "_2");
            HttpClient.Version version = HttpClient.Version.valueOf(versionName);
            builder.version(version);
        }

        if (cmd.containsKey("uri")) {
            URI uri = URI.create(cmd.remove("uri"));
            builder.uri(uri);
        }

        Pattern ok_status = Pattern.compile(Optional.ofNullable(cmd.remove("ok-status")).orElse(DEFAULT_OK_STATUS));
        Pattern ok_body = Optional.ofNullable(cmd.remove("ok-body")).map(Pattern::compile).orElse(null);

        String timeoutStr = cmd.remove("timeout");
        if (timeoutStr != null) {
            builder.timeout(Duration.of(Long.parseLong(timeoutStr), ChronoUnit.MILLIS));
        }

        // At this point, the only things left in the list must be headers,
        // but we check them for upper-case conventions as a sanity check for the user
        for (String headerName : cmd.keySet()) {
            if (headerName.charAt(0) >= 'A' && headerName.charAt(0) <= 'Z') {
                String headerValue = cmd.get(headerName);
                builder = builder.header(headerName, headerValue);
            } else {
                throw new BasicError("HTTP request parameter '" + headerName + "' was not recognized as a basic request parameter, and it is not capitalized to indicate that it is a header.");
            }
        }
//        cmd.clear();
//        if (cmd.size()>0) {
//            throw new BasicError("Some provided request fields were not used: " + cmd.toString());
//        }
//
        HttpRequest request = builder.build();
        return new HttpOp(request, ok_status, ok_body);
    }

    @Override
    public String toString() {
        return "ReadyHttpOp{" +
                "template=" + propertyTemplate +
                ", cachedOp=" + cachedOp +
                '}';
    }
}
