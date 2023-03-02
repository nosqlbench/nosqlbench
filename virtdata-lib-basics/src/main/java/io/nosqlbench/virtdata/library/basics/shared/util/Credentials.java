
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
package io.nosqlbench.virtdata.library.basics.shared.util;

public class Credentials {

    private static final String DEFAULT_IDENTITY = "cassandra";
    private String username;
    private String password;
    private String authToken;

    public static Credentials defaultCredentials() {
        return new Credentials(DEFAULT_IDENTITY, DEFAULT_IDENTITY);
    }

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Added for support of auth tokens not obtained via defaultCredentials
    public Credentials(String authToken) {
        this.authToken = authToken;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthToken() {
        return authToken;
    }
}
