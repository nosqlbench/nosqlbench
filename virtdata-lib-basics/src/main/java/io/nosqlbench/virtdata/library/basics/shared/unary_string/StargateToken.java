/*
 * Copyright (c) 2023 nosqlbench
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
package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import com.google.gson.Gson;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.util.Credentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;


@ThreadSafeMapper
@Categories({Category.general})
public class StargateToken implements Function<Object, String> {

    private static final Logger logger = LogManager.getLogger(StargateToken.class);
    private final Credentials credentials;
    private final String url;
    private final HttpClient httpClient;


    public StargateToken(String url) throws SecurityException {
        this(url, Credentials.defaultCredentials());
    }

    public StargateToken(String url, Credentials credentials) throws SecurityException {
        this(url, credentials, HttpClient.newBuilder().build());
    }

    public StargateToken(String url, Credentials credentials, HttpClient client) throws SecurityException {
        this.url = url;
        this.credentials = credentials;
        this.httpClient = client;
        if (TokenKeeper.isExpired()) {
            authTokenStargate(url, credentials);
        }
    }

    @Override
    public String apply(Object value) throws SecurityException {
        if (TokenKeeper.isExpired()) {
            authTokenStargate(url, credentials);
        }
        return TokenKeeper.token;
    }

    public static void setExpired() {
        TokenKeeper.isExpiredRequested = true;
    }

    private void authTokenStargate(String url, Credentials credentials) throws SecurityException {

        if (credentials == null || url == null) {
            throw new BasicError("Must provide url and credentials to obtain authTokenStargate");
        }
        logger.debug("Received url for Stargate auth token request: {} ", url);

        try {
            final Gson gson = new Gson();
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder = builder.uri(URI.create(url));
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(credentials)));
            builder.setHeader("Content-Type", "application/json");

            HttpRequest request = builder.build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug(() -> "Stargate response status code: " + resp.statusCode());

            if (resp.statusCode() != 201) {
                final String errorMessage = "Unable to obtain expected auth token, with status code: "
                        + resp.statusCode() + " :" + resp.body();
                logger.error(() -> errorMessage);
                throw new BasicError(errorMessage);
            }

            Credentials retrievedToken = gson.fromJson(resp.body(), Credentials.class);
            TokenKeeper.setToken(retrievedToken.getAuthToken());

        } catch (Exception e) {
            throw new SecurityException("Auth Token error, stargate-token retrieval failure", e);
        }
    }

    public static final class TokenKeeper {
        private static final long TOKEN_EXPIRE_MIN = 30;
        private static String token;
        private static boolean isExpiredRequested = true;
        private static Instant lastTokenInstant = Instant.now();

        private TokenKeeper() {
        }

        public static void reset() {
            token = null;
            isExpiredRequested = true;
            lastTokenInstant = Instant.now();
        }

        public static void setToken(String input) {
            token = input;
        }

        public static String getToken() {
            return token;
        }

        public static Instant lastTokenInstant() {
            return lastTokenInstant;
        }

        public static boolean isExpired() {

            if (isExpiredRequested || Duration.between(Instant.now(),
                    lastTokenInstant).toMinutes() > TOKEN_EXPIRE_MIN) {
                logger.trace("Token expiry detected.");
                lastTokenInstant = Instant.now();
                isExpiredRequested = false;
                return true;
            }
            return false;
        }
    }
}
