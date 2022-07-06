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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.clients.grafana.ApiToken;
import io.nosqlbench.engine.clients.grafana.transfer.ApiTokenRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.nio.CharBuffer;
import java.util.Optional;

public class CurlCmdInjector {
    private final static Logger logger = LogManager.getLogger("GRAFANA-CURL-AUTH");

    public static Optional<ApiToken> submit(ApiTokenRequest apirq, HttpRequest rq) {
        try {
            ProcessBuilder curlProcessSpec = new ProcessBuilder();
            String requestJson = """
                {
                 "Name": "NAME",
                 "Role": "ROLE"
                }
                """.replace("NAME", apirq.getName() + "_" + System.currentTimeMillis())
                .replace("ROLE", apirq.getRole())
                .replaceAll("\n", "");

            String[] args = new String[]{
                "/usr/bin/curl", "-s", "-XPOST", rq.uri().toString(),
                "-H", "Content-Type: application/json",
                "-d", requestJson
            };

            curlProcessSpec.command(
                args
            );

//            curlProcessSpec.inheritIO();
            Process curlProcess = curlProcessSpec.start();


//            OutputStream stdinStream = curlProcess.getOutputStream();

            InputStream stdoutStream = curlProcess.getInputStream();
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutStream));
            InputStream errorStream = curlProcess.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));

            curlProcess.waitFor();

            CharBuffer errorBuffer = CharBuffer.allocate(1000000);
            while (errorReader.ready()) {
                errorReader.read(errorBuffer);
            }
            errorBuffer.flip();
            String errorContents = errorBuffer.toString();
            if (errorContents.length() > 0) {
                logger.error("stderr from curl command follows:\n" + errorContents);
            }

            int status = curlProcess.exitValue();
            if (status != 0) {
                logger.error("exit status " + status + " from curl command indicates error");
                return Optional.empty();
            }

            CharBuffer stdoutBuffer = CharBuffer.allocate(1000000);
            while (stdoutReader.ready()) {
                stdoutReader.read(stdoutBuffer);
            }
            stdoutBuffer.flip();
            String stdoutContents = stdoutBuffer.toString();
            if (stdoutContents.length() == 0) {
                logger.error("unable to read contents of curl command output");
                return Optional.empty();
            }
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                ApiToken apiToken = gson.fromJson(stdoutContents, ApiToken.class);
                return Optional.of(apiToken);

            } catch (Exception e) {
                logger.error("Error while parsing response from api token request: " + stdoutContents);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("error while trying to authorized grafana client via curl comment: " + e, e);
            return Optional.empty();
        }

    }
}
