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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.SocketException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLServerSocket;

import com.github.autermann.sockets.server.ServerSocketFactory;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SSLServerSocketFactory extends ServerSocketFactory {

    private final SSLFactory delegate;

    public SSLServerSocketFactory(SSLConfiguration options) {
        this.delegate = new SSLFactory(checkNotNull(options));
    }

    @Override
    public SSLServerSocket createSocket(int port)
            throws IOException, SocketException {
        try {
            SSLServerSocket socket = delegate.createServerSocket(port);
            return socket;
        } catch (GeneralSecurityException ex) {
            throw new SSLSocketCreationException(ex);
        }
    }

}
