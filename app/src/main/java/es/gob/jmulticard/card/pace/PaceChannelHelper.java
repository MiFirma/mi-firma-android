package es.gob.jmulticard.card.pace;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.logging.Logger;

import org.spongycastle.asn1.teletrust.TeleTrusTNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECCurve.Fp;
import org.spongycastle.math.ec.ECFieldElement;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.Arrays;

import es.gob.jmulticard.CryptoHelper;
import es.gob.jmulticard.HexUtils;
import es.gob.jmulticard.apdu.CommandApdu;
import es.gob.jmulticard.apdu.ResponseApdu;
import es.gob.jmulticard.apdu.connection.ApduConnection;
import es.gob.jmulticard.apdu.connection.ApduConnectionException;
import es.gob.jmulticard.apdu.iso7816four.GeneralAuthenticateApduCommand;
import es.gob.jmulticard.apdu.iso7816four.pace.MseSetPaceAlgorithmApduCommand;
import es.gob.jmulticard.asn1.Tlv;
import es.gob.jmulticard.asn1.TlvException;
import es.gob.jmulticard.de.tsenger.androsmex.crypto.AmAESCrypto;
import es.gob.jmulticard.de.tsenger.androsmex.crypto.AmCryptoProvider;
import es.gob.jmulticard.de.tsenger.androsmex.iso7816.SecureMessaging;

/** Utilidades para el establecimiento de un canal <a href="https://www.bsi.bund.de/EN/Publications/TechnicalGuidelines/TR03110/BSITR03110.html">PACE</a>
 * (Password Authenticated Connection Establishment).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class PaceChannelHelper {

	private static final Logger LOGGER = Logger.getLogger("es.gob.jmulticard"); //$NON-NLS-1$

	private static final byte[] CAN_PADDING = new byte[] {
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03
	};

	private static final byte[] KENC_PADDING = new byte[] {
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01
		};

	private static final byte[] KMAC_PADDING = new byte[] {
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02
		};

	private static final byte[] MAC_PADDING = new byte[] {
			(byte) 0x7F, (byte) 0x49, (byte) 0x4F, (byte) 0x06
		};

	private static final byte[] MAC2_PADDING = new byte[] {
			(byte) 0x86, (byte) 0x41, (byte) 0x04
		};

	private static final byte TAG_DYNAMIC_AUTHENTICATION_DATA = (byte) 0x7C;

	private static final byte TAG_GEN_AUTH_2 = (byte) 0x81;

	private static final byte TAG_GEN_AUTH_3 = (byte) 0x83;

	private static final byte TAG_GEN_AUTH_4 = (byte) 0x85;

	private PaceChannelHelper() {
		// No instanciable
	}

	/** Abre un canal PACE mediante el CAN (<i>Card Access Number</i>).
	 * @param cla Clase de APDU para los comandos de establecimiento de canal.
	 * @param can CAN (<i>Card Access Number</i>).
	 * @param conn Conexi&oacute;n hacia la tarjeta inteligente.
	 * @param cryptoHelper Clase para la realizaci&oacute;n de operaciones criptogr&aacute;ficas auxiliares.
	 * @return SecureMessaging Objeto para el env&iacute;o de mensajes seguros a trav&eacute;s de canal PACE.
	 * @throws ApduConnectionException Si hay problemas de conexi&oacute;n con la tarjeta.
	 * @throws PaceException Si hay problemas en la apertura del canal.
	 *
	 */
	public static SecureMessaging openPaceChannel(final byte cla,
			                           final String can,
			                           final ApduConnection conn,
			                           final CryptoHelper cryptoHelper) throws ApduConnectionException,
			                                                                   PaceException {
		if (conn == null) {
			throw new IllegalArgumentException(
				"El canal de conexion no puede ser nulo" //$NON-NLS-1$
			);
		}
		if (can == null || "".equals(can)) { //$NON-NLS-1$
			throw new IllegalArgumentException(
				"Es necesario proporcionar el CAN para abrir canal PACE" //$NON-NLS-1$
			);
		}
		if (cryptoHelper == null) {
			throw new IllegalArgumentException(
				"El CryptoHelper no puede ser nulo" //$NON-NLS-1$
			);
		}

		if (!conn.isOpen()) {
			conn.open();
		}

		ResponseApdu res;
		CommandApdu comm;

		// 1.3.2 - Establecemos el algoritmo para PACE con el comando ‘MSE Set’:

		comm = new MseSetPaceAlgorithmApduCommand(
			cla,
			MseSetPaceAlgorithmApduCommand.PaceAlgorithmOid.PACE_ECDH_GM_AES_CBC_CMAC128,
			MseSetPaceAlgorithmApduCommand.PacePasswordType.CAN,
			MseSetPaceAlgorithmApduCommand.PaceAlgorithmParam.BRAINPOOL_256_R1
		);
		res = conn.transmit(comm);

		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error estableciendo el algoritmo del protocolo PACE." //$NON-NLS-1$
			);
		}

		// 1.3.3 - Primer comando General Autenticate - Get Nonce

		comm = new GeneralAuthenticateApduCommand(
			(byte) 0x10,
			new byte[] { (byte) 0x7C, (byte) 0x00 }
		);
		res = conn.transmit(comm);

		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error solicitando el aleatorio de calculo PACE (Nonce)" //$NON-NLS-1$
			);
		}

		// Calcular nonce devuelto por la tarjeta que se empleara en los calculos
		final byte[] nonce;
		try {
			nonce = new Tlv(new Tlv(res.getData()).getValue()).getValue();
		}
		catch (final TlvException e) {
			throw new PaceException(
				"El aleatorio de calculo PACE (Nonce) obtenido (" + HexUtils.hexify(res.getData(), true) + ") no sigue el formato esperado: " + e, e //$NON-NLS-1$ //$NON-NLS-2$
			);
		}

		// Calcular sk = SHA-1( CAN || 00000003 );
		// La clave son los 16 bytes MSB del hash

		final byte[] sk = new byte[16];
		try {
			System.arraycopy(
				cryptoHelper.digest(
					CryptoHelper.DigestAlgorithm.SHA1,
					HexUtils.concatenateByteArrays(
						can.getBytes(),
						CAN_PADDING
					)
				),
				0,
				sk,
				0,
				16
			);
		}
		catch (final IOException e) {
			throw new PaceException(
				"Error obteniendo el 'sk' a partir del CAN: " + e, e //$NON-NLS-1$
			);
		}

		// Calcular secret = AES_Dec(?nonce,?sk);

		final byte[] secret_nonce;
		try {
			secret_nonce = cryptoHelper.aesDecrypt(
				nonce,
				new byte[0],
				sk
			);
		}
		catch (final Exception e) {
			throw new PaceException(
				"Error descifranco el 'nonce': " + e, e //$NON-NLS-1$
			);
		}

		// 1.3.4 - Segundo comando General Autenticate - Map Nonce

		// Generamos un par de claves efimeras EC para el DH

		final X9ECParameters ecdhParameters = TeleTrusTNamedCurves.getByName("brainpoolp256r1"); //$NON-NLS-1$
		final ECPoint pointG = ecdhParameters.getG();
		final Fp curve = (org.spongycastle.math.ec.ECCurve.Fp) ecdhParameters.getCurve();

		// La privada del terminal se genera aleatoriamente (PrkIFDDH1)
		// La publica de la tarjeta sera devuelta por ella misma al enviar nuesra publica (pukIFDDH1)
		final Random rnd = new Random();
		rnd.setSeed(rnd.nextLong());
		final byte[] x1 = new byte[curve.getFieldSize()/8];
		rnd.nextBytes(x1);
		final BigInteger PrkIFDDH1 = new BigInteger(1, x1);
		// Enviamos nuestra clave publica (pukIFDDH1 = G*PrkIFDDH1)
		final ECPoint pukIFDDH1 = pointG.multiply(PrkIFDDH1);

		Tlv tlv = new Tlv(
				TAG_DYNAMIC_AUTHENTICATION_DATA,
				new Tlv(
					TAG_GEN_AUTH_2,
					pukIFDDH1.getEncoded(false)
				).getBytes()
			);

		// ... Y la enviamos a la tarjeta
		comm = new GeneralAuthenticateApduCommand(
			(byte) 0x10,
			tlv.getBytes()
		);

		res = conn.transmit(comm);

		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error mapeando el aleatorio de calculo PACE (Nonce)" //$NON-NLS-1$
			);
		}
		// Se obtiene la clave publica de la tarjeta
		final byte[] pukIccDh1;
		try {
			pukIccDh1 = unwrapEcKey(res.getData());
		}
		catch(final Exception e) {
			throw new PaceException(
				"Error obteniendo la clave efimera EC publica de la tarjeta: " + e, e //$NON-NLS-1$
			);
		}

		// calcular blinding point H = PrkIFDDH1 * PukICCDH1
		final ECPoint y1FromG = byteArrayToECPoint(pukIccDh1, curve);

		//Calculamos el punto H secreto
		final ECPoint SharedSecret_H = y1FromG.multiply(PrkIFDDH1);

		//Se calcula el nuevo punto G' = nonce*G + H
		final BigInteger ms = new BigInteger(1, secret_nonce);
		final ECPoint g_temp = pointG.multiply(ms);
		final ECPoint newPointG = g_temp.add(SharedSecret_H);


		// 1.3.5 Tercer comando General Authenticate

		//Se calcula la coordenada X de G' y generamos con la tarjeta un nuevo acuerdo de claves
		// La privada del terminal se genera aleatoriamente (PrkIFDDH2)
		// La publica de la tarjeta sera devuelta por ella misma al enviar nuesra publica (pukIFDDH2)
		final byte[] x2 = new byte[curve.getFieldSize()/8];
		rnd.setSeed(rnd.nextLong());
		rnd.nextBytes(x2);
		final BigInteger PrkIFDDH2 = new BigInteger(1, x2);

		// Enviamos nuestra clave publica (pukIFDDH2 = G'*PrkIFDDH2)
		final ECPoint pukIFDDH2 = newPointG.multiply(PrkIFDDH2);

		// ... La metemos en un TLV de autenticacion ...
		tlv = new Tlv(
					TAG_DYNAMIC_AUTHENTICATION_DATA,
					new Tlv(
						TAG_GEN_AUTH_3,
						pukIFDDH2.getEncoded(false)
					).getBytes()
				);


		comm = new GeneralAuthenticateApduCommand(
				(byte) 0x10,
				tlv.getBytes()
			);

			res = conn.transmit(comm);

			// Se obtiene la clave publica de la tarjeta (pukIccDh2) que es la coordenada Y del nuevo Punto G'
			final byte[] pukIccDh2;
			try {
				pukIccDh2 = unwrapEcKey(res.getData());
			}
			catch(final Exception e) {
				throw new PaceException(
					"Error obteniendo la clave efimera EC publica de la tarjeta: " + e, e //$NON-NLS-1$
				);
			}

			final ECPoint y2FromNewG = byteArrayToECPoint(pukIccDh2, curve);

			// Se calcula el secreto k = PukICCDH2 * PrkIFDDH2
			final ECPoint.Fp SharedSecret_K = (org.spongycastle.math.ec.ECPoint.Fp) y2FromNewG.multiply(PrkIFDDH2);
			final byte[] secretK = bigIntToByteArray(SharedSecret_K.normalize().getXCoord().toBigInteger());

			// 1.3.6 Cuarto comando General Authenticate
			// Se validan las claves de sesion generadas en el paso anterior,
			// por medio de un MAC que calcula el terminal y comprueba la tarjeta,
			// la cual devolvera un segundo MAC.

			// Calcular kenc = SHA-1( k || 00000001 );
			final byte[] kenc = new byte[16];
			try {
				System.arraycopy(
					cryptoHelper.digest(
						CryptoHelper.DigestAlgorithm.SHA1,
						HexUtils.concatenateByteArrays(
							secretK,
							KENC_PADDING
						)
					),
					0,
					kenc,
					0,
					16
				);
			}
			catch (final IOException e) {
				throw new PaceException(
					"Error obteniendo el 'kenc' a partir del CAN: " + e, e //$NON-NLS-1$
				);
			}

			// Calcular kmac = SHA-1( k || 00000002 );
			final byte[] kmac = new byte[16];
			try {
				System.arraycopy(
					cryptoHelper.digest(
						CryptoHelper.DigestAlgorithm.SHA1,
						HexUtils.concatenateByteArrays(
							secretK,
							KMAC_PADDING
						)
					),
					0,
					kmac,
					0,
					16
				);
			}
			catch (final IOException e) {
				throw new PaceException(
					"Error obteniendo el 'kmac' a partir del CAN: " + e, e //$NON-NLS-1$
				);
			}

			//Elimina el byte '04' del inicio que es el indicador de punto descomprimido
			final byte[] pukIccDh2Descompressed = new byte[pukIccDh2.length-1];
			System.arraycopy(pukIccDh2, 1, pukIccDh2Descompressed, 0, pukIccDh2.length-1);

			// Se calcula el Mac del terminal: ?data = '7f494F06'. ?oid. '864104'.PukICCDH2;
			final byte[] data = HexUtils.concatenateByteArrays(MAC_PADDING,
								HexUtils.concatenateByteArrays(MseSetPaceAlgorithmApduCommand.PaceAlgorithmOid.PACE_ECDH_GM_AES_CBC_CMAC128.getBytes(),
								HexUtils.concatenateByteArrays(MAC2_PADDING, pukIccDh2Descompressed)));

			byte[] mac8bytes;
			try {
				mac8bytes = cryptoHelper.doAesCmac(
					data,
					kmac
				);
			}
			catch (final Exception e) {
				throw new PaceException(
					"Error descifrando el 'nonce': " + e, e //$NON-NLS-1$
				);
			}

			// ... La metemos en un TLV de autenticacion ...
			tlv = new Tlv(
				TAG_DYNAMIC_AUTHENTICATION_DATA,
				new Tlv(
					TAG_GEN_AUTH_4,
					mac8bytes
				).getBytes()
			);

			// Se envia el comando General Authenticate y se recupera el MAC devuelto por la tarjeta.
			comm = new GeneralAuthenticateApduCommand(
			(byte) 0x00,
			tlv.getBytes()
			);

		res = conn.transmit(comm);

		// Se obtiene un MAC con respuesta 90-00 indicando que se ha establecido el canal correctamente
		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error estableciendo el algoritmo del protocolo PACE." //$NON-NLS-1$
			);
		}

		// Se inicializa el contador de secuencia a ceros
		final byte[] ssc = new byte[16];
		Arrays.fill(ssc, (byte)0);

		LOGGER.info("Canal Pace abierto"); //$NON-NLS-1$
		LOGGER.info("\nKenc: " + HexUtils.hexify(kenc, true) + //$NON-NLS-1$
					"Kmac: " + HexUtils.hexify(kmac, true) + //$NON-NLS-1$
					"Ssc: " + HexUtils.hexify(ssc, true)); //$NON-NLS-1$

		final AmCryptoProvider crypto = new AmAESCrypto();
		return new SecureMessaging(crypto, kenc, kmac, new byte[crypto.getBlockSize()]);
	}

	private static byte[] bigIntToByteArray(final BigInteger bi) {
		final byte[] temp = bi.toByteArray();
		byte[] returnbytes = null;
		if (temp[0] == 0) {
			returnbytes = new byte[temp.length - 1];
			System.arraycopy(temp, 1, returnbytes, 0, returnbytes.length);
			return returnbytes;
		}
		return temp;
	}

	private static byte[] unwrapEcKey(final byte[] key) throws TlvException {
		return new Tlv(new Tlv(key).getValue()).getValue();
	}


	private static ECPoint byteArrayToECPoint(final byte[] value, final ECCurve.Fp curve)
			throws IllegalArgumentException {
		final byte[] x = new byte[(value.length - 1) / 2];
		final byte[] y = new byte[(value.length - 1) / 2];
		if (value[0] != (byte) 0x04) {
			throw new IllegalArgumentException("No uncompressed Point found!"); //$NON-NLS-1$
		}
		System.arraycopy(value, 1, x, 0, (value.length - 1) / 2);
		System.arraycopy(value, 1 + (value.length - 1) / 2, y, 0,
				(value.length - 1) / 2);
		final ECFieldElement.Fp xE = (org.spongycastle.math.ec.ECFieldElement.Fp) curve.fromBigInteger(new BigInteger(1, x));
		final ECFieldElement.Fp yE = (org.spongycastle.math.ec.ECFieldElement.Fp) curve.fromBigInteger(new BigInteger(1, y));

		final ECPoint point = curve.createPoint(xE.toBigInteger(), yE.toBigInteger());
		return point;
	}
}