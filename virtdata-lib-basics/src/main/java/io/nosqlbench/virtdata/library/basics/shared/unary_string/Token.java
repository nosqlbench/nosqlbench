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
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;


@ThreadSafeMapper
@Categories({Category.general})
public class Token implements TriFunction<String, String, String, String> {

    private static final Logger logger = LogManager.getLogger(Token.class);
    private Credentials credentials;
    private URI uri;
    private String providedToken;


    public Token(String token, String uri, String uid, String password) {

        if (token != null && !token.trim().isEmpty()) {
            this.providedToken = token.trim();
            return;
        }

        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected uri to be specified for Token.");
        }
        this.uri = URI.create(uri.trim());

        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected uid to be specified for Token.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected password to be specified for Token.");
        }
        this.credentials = Credentials.create(uid.trim(), password.trim());

        if (TokenKeeper.isExpired()) {
            authTokenStargate(this.uri, this.credentials);
        }
    }

    @Override
    public String apply(String p1, String p2, String p3) {

        if (this.providedToken != null) {
            return this.providedToken;
        }

        if (TokenKeeper.isExpired()) {
            return authTokenStargate(this.uri, this.credentials);
        }
        return TokenKeeper.token;
    }

    public static void setExpired() {
        TokenKeeper.isExpiredRequested = true;
    }

    private String authTokenStargate(URI uri, Credentials credentials) throws SecurityException {

        if (credentials == null || uri == null) {
            throw new BasicError("Must provide url and credentials to obtain authTokenStargate");
        }

        logger.debug(() -> "Received uri for Stargate auth token request: " + uri);

        try {
            final Gson gson = new Gson();
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder = builder.uri(uri);
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(credentials)));
            builder.setHeader("Content-Type", "application/json");

            HttpRequest request = builder.build();
            HttpClient httpClient = HttpClient.newBuilder().build();
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
            return TokenKeeper.getToken();

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
