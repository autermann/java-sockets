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
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.autermann.sockets.ssl.SSLConfiguration;
import com.github.autermann.sockets.ssl.SSLServerSocketFactory;
import com.github.autermann.utils.NamedAndGroupedThreadFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SocketServerBuilder {

    private static final AtomicInteger serverCount = new AtomicInteger(0);
    private ServerSocketFactory serverSocketFactory;
    private int port = -1;
    private int threads = -1;
    private Executor executor;
    private ThreadFactory threadFactory;
    private final List<Runnable> shutdownHooks = Lists.newLinkedList();

    private SocketServerBuilder() {
    }

    public SocketServerBuilder withSocketFactory(ServerSocketFactory f) {
        this.serverSocketFactory = Preconditions.checkNotNull(f);
        return this;
    }

    public SocketServerBuilder withSSL(SSLConfiguration config) {
        if (config == null) {
            this.serverSocketFactory = null;
            return this;
        } else {
            return withSocketFactory(new SSLServerSocketFactory(config));
        }
    }

    public SocketServerBuilder atPort(int port) {
        Preconditions.checkArgument(port > 0);
        this.port = port;
        return this;
    }

    public SocketServerBuilder withExecutor(Executor executor) {
        this.executor = Preconditions.checkNotNull(executor);
        return this;
    }

    public SocketServerBuilder withThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.executor = null;
        return this;
    }

    public SocketServerBuilder withFixedThreads(int threads) {
        Preconditions.checkArgument(threads > 0);
        this.threads = threads;
        this.executor = null;
        return this;
    }

    public SocketServerBuilder withShutdownHook(Runnable hook) {
        this.shutdownHooks.add(Preconditions.checkNotNull(hook));
        return this;
    }

    private void validate() {
        checkState(port > 0);
        if (serverSocketFactory == null) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }
        if (threadFactory == null) {
            threadFactory
                    = NamedAndGroupedThreadFactory.builder()
                    .name("socket-server-" + serverCount.getAndIncrement())
                    .build();
        }
        if (executor == null) {
            executor = threads > 0
                       ? Executors.newFixedThreadPool(threads, threadFactory)
                       : Executors.newCachedThreadPool(threadFactory);
        }
    }

    public StreamingSocketServer build(StreamingSocketServerHandler h) {
        return build(Suppliers.ofInstance(checkNotNull(h)));
    }

    public StreamingSocketServer build(
            Supplier<StreamingSocketServerHandler> handlerFactory) {
        checkNotNull(handlerFactory);
        validate();
        return new StreamingSocketServer(serverSocketFactory,
                                         handlerFactory,
                                         executor,
                                         shutdownHooks,
                                         port);
    }

    public <I, O> RequestSocketServer<I, O> build(
            Supplier<RequestSocketServerCoder<I, O>> coderFactory,
            Supplier<RequestSocketServerHandler<I, O>> handlerFactory) {
        checkNotNull(coderFactory);
        checkNotNull(handlerFactory);
        validate();
        return new RequestSocketServer<I, O>(serverSocketFactory,
                                             coderFactory,
                                             handlerFactory,
                                             executor,
                                             shutdownHooks,
                                             port);
    }

    public <I, O> RequestSocketServer<I, O> build(
            RequestSocketServerCoder<I, O> coder,
            Supplier<RequestSocketServerHandler<I, O>> handlerFactory) {
        return build(Suppliers.ofInstance(checkNotNull(coder)), handlerFactory);
    }

    public <I, O> RequestSocketServer<I, O> build(
            Supplier<RequestSocketServerCoder<I, O>> coderFactory,
            RequestSocketServerHandler<I, O> handler) {
        return build(coderFactory, Suppliers.ofInstance(checkNotNull(handler)));
    }

    public <I, O> RequestSocketServer<I, O> build(
            RequestSocketServerCoder<I, O> coder,
            RequestSocketServerHandler<I, O> handler) {
        return build(Suppliers.ofInstance(checkNotNull(coder)),
                     Suppliers.ofInstance(checkNotNull(handler)));
    }

    public static SocketServerBuilder create() {
        return new SocketServerBuilder();
    }
}
