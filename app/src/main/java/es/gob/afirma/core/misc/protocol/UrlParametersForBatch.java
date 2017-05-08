/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.core.misc.protocol;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import es.gob.afirma.core.misc.AOUtil;

/** Par&aacute;metros para el proceso de lotes de firmas predefinidos en XML.
 * En este caso, los datos son el XML de definici&oacute;n de lote.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class UrlParametersForBatch extends UrlParameters {

	/** N&uacute;mero m&aacute;ximo de caracteres permitidos para el identificador de sesi&oacute;n de la operaci&oacute;n. */
	private static final int MAX_ID_LENGTH = 20;

	/** Par&aacute;metro de entrada con el identificador de sesi&oacute;n de la operaci&oacute;n. */
	private static final String PARAM_ID = "id"; //$NON-NLS-1$

	private static final String PARAM_BATCH_POSTSIGNER = "batchpostsignerurl"; //$NON-NLS-1$
	private static final String PARAM_BATCH_PRESIGNER = "batchpresignerurl"; //$NON-NLS-1$

	/** Par&aacute;metro de entrada con la m&iacute;nima versi&oacute;n requerida del aplicativo a usar en la invocaci&oacute;n por protocolo. */
	private static final String VER_PARAM = "ver"; //$NON-NLS-1$

	private String batchPreSignerUrl = null;
	private String batchPostSignerUrl = null;

	private String minimumVersion;

	/** Obtiene la URL del servicio de preprocesado de lotes de firma.
	 * @return URL del servicio de preprocesado de lotes de firma. */
	public String getBatchPresignerUrl() {
		return this.batchPreSignerUrl;
	}

	void setBatchPresignerUrl(final String url) {
		this.batchPreSignerUrl = url;
	}

	/** Obtiene la URL del servicio de preprocesado de lotes de firma.
	 * @return URL del servicio de preprocesado de lotes de firma. */
	public String getBatchPostSignerUrl() {
		return this.batchPostSignerUrl;
	}

	void setBatchPostsignerUrl(final String url) {
		this.batchPostSignerUrl = url;
	}

	void setBatchParameters(final Map<String, String> params) throws ParameterException {

		// idSession para el service Web. Con socket no se usa
		if (params.containsKey(PARAM_ID) || params.containsKey(FILE_ID_PARAM)) {
			// Comprobamos que el identificador de sesion de la firma no sea mayor de un cierto numero de caracteres
			final String signatureSessionId = params.containsKey(PARAM_ID) ? params.get(PARAM_ID) : params.get(FILE_ID_PARAM);
			if (signatureSessionId.length() > MAX_ID_LENGTH) {
				throw new ParameterException("La longitud del identificador para la firma es mayor de " + MAX_ID_LENGTH + " caracteres."); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Comprobamos que el identificador de sesion de la firma sea alfanumerico (se usara como nombre de fichero)
			for (final char c : signatureSessionId.toLowerCase(Locale.ENGLISH).toCharArray()) {
				if ((c < 'a' || c > 'z') && (c < '0' || c > '9')) {
					throw new ParameterException("El identificador de la firma debe ser alfanumerico."); //$NON-NLS-1$
				}
			}

			setSessionId(signatureSessionId);
		}

		// Version minima requerida del aplicativo
		if (params.containsKey(VER_PARAM)) {
			setMinimumVersion(params.get(VER_PARAM));
		}
		else {
			setMinimumVersion("0"); //$NON-NLS-1$
		}

		// Si hemos recibido el identificador para la descarga de la configuracion,
		// no encontraremos el resto de parametros
		if (getFileId() != null) {
			return;
		}

		if (!params.containsKey(PARAM_BATCH_POSTSIGNER)) {
			throw new ParameterException(
				"No se ha recibido la URL del postprocesador de lotes" //$NON-NLS-1$
			);
		}
		if (!params.containsKey(PARAM_BATCH_PRESIGNER)) {
			throw new ParameterException(
				"No se ha recibido la URL del preprocesador de lotes" //$NON-NLS-1$
			);
		}

		setDefaultKeyStore(verifyDefaultKeyStoreName(params));

		setBatchPostsignerUrl(
			validateURL(
				params.get(PARAM_BATCH_POSTSIGNER)
			).toString()
		);

		setBatchPresignerUrl(
			validateURL(
				params.get(PARAM_BATCH_PRESIGNER)
			).toString()
		);

		// Comprobamos la validez de la URL del servlet de guardado en caso de indicarse
		if (params.containsKey(STORAGE_SERVLET_PARAM)) {

			// Comprobamos que la URL sea valida
			URL storageServletUrl;
			try {
				storageServletUrl = validateURL(params.get(STORAGE_SERVLET_PARAM));
			}
			catch (final ParameterLocalAccessRequestedException e) {
				throw new ParameterLocalAccessRequestedException("La URL del servicio de guardado no puede ser local", e); //$NON-NLS-1$
			}
			catch (final ParameterException e) {
				throw new ParameterException("Error al validar la URL del servicio de guardado: " + e, e); //$NON-NLS-1$
			}
			setStorageServletUrl(storageServletUrl);
		}

		String props = null;
		if (params.containsKey(PROPERTIES_PARAM)) {
			props = params.get(PROPERTIES_PARAM);
		}

		if (props != null) {
			try {
				setExtraParams(AOUtil.base642Properties(props));
			}
			catch (final Exception e) {
				setExtraParams(new Properties());
			}
		}
		else {
			setExtraParams(new Properties());
		}

		setDefaultKeyStore(verifyDefaultKeyStoreName(params));
	}

	/** Obtiene la versi&oacute;n m&iacute;nima requerida del aplicativo.
	 * @return Versi&oacute;n m&iacute;nima requerida del aplicativo. */
	public String getMinimumVersion() {
		return this.minimumVersion;
	}

	void setMinimumVersion(final String minimumVersion) {
		this.minimumVersion = minimumVersion;
	}

}
