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
import static com.google.common.base.Preconditions.checkState;

import java.net.InetSocketAddress;

import com.github.autermann.sockets.ClientSocketFactory;
import com.github.autermann.sockets.ssl.SSLClientSocketFactory;
import com.github.autermann.sockets.ssl.SSLConfiguration;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SocketClientBuilder {
    private static final int DEFAULT_TIMEOUT = 10 * 1000;
    public static final int DEFAULT_ATTEMPTS = 3;
    private InetSocketAddress address;
    private ClientSocketFactory socketFactory;
    private int timeout = DEFAULT_TIMEOUT;
    private int attempts = DEFAULT_ATTEMPTS;

    public SocketClientBuilder withAddress(InetSocketAddress address) {
        this.address = checkNotNull(address);
        return this;
    }

    public SocketClientBuilder withAddress(String host, int port) {
        checkNotNull(host);
        checkArgument(port > 0);
        return withAddress(new InetSocketAddress(host, port));
    }

    public SocketClientBuilder withSocketFactory(
            ClientSocketFactory socketFactory) {
        this.socketFactory = checkNotNull(socketFactory);
        return this;
    }

    public SocketClientBuilder withSSL(SSLConfiguration config) {
        if (config == null) {
            this.socketFactory = null;
            return this;
        } else {
            return withSocketFactory(new SSLClientSocketFactory(config));
        }
    }

    public SocketClientBuilder withTimeout(int timeout) {
        checkArgument(timeout > 0);
        this.timeout = timeout;
        return this;
    }

    public SocketClientBuilder withAttempts(int attempts) {
        checkArgument(attempts > 0);
        this.attempts = attempts;
        return this;
    }

    private void validate() {
        checkState(address != null);
        if (socketFactory == null) {
            socketFactory = ClientSocketFactory.getDefault();
        }
    }

    public <I, O> RequestSocketClient<I, O> build(
            RequestSocketClientHandler<I, O> handler) {
        checkNotNull(handler);
        validate();
        return new RequestSocketClient<I, O>(handler, address, socketFactory, timeout, attempts);
    }

    public StreamingSocketClient build() {
        validate();
        return new StreamingSocketClient(address, socketFactory, timeout, attempts);
    }

    public static SocketClientBuilder create() {
        return new SocketClientBuilder();
    }
}
