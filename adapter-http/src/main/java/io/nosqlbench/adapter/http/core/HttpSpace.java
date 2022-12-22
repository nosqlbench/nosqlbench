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

package io.nosqlbench.adapter.http.core;

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * ThreadLocal http clients have been removed from this version, as the built-in
 * HTTP client implementation is meant to be immutable. If shared-state issues
 * occur, thread-local support will be re-added.
 */
public class HttpSpace implements NBNamedElement {
    private final static Logger logger = LogManager.getLogger(HttpSpace.class);

    private final String name;
    private final NBConfiguration cfg;
    private HttpConsoleFormats console;
    private HttpClient.Redirect followRedirects;
    private Duration timeout;
    private long timeoutMillis;
    private final HttpClient httpclient;
    private int hdrDigits;
    private HttpMetrics httpMetrics;
    private boolean diagnosticsEnabled;


    public HttpSpace(String spaceName, NBConfiguration cfg) {
        this.name = spaceName;
        this.cfg = cfg;
        applyConfig(cfg);
        this.httpclient = newClient();
    }

    public HttpClient getClient() {
        return this.httpclient;
    }

    private HttpClient newClient() {
        HttpClient.Builder builder = HttpClient.newBuilder();
        logger.debug(() -> "follow_redirects=>" + followRedirects);
        builder = builder.followRedirects(this.followRedirects);
        builder = builder.connectTimeout(this.timeout);
        return builder.build();
    }

    public synchronized void applyConfig(NBConfiguration cfg) {
        this.followRedirects =
            HttpClient.Redirect.valueOf(
                cfg.get("follow_redirects", String.class).toUpperCase(Locale.ROOT)
            );
        this.timeout = Duration.ofMillis(cfg.get("timeout", long.class));
        this.timeoutMillis = cfg.get("timeout", long.class);
        this.httpMetrics = new HttpMetrics(this);

        this.console = cfg.getOptional("diag").map(s -> HttpConsoleFormats.apply(s, this.console))
            .orElseGet(() -> HttpConsoleFormats.apply(null,null));

        this.diagnosticsEnabled = console.isDiagnosticMode();

    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public int getHdrDigits() {
        return hdrDigits;
    }

    @Override
    public String getName() {
        return name;
    }

    public HttpMetrics getHttpMetrics() {
        return httpMetrics;
    }

    public boolean isDiagnosticMode() {
        return diagnosticsEnabled;
    }

    public HttpConsoleFormats getConsole() {
        return console;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(HttpSpace.class)
            .add(Param.defaultTo("follow_redirects", "normal")
                .setRegex("normal|always|never")
                .setDescription("Whether to follow redirects. Normal redirects are those which do not " +
                    "redirect from HTTPS to HTTP.")
            )
            .add(Param.optional(List.of("diag","diagnostics"), String.class)
                .setDescription("Print extended diagnostics. This option has numerous" +
                    " possible values. See the markdown docs for details. (nb help http)")
            )
            .add(Param.defaultTo("timeout", 1000L*60L*15L) // 15 minutes
                .setDescription("How long to wait for requests before timeout out. Default is forever."))
            .add(Param.defaultTo("hdr_digits", 4)
                .setDescription("number of digits of precision to keep in HDR histograms"))
            .asReadOnly();

    }



}
