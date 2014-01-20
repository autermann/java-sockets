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
import static com.google.common.base.Preconditions.checkState;

import java.net.InetSocketAddress;

import com.github.autermann.sockets.ssl.SSLClientSocketFactory;
import com.github.autermann.sockets.ssl.SSLConfiguration;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SocketClientBuilder {
    private static final int DEFAULT_TIMEOUT = 10 * 1000;
    private InetSocketAddress address;
    private ClientSocketFactory socketFactory;
    private int timeout = DEFAULT_TIMEOUT;

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
        return new RequestSocketClient<I, O>(handler, address, socketFactory, timeout);
    }

    public StreamingSocketClient build() {
        validate();
        return new StreamingSocketClient(address, socketFactory, timeout);
    }

    public static SocketClientBuilder create() {
        return new SocketClientBuilder();
    }
}
