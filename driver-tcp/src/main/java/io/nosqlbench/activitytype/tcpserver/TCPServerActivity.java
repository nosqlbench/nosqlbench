/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activitytype.tcpserver;

import io.nosqlbench.activitytype.stdout.StdoutActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TCPServerActivity extends StdoutActivity {

    private final static Logger logger = LogManager.getLogger(TCPServerActivity.class);
    private final ServerSocketFactory socketFactory;
    private final LinkedBlockingQueue<String> queue;
    private ServerSocket listenerSocket;
    private final List<Shutdown> managedShutdown = new ArrayList<>();
    private int capacity=10;


    public TCPServerActivity(ActivityDef activityDef) {
        super(activityDef);
        boolean sslEnabled = activityDef.getParams().getOptionalBoolean("ssl").orElse(false);
        this.capacity=activityDef.getParams().getOptionalInteger("capacity").orElse(10);
        queue = new LinkedBlockingQueue<>(capacity);

        if (sslEnabled) {
            socketFactory = SSLKsFactory.get().createSSLServerSocketFactory(activityDef.getParams());
        } else {
            socketFactory = ServerSocketFactory.getDefault();
        }
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }

    @Override
    public void shutdownActivity() {
        super.shutdownActivity();
        for (Shutdown toClose : managedShutdown) {
            toClose.shutdown();
        }
    }

    @Override
    public synchronized void write(String statement) {
        while (true) {
            try {
                queue.put(statement);
                return;
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected synchronized Writer createPrintWriter() {

        String host = getActivityDef().getParams().getOptionalString("host").orElse("localhost");
        int port = getActivityDef().getParams().getOptionalInteger("port").orElse(12345);

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

    private interface Shutdown {
        void shutdown();
    }

    public static class SocketWriter implements Runnable, Shutdown {
        private final BlockingQueue<String> sourceQueue;
        private final OutputStream outputStream;
        private final OutputStreamWriter writer;
        private boolean running = true;


        public SocketWriter(BlockingQueue<String> sourceQueue, Socket connectedSocket) {
            this.sourceQueue = sourceQueue;
            try {
                outputStream = connectedSocket.getOutputStream();
                this.writer = new OutputStreamWriter(outputStream);
                //connectedSocket.shutdownInput();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void shutdown() {
            this.running = false;
        }

        @Override
        public void run() {
            try (Writer writer = this.writer) {
                while (true) {
                    while (!sourceQueue.isEmpty() || running) {
                        try {
                            String data = sourceQueue.take();
                            writer.write(data);
                            writer.flush();
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
            try (ServerSocket serverSocket = this.serverSocket) {
                while (running) {
                    serverSocket.setSoTimeout(1000);
                    serverSocket.setReuseAddress(true);
                    try {
                        Socket connectedSocket = serverSocket.accept();
                        SocketWriter writer = new SocketWriter(queue, connectedSocket);
                        TCPServerActivity.this.managedShutdown.add(writer);
                        Thread writerThread = new Thread(writer);
                        writerThread.setName("SocketWriter/" + connectedSocket);
                        writerThread.setDaemon(true);
                        writerThread.start();
                        logger.info("Started writer thread for " + connectedSocket);
                    } catch (SocketTimeoutException ignored) {
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}


