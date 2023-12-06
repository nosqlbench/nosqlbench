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

package io.nosqlbench.adapter.tcpclient;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.nb.api.engine.util.SSLKsFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

public class TcpClientAdapterSpace {

    private final static Logger logger = LogManager.getLogger(TcpClientAdapterSpace.class);
    private final NBConfiguration config;
    Writer writer;

    public TcpClientAdapterSpace(NBConfiguration config) {
        this.config = config;
        this.writer = createPrintWriter();
    }

    protected PrintWriter createPrintWriter() {

        SocketFactory socketFactory = SocketFactory.getDefault();
        boolean sslEnabled = config.getOptional(boolean.class, "ssl").orElse(false);
        if (sslEnabled) {
            NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(config);
            socketFactory = SSLKsFactory.get().createSocketFactory(sslCfg);
        }

        String host = config.getOptional("host").orElse("localhost");
        int port = config.getOptional(int.class, "port").orElse(12345);

        try {
            Socket socket = socketFactory.createSocket(host, port);
            logger.info("connected to " + socket.toString());
            return new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error opening socket:" + e, e);
        }
    }

    public void writeflush(String text) {
        try {
            writer.write(text);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(TcpClientAdapterSpace.class)
            .add(SSLKsFactory.get().getConfigModel())
            .add(
                Param.defaultTo("host","localhost")
                    .setDescription("the host address to use")
            )
            .add(
                Param.defaultTo("port",12345)
                    .setDescription("the designated port to connect to on the socket")
            )
            .add(
                Param.defaultTo("newline",true)
                    .setDescription("whether to automatically add a missing newline to the end of any output\n")
            )
            .add(
                Param.optional("format")
                    .setRegex("csv|readout|json|inlinejson|assignments|diag")
                    .setDescription("""
                        Which format to use.
                        If provided, the format will override any statement formats provided by the YAML.
                        If 'diag' is used, a diagnostic readout will be provided for binding constructions.""")
            )
            .add(
                Param.defaultTo("bindings","doc")
                    .setDescription("""
                        This is a simple way to specify a filter for the names of bindings that you want to use.
                        "If this is 'doc', then all the document level bindings are used. If it is any other value, it is taken
                        "as a pattern (regex) to subselect a set of bindings by name. You can simply use the name of a binding
                        "here as well.""")

            )
            .asReadOnly();
    }

}
