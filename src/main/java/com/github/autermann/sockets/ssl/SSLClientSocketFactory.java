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
package com.github.autermann.sockets.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocket;

import com.github.autermann.sockets.client.ClientSocketFactory;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SSLClientSocketFactory extends ClientSocketFactory {
    private final SSLFactory delegate;

    public SSLClientSocketFactory(SSLConfiguration options) {
        this.delegate = new SSLFactory(Preconditions.checkNotNull(options));
    }

    @Override
    public SSLSocket createSocket(InetSocketAddress address, int timeout)
            throws IOException, SocketException {
        try {
            return delegate.createSocket(address, timeout);
        } catch (GeneralSecurityException ex) {
            throw new SSLSocketCreationException(ex);
        }
    }

    @Override
    public SSLSocket createSocket(String host, int port, int timeout)
            throws IOException, SocketException {
        return createSocket(new InetSocketAddress(host, port), timeout);
    }
}
