/*
 * Copyright 2013 Christian Autermann
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
package com.github.autermann.sockets.server;

import static com.google.common.base.Preconditions.checkState;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.io.Closer;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class StreamingSocketServer {
    private static final Logger log = LoggerFactory
            .getLogger(StreamingSocketServer.class);
    private final ServerSocketFactory serverSocketFactory;
    private final Supplier<StreamingSocketServerHandler> handlerFactory;
    private final int port;
    private final Executor pool;
    private final List<Runnable> shutdownHooks;
    private ServerSocket serverSocket;

    StreamingSocketServer(ServerSocketFactory serverSocketFactory,
                          Supplier<StreamingSocketServerHandler> handlerFactory,
                          Executor executor, List<Runnable> shutdownHooks,
                          int port) {
        this.serverSocketFactory = serverSocketFactory;
        this.handlerFactory = handlerFactory;
        this.shutdownHooks = shutdownHooks;
        this.port = port;
        this.pool = executor;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public ServerSocketFactory getServerSocketFactory() {
        return this.serverSocketFactory;
    }

    public Supplier<StreamingSocketServerHandler> getHandlerFactory() {
        return this.handlerFactory;
    }

    public int getPort() {
        return this.port;
    }

    public void stop() {
        try {
            if (getServerSocket() != null) {
                getServerSocket().close();
            }
        } catch (IOException ex) {
            log.error("Error closing server socket", ex);
        }
        for (Runnable hook : shutdownHooks) {
            try {
                hook.run();
            } catch (Throwable t) {
                log.error("Error running Shutdown hook " + hook, t);
            }
        }
    }

    public void start(boolean block) throws IOException {
        synchronized (this) {
            checkState(getServerSocket() == null, "Server already started.");
        }
        this.serverSocket = getServerSocketFactory().createSocket(getPort());
        log.info("Listening on port {}...", getPort());
        if (block) {
            loop();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            }).start();
        }
    }

    private void loop() {
        try {
            while (!getServerSocket().isClosed()) {
                awaitConnection();
            }
        } finally {
            stop();
        }
    }

    private void awaitConnection() {
        try {
            Socket socket = getServerSocket().accept();
            log.info("Client {} connected.", socket
                    .getRemoteSocketAddress());
            pool.execute(new HandlerTask(socket));
        } catch (IOException e) {
            // this exception will be thrown a few times during shutdown, hence the check here
            if (!getServerSocket().isClosed()) {
                log.error("Could not accept client connection: {}",
                          e.getMessage());
            }
        }
    }

    private class HandlerTask implements Runnable {
        private final Socket socket;
        private final StreamingSocketServerHandler handler;

        HandlerTask(Socket socket) {
            this.socket = socket;
            this.handler = getHandlerFactory().get();
        }

        @Override
        public void run() {
            Closer c = Closer.create();
            try {
                c.register(new ClosableSocket(socket));
                InputStream in = c.register(socket.getInputStream());
                OutputStream out = c.register(socket.getOutputStream());
                handler.handle(in, out);
            } catch (IOException ex) {
                log.error("Couldn't handle input/output streams: " +
                          ex.getMessage(), ex);
            } finally {
                try {
                    c.close();
                } catch (IOException e) {
                    log.error("Couldn't close socket: " +
                              e.getMessage(), e);
                }
                log.info("Client {} disconnected.", socket
                    .getRemoteSocketAddress());
            }
        }

    }

    private class ClosableSocket implements Closeable {
        private final Socket socket;

        ClosableSocket(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void close() throws IOException {
            this.socket.close();
        }
    }
}
