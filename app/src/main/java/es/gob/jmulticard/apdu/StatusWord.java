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
package es.gob.jmulticard.apdu;

import java.io.Serializable;

import es.gob.jmulticard.HexUtils;

/** Palabra de estado (<cite>Status Word</cite>) de una APDU.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s Capote, Gonzalo Henr&iacute;quez Manzano
 * @version 1.0 */
public final class StatusWord implements Serializable {

    private static final long serialVersionUID = -735824987343408119L;
    private byte msb = 0x00;
    private byte lsb = 0x00;

    /** Contruye una palabra de estado de una APDU.
     * @param msb Octeto m&aacute;s significativo de la palabra de estado.
     * @param lsb Octeto menos significativo de la palabra de estado. */
    public StatusWord(final byte msb, final byte lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    /** Obtiene el octeto m&aacute;s significativo de la palabra de estado.
     * @return Octeto m&aacute;s significativo de la palabra de estado.
     * @see #getLsb() */
    public byte getMsb() {
        return this.msb;
    }

    /** Obtiene el octeto menos significativo de la palabra de estado.
     * @return Octeto menos significativo de la palabra de estado.
     * @see #getMsb() */
    public byte getLsb() {
        return this.lsb;
    }

    /**
     * Obtiene los octetos que conforman el StatusWord.
     * @return Array de octetos que conforman el StatusWord.
     */
    public byte[] getBytes() {
    	return new byte[] { this.msb, this.lsb };
    }

    /** Compara dos palabras de estado.
     * @return <code>true</code> si son iguales (a nivel de octetos), <code>false</code> si son distintas. */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof StatusWord)) {
            return false;
        }
        final StatusWord other = (StatusWord) obj;
        if (this.lsb == other.lsb && this.msb == other.msb) {
            return true;
        }
        return false;
    }

    /** Devuelve un c&oacute;digo <i>hash</i> para la palabra de estado.
     * @return C&oacute;digo <i>hash</i> para la palabra de estado. */
    @Override
    public int hashCode() {
        return HexUtils.getShort(new byte[] {
                this.msb, this.lsb
        }, 0);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
    	return HexUtils.hexify(new byte [] { this.msb, this.lsb }, true);
    }
}
