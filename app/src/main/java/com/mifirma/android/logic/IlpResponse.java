package com.mifirma.android.logic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Respuesta al env&oacute;a para registro de una ILP.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
final class IlpResponse {

	private static final Logger LOGGER = Logger.getLogger("com.mifirma.android"); //$NON-NLS-1$

	/** Obtiene el n&uacute;mero de registro de una ILP a partir de la respuesta del servidor.
	 * @param serverResponse Respuesta del servidor al resgistro de una ILP.
	 * @return N&uacute;mero de registro de una ILP.
	 * @throws IlpResponseException Si no se pudo registrar la ILP. */
	static String getToken(final byte[] serverResponse) throws IlpResponseException {
		if (serverResponse == null) {
			throw new IlpResponseException(null);
		}
		final String response = new String(serverResponse).trim();
		if (response.isEmpty()) {
			throw new IlpResponseException(
				Collections.singletonList(
					"El servidor ha devuelto una respuesta sin datos" //$NON-NLS-1$
				)
			);
		}
		else if (response.startsWith("<errors>")) { //$NON-NLS-1$
			throw new IlpResponseException(
				getErrorsFromResponse(serverResponse)
			);
		}
		return response;
	}

	private static List<String> getErrorsFromResponse(final byte[] serverResponse) {
		final List<String> ret = new ArrayList<String>();
		final InputStream is = new ByteArrayInputStream(serverResponse);
		final Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			is.close();
		}
		catch (final Exception e) {
			LOGGER.severe("Error analizando la respuesta del servidor: " + e); //$NON-NLS-1$
			return Collections.singletonList(
				"Error analizando la respuesta del servidor" //$NON-NLS-1$
			);
		}
		final Node errorsNode = doc.getDocumentElement();
		if (!"errors".equalsIgnoreCase(errorsNode.getNodeName())) { //$NON-NLS-1$
			LOGGER.severe("La respuesta del servidor no contiene un nodo padre de errores"); //$NON-NLS-1$
			return Collections.singletonList(
				"La respuesta del servidor no contiene un nodo padre de errores" //$NON-NLS-1$
			);
		}
		final NodeList childNodes = errorsNode.getChildNodes();
		int idx = nextNodeElementIndex(childNodes, 0);
		while (idx != -1) {
			final Node errorItemNode = childNodes.item(idx);
			ret.add(errorItemNode.getTextContent());
			idx = nextNodeElementIndex(childNodes, idx + 1);
		}
		return ret;
	}

	/** Recupera el &iacute;ndice del siguiente nodo de la lista de tipo
	 * <code>Element</code>. Empieza a comprobar los nodos a partir del
	 * &iacute;ndice marcado. Si no encuentra un nodo de tipo <i>elemento</i>
	 * devuelve -1.
	 * @param nodes Listado de nodos.
	 * @param currentIndex &Iacute;ndice del listado a partir del cual se empieza la
	 *                     comprobaci&oacute;n.
	 * @return &Iacute;ndice del siguiente node de tipo Element o -1 si no se
	 *         encontr&oacute;. */
	private static int nextNodeElementIndex(final NodeList nodes, final int currentIndex) {
		Node node;
		int i = currentIndex;
		while (i < nodes.getLength()) {
			node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/** Error referente al env&oacute;a para registro de una ILP. */
	static class IlpResponseException extends Exception {

		private static final long serialVersionUID = -5874784845659251214L;

		private List<String> errors;

		IlpResponseException(final List<String> errorTexts) {
			if (errorTexts == null || errorTexts.isEmpty()) {
				this.errors = Collections.singletonList("Error indefinido"); //$NON-NLS-1$
			}
			else {
				this.errors = errorTexts;
			}
		}

		/** Obtiene los textos de los errores del env&oacute;a para registro de una ILP.
		 * @return Textos de los errores del env&oacute;a para registro de una ILP. */
		public List<String> getErrors() {
			return this.errors;
		}
	}
}
