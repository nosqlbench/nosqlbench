/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.virtdata.library.basics.shared.util.Credentials;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenTest {

    private static final String TEST_AUTH_TOKEN = "8675309";
    private static final String VALID_TEST_URL = "http://foobar.com:8675";
    private static final String VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON =
            "{ 'authToken': " + "\"" + TEST_AUTH_TOKEN + "\"" + "}";
    private static final Credentials VALID_TEST_CREDS = new Credentials("username", "password");

    @Mock
    private static HttpResponse<String> httpResponse;
    @Mock
    private static HttpClient httpClient;

    private static MockedStatic<HttpClient> HttpCli;

    @Mock
    private static HttpClient.Builder httpBuilder;

    @BeforeAll
    public static void init() {
        httpResponse = mock(HttpResponse.class);
        httpClient = mock(HttpClient.class);
        httpBuilder = mock(HttpClient.Builder.class);
        HttpCli = Mockito.mockStatic(HttpClient.class);
    }

    @BeforeEach
    public void setup() {
        Token.TokenKeeper.reset();
    }

    @Test
    void applyTokenSuccess() throws Exception {

        when(httpResponse.body()).thenReturn(VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON);
        when(httpResponse.statusCode()).thenReturn(201);

        mockResponse();

        final Token token = new Token(null, VALID_TEST_URL, VALID_TEST_CREDS.getUsername(),
                VALID_TEST_CREDS.getPassword());
        // Since constructor handles state management, the inputs aren't utilized in the apply function.
        final String result = token.apply("p1");

        assertThat(result).isEqualTo(TEST_AUTH_TOKEN);
    }

    @Test
    void receivedResponse500() throws Exception {

        when(httpResponse.body()).thenReturn(VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON);
        when(httpResponse.statusCode()).thenReturn(500);
        mockResponse();

        assertThatExceptionOfType(SecurityException.class).isThrownBy(() -> new Token(null, VALID_TEST_URL,
                VALID_TEST_CREDS.getUsername(), VALID_TEST_CREDS.getPassword()));
    }

    @Test
    void applyTokenSuccessWithRefreshTokenRequested() throws Exception {

        Token.TokenKeeper.reset();

        when(httpResponse.body()).thenReturn(VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON);
        when(httpResponse.statusCode()).thenReturn(201);
        mockResponse();

        final Token token = new Token(null, VALID_TEST_URL, VALID_TEST_CREDS.getUsername(),
                VALID_TEST_CREDS.getPassword());
        final String resultFirstCheck = token.apply("p1");
        final Instant tokenInstantFirstCheck = Token.TokenKeeper.lastTokenInstant();

        assertThat(resultFirstCheck).isEqualTo(TEST_AUTH_TOKEN);
        assertThat(tokenInstantFirstCheck).isNotNull();

        // --- Subtest 2 - NOT having an expired token, expect that the lastTokenInstant does NOT change.
        when(httpResponse.body()).thenReturn("{ 'authToken': " + "\"" + "refreshed-token" + "\"" + "}");
        when(httpResponse.statusCode()).thenReturn(201);
        mockResponse();

        final String resultSecondCheck = token.apply("p1");
        final Instant tokenInstantSecondCheck = Token.TokenKeeper.lastTokenInstant();

        assertThat(resultSecondCheck).isEqualTo(resultFirstCheck);
        assertThat(tokenInstantSecondCheck).isEqualTo(tokenInstantFirstCheck);

        // --- Subtest 3 - Having expired token, expect that the lastTokenInstant changes and
        // tokens are different.
        // Note: Explicit token expiry, default is 30m
        Token.setExpired();

        final String resultThirdCheck = token.apply("p1");
        final FieldReader fileReaderLastCheck = new FieldReader(token,
                FieldUtils.getDeclaredField(Token.class,
                        "lastTokenInstant", true));
        final Instant tokenInstantThirdCheck = Token.TokenKeeper.lastTokenInstant();

        assertThat(tokenInstantThirdCheck.isAfter(tokenInstantFirstCheck)).isTrue();
        assertThat(resultSecondCheck).isNotEqualTo(resultThirdCheck);
    }

    @Test
    void provideToken() {

        final Token token = new Token(TEST_AUTH_TOKEN, VALID_TEST_URL, VALID_TEST_CREDS.getUsername(),
                VALID_TEST_CREDS.getPassword());

        final String result = token.apply("p1");

        assertThat(result).isEqualTo(TEST_AUTH_TOKEN);


        final Token token2 = new Token(TEST_AUTH_TOKEN, null, null, null);
        final String result2 = token2.apply("p1");

        assertThat(result2).isEqualTo(TEST_AUTH_TOKEN);
    }


    private void mockResponse() throws Exception {

        HttpCli.when(HttpClient::newBuilder).thenReturn(httpBuilder);
        when(httpBuilder.build()).thenReturn(httpClient);

        when(httpClient.send(Mockito.any(HttpRequest.class),
                Mockito.any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(httpResponse);
    }


}
