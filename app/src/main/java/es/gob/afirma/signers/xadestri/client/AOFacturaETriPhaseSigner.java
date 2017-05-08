/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.signers.xadestri.client;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.CounterSignTarget;

/** Manejador de firmas FacturaE (derivado de XAdES) trif&aacute;sicas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class AOFacturaETriPhaseSigner extends AOXAdESTriPhaseSigner {

	/** Construye un manejador de firmas FacturaE (derivado de XAdES) trif&aacute;sicas. */
	public AOFacturaETriPhaseSigner() {
		super(AOSignConstants.SIGN_FORMAT_FACTURAE);
	}

	@Override
	public byte[] cosign(final byte[] data,
			             final byte[] sign,
			             final String algorithm,
			             final PrivateKey key,
			             final Certificate[] certChain,
			             final Properties xParams) throws AOException {
		throw new UnsupportedOperationException(
			"FacturaE no soporta cofirmas" //$NON-NLS-1$
		);
	}

	@Override
	public byte[] countersign(final byte[] sign,
			final String algorithm,
			final CounterSignTarget targetType,
			final Object[] targets,
			final PrivateKey key,
			final Certificate[] certChain,
			final Properties xParams) throws AOException {
		throw new UnsupportedOperationException(
			"FacturaE no soporta contrafirmas" //$NON-NLS-1$
		);
	}

    /** {@inheritDoc} */
    @Override
	public boolean isSign(final byte[] is) {
        return super.isSign(is) && isValidDataFile(is);
    }

    /** Indica si los datos son una factura electr&oacute;nica.
     * Importante: El que los datos sean una factura electr&oacute;nica no implica que puedan ser firmados, si esta
     * ya est&aacute; firmada el a&ntilde;adido de una firma adicional invalidar&iacute;a la factura
     * @param is Datos a comprobar
     * @return <code>true</code> si los datos son una <a href="http://www.facturae.es/">factura electr&oacute;nica</a>,
     *         <code>false</code> en caso contrario */
    @Override
	public boolean isValidDataFile(final byte[] is) {
        if (is == null || is.length == 0) {
            return false;
        }
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            final Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(is));
            final Element rootNode = doc.getDocumentElement();
            final String rootNodePrefix = rootNode.getPrefix();

            if (!((rootNodePrefix != null ? rootNodePrefix + ":" : "") + "Facturae").equals(rootNode.getNodeName())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return false;
            }

            final Set<String> childs = new HashSet<String>(3);
            childs.add("FileHeader"); //$NON-NLS-1$
            childs.add("Parties"); //$NON-NLS-1$
            childs.add("Invoices"); //$NON-NLS-1$

            final NodeList nl = rootNode.getChildNodes();
            for (int i=0;i<nl.getLength();i++) {
                final String nodeName = nl.item(i).getNodeName();
                if (childs.contains(nodeName)) {
                    childs.remove(nodeName);
                }
            }
            if (childs.size() > 0) {
                return false;
            }

        }
        catch (final Exception e) {
            return false;
        }
        return true;
    }

}
