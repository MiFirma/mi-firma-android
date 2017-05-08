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

import java.util.Map;


/** Par&aacute;metros para el guardado de datos. */
public final class UrlParametersToSave extends UrlParameters {

	/** Par&aacute;metro de entrada con el t&iacute;tulo de la actividad o del di&aacute;logo de guardado. */
	private static final String TITLE_PARAM = "title"; //$NON-NLS-1$

	/** Par&aacute;metro de entrada con la descripci&oacute;n del tipo de fichero de salida. */
	private static final String FILETYPE_DESCRIPTION = "desc"; //$NON-NLS-1$

	/** Par&aacute;metro de entrada con el nombre propuesto para un fichero. */
	private static final String FILENAME_PARAM = "filename"; //$NON-NLS-1$

	/** Par&aacute;metro de entrada con las extensiones recomendadas para el fichero de salida. */
	private static final String FILENAME_EXTS = "exts"; //$NON-NLS-1$

	private String title = null;
	private String filename = null;
	private String extensions = null;
	private String fileTypeDescription = null;

	/** Establece la descripci&oacute;n del tipo de fichero a guardar.
	 * @param desc Descripci&oacute;n del tipo de fichero a guardar */
	void setFileTypeDescription(final String desc) {
		this.fileTypeDescription = desc;
	}

	/** Establece las extensiones recomendadas para el fichero a guardar.
	 * Deben indicarse como una lista separada por comas
	 * @param exts Extensiones recomendadas, indicadas como una lista separada por comas */
	void setExtensions(final String exts) {
		this.extensions = exts;
	}

	/** Establece el nombre de fichero propuesto para guardar los datos.
	 * @param filename Nombre de fichero propuesto para guardar los datos */
	void setFilename(final String filename) {
		this.filename = filename;
	}

	/** Establece el t&iacute;tulo del di&aacute;logo de guardado de datos.
	 * @param title T&iacute;tulo del di&aacute;logo de guardado de datos */
	void setTitle(final String title) {
		this.title = title;
	}

	/** Obtiene la descripci&oacute;n del tipo de fichero a guardar.
	 * @return Descripci&oacute;n del tipo de fichero a guardar */
	public String getFileTypeDescription() {
		return this.fileTypeDescription;
	}

	/** Obtiene, como una lista separada por comas, las extensiones recomendadas para el
	 * fichero de salida.
	 * @return Lista separada por comas con las extensiones para el fichero de salida */
	public String getExtensions() {
		return this.extensions;
	}

	/** Obtiene el nombre de fichero propuesto para guardar los datos.
	 * @return Nombre de fichero propuesto para guardar los datos */
	public String getFileName() {
		return this.filename;
	}

	/** Obtiene el t&iacute;tulo del di&aacute;logo de guardado de datos.
	 * @return T&iacute;tulo del di&aacute;logo de guardado de datos */
	public String getTitle() {
		return this.title;
	}

	void setSaveParameters(final Map<String, String> params) throws ParameterException {

		// Comprobamos que se nos hayan indicado los datos o, en su defecto, el
		// identificador de fichero remoto
		// para descargar los datos y la ruta del servicio remoto para el
		// fichero
		if (!params.containsKey(FILE_ID_PARAM) && !params.containsKey(DATA_PARAM)) {
			throw new ParameterException(
				"Error al validar la URL del servlet de recuperacion: " + //$NON-NLS-1$
					"La URI debe contener o un identificador de fichero o los datos a guardar" //$NON-NLS-1$
			);
		}

		setTitle(verifyTitle(params));
		setFilename(verifyFilename(params));
		setExtensions(verifyExtensions(params));
		setFileTypeDescription(verifyFileTypeDescription(params));
	}

	private static String verifyFilename(final Map<String, String> params) throws ParameterException {
		String filename = null;
		if (params.containsKey(FILENAME_PARAM)) {
			filename = params.get(FILENAME_PARAM);
			// Determinamos si el nombre tiene algun caracter que no consideremos valido para un nombre de fichero
			for (final char invalidChar : "\\/:*?\"<>|".toCharArray()) { //$NON-NLS-1$
				if (filename.indexOf(invalidChar) != -1) {
					throw new ParameterException("Se ha indicado un nombre de fichero con el caracter invalido: " + invalidChar); //$NON-NLS-1$
				}
			}
		}
		return filename;
	}

	private static String verifyExtensions(final Map<String, String> params) throws ParameterException {
		String extensions = null;
		if (params.containsKey(FILENAME_EXTS)) {
			extensions = params.get(FILENAME_EXTS);
			// Determinamos si el nombre tiene algun caracter que no consideremos valido para un nombre de fichero
			for (final char invalidChar : "\\/:*?\"<>|; ".toCharArray()) { //$NON-NLS-1$
				if (extensions.indexOf(invalidChar) != -1) {
					throw new ParameterException("Se ha indicado una lista de extensiones de nombre de fichero con caracteres invalidos: " + invalidChar); //$NON-NLS-1$
				}
			}
		}
		return extensions;
	}

	private static String verifyTitle(final Map<String, String> params) {
		if (params.containsKey(TITLE_PARAM)) {
			return params.get(TITLE_PARAM);
		}
		return ProtocoloMessages.getString("ProtocolInvocationUriParser.1"); //$NON-NLS-1$
	}

	private static String verifyFileTypeDescription(final Map<String, String> params) {
		String desc = null;
		if (params.containsKey(FILETYPE_DESCRIPTION)) {
			desc = params.get(FILETYPE_DESCRIPTION);
			// Anadimos las extensiones si fuese preciso
			if (params.containsKey(FILENAME_EXTS) && !desc.endsWith(")")) { //$NON-NLS-1$
				final StringBuilder sb = new StringBuilder(desc).append(" ("); //$NON-NLS-1$
				for (final String ext : params.get(FILENAME_EXTS).split(",")) { //$NON-NLS-1$
					sb.append("*."); //$NON-NLS-1$
					sb.append(ext);
				}
				sb.append(")"); //$NON-NLS-1$
				desc = sb.toString();
			}
		}
		return desc;
	}

}
