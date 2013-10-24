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
package com.github.autermann.sockets.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.sockets.ClientSocketFactory;
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
    private final int attempts;

    StreamingSocketClient(InetSocketAddress address,
                          ClientSocketFactory socketFactory,
                          int timeout,
                          int attempts) {
        this.address = checkNotNull(address);
        this.socketFactory = checkNotNull(socketFactory);
        checkArgument(timeout > 0);
        this.timeOut = timeout;
        checkArgument(attempts > 0);
        this.attempts = attempts;
    }

    public int getConnectionAttempts() {
        return attempts;
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
        SocketException thrown = null;
        for (int i = 0; i < getConnectionAttempts(); i++) {
            try {
                SocketConnection con = new SocketConnection();
                try {
                    handler.handle(con, con);
                } finally {
                    con.close();
                }
            } catch (SocketException e) {
                thrown = e;
            }
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    protected class SocketConnection implements InputSupplier<InputStream>,
                                                OutputSupplier<OutputStream> {
        private final Socket socket;

        SocketConnection() throws IOException {
            this.socket = getSocketConnection()
                    .createSocket(getAddress(),
                                  getConnectionTimeout());
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
    }

}
