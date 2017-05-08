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
package es.gob.jmulticard.card.dnie;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;

import es.gob.jmulticard.card.cwa14890.Cwa14890PrivateConstants;
import es.gob.jmulticard.card.cwa14890.Cwa14890PublicConstants;

/** Constantes de la FNMT-TIF (variante del DNIe) para el establecimiento de canal seguro CWA-14890.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.  */
final class TifCwa14890Constants implements Cwa14890PublicConstants, Cwa14890PrivateConstants {

    /** Referencia al fichero en donde reside la clave p&uacute;blica de la autoridad certificadora
     * ra&iacute;z de la jerarqu&iacute;a de certificados verificables por la tarjeta. */
    private static final byte[] REF_C_CV_CA_PUBLIC_KEY = new byte[] {
            (byte) 0x02, (byte) 0x0f
    };

    /** Certificado de la CA intermedia de Terminal verificable por la tarjeta. */
    private static final byte[] C_CV_CA = new byte[] {
        (byte) 0x7F, (byte) 0x21, (byte) 0x81, (byte) 0xCE, (byte) 0x5F, (byte) 0x37, (byte) 0x81, (byte) 0x80, (byte) 0x96, (byte) 0xCB,
        (byte) 0x56, (byte) 0xD8, (byte) 0x2C, (byte) 0xBE, (byte) 0xD9, (byte) 0x8C, (byte) 0x35, (byte) 0x70, (byte) 0x73, (byte) 0xCE,
        (byte) 0xED, (byte) 0xAE, (byte) 0xC0, (byte) 0x87, (byte) 0x2B, (byte) 0x98, (byte) 0x81, (byte) 0x33, (byte) 0x3A, (byte) 0x63,
        (byte) 0xD9, (byte) 0xFF, (byte) 0x89, (byte) 0xA7, (byte) 0x45, (byte) 0x9A, (byte) 0xE8, (byte) 0x4C, (byte) 0x85, (byte) 0xFE,
        (byte) 0x68, (byte) 0x3F, (byte) 0x50, (byte) 0x44, (byte) 0xEE, (byte) 0x30, (byte) 0x3F, (byte) 0x5C, (byte) 0xF2, (byte) 0x1F,
        (byte) 0x42, (byte) 0x8A, (byte) 0xA6, (byte) 0x68, (byte) 0x0E, (byte) 0xBA, (byte) 0xF3, (byte) 0x91, (byte) 0xEA, (byte) 0x80,
        (byte) 0x73, (byte) 0x32, (byte) 0xDD, (byte) 0x0D, (byte) 0xFF, (byte) 0x7F, (byte) 0x11, (byte) 0xA0, (byte) 0xA7, (byte) 0x0A,
        (byte) 0x5E, (byte) 0xAB, (byte) 0x8C, (byte) 0xCE, (byte) 0x2A, (byte) 0x3C, (byte) 0x4B, (byte) 0x3A, (byte) 0x20, (byte) 0x31,
        (byte) 0x46, (byte) 0xCC, (byte) 0x3C, (byte) 0xFD, (byte) 0xFD, (byte) 0x75, (byte) 0x6E, (byte) 0xBA, (byte) 0xDD, (byte) 0x21,
        (byte) 0x7A, (byte) 0x20, (byte) 0xC6, (byte) 0x2A, (byte) 0x35, (byte) 0x08, (byte) 0xC6, (byte) 0x39, (byte) 0x98, (byte) 0x94,
        (byte) 0xA1, (byte) 0xE0, (byte) 0x5B, (byte) 0x21, (byte) 0x8B, (byte) 0x47, (byte) 0x25, (byte) 0x18, (byte) 0x0A, (byte) 0xCD,
        (byte) 0x9A, (byte) 0xE6, (byte) 0xBD, (byte) 0x96, (byte) 0x87, (byte) 0x0A, (byte) 0xB3, (byte) 0x94, (byte) 0x44, (byte) 0x3D,
        (byte) 0x73, (byte) 0x2A, (byte) 0x98, (byte) 0x46, (byte) 0x24, (byte) 0x6F, (byte) 0xC3, (byte) 0x6F, (byte) 0xFA, (byte) 0xF9,
        (byte) 0x0C, (byte) 0xBF, (byte) 0xF4, (byte) 0xAE, (byte) 0x4C, (byte) 0x57, (byte) 0x5F, (byte) 0x38, (byte) 0x3D, (byte) 0xF3,
        (byte) 0xEE, (byte) 0xA9, (byte) 0x46, (byte) 0x5D, (byte) 0xB2, (byte) 0x08, (byte) 0x11, (byte) 0xD7, (byte) 0xA8, (byte) 0x5B,
        (byte) 0xF1, (byte) 0xDA, (byte) 0x55, (byte) 0x4C, (byte) 0x21, (byte) 0x64, (byte) 0x69, (byte) 0x07, (byte) 0x94, (byte) 0x38,
        (byte) 0xCF, (byte) 0x0C, (byte) 0x50, (byte) 0x3D, (byte) 0xD5, (byte) 0xBF, (byte) 0x9A, (byte) 0xDD, (byte) 0x86, (byte) 0x57,
        (byte) 0xB1, (byte) 0x88, (byte) 0x8B, (byte) 0xC8, (byte) 0x4E, (byte) 0xB3, (byte) 0x82, (byte) 0x90, (byte) 0x9A, (byte) 0x87,
        (byte) 0x5E, (byte) 0x75, (byte) 0x1B, (byte) 0x93, (byte) 0x47, (byte) 0xBD, (byte) 0xAE, (byte) 0xDD, (byte) 0xCD, (byte) 0x56,
        (byte) 0x8E, (byte) 0x14, (byte) 0x56, (byte) 0x0E, (byte) 0x6C, (byte) 0x87, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
        (byte) 0x42, (byte) 0x08, (byte) 0x65, (byte) 0x73, (byte) 0x52, (byte) 0x43, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x07
    };

    /** Identificador de la CA intermedia (CHR). El campo ocupa siempre 12 bytes y si el numero de serie es
     * de menor longitud se rellena con ceros a la izquierda. El n&uacute;mero de serie es de al menos 8 bytes.
     * Aqu&iacute; indicamos los 8 bytes del n&uacute;mero de serie obviando el resto del campo (que no se
     * utiliza). */
    private static final byte[] CHR_C_CV_CA = new byte[] {
            (byte) 0x65, (byte) 0x73, (byte) 0x53, (byte) 0x43, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x07
    };

    /** Referencia al fichero en donde reside la clave privada de componente. */
    private static final byte[] REF_ICC_PRIVATE_KEY = new byte[] {
            (byte) 0x02, (byte) 0x1f
    };

    /** Certificado de Terminal verificable por la tarjeta. */
    private static final byte[] C_CV_IFD = new byte[] {
    	(byte) 0x7F, (byte) 0x21, (byte) 0x81, (byte) 0xCD, (byte) 0x5F, (byte) 0x37, (byte) 0x81, (byte) 0x80, (byte) 0x04, (byte) 0xBA,
    	(byte) 0x10, (byte) 0xA1, (byte) 0xED, (byte) 0xDF, (byte) 0x61, (byte) 0xBC, (byte) 0xCF, (byte) 0x06, (byte) 0x99, (byte) 0xAF,
    	(byte) 0xEE, (byte) 0xC5, (byte) 0x41, (byte) 0x34, (byte) 0x49, (byte) 0x65, (byte) 0x97, (byte) 0x33, (byte) 0x0A, (byte) 0x2D,
    	(byte) 0x2A, (byte) 0x8C, (byte) 0x2E, (byte) 0xB3, (byte) 0x50, (byte) 0x3C, (byte) 0x41, (byte) 0x35, (byte) 0x51, (byte) 0x8E,
    	(byte) 0x89, (byte) 0xC2, (byte) 0xA2, (byte) 0x54, (byte) 0x9E, (byte) 0x0D, (byte) 0x0B, (byte) 0xAE, (byte) 0x49, (byte) 0xBC,
    	(byte) 0xBF, (byte) 0x3E, (byte) 0xEB, (byte) 0x11, (byte) 0x00, (byte) 0x0E, (byte) 0x58, (byte) 0xFB, (byte) 0x39, (byte) 0x53,
    	(byte) 0x0E, (byte) 0x8E, (byte) 0x54, (byte) 0xA0, (byte) 0x95, (byte) 0x69, (byte) 0xE8, (byte) 0xC8, (byte) 0x82, (byte) 0x44,
    	(byte) 0x08, (byte) 0xD3, (byte) 0xFB, (byte) 0x5F, (byte) 0xF4, (byte) 0x18, (byte) 0x59, (byte) 0x45, (byte) 0x4A, (byte) 0xA5,
    	(byte) 0x20, (byte) 0x00, (byte) 0x8A, (byte) 0xF6, (byte) 0x2E, (byte) 0xE2, (byte) 0xDE, (byte) 0x54, (byte) 0x0B, (byte) 0x2D,
    	(byte) 0x14, (byte) 0x0C, (byte) 0x48, (byte) 0xE0, (byte) 0xAE, (byte) 0xED, (byte) 0x3C, (byte) 0x6D, (byte) 0x76, (byte) 0x77,
    	(byte) 0x2F, (byte) 0x45, (byte) 0xD0, (byte) 0x81, (byte) 0x66, (byte) 0x4D, (byte) 0xBD, (byte) 0x63, (byte) 0xF7, (byte) 0xAF,
    	(byte) 0x75, (byte) 0xDC, (byte) 0xA8, (byte) 0xB6, (byte) 0x00, (byte) 0x22, (byte) 0x7F, (byte) 0x48, (byte) 0xED, (byte) 0xAB,
    	(byte) 0x51, (byte) 0xB3, (byte) 0x52, (byte) 0x7D, (byte) 0xB3, (byte) 0xA7, (byte) 0x08, (byte) 0x1E, (byte) 0x5E, (byte) 0x31,
    	(byte) 0xD5, (byte) 0x42, (byte) 0x35, (byte) 0xE7, (byte) 0x86, (byte) 0x33, (byte) 0x5F, (byte) 0x38, (byte) 0x3C, (byte) 0x87,
    	(byte) 0xA0, (byte) 0xFF, (byte) 0x8A, (byte) 0x7C, (byte) 0x71, (byte) 0x20, (byte) 0xE4, (byte) 0xF1, (byte) 0x0D, (byte) 0xEE,
    	(byte) 0x6C, (byte) 0x86, (byte) 0xE2, (byte) 0x85, (byte) 0xB6, (byte) 0x71, (byte) 0x7A, (byte) 0x12, (byte) 0xED, (byte) 0xC0,
    	(byte) 0xB2, (byte) 0xF7, (byte) 0x9E, (byte) 0x14, (byte) 0x78, (byte) 0x3D, (byte) 0x1E, (byte) 0xC2, (byte) 0xEB, (byte) 0x1F,
    	(byte) 0x59, (byte) 0x45, (byte) 0x8F, (byte) 0x74, (byte) 0x49, (byte) 0x1A, (byte) 0x8B, (byte) 0x00, (byte) 0x93, (byte) 0xF2,
    	(byte) 0xAE, (byte) 0xC6, (byte) 0xA6, (byte) 0x35, (byte) 0xC9, (byte) 0x28, (byte) 0x00, (byte) 0x49, (byte) 0x91, (byte) 0x33,
    	(byte) 0x55, (byte) 0xE4, (byte) 0x6D, (byte) 0xD4, (byte) 0xA7, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x42,
    	(byte) 0x08, (byte) 0x65, (byte) 0x73, (byte) 0x53, (byte) 0x43, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x07
    };

    /** Identificador de la CA intermedia (CHR). El campo ocupa siempre 12 bytes y si el numero de serie es
     * de menor longitud se rellena con ceros a la izquierda. El n&uacute;mero de serie es de al menos 8 bytes.
     * Aqu&iacute; indicamos los 8 bytes del n&uacute;mero de serie obviando el resto del campo (que no se
     * utiliza). */
    private static final byte[] CHR_C_CV_IFD = new byte[] {
        (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01
    };

    /** Clave privada del certificado de Terminal. */
    private static final RSAPrivateKey IFD_PRIVATE_KEY = new RSAPrivateKey() {

		private static final long serialVersionUID = 7871208407563977221L;

		private final BigInteger ifdModulus = new BigInteger(1, new byte[] {
            (byte) 0xD6, (byte) 0x31, (byte) 0xD3, (byte) 0xDD, (byte) 0x61, (byte) 0x97, (byte) 0x51, (byte) 0x16, (byte) 0x0B, (byte) 0x87,
            (byte) 0xBA, (byte) 0x04, (byte) 0xA6, (byte) 0xD2, (byte) 0xD0, (byte) 0xE3, (byte) 0x15, (byte) 0x2E, (byte) 0xBE, (byte) 0x40,
            (byte) 0x6D, (byte) 0x57, (byte) 0x98, (byte) 0x2F, (byte) 0x06, (byte) 0x7D, (byte) 0xB1, (byte) 0xD5, (byte) 0x49, (byte) 0xF3,
            (byte) 0x76, (byte) 0xBD, (byte) 0x88, (byte) 0xF3, (byte) 0x32, (byte) 0x17, (byte) 0x8D, (byte) 0x8E, (byte) 0x48, (byte) 0x4A,
            (byte) 0x85, (byte) 0x49, (byte) 0x58, (byte) 0x1A, (byte) 0x2A, (byte) 0xA7, (byte) 0x61, (byte) 0x04, (byte) 0xB9, (byte) 0x6A,
            (byte) 0x77, (byte) 0xDA, (byte) 0x28, (byte) 0x25, (byte) 0xE1, (byte) 0x91, (byte) 0x61, (byte) 0xAC, (byte) 0x5F, (byte) 0x2E,
            (byte) 0x77, (byte) 0x81, (byte) 0x56, (byte) 0x9C, (byte) 0xE4, (byte) 0x49, (byte) 0x75, (byte) 0x20, (byte) 0xFC, (byte) 0x28,
            (byte) 0x4F, (byte) 0x95, (byte) 0x87, (byte) 0xA0, (byte) 0xFF, (byte) 0x8A, (byte) 0x7C, (byte) 0x71, (byte) 0x20, (byte) 0xE4,
            (byte) 0xF1, (byte) 0x0D, (byte) 0xEE, (byte) 0x6C, (byte) 0x86, (byte) 0xE2, (byte) 0x85, (byte) 0xB6, (byte) 0x71, (byte) 0x7A,
            (byte) 0x12, (byte) 0xED, (byte) 0xC0, (byte) 0xB2, (byte) 0xF7, (byte) 0x9E, (byte) 0x14, (byte) 0x78, (byte) 0x3D, (byte) 0x1E,
            (byte) 0xC2, (byte) 0xEB, (byte) 0x1F, (byte) 0x59, (byte) 0x45, (byte) 0x8F, (byte) 0x74, (byte) 0x49, (byte) 0x1A, (byte) 0x8B,
            (byte) 0x00, (byte) 0x93, (byte) 0xF2, (byte) 0xAE, (byte) 0xC6, (byte) 0xA6, (byte) 0x35, (byte) 0xC9, (byte) 0x28, (byte) 0x00,
            (byte) 0x49, (byte) 0x91, (byte) 0x33, (byte) 0x55, (byte) 0xE4, (byte) 0x6D, (byte) 0xD4, (byte) 0xA7
        });

        private final BigInteger ifdPrivateExponent = new BigInteger(1, new byte[] {
            (byte) 0xA4, (byte) 0x70, (byte) 0x22, (byte) 0x6F, (byte) 0xBD, (byte) 0xC1, (byte) 0x56, (byte) 0x4D, (byte) 0xEF, (byte) 0x52,
            (byte) 0x26, (byte) 0xBB, (byte) 0xF5, (byte) 0x7A, (byte) 0xB9, (byte) 0x44, (byte) 0xEC, (byte) 0x79, (byte) 0x33, (byte) 0x33,
            (byte) 0xF0, (byte) 0xFD, (byte) 0xA3, (byte) 0x66, (byte) 0xCE, (byte) 0xBB, (byte) 0xE6, (byte) 0xAF, (byte) 0x91, (byte) 0x13,
            (byte) 0x69, (byte) 0x20, (byte) 0x3F, (byte) 0x28, (byte) 0x3D, (byte) 0x57, (byte) 0x99, (byte) 0xA4, (byte) 0x16, (byte) 0xD9,
            (byte) 0xA7, (byte) 0x13, (byte) 0xD4, (byte) 0xA0, (byte) 0x5A, (byte) 0x08, (byte) 0x9F, (byte) 0xD1, (byte) 0xE9, (byte) 0xAA,
            (byte) 0xD4, (byte) 0x12, (byte) 0x56, (byte) 0x6A, (byte) 0x26, (byte) 0x8E, (byte) 0xDF, (byte) 0x06, (byte) 0x5F, (byte) 0x18,
            (byte) 0x24, (byte) 0x44, (byte) 0xD6, (byte) 0x41, (byte) 0x39, (byte) 0x98, (byte) 0xFA, (byte) 0xE0, (byte) 0xC2, (byte) 0x31,
            (byte) 0x5D, (byte) 0xEF, (byte) 0x34, (byte) 0x46, (byte) 0x7D, (byte) 0x10, (byte) 0xA2, (byte) 0x70, (byte) 0x3C, (byte) 0xA7,
            (byte) 0x4B, (byte) 0xE4, (byte) 0x2F, (byte) 0x7E, (byte) 0xFD, (byte) 0x41, (byte) 0xFC, (byte) 0x15, (byte) 0x72, (byte) 0xDF,
            (byte) 0x7A, (byte) 0xF3, (byte) 0x54, (byte) 0x42, (byte) 0x9F, (byte) 0xC5, (byte) 0x64, (byte) 0x04, (byte) 0x54, (byte) 0xD6,
            (byte) 0x87, (byte) 0x1B, (byte) 0xC6, (byte) 0x7C, (byte) 0x12, (byte) 0xBB, (byte) 0x78, (byte) 0x21, (byte) 0xA5, (byte) 0xD7,
            (byte) 0xBC, (byte) 0x93, (byte) 0x3E, (byte) 0x0B, (byte) 0xD7, (byte) 0x9B, (byte) 0x28, (byte) 0x4E, (byte) 0xB2, (byte) 0xE8,
            (byte) 0x35, (byte) 0xDF, (byte) 0xFE, (byte) 0xEB, (byte) 0xD3, (byte) 0x48, (byte) 0xC0, (byte) 0xD1
        });

        private final byte[] encoded = new byte[] {
    		(byte) 0x30, (byte) 0x82, (byte) 0x01, (byte) 0x35, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x30,
    		(byte) 0x0D, (byte) 0x06, (byte) 0x09, (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xF7,
    		(byte) 0x0D, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x82,
    		(byte) 0x01, (byte) 0x1F, (byte) 0x30, (byte) 0x82, (byte) 0x01, (byte) 0x1B, (byte) 0x02, (byte) 0x01,
    		(byte) 0x00, (byte) 0x02, (byte) 0x81, (byte) 0x80, (byte) 0xD6, (byte) 0x31, (byte) 0xD3, (byte) 0xDD,
    		(byte) 0x61, (byte) 0x97, (byte) 0x51, (byte) 0x16, (byte) 0x0B, (byte) 0x87, (byte) 0xBA, (byte) 0x04,
    		(byte) 0xA6, (byte) 0xD2, (byte) 0xD0, (byte) 0xE3, (byte) 0x15, (byte) 0x2E, (byte) 0xBE, (byte) 0x40,
    		(byte) 0x6D, (byte) 0x57, (byte) 0x98, (byte) 0x2F, (byte) 0x06, (byte) 0x7D, (byte) 0xB1, (byte) 0xD5,
    		(byte) 0x49, (byte) 0xF3, (byte) 0x76, (byte) 0xBD, (byte) 0x88, (byte) 0xF3, (byte) 0x32, (byte) 0x17,
    		(byte) 0x8D, (byte) 0x8E, (byte) 0x48, (byte) 0x4A, (byte) 0x85, (byte) 0x49, (byte) 0x58, (byte) 0x1A,
    		(byte) 0x2A, (byte) 0xA7, (byte) 0x61, (byte) 0x04, (byte) 0xB9, (byte) 0x6A, (byte) 0x77, (byte) 0xDA,
    		(byte) 0x28, (byte) 0x25, (byte) 0xE1, (byte) 0x91, (byte) 0x61, (byte) 0xAC, (byte) 0x5F, (byte) 0x2E,
    		(byte) 0x77, (byte) 0x81, (byte) 0x56, (byte) 0x9C, (byte) 0xE4, (byte) 0x49, (byte) 0x75, (byte) 0x20,
    		(byte) 0xFC, (byte) 0x28, (byte) 0x4F, (byte) 0x95, (byte) 0x87, (byte) 0xA0, (byte) 0xFF, (byte) 0x8A,
    		(byte) 0x7C, (byte) 0x71, (byte) 0x20, (byte) 0xE4, (byte) 0xF1, (byte) 0x0D, (byte) 0xEE, (byte) 0x6C,
    		(byte) 0x86, (byte) 0xE2, (byte) 0x85, (byte) 0xB6, (byte) 0x71, (byte) 0x7A, (byte) 0x12, (byte) 0xED,
    		(byte) 0xC0, (byte) 0xB2, (byte) 0xF7, (byte) 0x9E, (byte) 0x14, (byte) 0x78, (byte) 0x3D, (byte) 0x1E,
    		(byte) 0xC2, (byte) 0xEB, (byte) 0x1F, (byte) 0x59, (byte) 0x45, (byte) 0x8F, (byte) 0x74, (byte) 0x49,
    		(byte) 0x1A, (byte) 0x8B, (byte) 0x00, (byte) 0x93, (byte) 0xF2, (byte) 0xAE, (byte) 0xC6, (byte) 0xA6,
    		(byte) 0x35, (byte) 0xC9, (byte) 0x28, (byte) 0x00, (byte) 0x49, (byte) 0x91, (byte) 0x33, (byte) 0x55,
    		(byte) 0xE4, (byte) 0x6D, (byte) 0xD4, (byte) 0xA7, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x02,
    		(byte) 0x81, (byte) 0x80, (byte) 0xA4, (byte) 0x70, (byte) 0x22, (byte) 0x6F, (byte) 0xBD, (byte) 0xC1,
    		(byte) 0x56, (byte) 0x4D, (byte) 0xEF, (byte) 0x52, (byte) 0x26, (byte) 0xBB, (byte) 0xF5, (byte) 0x7A,
    		(byte) 0xB9, (byte) 0x44, (byte) 0xEC, (byte) 0x79, (byte) 0x33, (byte) 0x33, (byte) 0xF0, (byte) 0xFD,
    		(byte) 0xA3, (byte) 0x66, (byte) 0xCE, (byte) 0xBB, (byte) 0xE6, (byte) 0xAF, (byte) 0x91, (byte) 0x13,
    		(byte) 0x69, (byte) 0x20, (byte) 0x3F, (byte) 0x28, (byte) 0x3D, (byte) 0x57, (byte) 0x99, (byte) 0xA4,
    		(byte) 0x16, (byte) 0xD9, (byte) 0xA7, (byte) 0x13, (byte) 0xD4, (byte) 0xA0, (byte) 0x5A, (byte) 0x08,
    		(byte) 0x9F, (byte) 0xD1, (byte) 0xE9, (byte) 0xAA, (byte) 0xD4, (byte) 0x12, (byte) 0x56, (byte) 0x6A,
    		(byte) 0x26, (byte) 0x8E, (byte) 0xDF, (byte) 0x06, (byte) 0x5F, (byte) 0x18, (byte) 0x24, (byte) 0x44,
    		(byte) 0xD6, (byte) 0x41, (byte) 0x39, (byte) 0x98, (byte) 0xFA, (byte) 0xE0, (byte) 0xC2, (byte) 0x31,
    		(byte) 0x5D, (byte) 0xEF, (byte) 0x34, (byte) 0x46, (byte) 0x7D, (byte) 0x10, (byte) 0xA2, (byte) 0x70,
    		(byte) 0x3C, (byte) 0xA7, (byte) 0x4B, (byte) 0xE4, (byte) 0x2F, (byte) 0x7E, (byte) 0xFD, (byte) 0x41,
    		(byte) 0xFC, (byte) 0x15, (byte) 0x72, (byte) 0xDF, (byte) 0x7A, (byte) 0xF3, (byte) 0x54, (byte) 0x42,
    		(byte) 0x9F, (byte) 0xC5, (byte) 0x64, (byte) 0x04, (byte) 0x54, (byte) 0xD6, (byte) 0x87, (byte) 0x1B,
    		(byte) 0xC6, (byte) 0x7C, (byte) 0x12, (byte) 0xBB, (byte) 0x78, (byte) 0x21, (byte) 0xA5, (byte) 0xD7,
    		(byte) 0xBC, (byte) 0x93, (byte) 0x3E, (byte) 0x0B, (byte) 0xD7, (byte) 0x9B, (byte) 0x28, (byte) 0x4E,
    		(byte) 0xB2, (byte) 0xE8, (byte) 0x35, (byte) 0xDF, (byte) 0xFE, (byte) 0xEB, (byte) 0xD3, (byte) 0x48,
    		(byte) 0xC0, (byte) 0xD1, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x00,
    		(byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x01,
    		(byte) 0x00
        };

        /** {@inheritDoc} */
        @Override
        public BigInteger getModulus() {
            return this.ifdModulus;
        }

        /** {@inheritDoc} */
        @Override
        public String getFormat() {
            return "PKCS#8"; //$NON-NLS-1$
        }

        /** {@inheritDoc} */
        @Override
        public byte[] getEncoded() {
            final byte[] out = new byte[this.encoded.length];
            System.arraycopy(this.encoded, 0, out, 0, this.encoded.length);
            return out;
        }

        /** {@inheritDoc} */
        @Override
        public String getAlgorithm() {
            return "RSA"; //$NON-NLS-1$
        }

        /** {@inheritDoc} */
        @Override
        public BigInteger getPrivateExponent() {
            return this.ifdPrivateExponent;
        }
    };

    /** Clave p&uacute;blica del certificado de componente de la tarjeta. */
    private static final PublicKey CA_COMPONENT_PUBLIC_KEY = new PublicKey() {

        private static final long serialVersionUID = -143874096089393139L;

        private final byte[] encoded = new byte[] {
                (byte) 0x30, (byte) 0x81, (byte) 0x9E, (byte) 0x30, (byte) 0x0D, (byte) 0x06, (byte) 0x09, (byte) 0x2A, (byte) 0x86, (byte) 0x48,
                (byte) 0x86, (byte) 0xF7, (byte) 0x0D, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x03, (byte) 0x81,
                (byte) 0x8C, (byte) 0x00, (byte) 0x30, (byte) 0x81, (byte) 0x88, (byte) 0x02, (byte) 0x81, (byte) 0x80, (byte) 0xEA, (byte) 0xDE,
                (byte) 0xDA, (byte) 0x45, (byte) 0x53, (byte) 0x32, (byte) 0x94, (byte) 0x50, (byte) 0x39, (byte) 0xDA, (byte) 0xA4, (byte) 0x04,
                (byte) 0xC8, (byte) 0xEB, (byte) 0xC4, (byte) 0xD3, (byte) 0xB7, (byte) 0xF5, (byte) 0xDC, (byte) 0x86, (byte) 0x92, (byte) 0x83,
                (byte) 0xCD, (byte) 0xEA, (byte) 0x2F, (byte) 0x10, (byte) 0x1E, (byte) 0x2A, (byte) 0xB5, (byte) 0x4F, (byte) 0xB0, (byte) 0xD0,
                (byte) 0xB0, (byte) 0x3D, (byte) 0x8F, (byte) 0x03, (byte) 0x0D, (byte) 0xAF, (byte) 0x24, (byte) 0x58, (byte) 0x02, (byte) 0x82,
                (byte) 0x88, (byte) 0xF5, (byte) 0x4C, (byte) 0xE5, (byte) 0x52, (byte) 0xF8, (byte) 0xFA, (byte) 0x57, (byte) 0xAB, (byte) 0x2F,
                (byte) 0xB1, (byte) 0x03, (byte) 0xB1, (byte) 0x12, (byte) 0x42, (byte) 0x7E, (byte) 0x11, (byte) 0x13, (byte) 0x1D, (byte) 0x1D,
                (byte) 0x27, (byte) 0xE1, (byte) 0x0A, (byte) 0x5B, (byte) 0x50, (byte) 0x0E, (byte) 0xAA, (byte) 0xE5, (byte) 0xD9, (byte) 0x40,
                (byte) 0x30, (byte) 0x1E, (byte) 0x30, (byte) 0xEB, (byte) 0x26, (byte) 0xC3, (byte) 0xE9, (byte) 0x06, (byte) 0x6B, (byte) 0x25,
                (byte) 0x71, (byte) 0x56, (byte) 0xED, (byte) 0x63, (byte) 0x9D, (byte) 0x70, (byte) 0xCC, (byte) 0xC0, (byte) 0x90, (byte) 0xB8,
                (byte) 0x63, (byte) 0xAF, (byte) 0xBB, (byte) 0x3B, (byte) 0xFE, (byte) 0xD8, (byte) 0xC1, (byte) 0x7B, (byte) 0xE7, (byte) 0x67,
                (byte) 0x30, (byte) 0x34, (byte) 0xB9, (byte) 0x82, (byte) 0x3E, (byte) 0x97, (byte) 0x7E, (byte) 0xD6, (byte) 0x57, (byte) 0x25,
                (byte) 0x29, (byte) 0x27, (byte) 0xF9, (byte) 0x57, (byte) 0x5B, (byte) 0x9F, (byte) 0xFF, (byte) 0x66, (byte) 0x91, (byte) 0xDB,
                (byte) 0x64, (byte) 0xF8, (byte) 0x0B, (byte) 0x5E, (byte) 0x92, (byte) 0xCD, (byte) 0x02, (byte) 0x03, (byte) 0x01, (byte) 0x00,
                (byte) 0x01
        };

        /** {@inheritDoc} */
        @Override
        public String getFormat() {
            return "X.509"; //$NON-NLS-1$
        }

        /** {@inheritDoc} */
        @Override
        public byte[] getEncoded() {
            final byte[] out = new byte[this.encoded.length];
            System.arraycopy(this.encoded, 0, out, 0, this.encoded.length);
            return out;
        }

        /** {@inheritDoc} */
        @Override
        public String getAlgorithm() {
            return "RSA"; //$NON-NLS-1$
        }
    };

	@Override
	public byte[] getRefCCvCaPublicKey() {
		return REF_C_CV_CA_PUBLIC_KEY;
	}

	@Override
	public byte[] getCCvCa() {
		return C_CV_CA;
	}

	@Override
	public byte[] getChrCCvCa() {
		return CHR_C_CV_CA;
	}

	@Override
	public byte[] getRefIccPrivateKey() {
		return REF_ICC_PRIVATE_KEY;
	}

	@Override
	public byte[] getCCvIfd() {
		return C_CV_IFD;
	}

	@Override
	public byte[] getChrCCvIfd() {
		return CHR_C_CV_IFD;
	}

	@Override
	public RSAPrivateKey getIfdPrivateKey() {
		return IFD_PRIVATE_KEY;
	}

	@Override
	public PublicKey getCaComponentPublicKey() {
		return CA_COMPONENT_PUBLIC_KEY;
	}

	@Override
	public int getIfdKeyLength() {
		return 128;
	}
}