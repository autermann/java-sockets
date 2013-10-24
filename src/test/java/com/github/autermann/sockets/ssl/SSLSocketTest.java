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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.sockets.client.RequestSocketClient;
import com.github.autermann.sockets.client.RequestSocketClientHandler;
import com.github.autermann.sockets.client.SocketClientBuilder;
import com.github.autermann.sockets.server.RequestSockerServerHandler;
import com.github.autermann.sockets.server.RequestSocketServerCoder;
import com.github.autermann.sockets.server.SocketServerBuilder;
import com.github.autermann.sockets.server.StreamingSocketServer;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import com.google.common.io.OutputSupplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SSLSocketTest {
    private static final Logger log = LoggerFactory
            .getLogger(SSLSocketTest.class);
    private static final String CLIENT1 = "client1";
    private static final String CLIENT2 = "client2";
    private static final String CLIENT3 = "client3";
    private static final String SERVER = "server";
    private static final String LOCALHOST = "localhost";
    private static final int PORT = 9999;
    public static final int CLIENT_TIMEOUT = 10000;
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    public SSLClientSocketFactory createClientFactory(String who) {
        return new SSLClientSocketFactory(createSSLOptions(who));
    }

    public SSLServerSocketFactory createServerFactory(String who) {
        return new SSLServerSocketFactory(createSSLOptions(who));
    }

    private PemFileSSLConfiguration createSSLOptions(String who) {
        PemFileSSLConfiguration options = new PemFileSSLConfiguration(
                getResourcePath(String.format("/ssl/%s/key.pem", who)),
                getResourcePath(String.format("/ssl/%s/cert.pem", who)),
                getResourcePath(String.format("/ssl/%s/trust.pem", who)),
                true);
        return options;
    }

    private String getResourcePath(String path) {
        try {
            File f = new File(getClass().getResource(path).toURI());
            assertThat(f, is(notNullValue()));
            assertThat(f.exists(), is(true));
            return f.getAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new AssertionError(ex.getMessage());
        }
    }

    @Test
    public void testClient1() throws IOException {
        SSLServerSocketFactory serverFactory = createServerFactory(SERVER);
        SSLClientSocketFactory clientFactory = createClientFactory(CLIENT1);
        communicate(serverFactory, clientFactory);
    }

    @Test
    public void testClient2() throws IOException {
        SSLServerSocketFactory serverFactory = createServerFactory(SERVER);
        SSLClientSocketFactory clientFactory = createClientFactory(CLIENT2);
        communicate(serverFactory, clientFactory);
    }

    @Test
    public void testClient3() throws IOException {
        SSLServerSocketFactory serverFactory = createServerFactory(SERVER);
        SSLClientSocketFactory clientFactory = createClientFactory(CLIENT3);
        thrown.expect(SSLException.class);
        communicate(serverFactory, clientFactory);
    }

    private void communicate(SSLServerSocketFactory serverFactory,
                             SSLClientSocketFactory clientFactory) throws
            IOException {
        StreamingSocketServer server = SocketServerBuilder.create()
                .atPort(PORT)
                .withSocketFactory(serverFactory)
                .build(new EchoResponseCoder(),
                       new EchoResponseHandler());
        RequestSocketClient<List<String>, List<String>> client
                = SocketClientBuilder.create()
                .withAddress(LOCALHOST, PORT)
                .withTimeout(CLIENT_TIMEOUT)
                .withSocketFactory(clientFactory)
                .build(new LineRequestHandler());
        try {
            server.start(false);
            List<String> lines = client.exec(Collections.singletonList(MESSAGE));
            assertThat(lines.size(), is(1));
            assertThat(lines.get(0), is(MESSAGE));
        } finally {
            server.stop();
        }
    }
    public static final String MESSAGE = "HELLO";

    private class QuittingLineProcessor implements
            LineProcessor<ImmutableList<String>> {
        private final ImmutableList.Builder<String> builder
                = ImmutableList.builder();

        @Override
        public boolean processLine(String line)
                throws IOException {
            if (line.equals("QUIT")) {
                return false;
            } else {
                builder.add(line);
                return true;
            }
        }

        @Override
        public ImmutableList<String> getResult() {
            return builder.build();
        }
    }

    private class EchoResponseHandler implements RequestSockerServerHandler<List<String>, List<String>> {
        @Override
        public List<String> handle(List<String> request) {
            return request;
        }
    }

    private class EchoResponseCoder implements RequestSocketServerCoder<List<String>, List<String>> {

        @Override
        public List<String> decode(InputStream is) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            return CharStreams.readLines(in, new QuittingLineProcessor());
        }

        @Override
        public void encode(List<String> response, OutputStream os) throws IOException {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os, Charsets.UTF_8));
            for (String line : response) {
                out.write(line);
                out.flush();
            }
        }
    }

    private class LineRequestHandler implements RequestSocketClientHandler<List<String>, List<String>> {
        @Override
        public void encode(List<String> request,
                           OutputSupplier<OutputStream> out) throws IOException {
            PrintStream ps = new PrintStream(out.getOutput());
            for (String line : request) {
                ps.println(line);
            }
            ps.println("QUIT");
            ps.flush();
        }

        @Override
        public List<String> decode(InputSupplier<InputStream> in) throws
                IOException {
            return CharStreams.readLines(new InputStreamReader(in.getInput()));
        }
    }
    }
