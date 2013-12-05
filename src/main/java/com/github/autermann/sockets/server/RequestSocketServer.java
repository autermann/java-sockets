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
package com.github.autermann.sockets.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class RequestSocketServer<I, O> extends StreamingSocketServer {

    RequestSocketServer(ServerSocketFactory serverSocketFactory,
                        Supplier<RequestSocketServerCoder<I, O>> coderFactory,
                        Supplier<RequestSocketServerHandler<I, O>> handlerFactory,
                        Executor executor, List<Runnable> shutdownHooks,
                        int port) {
        super(serverSocketFactory, createStreamingHandlerFactory(coderFactory, handlerFactory),
              executor, shutdownHooks, port);
    }

    private static <I, O> Supplier<StreamingSocketServerHandler> createStreamingHandlerFactory(
            Supplier<RequestSocketServerCoder<I, O>> coderFactory,
            Supplier<RequestSocketServerHandler<I, O>> handlerFactory) {
        return Suppliers.<StreamingSocketServerHandler>ofInstance(
                new HandlerImpl<I, O>(coderFactory, handlerFactory));
    }

    private static class HandlerImpl<I, O> implements
            StreamingSocketServerHandler {
        private final Supplier<RequestSocketServerCoder<I, O>> coderFactory;
        private final Supplier<RequestSocketServerHandler<I, O>> handlerFactory;

        HandlerImpl(Supplier<RequestSocketServerCoder<I, O>> coderFactory,
                    Supplier<RequestSocketServerHandler<I, O>> handlerFactory) {
            this.coderFactory = checkNotNull(coderFactory);
            this.handlerFactory = checkNotNull(handlerFactory);
        }

        @Override
        public void handle(InputStream in, OutputStream out)
                throws IOException {
            RequestSocketServerCoder<I, O> coder = coderFactory.get();
            RequestSocketServerHandler<I, O> handler = handlerFactory.get();
            I request = coder.decode(in);
            O response = handler.handle(request);
            coder.encode(response, out);
        }
    }

}
