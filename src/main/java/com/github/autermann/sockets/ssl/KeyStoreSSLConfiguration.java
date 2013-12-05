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
import static com.google.common.base.Strings.emptyToNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class KeyStoreSSLConfiguration extends SSLConfiguration {
    public static final String KEY_STORE_PATH = "keyStore.path";
    public static final String KEY_STORE_PASS = "keyStore.pass";
    public static final String KEY_STORE_TYPE = "keyStore.type";
    public static final String TRUST_STORE_PATH = "trustStore.path";
    public static final String TRUST_STORE_PASS = "trustStore.pass";
    public static final String TRUST_STORE_TYPE = "trustStore.type";
    public static final String CLIENT_AUTH = "clientAuth";

    private final KeyStoreOptions trustStoreOptions;
    private final KeyStoreOptions keyStoreOptions;

    public KeyStoreSSLConfiguration(KeyStoreOptions trustStoreOptions,
                                    KeyStoreOptions keyStoreOptions,
                                    boolean requireClientAuth) {
        super(requireClientAuth);
        this.trustStoreOptions = Preconditions.checkNotNull(trustStoreOptions);
        this.keyStoreOptions = Preconditions.checkNotNull(keyStoreOptions);
    }

    @Override
    protected KeyStore createKeyStore() throws IOException,
                                               GeneralSecurityException {
        return this.keyStoreOptions.read();
    }

    @Override
    public char[] getKeyStorePass() {
        return this.keyStoreOptions.getPass().toCharArray();
    }

    @Override
    protected KeyStore createTrustStore() throws IOException,
                                                 GeneralSecurityException {
        return this.trustStoreOptions.read();
    }

    @Override
    public char[] getTrustStorePass() {
        return this.trustStoreOptions.getPass().toCharArray();
    }

    public static SSLConfiguration load(Properties p) {
        checkNotNull(p);
        String keyStorePath = emptyToNull(p.getProperty(KEY_STORE_PATH, null));
        String keyStorePass = emptyToNull(p.getProperty(KEY_STORE_PASS, null));
        String keyStoreType = emptyToNull(p.getProperty(KEY_STORE_TYPE, null));
        String trustStorePath = emptyToNull(p
                .getProperty(TRUST_STORE_PATH, null));
        String trustStorePass = emptyToNull(p
                .getProperty(TRUST_STORE_PASS, null));
        String trustStoreType = emptyToNull(p
                .getProperty(TRUST_STORE_TYPE, null));
        String clientAuth = emptyToNull(p.getProperty(CLIENT_AUTH, "true"));

        return new KeyStoreSSLConfiguration(
                new KeyStoreOptions(checkNotNull(trustStorePath, TRUST_STORE_PATH),
                                    checkNotNull(trustStorePass, KEY_STORE_PASS),
                                    trustStoreType),
                new KeyStoreOptions(checkNotNull(keyStorePath, KEY_STORE_PATH),
                                    checkNotNull(keyStorePass, keyStorePass),
                                    keyStoreType),
                Boolean.parseBoolean(clientAuth));
    }

    public static SSLConfiguration load(String path) throws IOException {
        final File file = new File(checkNotNull(path));
        if (file.exists() && file.isFile() && file.canRead()) {
            return load(file);
        } else {
            throw new IOException("Can not read " + file);
        }
    }

    public static SSLConfiguration load(File file) throws IOException {
        return load(new FileInputStream(checkNotNull(file)));
    }

    public static SSLConfiguration load(InputStream in) throws IOException {
        try {
            Properties p = new Properties();
            p.load(checkNotNull(in));
            return load(p);
        } finally {
            Closeables.close(in, true);
        }
    }
}
