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
package com.github.autermann.sockets.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class SSLConfiguration {
    private KeyStore keyStore;
    private KeyStore trustStore;
    private final boolean requireClientAuth;

    public SSLConfiguration(boolean requireClientAuth) {
        this.requireClientAuth = requireClientAuth;
    }

    public KeyStore getKeyStore()
            throws IOException, GeneralSecurityException {
        if (this.keyStore == null) {
            this.keyStore = createKeyStore();
        }
        return this.keyStore;
    }

    public KeyStore getTrustStore()
            throws IOException, GeneralSecurityException {
        if (this.trustStore == null) {
            this.trustStore = createTrustStore();
        }
        return this.trustStore;
    }

    protected abstract KeyStore createTrustStore()
            throws IOException, GeneralSecurityException;

    protected abstract KeyStore createKeyStore()
            throws IOException, GeneralSecurityException;

    public abstract char[] getKeyStorePass();

    public abstract char[] getTrustStorePass();

    public boolean isRequireClientAuth() {
        return requireClientAuth;
    }
}
