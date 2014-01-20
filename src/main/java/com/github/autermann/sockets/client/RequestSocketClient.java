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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 * @param <I>
 * @param <O>
 */
public class RequestSocketClient<I, O> extends StreamingSocketClient {
    private final RequestSocketClientHandler<I, O> requestHandler;

    RequestSocketClient(RequestSocketClientHandler<I, O> requestHandler,
                     InetSocketAddress address,
                     ClientSocketFactory socketFactory,
                     int timeout) {
        super(address, socketFactory, timeout);
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
