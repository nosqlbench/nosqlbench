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

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.http.HttpRequest;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class RawSocketInjector {
    private final static Logger logger = LogManager.getLogger("GRAFANA-SOCKET-AUTH");

    public static Optional<ApiToken> submit(ApiTokenRequest apirq, HttpRequest rq) {
        Socket clientSocket = null;
        InputStream fromServer = null;
        OutputStream toServer = null;

        try {
            SocketFactory socketFactory = SocketFactory.getDefault();
            if (!rq.uri().getScheme().equals("http")) {
                throw new RuntimeException("URI scheme " + rq.uri().getScheme() + " not supported for auto-key yet.");
            }
            clientSocket = socketFactory.createSocket(rq.uri().getHost(), rq.uri().getPort());
            fromServer = clientSocket.getInputStream();
            toServer = clientSocket.getOutputStream();

            String rqbody = """
                {
                 "Name": "_NAME_",
                 "Role": "_ROLE_"
                }
                """
                .replace("_NAME_", apirq.getName() + "_" + System.currentTimeMillis())
                .replace("_ROLE_", apirq.getRole());

            String basicAuthDigest = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
            String rqProtocol = """
                POST __PATH__ HTTP/1.1
                Host: __HOST__:__PORT__
                Authorization: __AUTHORIZATION__
                User-Agent: **nb**
                Accept: */*
                Content-Type: application/json
                Content-Length: __CONTENT_LENGTH__
                    """.replace("__PATH__", rq.uri().getPath())
                .replace("__CONTENT_LENGTH__", String.valueOf(rqbody.getBytes(StandardCharsets.UTF_8).length))
                .replace("__AUTHORIZATION__", basicAuthDigest)
                .replace("__HOST__", rq.uri().getHost())
                .replace("__PORT__", String.valueOf(rq.uri().getPort()));

            CharBuffer rqbuf = CharBuffer.allocate(1000000);
            rqbuf.put(rqProtocol)
                .put("\r\n")
                .put(rqbody)
                .put("\r\n");
            rqbuf.flip();

            String requestContent = rqbuf.toString();
            logger.trace("authorizer request:\n" + requestContent + "\n");
            toServer.write(requestContent.getBytes(StandardCharsets.UTF_8));
            toServer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(fromServer));
            CharBuffer inbuf = CharBuffer.allocate(1000000);
            while (reader.ready() && (reader.read(inbuf)) >= 0) {
            }
            inbuf.flip();
            String response = inbuf.toString();


            logger.trace("authorizer response:\n" + response + "\n");
            String[] headersAndBody = response.split("\r\n\r\n", 2);
            String[] statusAndHeaders = headersAndBody[0].split("\r\n", 2);
            if (!statusAndHeaders[0].contains("200 OK")) {
                logger.error("Status was unexpected: '" + statusAndHeaders[0] + "'");
                return Optional.empty();
            }

//            toServer.close();
//            fromServer.close();
//            clientSocket.close();
//            toServer=null;
//            fromServer=null;
//            clientSocket=null;

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ApiToken apiToken = gson.fromJson(headersAndBody[1], ApiToken.class);
            return Optional.of(apiToken);

        } catch (Exception e) {
            logger.error("error while authorizing grafana client over raw socket: " + e, e);
            return Optional.empty();
        } finally {
            try {
                if (toServer != null) {
                    toServer.close();
                }
                if (fromServer != null) {
                    fromServer.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }
}
