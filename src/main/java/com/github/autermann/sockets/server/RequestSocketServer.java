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
package com.github.autermann.sockets.server;

import com.github.autermann.sockets.util.Factory;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;


/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class RequestSocketServer<I, O> extends StreamingSocketServer {

    RequestSocketServer(ServerSocketFactory serverSocketFactory,
                        Factory<RequestSocketServerCoder<I, O>> coderFactory,
                        Factory<RequestSocketServerHandler<I, O>> handlerFactory,
                        Executor executor, List<Runnable> shutdownHooks,
                        int port) {
        super(serverSocketFactory, createStreamingHandlerFactory(coderFactory, handlerFactory),
              executor, shutdownHooks, port);
    }

    private static <I, O> Factory<StreamingSocketServerHandler> createStreamingHandlerFactory(
            Factory<RequestSocketServerCoder<I, O>> coderFactory,
            Factory<RequestSocketServerHandler<I, O>> handlerFactory) {
        return Factory.<StreamingSocketServerHandler>fromInstance(
                new HandlerImpl<I, O>(coderFactory, handlerFactory));
    }

    private static class HandlerImpl<I, O> implements
            StreamingSocketServerHandler {
        private final Factory<RequestSocketServerCoder<I, O>> coderFactory;
        private final Factory<RequestSocketServerHandler<I, O>> handlerFactory;

        HandlerImpl(Factory<RequestSocketServerCoder<I, O>> coderFactory,
                    Factory<RequestSocketServerHandler<I, O>> handlerFactory) {
            this.coderFactory = checkNotNull(coderFactory);
            this.handlerFactory = checkNotNull(handlerFactory);
        }

        @Override
        public void handle(InputStream in, OutputStream out)
                throws IOException {
            RequestSocketServerCoder<I, O> coder = coderFactory.create();
            RequestSocketServerHandler<I, O> handler = handlerFactory.create();
            I request = coder.decode(in);
            O response = handler.handle(request);
            coder.encode(response, out);
        }
    }

}
