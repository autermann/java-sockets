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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.autermann.sockets.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class ClientSocketFactory {
    public abstract Socket createSocket(InetSocketAddress address, int timeout)
            throws IOException, SocketException;

    public abstract Socket createSocket(String host, int port, int timeout)
            throws IOException, SocketException;

    public static ClientSocketFactory getDefault() {
        return new ClientSocketFactory() {

            @Override
            public Socket createSocket(InetSocketAddress address, int timeout)
                    throws IOException, SocketException {
                Socket socket = new Socket();
                socket.connect(address, timeout);
                return socket;
            }

            @Override
            public Socket createSocket(String host, int port, int timeout)
                    throws IOException, SocketException {
                return createSocket(new InetSocketAddress(host, port), timeout);
            }
        };
    }
}
