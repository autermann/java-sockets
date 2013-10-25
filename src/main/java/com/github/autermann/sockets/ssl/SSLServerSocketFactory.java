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
