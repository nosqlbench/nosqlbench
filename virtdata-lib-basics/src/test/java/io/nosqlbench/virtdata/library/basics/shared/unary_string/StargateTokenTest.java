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

import io.nosqlbench.virtdata.library.basics.shared.util.Credentials;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
class StargateTokenTest extends TokenTest {

    private static final String TEST_AUTH_TOKEN = "8675309";
    private static final String VALID_TEST_URL = "http://foobar.com:8675";
    private static final String VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON =
            "{ 'authToken': " + "\"" + TEST_AUTH_TOKEN + "\"" + "}";
    private static final Credentials VALID_TEST_CREDS = new Credentials("username", "password");

    private static final Object TOKEN_APPLY_PLACEHOLDER = new Object();
    @Mock
    private static HttpResponse<String> httpResponse;
    @Mock
    private static HttpClient httpClient;

    @BeforeAll
    public static void init() {
        httpResponse = mock(HttpResponse.class);
        httpClient = mock(HttpClient.class);
    }

    @BeforeEach
    public void setup() {
        StargateToken.TokenKeeper.reset();
    }

    @Test
    void applyTokenSuccess() throws Exception {

        when(httpResponse.body()).thenReturn(VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpClient.send(Mockito.any(HttpRequest.class),
                Mockito.any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(httpResponse);

        final StargateToken stargateToken = new StargateToken(VALID_TEST_URL,
                VALID_TEST_CREDS, httpClient);
        final String result = stargateToken.apply(TOKEN_APPLY_PLACEHOLDER);

        assertThat(result).isEqualTo(TEST_AUTH_TOKEN);

    }

    @Test
    void receivedResponse500() throws Exception {

        when(httpResponse.body()).thenReturn(VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON);
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClient.send(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandlers.ofString()
                .getClass())))
                .thenReturn(httpResponse);

        assertThatExceptionOfType(SecurityException.class).isThrownBy(() -> new StargateToken(VALID_TEST_URL,
                VALID_TEST_CREDS, httpClient));

    }

    @Test
    void applyTokenSuccessWithRefreshTokenRequested() throws Exception {

        // --- Initial check
        StargateToken.TokenKeeper.reset();

        when(httpResponse.body()).thenReturn(VALID_STARGATE_AUTH_TOKEN_RESPONSE_JSON);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpClient.send(Mockito.any(HttpRequest.class),
                Mockito.any(HttpResponse.BodyHandlers.ofString()
                        .getClass())))
                .thenReturn(httpResponse);

        final StargateToken stargateToken = new StargateToken(VALID_TEST_URL, VALID_TEST_CREDS, httpClient);
        final String resultFirstCheck = stargateToken.apply(TOKEN_APPLY_PLACEHOLDER);
        final Instant tokenInstantFirstCheck = StargateToken.TokenKeeper.lastTokenInstant();

        assertThat(resultFirstCheck).isEqualTo(TEST_AUTH_TOKEN);
        assertThat(tokenInstantFirstCheck).isNotNull();

        // --- Subtest 2 - When NOT having an expired token, expect that the lastTokenInstant does NOT change.
        when(httpResponse.body()).thenReturn("{ 'authToken': " + "\"" + "refreshed-token" + "\"" + "}");
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpClient.send(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandlers.ofString()
                .getClass())))
                .thenReturn(httpResponse);

        final String resultSecondCheck = stargateToken.apply(TOKEN_APPLY_PLACEHOLDER);
        final Instant tokenInstantSecondCheck = StargateToken.TokenKeeper.lastTokenInstant();

        assertThat(resultSecondCheck).isEqualTo(resultFirstCheck);
        assertThat(tokenInstantSecondCheck).isEqualTo(tokenInstantFirstCheck);

        // --- Subtest 3 - When having an expired token, expect that the lastTokenInstant changes and
        // tokens are different.
        // Note: Explicit token expiry, default is 30 minutes
        StargateToken.setExpired();

        final String resultThirdCheck = stargateToken.apply(TOKEN_APPLY_PLACEHOLDER);
        final FieldReader fileReaderLastCheck = new FieldReader(stargateToken,
                FieldUtils.getDeclaredField(StargateToken.class,
                        "lastTokenInstant", true));
        final Instant tokenInstantThirdCheck = StargateToken.TokenKeeper.lastTokenInstant();

        assertThat(tokenInstantThirdCheck.isAfter(tokenInstantFirstCheck)).isTrue();
        assertThat(resultSecondCheck).isNotEqualTo(resultThirdCheck);
    }

}