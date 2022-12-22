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

package io.nosqlbench.adapter.tcp;

import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.util.SSLKsFactory;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpAdapterSpace {

    private final static Logger logger = LogManager.getLogger(TcpAdapterSpace.class);
    private final String name;
    private final NBConfiguration config;

    public TcpAdapterSpace(String name, NBConfiguration config) {
        this.name = name;
        this.config = config;
    }

    protected PrintWriter createPrintWriter() {

        SocketFactory socketFactory = SocketFactory.getDefault();
        boolean sslEnabled = config.getOptional(Boolean.class, "ssl").orElse(false);
        if (sslEnabled) {
            NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(config);
            socketFactory = SSLKsFactory.get().createSocketFactory(sslCfg);
        }

        String host = config.getOptional("host").orElse("localhost");
        int port = config.getOptional(Integer.class, "port").orElse(12345);

        try {
            Socket socket = socketFactory.createSocket(host, port);
            logger.info(() -> "connected to " + socket.toString());
            return new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error opening socket:" + e, e);
        }
    }

    public void TCPServerActivity(ActivityDef activityDef) {
//        super(activityDef);
//        boolean sslEnabled = activityDef.getParams().getOptionalBoolean("ssl").orElse(false);
//        this.capacity=activityDef.getParams().getOptionalInteger("capacity").orElse(10);
//        queue = new LinkedBlockingQueue<>(capacity);
//
//        if (sslEnabled) {
//
//            NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(activityDef.getParams());
//            socketFactory = SSLKsFactory.get().createSSLServerSocketFactory(sslCfg);
//        } else {
//            socketFactory = ServerSocketFactory.getDefault();
//        }
    }

//    public void shutdownActivity() {
//        super.shutdownActivity();
//        for (TCPServerActivity.Shutdown toClose : managedShutdown) {
//            toClose.shutdown();
//        }
//    }

    // server write
//    @Override
//    public synchronized void write(String statement) {
//        while (true) {
//            try {
//                queue.put(statement);
//                return;
//            } catch (InterruptedException ignored) {
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    // create server writer
//
//    @Override
//    protected synchronized Writer createPrintWriter() {
//
//        String host = getActivityDef().getParams().getOptionalString("host").orElse("localhost");
//        int port = getActivityDef().getParams().getOptionalInteger("port").orElse(12345);
//
//        if (listenerSocket == null || listenerSocket.isClosed()) {
//            try {
//                InetAddress hostAddr = InetAddress.getByName(host);
//                listenerSocket = socketFactory.createServerSocket(port, 10, hostAddr);
//                if (socketFactory instanceof SSLServerSocketFactory) {
//                    logger.info(() -> "SSL enabled on server socket " + listenerSocket);
//                }
//                TCPServerActivity.SocketAcceptor socketAcceptor = new TCPServerActivity.SocketAcceptor(queue, listenerSocket);
//                managedShutdown.add(socketAcceptor);
//                Thread acceptorThread = new Thread(socketAcceptor);
//                acceptorThread.setDaemon(true);
//                acceptorThread.setName("Listener/" + listenerSocket);
//                acceptorThread.start();
//            } catch (IOException e) {
//                throw new RuntimeException("Error listening on listenerSocket:" + e, e);
//            }
//        }
//
//        TCPServerActivity.QueueWriterAdapter queueWriterAdapter = new TCPServerActivity.QueueWriterAdapter(this.queue);
//        logger.info(() -> "initialized queue writer:" + queueWriterAdapter);
//        return queueWriterAdapter;
//
//    }

    // socket writer
//    public SocketWriter(BlockingQueue<String> sourceQueue, Socket connectedSocket) {
//        this.sourceQueue = sourceQueue;
//        try {
//            outputStream = connectedSocket.getOutputStream();
//            this.writer = new OutputStreamWriter(outputStream);
//            //connectedSocket.shutdownInput();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    // server thread
//    public void run() {
//        try (Writer writer = this.writer) {
//            while (true) {
//                while (!sourceQueue.isEmpty() || running) {
//                    try {
//                        String data = sourceQueue.take();
//                        writer.write(data);
//                        writer.flush();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException ignored) {
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }

    // server writer adapter
//    public static class QueueWriterAdapter extends Writer {
//        private BlockingQueue<String> queue;
//
//        public QueueWriterAdapter(BlockingQueue<String> queue) {
//            this.queue = queue;
//        }
//
//        @Override
//        public synchronized void write( char[] cbuf, int off, int len) {
//            while (true) {
//                try {
//                    queue.put(new String(cbuf, off, len));
//                    return;
//                } catch (InterruptedException ignored) {
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//
//        @Override
//        public synchronized void flush() throws IOException {
//        }
//
//        @Override
//        public synchronized void close() throws IOException {
//            flush();
//            queue = null;
//        }
//
//    }

    // server socket acceptor
//    public class SocketAcceptor implements Runnable, TCPServerActivity.Shutdown {
//        private final BlockingQueue<String> queue;
//        private final ServerSocket serverSocket;
//        private boolean running = true;
//
//        public SocketAcceptor(BlockingQueue<String> queue, ServerSocket serverSocket) {
//            this.queue = queue;
//            this.serverSocket = serverSocket;
//        }
//
//        public void shutdown() {
//            this.running = false;
//        }
//
//        @Override
//        public void run() {
//            try (ServerSocket serverSocket = this.serverSocket) {
//                while (running) {
//                    serverSocket.setSoTimeout(1000);
//                    serverSocket.setReuseAddress(true);
//                    try {
//                        Socket connectedSocket = serverSocket.accept();
//                        TCPServerActivity.SocketWriter writer = new TCPServerActivity.SocketWriter(queue, connectedSocket);
//                        TCPServerActivity.this.managedShutdown.add(writer);
//                        Thread writerThread = new Thread(writer);
//                        writerThread.setName("SocketWriter/" + connectedSocket);
//                        writerThread.setDaemon(true);
//                        writerThread.start();
//                        logger.info(() -> "Started writer thread for " + connectedSocket);
//                    } catch (SocketTimeoutException ignored) {
//                    }
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }



    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(TcpAdapterSpace.class)
            .asReadOnly();
    }

}
