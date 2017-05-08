package com.mifirma.android.logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.mifirma.android.R;
import com.mifirma.android.fragments.Success;
import com.mifirma.android.keystore.DniDialog;
import com.mifirma.android.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Properties;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.http.UrlHttpManagerFactory;
import es.gob.afirma.core.misc.http.UrlHttpMethod;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.signers.xadestri.client.AOXAdESTriPhaseSigner;

/** Tarea que ejecuta una firma electr&oacute;nica XAdES trif&aacute;sica.
 * La firma es una operaci&oacute;n que necesariamente debe ejecutarse en segundo
 * plano ya que hace conexiones de red.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public class SignTask extends AsyncTask<Ilp, Void, byte[]>{

    private static final String COM_MIFIRMA_ANDROID = "com.mifirma.android"; //$NON-NLS-1$

    private Activity context;
    private final PrivateKeyEntry pke;
    private final Properties extraParams;
    private final String urlSign;

    private ProgressDialog progDailog;
    /** Construye la tarea encargada de realizar la operaci&oacute;n criptogr&aacute;fica.
     * @param context Contexto de la aplicaci&oacute;n.
     * @param pke Clave privada para la firma.
     * @param extraParams Par&aacute;metros adicionales para la configuraci&oacute;n de la firma.*/
    public SignTask(final Activity context,
                    final PrivateKeyEntry pke,
                    final Properties extraParams,
                    final ProgressDialog progDialog) throws IOException {
        this.context = context;
        this.pke = pke;
        this.extraParams = extraParams != null ? extraParams : new Properties();
        this.progDailog = progDialog;

        final Properties p = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(Utils.CONFIG_FILE);
        p.load(inputStream);

        final String urlProp = p.getProperty(Utils.CONFIG_FILE_KEY_PROPOSALS);
        if (urlProp == null) {
            throw new IOException(
                    "El fichero de configuracion no contiene la URL: " + Utils.CONFIG_FILE_KEY_PROPOSALS //$NON-NLS-1$
            );
        }

        urlSign = p.getProperty(Utils.CONFIG_FILE_KEY_SIGNATURE);
        if (urlSign == null) {
            throw new IOException(
                    "El fichero de configuracion no contiene la URL: " + Utils.CONFIG_FILE_KEY_PROPOSALS //$NON-NLS-1$
            );
        }
    }


    @Override
    protected byte[] doInBackground(final Ilp... params) {
        byte[] result = new byte[0];
        final String proposalToBeRegistered = params[0].toString();
        final byte[] registerResponseBytes;
        try {
            registerResponseBytes = UrlHttpManagerFactory.getInstalledManager().readUrl(urlSign + Utils.URL_PARAM_BEGIN + proposalToBeRegistered,
                    -1, Utils.CONTENT_TYPE, Utils.ACCEPT, UrlHttpMethod.POST);
            Log.d(COM_MIFIRMA_ANDROID, "Resultado operación de registro: " + new String(registerResponseBytes));
        } catch (IOException e) {
            Log.e(COM_MIFIRMA_ANDROID, "Error durante la operación de registro: " + e); //$NON-NLS-1$
            return null;
        }

        final String token;

        try {
            token = IlpResponse.getToken(registerResponseBytes);
            Log.d(COM_MIFIRMA_ANDROID, "Token obtenido: " + token);
        } catch (IlpResponse.IlpResponseException e) {
            Log.e(COM_MIFIRMA_ANDROID, "Registro erroneo: " + e); //$NON-NLS-1$
            return null;
        }


        final byte[] xmlToBeSigned;
        try {
            xmlToBeSigned = UrlHttpManagerFactory.getInstalledManager().readUrl(urlSign + Utils.URL_SEPARATOR + token, -1, Utils.CONTENT_TYPE, Utils.ACCEPT, UrlHttpMethod.GET);
            Log.d(COM_MIFIRMA_ANDROID, "XML que debe ser firmado: " + new String(xmlToBeSigned));
        } catch (IOException e) {
            Log.e(COM_MIFIRMA_ANDROID, "Error durante el envío del token: " + e); //$NON-NLS-1$
            return null;
        }

        final AOSigner signer = new AOXAdESTriPhaseSigner();

        // Generacion de la firma
        try {
            // Ejecutamos la operacion pertinente.
            if(params[0] != null) {
                result =  signer.sign(
                        xmlToBeSigned,
                        "SHA512withRSA", //$NON-NLS-1$
                        this.pke.getPrivateKey(),
                        this.pke.getCertificateChain(),
                        this.extraParams
                );
                Log.d(COM_MIFIRMA_ANDROID, "Generación de la firma: " + new String(result));
            }
        }
        catch (final AOException e) {
            Log.e(COM_MIFIRMA_ANDROID, "Error durante la operacion de firma: " + e); //$NON-NLS-1$
            return null;
        }
        catch (final Exception e) {
            Log.e(COM_MIFIRMA_ANDROID, "Error en la firma: " + e); //$NON-NLS-1$
            return null;
        }

        final String signedXmlMessage = "<xmlSigned2>" + Base64.encode(result) + "</xmlSigned2>";

        final byte[] signedIlpResponse;
        try {
            signedIlpResponse = UrlHttpManagerFactory.getInstalledManager().readUrl(urlSign+ Utils.URL_SEPARATOR + token + Utils.SHARE + Utils.URL_PARAM_BEGIN + signedXmlMessage, -1, Utils.CONTENT_TYPE, Utils.ACCEPT, UrlHttpMethod.PUT);
            Log.d(COM_MIFIRMA_ANDROID,"Resultado del último paso de la firma: " + new String(signedIlpResponse));
        } catch (IOException e) {
            Log.e(COM_MIFIRMA_ANDROID, "Error durante último paso de la firma: " + e); //$NON-NLS-1$
            return null;
        }
        return signedIlpResponse;
    }

    @Override
    protected void onPostExecute(final byte[] signedIlpResponse) {
        super.onPostExecute(signedIlpResponse);
        if(progDailog != null){
            progDailog.dismiss();
        }
        if(DniDialog.getInstance() != null){
            DniDialog.getInstance().dismiss();
        }

        if (signedIlpResponse != null) {
            if ("OK".equalsIgnoreCase(new String(signedIlpResponse).trim())) {
                // OK
                final Fragment newFragment = new Success();

                FragmentTransaction transaction = context.getFragmentManager().beginTransaction();

                transaction.replace(R.id.container, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final String errorMsg = new String(signedIlpResponse).trim();
                builder.setTitle("Error:")
                        .setMessage(errorMsg)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                builder.show();
            }
        }
        else {
            // ERROR RESP NULA
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle("Error:")
                    .setMessage(R.string.sign_error)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

            builder.show();
        }
    }
}
