/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.core.misc.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.Platform;

/** Clase para la lectura y env&iacute;o de datos a URL remotas.
 * @author Carlos Gamuci. */
public class UrlHttpManagerImpl implements UrlHttpManager {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String JAVA_PARAM_ENABLE_SSL_CHECKS = "enableSslChecks"; //$NON-NLS-1$

	/** Tiempo de espera por defecto para descartar una conexi&oacute;n HTTP. */
	public static final int DEFAULT_TIMEOUT = -1;

	private static final String HTTPS = "https"; //$NON-NLS-1$

	private static final String URN_SEPARATOR = ":"; //$NON-NLS-1$
	private static final String PROT_SEPARATOR = URN_SEPARATOR + "//"; //$NON-NLS-1$


	private static final HostnameVerifier DEFAULT_HOSTNAME_VERIFIER = HttpsURLConnection.getDefaultHostnameVerifier();
	private static final SSLSocketFactory DEFAULT_SSL_SOCKET_FACTORY = HttpsURLConnection.getDefaultSSLSocketFactory();
	private static final String KEYSTORE = "javax.net.ssl.keyStore"; //$NON-NLS-1$
	private static final String KEYSTORE_PASS = "javax.net.ssl.keyStorePassword"; //$NON-NLS-1$
	private static final String KEYSTORE_TYPE = "javax.net.ssl.keyStoreType"; //$NON-NLS-1$
	private static final String KEYSTORE_DEFAULT_TYPE = "JKS"; //$NON-NLS-1$
	private static final String KEYMANAGER_INSTANCE = "SunX509";//$NON-NLS-1$
	private static final String SSL_CONTEXT = "SSL";//$NON-NLS-1$

	static {
		final CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}

	protected UrlHttpManagerImpl() {
		// Vacio y "protected"
	}

	private static final TrustManager[] DUMMY_TRUST_MANAGER = new TrustManager[] {
		new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkClientTrusted(final X509Certificate[] certs, final String authType) { /* No hacemos nada */ }
			@Override
			public void checkServerTrusted(final X509Certificate[] certs, final String authType) {  /* No hacemos nada */  }

		}
	};

	//@Override
	/*public byte[] readUrl(final String url, final UrlHttpMethod method) throws IOException {
		return readUrl(url, DEFAULT_TIMEOUT, null, null, method);
	}*/

	private static boolean isLocal(final URL url) {
		if (url == null) {
			throw new IllegalArgumentException("La URL no puede ser nula"); //$NON-NLS-1$
		}
		try {
			return InetAddress.getByName(url.getHost()).isLoopbackAddress();
		}
		catch (final Exception e) {
			// La direccion local siempre es conocida
			return false;
		}
	}

	@Override
	public byte[] readUrl(final String urlToRead,
			              final int timeout,
			              final String contentType,
			              final String accept,
			              final UrlHttpMethod method) throws IOException {
		if (urlToRead == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		// Vemos si lleva usuario y contrasena
		final String authString;
		final String url;
		final URLName un = new URLName(urlToRead);

		if (un.getUsername() != null || un.getPassword() != null) {
			final String tmpStr;
			if (un.getUsername() != null && un.getPassword() != null) {
				tmpStr = un.getUsername() + URN_SEPARATOR + un.getPassword();
			}
			else if (un.getUsername() != null) {
				tmpStr = un.getUsername();
			}
			else {
				tmpStr = un.getPassword();
			}
			authString = Base64.encode(tmpStr.getBytes());
			url = un.getProtocol() + PROT_SEPARATOR + un.getHost() + (un.getPort() != -1 ? URN_SEPARATOR + Integer.toString(un.getPort()) : "") + "/" + un.getFile(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			url = urlToRead;
			authString = null;
		}

		String urlParameters = null;
		String request = null;
		if (UrlHttpMethod.POST.equals(method) || UrlHttpMethod.PUT.equals(method)) {
			final StringTokenizer st = new StringTokenizer(url, "?"); //$NON-NLS-1$
			request = st.nextToken();
			urlParameters = st.nextToken();
		}

		final URL uri = new URL(request != null ? request : url);

		final boolean enableSSLChecks = Boolean.getBoolean(JAVA_PARAM_ENABLE_SSL_CHECKS);

		if (!enableSSLChecks && uri.getProtocol().equals(HTTPS)) {
			try {
				disableSslChecks();
			}
			catch(final Exception e) {
				Logger.getLogger("es.gob.afirma").warning( //$NON-NLS-1$
					"No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e //$NON-NLS-1$
				);
			}
		}

		final HttpURLConnection conn;
		if (Platform.OS.ANDROID.equals(Platform.getOS()) || isLocal(uri)) {
			conn = (HttpURLConnection) uri.openConnection(Proxy.NO_PROXY);
		}
		else {
			conn = (HttpURLConnection) uri.openConnection();
		}

		conn.setRequestMethod(method.toString());

		if (authString != null) {
			conn.addRequestProperty("Authorization", "Basic " + authString); //$NON-NLS-1$ //$NON-NLS-2$
		}
		conn.addRequestProperty(
			"Accept", //$NON-NLS-1$
			accept != null ? accept : "*/*" //$NON-NLS-1$
		);
		conn.addRequestProperty("Connection", "keep-alive"); //$NON-NLS-1$ //$NON-NLS-2$
		if (contentType != null) {
			conn.addRequestProperty("Content-Type", contentType); //$NON-NLS-1$
		}

		conn.addRequestProperty("Host", uri.getHost()); //$NON-NLS-1$
		conn.addRequestProperty("Origin", uri.getProtocol() +  "://" + uri.getHost()); //$NON-NLS-1$ //$NON-NLS-2$

		if (timeout != DEFAULT_TIMEOUT) {
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
		}

		if (urlParameters != null) {
			conn.setRequestProperty("Content-Length", String.valueOf(urlParameters.getBytes("UTF-8").length)); //$NON-NLS-1$
			conn.setDoOutput(true);
			final OutputStream os = conn.getOutputStream();
			os.write(urlParameters.getBytes("UTF-8")); //$NON-NLS-1$
			os.close();
		}

		conn.connect();
		final int resCode = conn.getResponseCode();
		final String statusCode = Integer.toString(resCode);
		if (statusCode.startsWith("4") || statusCode.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (uri.getProtocol().equals(HTTPS)) {
				enableSslChecks();
			}
			throw new HttpError(resCode, conn.getResponseMessage(), url);
		}

		final InputStream is = conn.getInputStream();
		final byte[] data = AOUtil.getDataFromInputStream(is);
		is.close();

		if (!enableSSLChecks && uri.getProtocol().equals(HTTPS)) {
			enableSslChecks();
		}

		return data;

	}

	/** Habilita las comprobaciones de certificados en conexiones SSL dej&aacute;ndolas con su
	 * comportamiento por defecto. */
	public static void enableSslChecks() {
		HttpsURLConnection.setDefaultSSLSocketFactory(DEFAULT_SSL_SOCKET_FACTORY);
		HttpsURLConnection.setDefaultHostnameVerifier(DEFAULT_HOSTNAME_VERIFIER);
	}

	/** Deshabilita las comprobaciones de certificados en conexiones SSL, acept&aacute;dose entonces
	 * cualquier certificado.
	 * @throws KeyManagementException Si hay problemas en la gesti&oacute;n de claves SSL.
	 * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario.
	 * @throws KeyStoreException Si no se puede cargar el KeyStore SSL.
	 * @throws IOException Si hay errores en la carga del fichero KeyStore SSL.
	 * @throws CertificateException Si los certificados del KeyStore SSL son inv&aacute;lidos.
	 * @throws UnrecoverableKeyException Si una clave del KeyStore SSL es inv&aacute;lida.
	 * @throws NoSuchProviderException Si ocurre un error al recuperar la instancia del Keystore.*/
	public static void disableSslChecks() throws KeyManagementException,
	                                             NoSuchAlgorithmException,
	                                             KeyStoreException,
	                                             UnrecoverableKeyException,
	                                             CertificateException,
	                                             IOException,
	                                             NoSuchProviderException {
		final SSLContext sc = SSLContext.getInstance(SSL_CONTEXT);
		sc.init(getKeyManager(), DUMMY_TRUST_MANAGER, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(
			new HostnameVerifier() {
				@Override
				public boolean verify(final String hostname, final SSLSession session) {
					return true;
				}
			}
		);
	}

	/** Devuelve un KeyManager a utilizar cuando se desea deshabilitar las comprobaciones de certificados en las conexiones SSL.
	 * @return KeyManager[] Se genera un KeyManager[] utilizando el keystore almacenado en las propiedades del sistema.
	 * @throws KeyStoreException Si no se puede cargar el KeyStore SSL.
	 * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario.
	 * @throws CertificateException Si los certificados del KeyStore SSL son inv&aacute;lidos.
	 * @throws IOException Si hay errores en la carga del fichero KeyStore SSL.
	 * @throws UnrecoverableKeyException Si una clave del KeyStore SSL es inv&aacute;lida.
	 * @throws NoSuchProviderException Si ocurre un error al recuperar la instancia del KeyStore. */
	private static KeyManager[] getKeyManager() throws KeyStoreException,
	                                                   NoSuchAlgorithmException,
	                                                   CertificateException,
	                                                   IOException,
	                                                   UnrecoverableKeyException,
	                                                   NoSuchProviderException {
		final String keyStore = System.getProperty(KEYSTORE);
		final String keyStorePassword = System.getProperty(KEYSTORE_PASS);
		final String keyStoreType = System.getProperty(KEYSTORE_TYPE);
		if (keyStore == null || keyStore.isEmpty()) {
			return null;
		}
		final File f = new File(keyStore);
		if (!f.isFile() || !f.canRead()) {
			LOGGER.warning("El KeyStore SSL no existe o no es legible: " + f.getAbsolutePath()); //$NON-NLS-1$
			return null;
		}
		final KeyStore keystore = KeyStore.getInstance(
			keyStoreType != null && !keyStoreType.isEmpty() ? keyStoreType : KEYSTORE_DEFAULT_TYPE
		);
		final InputStream fis = new FileInputStream(f);
		keystore.load(
			fis,
			keyStorePassword != null ? keyStorePassword.toCharArray() : null
		);
		fis.close();
		final KeyManagerFactory keyFac = KeyManagerFactory.getInstance(KEYMANAGER_INSTANCE);
		keyFac.init(
			keystore,
			keyStorePassword != null ? keyStorePassword.toCharArray() : null
		);
		return keyFac.getKeyManagers();
	}

}
