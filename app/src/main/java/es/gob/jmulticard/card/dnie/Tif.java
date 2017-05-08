package es.gob.jmulticard.card.dnie;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

import es.gob.jmulticard.CryptoHelper;
import es.gob.jmulticard.apdu.connection.ApduConnection;
import es.gob.jmulticard.apdu.connection.ApduConnectionException;
import es.gob.jmulticard.card.AuthenticationModeLockedException;

/** Tarjeta FNMT TIF (variante del DNIe).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class Tif extends Dnie {

	/** Construye una tarjeta FNMT TIF (variante del DNIe).
     * @param conn Conexi&oacute;n con la tarjeta.
     * @param pwc <i>PasswordCallback</i> para obtener el PIN de la TIF.
     * @param cryptoHelper Funcionalidades criptogr&aacute;ficas de utilidad que pueden variar entre m&aacute;quinas virtuales.
     * @param ch Gestor de <i>callbacks</i> para la solicitud de datos al usuario.
     * @throws ApduConnectionException Si la conexi&oacute;n con la tarjeta se proporciona cerrada y no es posible abrirla.*/
	public Tif(final ApduConnection conn,
			   final PasswordCallback pwc,
			   final CryptoHelper cryptoHelper,
			   final CallbackHandler ch) throws ApduConnectionException {
		super(conn, pwc, cryptoHelper, ch);
	}

	/** {@inheritDoc} */
	@Override
	public byte[] changePIN(final String oldPin, final String newPin) throws AuthenticationModeLockedException {
		throw new UnsupportedOperationException("El cambio de PIN no esta permitido para la tarjeta insertada."); //$NON-NLS-1$
	}

}
