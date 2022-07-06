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

package io.nosqlbench.engine.clients.grafana.authorizers;

import io.nosqlbench.engine.clients.grafana.ApiToken;
import io.nosqlbench.engine.clients.grafana.transfer.ApiTokenRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Optional;

public class CurlCmdInjectorTest {

    private static ApiTokenRequest apiRequest;
    private static HttpRequest rq;

    @BeforeAll
    public static void setup() {
        apiRequest = new ApiTokenRequest("testrole", "Admin", 1000000);
        HttpRequest.Builder rqB = HttpRequest.newBuilder(URI.create("http://admin:admin@localhost:3000/api/auth/keys"));
        rqB.header("Content-Type","application/json");
        rq = rqB.build();
    }


    @Disabled
    @Test
    public void testCurlCmdAuthorizer() {
        Optional<ApiToken> result = CurlCmdInjector.submit(apiRequest, rq);
        System.out.println("result:\n"+ result);
    }

    @Disabled
    @Test
    public void testDirectSocketAuthorizer() {
        Optional<ApiToken> result = RawSocketInjector.submit(apiRequest, rq);
        System.out.println("result:\n" + result);
    }

}
