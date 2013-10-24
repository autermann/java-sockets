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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.github.autermann.sockets.ClientSocketFactory;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @param <I>
 * @param <O>
 */
public class RequestSocketClient<I, O> extends StreamingSocketClient {
    private final RequestSocketClientHandler<I, O> requestHandler;

    RequestSocketClient(RequestSocketClientHandler<I, O> requestHandler,
                     InetSocketAddress address,
                     ClientSocketFactory socketFactory,
                     int timeout,
                     int attempts) {
        super(address, socketFactory, timeout, attempts);
        this.requestHandler = checkNotNull(requestHandler);
    }

    public RequestSocketClientHandler<I, O> getRequestHandler() {
        return requestHandler;
    }

    /**
     * Sends a request to a socket server. The server must be connections on the
     * specified port.
     *
     * @param request the request to send
     *
     * @return the response
     *
     * @throws IOException if the connection to the socket server failed
     */
    public O exec(I request) throws IOException {
        StreamingHandlerImpl h = new StreamingHandlerImpl(request);
        exec(h);
        return h.getResponse();
    }

    private class StreamingHandlerImpl implements StreamingSocketClientHandler {
        private O response;
        final I request;

        StreamingHandlerImpl(I request) {
            this.request = request;
        }

        @Override
        public void handle(InputSupplier<InputStream> in,
                           OutputSupplier<OutputStream> out)
                throws IOException {
            getRequestHandler().encode(request, out);
            response = getRequestHandler().decode(in);
        }

        public O getResponse() {
            return response;
        }
    }

}
