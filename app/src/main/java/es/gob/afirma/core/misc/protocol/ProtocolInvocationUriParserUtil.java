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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.misc.protocol.UrlParametersToSign.Operation;

final class ProtocolInvocationUriParserUtil {

	static final String DEFAULT_URL_ENCODING = "UTF-8"; //$NON-NLS-1$

	private ProtocolInvocationUriParserUtil() {
		// No instanciable
	}

	static UrlParametersForBatch getParametersForBatch(final Map<String, String> params) throws ParameterException {
		final UrlParametersForBatch ret = new UrlParametersForBatch();
		ret.setCommonParameters(params);
		ret.setBatchParameters(params);
		return ret;
	}

	/** Recupera los par&aacute;metros necesarios para la configuraci&oacute;n de una
	 * operaci&oacute;n de guardado de datos en el dispositivo. Si falta alg&uacute;n par&aacute;metro o
	 * es err&oacute;neo se lanzar&aacute; una excepci&oacute;n.
	 * @param params Par&aacute;metros de con la configuraci&oacute;n de la operaci&oacute;n.
	 * @return Par&aacute;metros
	 * @throws ParameterException Si alg&uacute;n par&aacute;metro proporcionado es incorrecto
	 * @throws UnsupportedEncodingException Si no se soporta UTF-8 en URL (no debe ocurrir nunca) */
	static UrlParametersToSave getParametersToSave(final Map<String, String> params) throws ParameterException,
	                                                                                        UnsupportedEncodingException {
		final UrlParametersToSave ret = new UrlParametersToSave();

		ret.setCommonParameters(params);
		ret.setSaveParameters(params);

		return ret;
	}

	/** Analiza un XML de entrada para obtener la lista de par&aacute;metros asociados
	 * @param xml XML con el listado de par&aacute;metros.
	 * @return Devuelve una tabla <i>hash</i> con cada par&aacute;metro asociado a un valor
	 * @throws ParameterException Cuando el XML de entrada no es v&acute;lido. */
	static Map<String, String> parseXml(final byte[] xml) throws ParameterException {
		final Map<String, String> params = new HashMap<String, String>();
		final NodeList elems;

		try {
			final Element docElement = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(xml)).getDocumentElement();

			// Si el elemento principal es OPERATION_PARAM entendemos que es una firma
			params.put(
				ProtocolConstants.OPERATION_PARAM,
				ProtocolConstants.OPERATION_PARAM.equalsIgnoreCase(docElement.getNodeName()) ?
					Operation.SIGN.toString() :
						docElement.getNodeName()
			);

			elems = docElement.getChildNodes();
		}
		catch (final Exception e) {
			throw new ParameterException("Error grave durante el analisis del XML: " + e, e); //$NON-NLS-1$
		}

		for (int i = 0; i < elems.getLength(); i++) {
			final Node element = elems.item(i);
			if (!"e".equals(element.getNodeName())) { //$NON-NLS-1$
				throw new ParameterException("El XML no tiene la forma esperada"); //$NON-NLS-1$
			}
			final NamedNodeMap attrs = element.getAttributes();
			final Node keyNode = attrs.getNamedItem("k"); //$NON-NLS-1$
			final Node valueNode = attrs.getNamedItem("v"); //$NON-NLS-1$
			if (keyNode == null || valueNode == null) {
				throw new ParameterException("El XML no tiene la forma esperada"); //$NON-NLS-1$
			}
			try {
				params.put(keyNode.getNodeValue(), URLDecoder.decode(valueNode.getNodeValue(), DEFAULT_URL_ENCODING));
			}
			catch (final UnsupportedEncodingException e) {
				params.put(keyNode.getNodeValue(), valueNode.getNodeValue());
			}
		}
		return params;
	}

	/** Comprueba que est&eacute;n disponibles todos los parametros disponibles en la entrada de
	 * datos para la operaci&oacute;n de firma.
	 * @param params Par&aacute;metros para el proceso de firma
	 * @return Par&aacute;metros
	 * @throws ParameterException Si alg&uacute;n par&aacute;metro proporcionado es incorrecto. */
	static UrlParametersToSign getParametersToSign(final Map<String, String> params) throws ParameterException {
		final UrlParametersToSign ret = new UrlParametersToSign();
		ret.setCommonParameters(params);
		ret.setSignParameters(params);
		return ret;
	}

	/** Comprueba que est&eacute;n disponibles todos los parametros disponibles en la entrada de
	 * datos para la operaci&oacute;n de selecci&oacute;n de certificado.
	 * @param params Par&aacute;metros para el proceso de firma
	 * @return Par&aacute;metros
	 * @throws ParameterException Si alg&uacute;n par&aacute;metro proporcionado es incorrecto. */
	static UrlParametersToSelectCert getParametersToSelectCert(final Map<String, String> params) throws ParameterException {
		final UrlParametersToSelectCert ret = new UrlParametersToSelectCert();
		ret.setCommonParameters(params);
		ret.setSelectCertParameters(params);
		return ret;
	}
}
