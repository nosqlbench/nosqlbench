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

package io.nosqlbench.adapter.tcpserver;

import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.util.SSLKsFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.ServerSocket;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

public class TcpServerAdapterSpace implements AutoCloseable{


    private final static Logger logger = LogManager.getLogger(TcpServerAdapterSpace.class);
    private final NBConfiguration config;
    Writer writer;
    private LinkedBlockingQueue<String> queue;
    private ServerSocket listenerSocket;
    private final List<Shutdown> managedShutdown = new ArrayList<>();
    private int capacity=10;

    public TcpServerAdapterSpace(NBConfiguration config) {
        this.config = config;
        this.writer = createPrintWriter();
    }

    private Writer createPrintWriter() {

        boolean sslEnabled = config.getOptional(Boolean.class, "ssl").orElse(false);
        this.capacity=config.getOptional(int.class, "capacity").orElse(10);
        queue = new LinkedBlockingQueue<>(capacity);
        ServerSocketFactory socketFactory;
        if (sslEnabled) {

            NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(config);
            socketFactory = SSLKsFactory.get().createSSLServerSocketFactory(sslCfg);
        } else {
            socketFactory = ServerSocketFactory.getDefault();
        }

        String host = config.getOptional("host").orElse("localhost");
        int port = config.getOptional(int.class, "port").orElse(12345);

        if (listenerSocket == null || listenerSocket.isClosed()) {
            try {
                InetAddress hostAddr = InetAddress.getByName(host);
                listenerSocket = socketFactory.createServerSocket(port, 10, hostAddr);
                if (socketFactory instanceof SSLServerSocketFactory) {
                    logger.info("SSL enabled on server socket " + listenerSocket);
                }
                SocketAcceptor socketAcceptor = new SocketAcceptor(queue, listenerSocket);
                managedShutdown.add(socketAcceptor);
                Thread acceptorThread = new Thread(socketAcceptor);
                acceptorThread.setDaemon(true);
                acceptorThread.setName("Listener/" + listenerSocket);
                acceptorThread.start();
            } catch (IOException e) {
                throw new RuntimeException("Error listening on listenerSocket:" + e, e);
            }
        }

        QueueWriterAdapter queueWriterAdapter = new QueueWriterAdapter(this.queue);
        logger.info("initialized queue writer:" + queueWriterAdapter);
        return queueWriterAdapter;
    }

    @Override
    public void close() throws Exception {
        logger.info("TcpServerAdapterSpace is waiting for message queue to empty");
        while(this.queue != null && !this.queue.isEmpty())
        {
            try {
                Thread.sleep(10);
            }catch (InterruptedException e) {
            }
        }
        logger.info("TcpServerAdapterSpace is being closed");
        for (Shutdown toClose : managedShutdown) {
            toClose.shutdown();
        }
    }

    public void writeflush(String text) {
        try {
            if(this.writer == null)
            {
                this.writer = createPrintWriter();
            }
            this.writer.write(text);
            this.writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(TcpServerAdapterSpace.class)
            .add(SSLKsFactory.get().getConfigModel())
            .add(
                Param.defaultTo("capacity",10)
                    .setDescription("the capacity of the queue")
            )
            .add(
                Param.defaultTo("host","localhost")
                    .setDescription("the host address to use")
            )
            .add(
                Param.defaultTo("port",12345)
                    .setDescription("the designated port to connect to on the socket")
            )
            .add(
                Param.defaultTo("filename","tcpserver")
                    .setDescription("this is the name of the output file. If 'stdout', output is sent to tcpserver, not a file.")
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
                        "If provided, the format will override any statement formats provided by the YAML.
                        "If 'diag' is used, a diagnostic readout will be provided for binding constructions.""")
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
    private interface Shutdown {
        void shutdown();
    }
    public static class SocketWriter implements Runnable, Shutdown {
        private final BlockingQueue<String> sourceQueue;
        private final OutputStreamWriter outWriter;
        private boolean running = true;


        public SocketWriter(BlockingQueue<String> sourceQueue, Socket connectedSocket) {
            this.sourceQueue = sourceQueue;
            try {
                OutputStream outputStream = connectedSocket.getOutputStream();
                this.outWriter = new OutputStreamWriter(outputStream);
                connectedSocket.shutdownInput();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void shutdown() {
            this.running = false;
        }

        @Override
        public void run() {
            try (Writer runWriter = this.outWriter) {
                while (running) {
                    while (!sourceQueue.isEmpty() ) {
                        try {
                            String data = sourceQueue.take();
                            runWriter.write(data);
                            runWriter.flush();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            finally {
                //TODO: Add the shutdown logic to do after closing the socket
            }

        }

    }

    public static class QueueWriterAdapter extends Writer {
        private BlockingQueue<String> queue;

        public QueueWriterAdapter(BlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        public synchronized void write( char[] cbuf, int off, int len) {
            while (true) {
                try {
                    queue.put(new String(cbuf, off, len));
                    return;
                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public synchronized void flush() throws IOException {

        }

        @Override
        public synchronized void close() throws IOException {
            flush();
            queue = null;
        }

    }

    public class SocketAcceptor implements Runnable, Shutdown {
        private final BlockingQueue<String> queue;
        private final ServerSocket serverSocket;
        private boolean running = true;

        public SocketAcceptor(BlockingQueue<String> queue, ServerSocket serverSocket) {
            this.queue = queue;
            this.serverSocket = serverSocket;
        }

        public void shutdown() {
            this.running = false;
        }

        @Override
        public void run() {
            try (ServerSocket runServerSocket = this.serverSocket) {
                while (running) {
                    runServerSocket.setSoTimeout(1000);
                    runServerSocket.setReuseAddress(true);
                    try {
                        Socket connectedSocket = runServerSocket.accept();
                        SocketWriter writer = new SocketWriter(queue, connectedSocket);
                        managedShutdown.add(writer);
                        Thread writerThread = new Thread(writer);
                        writerThread.setName("SocketWriter/" + connectedSocket);
                        writerThread.setDaemon(true);
                        writerThread.start();
                        logger.info("Started writer thread for " + connectedSocket);
                    } catch (SocketTimeoutException e) {
                        logger.debug("Socket timeout when waiting for a client connection to SocketWriter Server");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
