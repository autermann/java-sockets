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

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class KeyStoreOptions {
    private final String pass;
    private final String type;
    private final String path;

    public KeyStoreOptions(String path, String pass, String type) {
        this.path = Preconditions.checkNotNull(path);
        this.pass = Preconditions.checkNotNull(pass);
        this.type = Objects.firstNonNull(Strings.emptyToNull(type),
                                         SSLConstants.KEYSTORE_TYPE_JKS);
    }

    public String getPass() {
        return pass;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public KeyStore read() throws IOException,
                                  KeyStoreException,
                                  NoSuchAlgorithmException,
                                  CertificateException {
        KeyStore ks = KeyStore.getInstance(getType());
        FileInputStream in = null;
        try {
            in = new FileInputStream(getPath());
            ks.load(in, getPass().toCharArray());
            return ks;
        } finally {
            Closeables.close(in, true);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPass(), getType(), getPath());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyStoreOptions) {
            KeyStoreOptions other = (KeyStoreOptions) obj;
            return Objects.equal(getPass(), other.getPass()) &&
                   Objects.equal(getPath(), other.getPath()) &&
                   Objects.equal(getType(), other.getType());
        }
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("pass", getPass())
                .add("path", getPath())
                .add("type", getType())
                .toString();
    }
}
