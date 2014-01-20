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
package com.github.autermann.sockets.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class StreamingSocketClient {

    private static final Logger log = LoggerFactory
            .getLogger(SocketClientBuilder.class);
    private final ClientSocketFactory socketFactory;
    private final InetSocketAddress address;
    private final int timeOut;
    private SocketConnection con;
    StreamingSocketClient(InetSocketAddress address,
                          ClientSocketFactory socketFactory,
                          int timeout) {
        this.address = checkNotNull(address);
        this.socketFactory = checkNotNull(socketFactory);
        checkArgument(timeout > 0);
        this.timeOut = timeout;
    }

    public int getConnectionTimeout() {
        return timeOut;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public ClientSocketFactory getSocketConnection() {
        return socketFactory;
    }

    public void exec(StreamingSocketClientHandler handler) throws IOException {
        if (con == null || con.isClosed()) {
            con = new SocketConnection();
        }
        handler.handle(con, con);
    }

    public void close() {
        if (con != null) {
            con.close();
        }
    }

    protected class SocketConnection implements InputSupplier<InputStream>,
                                                OutputSupplier<OutputStream> {
        private final Socket socket;

        SocketConnection() throws IOException {
            this.socket = getSocketConnection()
                    .createSocket(getAddress(), getConnectionTimeout());
        }

        private Socket getSocket() {
            return socket;
        }

        @Override
        public InputStream getInput() throws IOException {
            return getSocket().getInputStream();
        }

        @Override
        public OutputStream getOutput() throws IOException {
            return getSocket().getOutputStream();
        }

        public void close() {
            try {
                getSocket().close();
            } catch (IOException e) {
                log.error("Error closing socket", e);
            }
        }

        public boolean isClosed() {
            return getSocket().isClosed();
        }

        public boolean isConnected() {
            return getSocket().isConnected();
        }
    }

}
