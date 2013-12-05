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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.utils.Java;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class SSLUtils {
    private static final Logger log = LoggerFactory.getLogger(SSLUtils.class);

    private SSLUtils() {
    }

    public static KeyStore createEmptyKeyStore()
            throws KeyStoreException,
                   CertificateException,
                   NoSuchAlgorithmException,
                   IOException {
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        return store;
    }

    public static String randomAlias() {
        return UUID.randomUUID().toString();
    }

    public static PrivateKey createKeyFromDER(byte[] key)
            throws NoSuchAlgorithmException,
                   InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory kf = KeyFactory.getInstance(SSLConstants.KEY_ALGORITHM_RSA);
        return kf.generatePrivate(keySpec);
    }

    public static Certificate[] toChain(List<? extends Certificate> certs) {
        return Lists.reverse(certs).toArray(new Certificate[certs.size()]);
    }

    public static Certificate[] readChain(String fileName)
            throws CertificateException,
                   IOException {
        return toChain(readCertificates(fileName));
    }

    public static Certificate[] readChain(File file)
            throws CertificateException,
                   IOException {
        return toChain(readCertificates(file));
    }

    public static Certificate[] readChain(
            InputSupplier<? extends InputStream> in)
            throws CertificateException,
                   IOException {
        return toChain(readCertificates(in));
    }

    public static PrivateKey readKey(String fileName)
            throws IOException,
                   NoSuchAlgorithmException,
                   InvalidKeySpecException {
        return readKey(new File(fileName));
    }

    public static PrivateKey readKey(File file)
            throws IOException,
                   NoSuchAlgorithmException,
                   InvalidKeySpecException {
        return readKey(Files.newInputStreamSupplier(file));
    }

    public static PrivateKey readKey(InputSupplier<? extends InputStream> in)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Closer closer = Closer.create();
        try {
            Reader reader = closer.register(CharStreams
                    .newReaderSupplier(in, Charsets.UTF_8).getInput());
            return createPrivateKey(new PemReader(reader).readPemObject());
        } catch (IOException e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static List<X509Certificate> readCertificates(String filename)
            throws CertificateException, IOException {
        return readCertificates(new File(filename));
    }

    public static List<X509Certificate> readCertificates(File file)
            throws CertificateException, IOException {
        return readCertificates(Files.newInputStreamSupplier(file));

    }

    public static List<X509Certificate> readCertificates(
            InputSupplier<? extends InputStream> in)
            throws CertificateException, IOException {
        InputStream is = null;
        try {
            is = in.getInput();
            List<X509Certificate> certs = new LinkedList<X509Certificate>();
            CertificateFactory cf = CertificateFactory
                    .getInstance(SSLConstants.CERTIFICATE_TYPE_X509);
            while (is.available() > 0) {
                X509Certificate cert = (X509Certificate) cf
                        .generateCertificate(is);
                certs.add(cert);
                log.info("Read {}", cert.getSubjectX500Principal().getName());
            }
            return certs;
        } catch (CertificateParsingException ex) {
            // FIXME check compatibility of X509CertificateFactory in Java 6/7
            if (Java.v6 && ex.getMessage() != null &&
                ex.getMessage().equals("invalid DER-encoded certificate data")) {
                log.warn("X509CertificateFactory was not able to parse certificate. " +
                         "Consider switching to Java 7 to overcome this issue.");
            }
            throw ex;
        } finally {
            Closeables.close(is, true);
        }
    }

    private static PrivateKey createPrivateKey(PemObject privatePemObject)
            throws IOException, InvalidKeySpecException,
                   NoSuchAlgorithmException {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(
                PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
        RSAPrivateKey instance = RSAPrivateKey.getInstance(privatePemObject
                .getContent());
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algId, instance);
        return createKeyFromDER(privateKeyInfo.toASN1Primitive().getEncoded());
    }
}
