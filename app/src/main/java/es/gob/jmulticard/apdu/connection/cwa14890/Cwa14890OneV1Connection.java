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
package es.gob.jmulticard.apdu.connection.cwa14890;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import es.gob.jmulticard.CryptoHelper;
import es.gob.jmulticard.HexUtils;
import es.gob.jmulticard.apdu.CommandApdu;
import es.gob.jmulticard.apdu.ResponseApdu;
import es.gob.jmulticard.apdu.StatusWord;
import es.gob.jmulticard.apdu.connection.ApduConnection;
import es.gob.jmulticard.apdu.connection.ApduConnectionException;
import es.gob.jmulticard.apdu.connection.ApduConnectionProtocol;
import es.gob.jmulticard.apdu.connection.ApduEncrypter;
import es.gob.jmulticard.apdu.connection.ApduEncrypterDes;
import es.gob.jmulticard.apdu.connection.CardConnectionListener;
import es.gob.jmulticard.card.cwa14890.Cwa14890Card;
import es.gob.jmulticard.card.cwa14890.Cwa14890PrivateConstants;
import es.gob.jmulticard.card.cwa14890.Cwa14890PublicConstants;

/** Clase para el establecimiento y control del canal seguro con tarjeta inteligente.
 * @author Carlos Gamuci */
public class Cwa14890OneV1Connection implements Cwa14890Connection {

	private static final int SHA1_LENGTH = 20;
	private static final int KICC_LENGTH = 32;
	private static final int KIFD_LENGTH = 32;
	private static final byte ISO_9796_2_PADDING_START = (byte) 0x6a;
	private static final byte ISO_9796_2_PADDING_END = (byte) 0xbc;

	private static final StatusWord INVALID_CRYPTO_CHECKSUM = new StatusWord((byte)0x66, (byte)0x88);

	/** Byte de valor m&aacute;s significativo que indica un Le incorrecto en la petici&oacute;n. */
	private static final byte MSB_INCORRECT_LE = (byte) 0x6C;

	/** Byte de valor m&aacute;s significativo que indica un Le incorrecto en la petici&oacute;n. */
	private static final byte MSB_INCORRECT_LE_PACE = (byte) 0x62;

    /** C&oacute;digo auxiliar para el c&aacute;lculo de la clave Kenc del canal seguro. */
    private static final byte[] SECURE_CHANNEL_KENC_AUX = new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01
    };

    /** C&oacute;digo auxiliar para el c&aacute;lculo de la clave Kmac del canal seguro. */
    private static final byte[] SECURE_CHANNEL_KMAC_AUX = new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02
    };

    /** Helper para la ejecuci&oacute;n de funciones criptogr&aacute;ficas. */
    protected final CryptoHelper cryptoHelper;

    /** Tarjeta CWA-14890 con la que se desea establecer el canal seguro. */
    private Cwa14890Card card;

    /** Conexi&oacute;n subyacente para el env&iacute;o de APDUs. */
    protected ApduConnection subConnection;

    /** Clave Triple DES (TDES o DESEDE) para encriptar y desencriptar criptogramas. */
    private byte[] kenc = null;

    /** Clave Triple DES (TDES o DESEDE) para calcular y verificar checksums. */
    private byte[] kmac = null;

    /** Contador de secuencia. */
    private byte[] ssc = null;

    /** Indica el estado de la conexi&oacute;n. */
    protected boolean openState = false;

    protected final ApduEncrypter apduEncrypter;

    private Cwa14890PublicConstants pubConsts;
    private Cwa14890PrivateConstants privConsts;


    @SuppressWarnings("static-method")
	protected ApduEncrypter instantiateApduEncrypter() {
    	return new ApduEncrypterDes();
    }

    /** Crea el canal seguro CWA-14890 para la comunicaci&oacute;n de la tarjeta. Es necesario abrir el
     * canal asoci&aacute;ndolo a una conexi&oacute;n para poder trasmitir APDUs. Si no se indica una conexi&oacute;n
     * se utilizar&aacute;a la conexi&oacute;n implicita de la tarjeta indicada.
     * @param connection Conexi&oacute;n sobre la cual montar el canal seguro.
     * @param cryptoHelper Motor de operaciones criptogr&aacute;ficas. */
    public Cwa14890OneV1Connection(final ApduConnection connection,
    		                       final CryptoHelper cryptoHelper) {

        if (cryptoHelper == null) {
            throw new IllegalArgumentException(
        		"CryptoHelper no puede ser nulo" //$NON-NLS-1$
            );
        }

    	this.subConnection = connection instanceof Cwa14890Connection ?
			((Cwa14890Connection)connection).getSubConnection() :
				connection;
        this.cryptoHelper = cryptoHelper;
    	this.apduEncrypter = instantiateApduEncrypter();
    }

    /** Crea el canal seguro CWA-14890 para la comunicaci&oacute;n de la tarjeta. Es necesario abrir el
     * canal asoci&aacute;ndolo a una conexi&oacute;n para poder trasmitir APDUs. Si no se indica una conexi&oacute;n
     * se utilizar&aacute;a la conexi&oacute;n implicita de la tarjeta indicada.
     * @param card Tarjeta con la funcionalidad CWA-14890.
     * @param connection Conexi&oacute;n sobre la cual montar el canal seguro.
     * @param cryptoHelper Motor de operaciones criptogr&aacute;ficas.
     * @param cwaConsts Clase de claves p&uacute;blicas CWA-14890.
     * @param cwaPrivConsts Clase de claves privadas CWA-14890. */
    public Cwa14890OneV1Connection(final Cwa14890Card card,
    		                       final ApduConnection connection,
    		                       final CryptoHelper cryptoHelper,
    		                       final Cwa14890PublicConstants cwaConsts,
    		                       final Cwa14890PrivateConstants cwaPrivConsts) {

        if (card == null) {
            throw new IllegalArgumentException(
        		"No se ha proporcionado la tarjeta CWA-14890 con la que abrir el canal seguro" //$NON-NLS-1$
            );
        }

        if (cryptoHelper == null) {
            throw new IllegalArgumentException(
        		"CryptoHelper no puede ser nulo" //$NON-NLS-1$
            );
        }

        if (cwaConsts == null) {
        	throw new IllegalArgumentException(
        		"las claves CWA-14890 no pueden ser nulas" //$NON-NLS-1$
            );
        }

        this.card = card;
        this.subConnection = connection instanceof Cwa14890Connection ?
			((Cwa14890Connection)connection).getSubConnection() :
				connection;
        this.cryptoHelper = cryptoHelper;
    	this.apduEncrypter = instantiateApduEncrypter();
    	this.pubConsts = cwaConsts;
    	this.privConsts = cwaPrivConsts;
    }


	/** Abre el canal seguro con la tarjeta. La conexi&oacute;n se reiniciar&aacute; previamente
     * a la apertura del canal. */
    @Override
    public void open() throws ApduConnectionException {

        final ApduConnection conn = this.subConnection;
		conn.open();

        // Obtenemos el numero de serie de la tarjeta.
        // IMPORTANTE: Esta operacion debe realizarse antes del inicio del proceso de autenticacion
        final byte[] serial = getPaddedSerial();

        // --- STAGE 1 ---
        // Verificamos el certificado de la tarjeta.
        // ---------------

        try {
            this.card.verifyCaIntermediateIcc();
            this.card.verifyIcc();
        }
        catch (final SecurityException e) {
            conn.close();
            throw new IllegalStateException(
        		"Condicion de seguridad no satisfecha en la validacion de los certificados CWA-14890: " + e //$NON-NLS-1$
            );
        }
        catch (final CertificateException e) {
            conn.close();
            throw new IllegalStateException(
        		"No se han podido tratar los certificados CWA-14890: " + e//$NON-NLS-1$
            );
        }
        catch (final IOException e) {
            conn.close();
            throw new IllegalStateException(
        		"No se han podido validar los certificados CWA-14890: " + e//$NON-NLS-1$
            );
        }

        // Clave publica del certificado de componente de la tarjeta. Necesario para autenticacion interna
        // y externa
        final RSAPublicKey iccPublicKey;
        try {
            iccPublicKey = (RSAPublicKey) this.cryptoHelper.generateCertificate(
        		this.card.getIccCertEncoded()
    		).getPublicKey();
        }
        catch (final CertificateException e) {
            conn.close();
            throw new ApduConnectionException(
        		"No se pudo obtener la clave publica del certificado de componente: " + e, e //$NON-NLS-1$
            );
        }
        catch (final IOException e) {
        	conn.close();
            throw new ApduConnectionException(
        		"No se pudo leer certificado de componente: " + e, e //$NON-NLS-1$
            );
		}

        // --- STAGE 2 ---
        // Permitimos que la tarjeta verifique la cadena de certificacion del controlador.
        // ---------------
        try {
            this.card.verifyIfdCertificateChain(this.pubConsts);
        }
        catch (final Exception e) {
            conn.close();
            throw new ApduConnectionException(
        		"Error al verificar la cadena de certificados del controlador: " + e, e //$NON-NLS-1$
    		);
        }

        // --- STAGE 3 ---
        // Autenticacion interna (El driver comprueba la tarjeta)
        // ---------------
        final byte[] randomIfd;
        try {
            randomIfd = this.cryptoHelper.generateRandomBytes(8);
        }
        catch (final IOException e1) {
            conn.close();
            throw new SecureChannelException(
        		"No se pudo generar el array de aleatorios: " + e1, e1 //$NON-NLS-1$
    		);
        }

        final byte[] kicc;
        try {
            kicc = this.internalAuthentication(randomIfd, iccPublicKey);
        }
        catch (final Exception e) {
            conn.close();
            throw new ApduConnectionException(
        		"Error durante el proceso de autenticacion interna de la tarjeta: " + e, e //$NON-NLS-1$
    		);
        }

        // --- STAGE 4 ---
        // Autenticacion externa (La tarjeta comprueba el driver)
        // ---------------
        final byte[] randomIcc = this.card.getChallenge();
        final byte[] kifd;
        try {
            kifd = this.externalAuthentication(serial, randomIcc, iccPublicKey);
        }
        catch (final Exception e) {
            conn.close();
            throw new ApduConnectionException(
        		"Error durante el proceso de autenticacion externa de la tarjeta", e //$NON-NLS-1$
    		);
        }

        // --- STAGE 5 ---
        // Esta fase no pertenece al procedimiento de apertura del canal seguro (ya esta
        // establecido), sino a la obtencion de las claves necesarias para su control. Estas
        // son:
        // - Kenc: Clave TripleDES (TDES o DESEDE) para encriptar y desencriptar criptogramas.
        // - Kmac: Clave TripleDES (TDES o DESEDE) para calcular y verificar checksums.
        // - SSC: Contador de secuencia.
        // ---------------

        // Calculamos Kifdicc como el XOR de los valores Kifd y Kicc
        final byte[] kidficc = HexUtils.xor(kicc, kifd);
        try {
            this.kenc = generateKenc(kidficc);
        }
        catch (final IOException e) {
            conn.close();
            throw new ApduConnectionException(
        		"Error al generar la clave Kenc para el tratamiento del canal seguro", e //$NON-NLS-1$
            );
        }

        try {
            this.kmac = generateKmac(kidficc);
        }
        catch (final IOException e) {
            conn.close();
            throw new ApduConnectionException(
        		"Error al generar la clave Kmac para el tratamiento del canal seguro", e //$NON-NLS-1$
            );
        }

        this.ssc = generateSsc(randomIfd, randomIcc);

        this.openState = true;
    }

    /** Genera la clave KENC para encriptar y desencriptar criptogramas.
     * @param kidficc XOR de los valores Kifd y Kicc.
     * @return Clave TripleDES.
     * @throws IOException Cuando no puede generarse la clave. */
    private byte[] generateKenc(final byte[] kidficc) throws IOException {
        // La clave de cifrado Kenc se obtiene como los 16 primeros bytes del hash SHA-1 de la
        // concatenacion de kifdicc con el valor "00 00 00 01" (SECURE_CHANNEL_KENC_AUX).
    	final byte[] kidficcConcat = HexUtils.concatenateByteArrays(kidficc, SECURE_CHANNEL_KENC_AUX);


        final byte[] keyEnc = new byte[16];
        System.arraycopy(
    		this.cryptoHelper.digest(
				CryptoHelper.DigestAlgorithm.SHA1,
				kidficcConcat
			),
			0,
			keyEnc,
			0,
			keyEnc.length
		);

        return keyEnc;
    }

    /** Genera la clave KMAC para calcular y verificar checksums.
     * @param kidficc XOR de los valores Kifd y Kicc.
     * @return Clave TripleDES.
     * @throws IOException Cuando no puede generarse la clave. */
    private byte[] generateKmac(final byte[] kidficc) throws IOException {
        // La clave para el calculo del MAC Kmac se obtiene como los 16 primeros bytes
        // del hash SHA-1 de la concatenacion de kifdicc con el valor "00 00 00 02" (SECURE_CHANNEL_KMAC_AUX).
        final byte[] kidficcConcat = HexUtils.concatenateByteArrays(kidficc, SECURE_CHANNEL_KMAC_AUX);

        final byte[] keyMac = new byte[16];
        System.arraycopy(
    		this.cryptoHelper.digest(
				CryptoHelper.DigestAlgorithm.SHA1,
				kidficcConcat
			),
    		0,
    		keyMac,
    		0,
    		keyMac.length
		);

        return keyMac;
    }

    /** Genera el contador de secuencia SSC a partir de los semillas aleatorias calculadas
     * en los procesos de autenticacion interna y externa.
     * @param randomIfd Aleatorio del desaf&iacute;o del Terminal.
     * @param randomIcc Aleatorio del desaf&iacute;o de la tarjeta.
     * @return Contador de secuencia. */
    private static byte[] generateSsc(final byte[] randomIfd, final byte[] randomIcc) {
        // El contador de secuencia SSC se obtiene concatenando los 4 bytes menos
        // significativos del desafio de la tarjeta (RND.ICC) con los 4 menos
        // significativos del desafio del Terminal (RND.IFD)
        final byte[] ssc = new byte[8];
        System.arraycopy(randomIcc, 4, ssc, 0, 4);
        System.arraycopy(randomIfd, 4, ssc, 4, 4);

        return ssc;
    }

    /** Lleva a cabo el proceso de autenticaci&oacute;n interna de la tarjeta mediante el
     * cual el controlador comprueba la tarjeta.
     * @param randomIfd Array de 8 bytes aleatorios.
     * @param iccPublicKey Clava p&uacute;blica del certificado de componente.
     * @return Semilla de 32 [KICC_LENGTH] bits, generada por la tarjeta, para la derivaci&oacute;n de
     *         claves del canal seguro.
     * @throws SecureChannelException Cuando ocurre un error en el establecimiento de claves.
     * @throws ApduConnectionException Cuando ocurre un error en la comunicaci&oacute;n con la tarjeta.
     * @throws IOException Cuando ocurre un error en el cifrado/descifrado de los mensajes. */
    private byte[] internalAuthentication(final byte[] randomIfd,
    		                              final RSAPublicKey iccPublicKey) throws SecureChannelException,
                                                                                  ApduConnectionException,
                                                                                  IOException {
        // Seleccionamos la clave publica del certificado de Terminal a la vez
        // que aprovechamos para seleccionar la clave privada de componente para autenticar
        // este certificado de Terminal
        try {
            this.card.setKeysToAuthentication(
        		this.card.getChrCCvIfd(this.pubConsts),
        		this.card.getRefIccPrivateKey(this.pubConsts)
    		);
        }
        catch (final Exception e) {
            throw new SecureChannelException(
        		"Error durante el establecimiento de la clave " + //$NON-NLS-1$
                "publica de Terminal y la privada de componente para su atenticacion", e //$NON-NLS-1$
            );
        }

        // Iniciamos la autenticacion interna de la clave privada del certificado de componente
        final byte[] sigMinCiphered = this.card.getInternalAuthenticateMessage(
    		randomIfd,
    		this.card.getChrCCvIfd(this.pubConsts)
		);

        // Esta respuesta de la tarjeta es un mensaje:
        // - Cifrado con la clave privada de componente de la tarjeta
        // - Al que se le ha aplicado la funcion SIGMIN
        // - Y que se ha cifrado con la clave publica del Terminal
        // Para obtener el mensaje original deberemos deshacer cada una de estas operaciones en
        // sentido inverso.
        // El resultado sera correcto si empieza por el byte 0x6a [ISO_9796_2_PADDING_START] (ISO 9796-2, DS scheme 1) y
        // termina con el byte 0xbc [ISO_9796_2_PADDING_END] (ISO-9796-2, Option 1).

        // -- Descifrado con la clave privada del Terminal
        final byte[] sigMin = this.cryptoHelper.rsaDecrypt(
    		sigMinCiphered,
    		this.card.getIfdPrivateKey(this.privConsts)
		);

        // Este resultado es el resultado de la funcion SIGMIN que es minimo de SIG (los
        // datos sobre los que se ejecuto la funcion) y N.ICC-SIG.
        // Debemos averiguar cual de los dos es. Empezamos por comprobar si es SIG con lo que no
        // habra que deshacer la funcion y podemos descifrar directamente con la clave publica del
        // certificado de componente de la tarjeta.

        final byte[] sig = sigMin;
        byte[] desMsg = this.cryptoHelper.rsaEncrypt(sig, iccPublicKey);

        // Si el resultado no empieza por 0x6a [ISO_9796_2_PADDING_START] y termina por
        // 0xbc [ISO_9796_2_PADDING_END] (Valores definidos en la ISO 9796-2), se considera que
        // es erroneo y deberemos probar la segunda opcion.
        // Esto es, calcular N.ICC-SIG y volver a descifrar con la clave publica del
        // certificado de componente

        // Comprobamos que empiece por 0x6a [ISO_9796_2_PADDING_START] y termine con 0xbc [ISO_9796_2_PADDING_END]
        if (desMsg[0] != ISO_9796_2_PADDING_START || desMsg[desMsg.length - 1] != ISO_9796_2_PADDING_END) {

            // Calculamos N.ICC-SIG
            final byte[] sub = iccPublicKey.getModulus().subtract(new BigInteger(sigMin)).toByteArray();
            final byte[] niccMinusSig = new byte[this.card.getIfdKeyLength(this.pubConsts)];
            // Ignoramos los ceros de la izquierda
            if (sub.length > this.card.getIfdKeyLength(this.pubConsts) && sub[0] == (byte) 0x00) {
                System.arraycopy(sub, 1, niccMinusSig, 0, sub.length - 1);
            }
            else {
                System.arraycopy(sub, 0, niccMinusSig, 0, sub.length);
            }

            // Desciframos el mensaje con N.ICC-SIG
            desMsg = this.cryptoHelper.rsaDecrypt(niccMinusSig, iccPublicKey);

            // Si en esta ocasion no empieza por 0x6a [ISO_9796_2_PADDING_START] y termina con 0xbc [ISO_9796_2_PADDING_END],
            // la autenticacion interna habra fallado
            if (desMsg[0] != ISO_9796_2_PADDING_START || desMsg[desMsg.length - 1] != ISO_9796_2_PADDING_END) {
                throw new SecureChannelException(
            		"Error en la autenticacion interna para el establecimiento del canal seguro. " + //$NON-NLS-1$
                    "El mensaje descifrado es:\n" + HexUtils.hexify(desMsg, true) //$NON-NLS-1$
                );
            }
        }

        // -- Descomponemos el resultado anterior en sus partes:
        // Byte 0: Relleno segun ISO 9796-2 (DS scheme 1)
        // Bytes [PRND1] Bytes de relleno aleatorios para completar la longitud de la clave RSA
        // Bytes [Kicc] Semilla de 32 [KICC_LENGTH] bytes generada por la tarjeta para la derivacion de claves
        // Bytes [h: PRND1||Kicc||RND.IFD||SN.IFD] Hash SHA1
        // Ultimo Byte: Relleno segun ISO-9796-2 (option 1)
        final byte[] prnd1 = new byte[this.card.getIfdKeyLength(this.pubConsts) - KICC_LENGTH - SHA1_LENGTH - 2];
        System.arraycopy(
    		desMsg,
    		1,
    		prnd1,
    		0,
    		prnd1.length
		);

        final byte[] kicc = new byte[KICC_LENGTH];
        System.arraycopy(
    		desMsg,
    		prnd1.length + 1,
    		kicc,
    		0,
    		kicc.length
		);

        final byte[] hash = new byte[SHA1_LENGTH];
        System.arraycopy(
    		desMsg,
    		prnd1.length + kicc.length + 1,
    		hash,
    		0,
    		hash.length
		);

        // -- Calculamos el hash para la comprobacion de la autenticacion, si coincide con el hash
        // extraido en el paso anterior, se confirma que se ha realizado correctamente

        // El hash se calcula a partir de la concatenacion de:
        // - PRND1: Extraido del paso anterior
        // - Kicc: Extraido del paso anterior
        // - RND.IFD: Numero aleatorio generado en pasos anteriores
        // - SN.IFD: CHR del IFD
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(prnd1);
        baos.write(kicc);
        baos.write(randomIfd);
        baos.write(this.card.getChrCCvIfd(this.pubConsts));

        final byte[] calculatedHash = this.cryptoHelper.digest(
    		CryptoHelper.DigestAlgorithm.SHA1,
    		baos.toByteArray()
		);
        if (!HexUtils.arrayEquals(hash, calculatedHash)) {
            throw new SecureChannelException(
        		"Error en la comprobacion de la clave de autenticacion interna. Se obtuvo el hash '" + //$NON-NLS-1$
                     HexUtils.hexify(calculatedHash, false)
                         + "' cuando se esperaba: '" + HexUtils.hexify(hash, false) + "'" //$NON-NLS-1$ //$NON-NLS-2$
            );
        }

        return kicc;
    }

    /** Lleva a cabo el proceso de autenticaci&oacute;n externa mediante el cual la tarjeta
     * comprueba el controlador.
     * @param serial Numero de serie de la tarjeta.
     * @param randomIcc Array de 8 bytes aleatorios generados por la tarjeta.
     * @param iccPublicKey Clava p&uacute;blica del certificado de componente.
     * @return Semilla de 32 [KIFD_LENGTH] bytes, generada por el Terminal, para la derivacion de claves del
     *         canal seguro.
     * @throws es.gob.jmulticard.apdu.connection.cwa14890.SecureChannelException Cuando ocurre un error en el establecimiento de claves.
     * @throws es.gob.jmulticard.apdu.connection.ApduConnectionException Cuando ocurre un error en la comunicaci&oacute;n con
     *         la tarjeta.
     * @throws IOException Cuando ocurre un error en el cifrado/descifrado de los mensajes. */
    private byte[] externalAuthentication(final byte[] serial,
    		                              final byte[] randomIcc,
    		                              final RSAPublicKey iccPublicKey) throws IOException {

        // Construimos el campo de datos para el comando "External authentication" de acuerdo
        // al siguiente formato:
        // ----------------------
        // E[PK.ICC.AUT](SIGMIN)
        //
        // Donde:
        // SIGMIN = min (SIG, N.IFD - SIG)
        // y
        // SIG= DS[SK.IFD.AUT]
        // (
        // "6A" = relleno segun ISO 9796-2 (DS scheme 1)
        // PRND2 ="XX ... XX" bytes de relleno aleatorios generados por el terminal. La longitud
        // debe ser la necesaria para que la longitud desde "6A" hasta "BC" coincida con
        // la longitud de la clave RSA
        // KIFD = Semilla de 32 [KIFD_LENGTH] bytes, generada por el terminal, para la derivacion
    	// de claves del canal seguro.
        // h[PRND2 || KIFD || RND.ICC || SN.ICC ] = hash SHA1 que incluye los datos aportados por
        // la tarjeta y por el terminal
        // "BC" = relleno segun ISO 9796-2 (option 1)
        // )
        // ----------------------

        // Generamos PRN2 y Kifd como valores aleatorios de la longitud apropiada
        final byte[] prnd2 = this.cryptoHelper.generateRandomBytes(
    		this.card.getIfdKeyLength(this.pubConsts) - 2 - KIFD_LENGTH - SHA1_LENGTH
		);
        final byte[] kifd = this.cryptoHelper.generateRandomBytes(KIFD_LENGTH);

        // Calculamos el hash que incorporaremos al mensaje a partir de los siguientes
        // datos concatenados:
        // - PRND2
        // - Kifd
        // - RND.ICC
        // - SN.ICC (Numero de serie del Chip, extraido del Chip Info). Debe tener 8 bytes.
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(prnd2);
        baos.write(kifd);
        baos.write(randomIcc);
        baos.write(serial);

        final byte[] hash = this.cryptoHelper.digest(
    		CryptoHelper.DigestAlgorithm.SHA1,
    		baos.toByteArray()
		);

        // Construimos el mensaje para el desafio a la tarjeta. Este estara compuesto por:
        // Byte 0: 0x6a [ISO_9796_2_PADDING_START] - Relleno segun ISO 9796-2 (DS scheme 1)
        // Bytes [PRND2] Bytes de relleno aleatorios para completar la longitud de la clave RSA
        // Bytes [Kifd] Semilla de 32 [KICC_LENGTH] bytes generada por la tarjeta para la derivacion de claves
        // Bytes [h: PRND2||Kifd||RND.ICC||SN.ICC] Hash SHA1
        // Ultimo Byte: 0xbc [ISO_9796_2_PADDING_END] - Relleno segun ISO-9796-2 (option 1)
        baos.reset();
        baos.write(ISO_9796_2_PADDING_START);
        baos.write(prnd2);
        baos.write(kifd);
        baos.write(hash);
        baos.write(ISO_9796_2_PADDING_END);

        final byte[] msg = baos.toByteArray();
        final RSAPrivateKey ifdPrivateKey = this.card.getIfdPrivateKey(this.privConsts);

        // Ciframos con la clave privada del terminal
        final byte[] sig = this.cryptoHelper.rsaDecrypt(msg, ifdPrivateKey);

        // Calculamos N.IFD-SIG para obtener SIGMIN (el menor de SIG y N.IFD-SIG)
        final BigInteger biSig = new BigInteger(1, sig);
        final byte[] sigMin = ifdPrivateKey.getModulus().subtract(biSig).min(biSig).toByteArray();

        // Ciframos con la clave publica de componente de la tarjeta
        final byte[] extAuthenticationData = this.cryptoHelper.rsaEncrypt(sigMin, iccPublicKey);

        final boolean valid = this.card.externalAuthentication(extAuthenticationData);
        if (!valid) {
            throw new SecureChannelException(
        		"Error durante la autenticacion externa del canal seguro" //$NON-NLS-1$
            );
        }

        return kifd;
    }

    /** Obtiene el numero de serie de la tarjeta en un array de 8 bytes, completando
     * con ceros a la izquierda si es necesario.
     * @return N&uacute;mero de serie en formato de 8 bytes.
     * @throws ApduConnectionException Cuando ocurre un error en la comunicaci&oacute;n con
     *         la tarjeta. */
    private byte[] getPaddedSerial() throws ApduConnectionException {
        // Completamos el numero de serie (SN.ICC) para que tenga 8 bytes
        final byte[] serial = this.card.getSerialNumber();
        byte[] paddedSerial = serial;
        if (paddedSerial.length < 8) {
            paddedSerial = new byte[8];
            int i;
            for (i = 0; i < 8 - serial.length; i++) {
                paddedSerial[i] = (byte) 0x00;
            }
            System.arraycopy(serial, 0, paddedSerial, i, serial.length);
        }
        return paddedSerial;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws ApduConnectionException {
    	if (this.openState) {
    		this.subConnection.close();
    		this.openState = false;
    	}
    }

    /** {@inheritDoc} */
    @Override
    public ResponseApdu transmit(final CommandApdu command) throws ApduConnectionException {

        final CommandApdu protectedApdu;
        try {
        	this.ssc = increment(this.ssc);
            protectedApdu = this.apduEncrypter.protectAPDU(
        		command,
        		this.kenc,
        		this.kmac,
        		this.ssc,
        		this.cryptoHelper
    		);
        }
        catch (final IOException e) {
            throw new SecureChannelException(
        		"Error en la encriptacion de la APDU para su envio por el canal seguro: " + e, e //$NON-NLS-1$
            );
        }

        final ResponseApdu responseApdu = this.subConnection.transmit(protectedApdu);
        if (INVALID_CRYPTO_CHECKSUM.equals(responseApdu.getStatusWord())) {
        	throw new InvalidCryptographicChecksum();
        }

        // Desencriptamos la respuesta
        try {
        	this.ssc = increment(this.ssc);
        	final ResponseApdu decipherApdu = this.apduEncrypter.decryptResponseApdu(
    			responseApdu,
    			this.kenc,
    			this.ssc,
    			this.kmac,
    			this.cryptoHelper
			);

            // Si la APDU descifrada indicase que no se indico bien el tamano de la respuesta, volveriamos
            // a enviar el comando indicando la longitud correcta
            if (decipherApdu.getStatusWord().getMsb() == MSB_INCORRECT_LE) {
            	command.setLe(decipherApdu.getStatusWord().getLsb());
            	return transmit(command);
            }
            else if (decipherApdu.getStatusWord().getMsb() == MSB_INCORRECT_LE_PACE) {
            	command.setLe(command.getLe().intValue()-1);
            	return transmit(command);
            }
            return decipherApdu;
        }
        catch (final Exception e) {
            throw new ApduConnectionException(
        		"Error en la desencriptacion de la APDU de respuesta recibida por el canal seguro: " + e, e //$NON-NLS-1$
            );
		}
    }

    /** {@inheritDoc} */
    @Override
    public byte[] reset() throws ApduConnectionException {

        this.openState = false;

        // Reseteamos para obtener el ATR de la tarjeta
        final byte[] atr = this.subConnection.reset();

        // Volvemos a abrir la conexion
        this.open();

        return atr;
    }

    /** {@inheritDoc} */
    @Override
    public void addCardConnectionListener(final CardConnectionListener ccl) {
        this.subConnection.addCardConnectionListener(ccl);
    }

    /** {@inheritDoc} */
    @Override
    public void removeCardConnectionListener(final CardConnectionListener ccl) {
        this.subConnection.removeCardConnectionListener(ccl);
    }

    /** {@inheritDoc} */
    @Override
    public long[] getTerminals(final boolean onlyWithCardPresent) throws ApduConnectionException {
        return this.subConnection.getTerminals(onlyWithCardPresent);
    }

    /** {@inheritDoc} */
    @Override
    public String getTerminalInfo(final int terminal) throws ApduConnectionException {
        return this.subConnection.getTerminalInfo(terminal);
    }

    /** {@inheritDoc} */
    @Override
    public void setTerminal(final int t) {
        this.subConnection.setTerminal(t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOpen() {
        return this.openState && this.subConnection.isOpen();
    }


    /** Calcula y devuelve el valor entregado m&aacute;s 1.
     * @param data Datos a incrementar.
     * @return Valor incrementado. */
    private static byte[] increment(final byte[] data) {
        BigInteger bi = new BigInteger(1, data);
        bi = bi.add(BigInteger.ONE);

        final byte[] biArray = bi.toByteArray();
        if (biArray.length > 8) {
        	final byte[] incrementedValue = new byte[8];
        	System.arraycopy(
    			biArray,
    			biArray.length - incrementedValue.length,
    			incrementedValue,
    			0,
    			incrementedValue.length
			);
        	return incrementedValue;
        }
        else if (biArray.length < 8) {
        	final byte[] incrementedValue = new byte[8];
        	System.arraycopy(
    			biArray,
    			0,
    			incrementedValue,
    			incrementedValue.length - biArray.length,
    			biArray.length
			);
        	return incrementedValue;
        }
        return biArray;
    }

    @Override
	public ApduConnection getSubConnection() {
    	return this.subConnection;
    }

	@Override
	public void setProtocol(final ApduConnectionProtocol p) {
		if (this.subConnection != null) {
			this.subConnection.setProtocol(p);
		}
	}

}