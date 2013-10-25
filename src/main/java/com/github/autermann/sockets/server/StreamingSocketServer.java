/*
 * Copyright (C) 2013-2013 by it's authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.autermann.sockets.server;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.sockets.util.Factory;
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
    private final Factory<StreamingSocketServerHandler> handlerFactory;
    private final int port;
    private final Executor pool;
    private final List<Runnable> shutdownHooks;
    private ServerSocket serverSocket;

    StreamingSocketServer(ServerSocketFactory serverSocketFactory,
                         Factory<StreamingSocketServerHandler> handlerFactory,
                         Executor executor,
                         List<Runnable> shutdownHooks,
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

    public Factory<StreamingSocketServerHandler> getHandlerFactory() {
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
            this.handler = getHandlerFactory().create();
        }

        @Override
        public void run() {
            Closer c = Closer.create();
            try {
                c.register(this.socket);
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
            }
        }
    }
}
