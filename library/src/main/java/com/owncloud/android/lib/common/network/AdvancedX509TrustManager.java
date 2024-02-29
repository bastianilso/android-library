/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023 Elv1zz <elv1zz.git@gmail.com>
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import android.annotation.SuppressLint;

import com.owncloud.android.lib.common.utils.Log_OC;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;



/**
 * @author David A. Velasco
 */
@SuppressLint("CustomX509TrustManager")
public class AdvancedX509TrustManager implements X509TrustManager {
    
    private static final String TAG = AdvancedX509TrustManager.class.getSimpleName();

    private X509TrustManager mStandardTrustManager = null;
    private KeyStore mKnownServersKeyStore;

    /**
     * Constructor for AdvancedX509TrustManager
     * 
     * @param  knownServersKeyStore    Local certificates store with server certificates explicitly trusted by the user.
     * @throws CertStoreException       When no default X509TrustManager instance was found in the system.
     */
    public AdvancedX509TrustManager(KeyStore knownServersKeyStore)
            throws NoSuchAlgorithmException, KeyStoreException, CertStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore)null);
        mStandardTrustManager = findX509TrustManager(factory);

        mKnownServersKeyStore = knownServersKeyStore;
    }
    
    
    /**
     * Locates the first X509TrustManager provided by a given TrustManagerFactory
     * @param factory               TrustManagerFactory to inspect in the search for a X509TrustManager
     * @return                      The first X509TrustManager found in factory.
     */
    private X509TrustManager findX509TrustManager(TrustManagerFactory factory) {
        TrustManager[] tms = factory.getTrustManagers();
        for (TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        return null;
    }
    

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],
     *      String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        mStandardTrustManager.checkClientTrusted(certificates, authType);
    }

    
    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],
     *      String authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) {
        if (!isKnownServer(certificates[0])) {
        	CertificateCombinedException result = new CertificateCombinedException(certificates[0]);
        	try {
        		certificates[0].checkValidity();
        	} catch (CertificateExpiredException c) {
        		result.setCertificateExpiredException(c);
        		
        	} catch (CertificateNotYetValidException c) {
                result.setCertificateNotYetException(c);
        	}
        	
        	try {
        	    mStandardTrustManager.checkServerTrusted(certificates, authType);
        	} catch (CertificateException c) {
                Throwable cause = c.getCause();
                Throwable previousCause = null;
                while (cause != null && cause != previousCause && !(cause instanceof CertPathValidatorException)) {     // getCause() is not funny
                    previousCause = cause;
                    cause = cause.getCause();
                }
                if (cause instanceof CertPathValidatorException) {
                    result.setCertPathValidatorException((CertPathValidatorException) cause);
                } else {
                    result.setOtherCertificateException(c);
                }
        	}
        	
        	if (result.isException())
        		throw result;

        }
    }
    
    
    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return mStandardTrustManager.getAcceptedIssuers();
    }

    
    public boolean isKnownServer(X509Certificate cert) {
        try {
            return (mKnownServersKeyStore.getCertificateAlias(cert) != null);
        } catch (KeyStoreException e) {
            Log_OC.d(TAG, "Fail while checking certificate in the known-servers store");
            return false;
        }
    }
    
}
