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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class PemFileSSLConfiguration extends SSLConfiguration {
    private static final Logger log = LoggerFactory
            .getLogger(PemFileSSLConfiguration.class);
    private static final String PASSWORD = "password";
    private final String trusted;
    private final String key;
    private final String certificate;

    public PemFileSSLConfiguration(String keyFile,
                                   String certificateFile,
                                   String trustedFile,
                                   boolean requireClientAuth) {
        super(requireClientAuth);
        this.key = Preconditions.checkNotNull(keyFile);
        this.certificate = Preconditions.checkNotNull(certificateFile);
        this.trusted = Preconditions.checkNotNull(trustedFile);
    }

    @Override
    public char[] getKeyStorePass() {
        return PASSWORD.toCharArray();
    }

    @Override
    public char[] getTrustStorePass() {
        return PASSWORD.toCharArray();
    }

    @Override
    protected KeyStore createTrustStore()
            throws IOException, GeneralSecurityException {
        KeyStore store = SSLUtils.createEmptyKeyStore();
        log.debug("Creating Trust Store");
        for (X509Certificate cert : SSLUtils.readCertificates(this.trusted)) {
            store.setCertificateEntry(SSLUtils.randomAlias(), cert);
        }
        return store;
    }

    @Override
    protected KeyStore createKeyStore()
            throws IOException, GeneralSecurityException {
        KeyStore store = SSLUtils.createEmptyKeyStore();
        log.debug("Creating Key Store");
        store.setKeyEntry(SSLUtils.randomAlias(),
                          SSLUtils.readKey(this.key),
                          getKeyStorePass(),
                          SSLUtils.readChain(this.certificate));
        return store;
    }
}
