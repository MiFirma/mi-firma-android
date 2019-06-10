package com.mifirma.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/** Constantes y utilidades de la app.
 * @author Javier */
public class Utils {

    public static final String CONFIG_FILE = "mifirma.properties"; //$NON-NLS-1$
    public static final String CONFIG_FILE_KEY_PROPOSALS = "proposalsurl"; //$NON-NLS-1$
    public static final String CONFIG_FILE_KEY_SIGNATURE = "signaturesurl"; //$NON-NLS-1$
    public static final String CONFIG_FILE_KEY_IMAGES_BASE_URL = "imagesbaseurl"; //$NON-NLS-1$
    public static final String CONTENT_TYPE = "application/xml";
    public static final String ACCEPT = "text/xml";
    public static final String URL_PARAM_BEGIN = "?";
    public static final String URL_SEPARATOR = "/";
    public static final String SHARE = "/share";
    public static final String HOME_TAG = "home";

    /** Verifica si hay conexion con internet.
     * @param ctx contexto desde el que hacemos la comprobacion
     * @return true si hay conexion, false en caso contrario.
     */
    public static boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        for (int i = 0; i < 2; i++) {
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }

    /** Analiza si una cadena es vac&iacute;a.
     * @param cs Cadena a comprobar.
     * @return <code>true</code> si la cadena es vac&iacute;a, <code>false</code> en
     *         caso contrario. */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
