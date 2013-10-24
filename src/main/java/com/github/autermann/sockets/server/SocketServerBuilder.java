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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.autermann.sockets.ServerSocketFactory;
import com.github.autermann.sockets.ssl.SSLConfiguration;
import com.github.autermann.sockets.ssl.SSLServerSocketFactory;
import com.github.autermann.sockets.util.Factory;
import com.github.autermann.sockets.util.NamedAndGroupedThreadFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
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
        return build(Factory.fromInstance(h));
    }

    public StreamingSocketServer build(
            Factory<StreamingSocketServerHandler> handlerFactory) {
        checkNotNull(handlerFactory);
        validate();
        return new StreamingSocketServer(serverSocketFactory,
                                         handlerFactory,
                                         executor,
                                         shutdownHooks,
                                         port);
    }

    public <I, O> RequestSocketServer<I, O> build(
            Factory<RequestSocketServerCoder<I, O>> coderFactory,
            Factory<RequestSockerServerHandler<I, O>> handlerFactory) {
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
            Factory<RequestSockerServerHandler<I, O>> handlerFactory) {
        return build(Factory.fromInstance(coder), handlerFactory);
    }

    public <I, O> RequestSocketServer<I, O> build(
            Factory<RequestSocketServerCoder<I, O>> coderFactory,
            RequestSockerServerHandler<I, O> handler) {
        return build(coderFactory, Factory.fromInstance(handler));
    }

    public <I, O> RequestSocketServer<I, O> build(
            RequestSocketServerCoder<I, O> coder,
            RequestSockerServerHandler<I, O> handler) {
        return build(Factory.fromInstance(coder),
                     Factory.fromInstance(handler));
    }

    public static SocketServerBuilder create() {
        return new SocketServerBuilder();
    }
}
