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

package io.nosqlbench.adapter.http.core;

import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.regex.Pattern;

public class HttpOpDispenser extends BaseOpDispenser<HttpOp, HttpSpace> {

    private final LongFunction<HttpOp> opFunc;
    public static final String DEFAULT_OK_BODY = ".+?";
    public static final String DEFAULT_OK_STATUS = "2..";


    public HttpOpDispenser(DriverAdapter adapter, LongFunction<HttpSpace> ctxF, ParsedOp op) {
        super(adapter, op);
        opFunc = getOpFunc(ctxF, op);
    }

    private LongFunction<HttpOp> getOpFunc(LongFunction<HttpSpace> ctxF, ParsedOp op) {

        LongFunction<HttpRequest.Builder> builderF = l -> HttpRequest.newBuilder();
        LongFunction<String> bodyF = op.getAsFunctionOr("body", null);
        LongFunction<HttpRequest.BodyPublisher> bodyPublisherF =
            l -> Optional.ofNullable(bodyF.apply(l)).map(HttpRequest.BodyPublishers::ofString).orElse(
                HttpRequest.BodyPublishers.noBody()
            );

        LongFunction<String> methodF = op.getAsFunctionOr("method", "GET");
        LongFunction<HttpRequest.Builder> initBuilderF =
            l -> builderF.apply(l).method(methodF.apply(l), bodyPublisherF.apply(l));

        initBuilderF = op.enhanceFuncOptionally(
            initBuilderF, "version", String.class,
            (b, v) -> b.version(HttpClient.Version.valueOf(
                    v.replaceAll("/1.1", "_1_1")
                        .replaceAll("/2.0", "_2")
                )
            )
        );

        Optional<LongFunction<String>> optionalUriFunc = op.getAsOptionalFunction("uri", String.class);
        LongFunction<String> urifunc;
        // Add support for URLENCODE on the uri field if either it statically or dynamically contains the E or URLENCODE pattern,
        // OR the enable_urlencode op field is set to true.
        if (optionalUriFunc.isPresent()) {
            String testUriValue = optionalUriFunc.get().apply(0L);
            if (HttpFormatParser.URLENCODER_PATTERN.matcher(testUriValue).find()
                || op.getStaticConfigOr("enable_urlencode", true)) {
                initBuilderF =
                    op.enhanceFuncOptionally(
                        initBuilderF,
                        "uri",
                        String.class,
                        (b, v) -> b.uri(URI.create(HttpFormatParser.rewriteExplicitSections(v)))
                    );
            }
        } else {
            initBuilderF = op.enhanceFuncOptionally(initBuilderF, "uri", String.class, (b, v) -> b.uri(URI.create(v)));
        }

        op.getOptionalStaticValue("follow_redirects", boolean.class);

        /**
         * Add header adders for any key provided in the op template which is capitalized
         */
        List<String> headerNames = op.getDefinedNames().stream()
            .filter(n -> n.charAt(0) >= 'A')
            .filter(n -> n.charAt(0) <= 'Z')
            .toList();
        if (headerNames.size() > 0) {
            for (String headerName : headerNames) {
                initBuilderF = op.enhanceFunc(initBuilderF, headerName, String.class, (b, h) -> b.header(headerName, h));
            }
        }

        initBuilderF = op.enhanceFuncOptionally(initBuilderF, "timeout", long.class, (b, v) -> b.timeout(Duration.ofMillis(v)));

        LongFunction<HttpRequest.Builder> finalInitBuilderF = initBuilderF;
        LongFunction<HttpRequest> reqF = l -> finalInitBuilderF.apply(l).build();


        Pattern ok_status = op.getOptionalStaticValue("ok-status", String.class)
            .map(Pattern::compile)
            .orElse(Pattern.compile(DEFAULT_OK_STATUS));

        Pattern ok_body = op.getOptionalStaticValue("ok-body", String.class)
            .map(Pattern::compile)
            .orElse(null);

        LongFunction<HttpOp> opFunc = cycle -> new HttpOp(
            ctxF.apply(cycle).getClient(),
            reqF.apply(cycle),
            ok_status,
            ok_body,
            ctxF.apply(cycle), cycle
        );
        return opFunc;
    }

    @Override
    public HttpOp apply(long value) {
        HttpOp op = this.opFunc.apply(value);
        return op;

    }
}
