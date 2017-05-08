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
package es.gob.jmulticard.asn1.der.pkcs15;

import es.gob.jmulticard.asn1.OptionalDecoderObjectElement;
import es.gob.jmulticard.asn1.der.DerBoolean;
import es.gob.jmulticard.asn1.der.Sequence;

/** Tipo ASN&#46;1 PKCS#15 <i>CommonKeyAttributes</i>.
 * <pre>
 *  CommonKeyAttributes ::= SEQUENCE {
 *    iD           Identifier,
 *    usage        KeyUsageFlags,
 *    native       BOOLEAN OPTIONAL,
 *    accessFlags  KeyAccessFlags OPTIONAL,
 *    keyReference PKCS15Reference OPTIONAL,
 *    startDate    GeneralizedTime OPTIONAL,
 *    endDate      [0] GeneralizedTime OPTIONAL,
 *  }
 *  Identifier ::= OCTET STRING (SIZE (1..pkcs15-ub-identifier))
 *  KeyUsageFlags ::= BIT STRING
 *  PKCS15Reference ::= INTEGER (0..pkcs15-ub-reference)
 *  KeyAccessFlags ::= BIT STRING
 *  PKCS15Reference  ::= INTEGER (0..pkcs15-ub-reference)
 * </pre>
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class CommonKeyAttributes extends Sequence {

	/** Construye un objeto ASN&#46;1 PKCS#15 <i>CommonKeyAttributes</i>. */
	public CommonKeyAttributes() {
		super(
			new OptionalDecoderObjectElement[] {
				new OptionalDecoderObjectElement(
					Identifier.class,    // Subtipo de Octet String
					false
				),
				new OptionalDecoderObjectElement(
					KeyUsageFlags.class, // Subtipo de Bit String
					false
				),
				new OptionalDecoderObjectElement(
					DerBoolean.class,
					true // Opcional
				),
				new OptionalDecoderObjectElement(
					AccessFlags.class,   // Subtipo de Bit String
					false
				),
				new OptionalDecoderObjectElement(
					Reference.class,     // Subtipo de Integer
					false
				)
			}
		);
	}

	/** Devuelve el identificador de la clave.
	 * @return Identificador de la clave. */
	public byte[] getIdentifier() {
		return ((Identifier) getElementAt(0)).getOctectStringByteValue();
	}

	/** Devuelve la referencia de la clave,
	 * @return Referencia de la clave, o <code>null</code> si la clave no tiene referencia. */
	public Reference getReference() {
		// Recorro la secuencia buscando la referencia, porque no puedo saber la posicion exacta con los opcionales
		for (int i=0;i<getElementCount();i++) {
			final Object o = getElementAt(i);
			if (o instanceof Reference) {
				return (Reference) o;
			}
		}
		return null;
	}

}
