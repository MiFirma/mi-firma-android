/*
 * Controlador Java de la Secretaria de Estado de Administraciones Publicas
 * para el DNI electronico.
 *
 * El Controlador Java para el DNI electronico es un proveedor de seguridad de JCA/JCE
 * que permite el acceso y uso del DNI electronico en aplicaciones Java de terceros
 * para la realizacion de procesos de autenticacion, firma electronica y validacion
 * de firma. Para ello, se implementan las funcionalidades KeyStore y Signature para
 * el acceso a los certificados y claves del DNI electronico, asi como la realizacion
 * de operaciones criptograficas de firma con el DNI electronico. El Controlador ha
 * sido disenado para su funcionamiento independiente del sistema operativo final.
 *
 * Copyright (C) 2012 Direccion General de Modernizacion Administrativa, Procedimientos
 * e Impulso de la Administracion Electronica
 *
 * Este programa es software libre y utiliza un licenciamiento dual (LGPL 2.1+
 * o EUPL 1.1+), lo cual significa que los usuarios podran elegir bajo cual de las
 * licencias desean utilizar el codigo fuente. Su eleccion debera reflejarse
 * en las aplicaciones que integren o distribuyan el Controlador, ya que determinara
 * su compatibilidad con otros componentes.
 *
 * El Controlador puede ser redistribuido y/o modificado bajo los terminos de la
 * Lesser GNU General Public License publicada por la Free Software Foundation,
 * tanto en la version 2.1 de la Licencia, o en una version posterior.
 *
 * El Controlador puede ser redistribuido y/o modificado bajo los terminos de la
 * European Union Public License publicada por la Comision Europea,
 * tanto en la version 1.1 de la Licencia, o en una version posterior.
 *
 * Deberia recibir una copia de la GNU Lesser General Public License, si aplica, junto
 * con este programa. Si no, consultelo en <http://www.gnu.org/licenses/>.
 *
 * Deberia recibir una copia de la European Union Public License, si aplica, junto
 * con este programa. Si no, consultelo en <http://joinup.ec.europa.eu/software/page/eupl>.
 *
 * Este programa es distribuido con la esperanza de que sea util, pero
 * SIN NINGUNA GARANTIA; incluso sin la garantia implicita de comercializacion
 * o idoneidad para un proposito particular.
 */
package es.gob.jmulticard.asn1.der.x509;

import es.gob.jmulticard.asn1.der.SequenceOf;

/** Tipo ASN&#46;1 X&#46;509 <i>RdnSequence</i> (secuencia de <i>RelativeDistinguishedName</i>).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class RdnSequence extends SequenceOf {

	/** Construye un objeto ASN&#46;1 X&#46;509 <i>RdnSequence</i> (secuencia de <i>RelativeDistinguishedName</i>). */
	public RdnSequence() {
		super(RelativeDistinguishedName.class);
	}

	/** {@inheritDoc} */
	@Override
    public String toString() {
		final StringBuffer sb = new StringBuffer();
		final int lastIndex = getElementCount();
		for (int i=0; i<lastIndex;i++) {
			sb.append(getElementAt(i));
			if (i != lastIndex-1) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}

}
