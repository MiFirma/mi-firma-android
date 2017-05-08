package com.mifirma.android.logic;

import android.app.ProgressDialog;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mifirma.android.MainActivity;
import com.mifirma.android.keystore.CanResult;
import com.mifirma.android.keystore.DniDialog;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import es.gob.jmulticard.android.nfc.AndroidNfcConnection;
import es.gob.jmulticard.card.dnie.Dnie3Dg01Mrz;
import es.gob.jmulticard.card.dnie.DnieCertParseUtil;
import es.gob.jmulticard.jse.provider.DnieKeyStoreImpl;
import es.gob.jmulticard.jse.provider.DnieProvider;

import static es.gob.jmulticard.card.dnie.Dnie.CERT_ALIAS_SIGN;

/** Tarea que ejecuta una firma electr&oacute;nica XAdES trif&aacute;sica.
 * La firma es una operaci&oacute;n que necesariamente debe ejecutarse en segundo
 * plano ya que hace conexiones de red.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public class KeyStoreLoadTask extends AsyncTask<Tag, Void, KeyStore.PrivateKeyEntry>{

    private final MainActivity context;
    private CanResult canResult;

    private ProgressDialog progDailog;
    /** Construye la tarea encargada de realizar la operaci&oacute;n criptogr&aacute;fica.
     * @param context Contexto de la aplicaci&oacute;n.*/
    public KeyStoreLoadTask(final MainActivity context, CanResult canResult, final ProgressDialog progDialog) throws IOException {
        this.context = context;
        this.progDailog = progDialog;
        this.canResult = canResult;
    }


    @Override
    protected KeyStore.PrivateKeyEntry doInBackground(final Tag... params) {

        final Tag tag = params[0];

        // En este punto tenemos ILP, PIN, CAN y el DNI bajo el movil,
        // podemos proceder a la firma
        KeyStore.PrivateKeyEntry pke;
        try {
            Provider p = Security.getProvider("DNI");
            if (p == null) {
                p = new DnieProvider(
                        new AndroidNfcConnection(
                                tag
                        )
                );
                Security.addProvider(p);
            }
            final KeyStore ks = KeyStore.getInstance(
                    "DNI",
                    p
            );
            ks.load(
                new KeyStore.LoadStoreParameter() {
                    @Override
                    public KeyStore.ProtectionParameter getProtectionParameter() {
                        return new KeyStore.CallbackHandlerProtection(canResult.getCallbackHandler());
                    }
                }
            );

            final Field f = ks.getClass().getDeclaredField("implSpi");
            f.setAccessible(true);
            final Dnie3Dg01Mrz o = ((DnieKeyStoreImpl) f.get(ks)).getDnie3Dg01();

            Ilp.IlpSigner signer = canResult.getIlp().getSigner();

            signer.setBirthDay(Integer.parseInt(new SimpleDateFormat("dd").format(o.getDateOfBirth())));
            signer.setBirthMonth(Integer.parseInt(new SimpleDateFormat("MM").format(o.getDateOfBirth())));
            signer.setBirthYear(Integer.parseInt(new SimpleDateFormat("yyyy").format(o.getDateOfBirth())));
            signer.setId(o.getSubjectNumber());
            signer.setName(o.getName());
            signer.setIdType(Ilp.IlpSigner.IdType.DNI);

            final X509Certificate c = (X509Certificate) ks.getCertificate(CERT_ALIAS_SIGN);
            final DnieCertParseUtil dcpu = new DnieCertParseUtil(c);
            signer.setSurname1(dcpu.getSurname1());
            signer.setSurname2(dcpu.getSurname2());

            // El KeyStore ha creado bien, obtenemos el PrivateKeyEntry de firma
            pke = (KeyStore.PrivateKeyEntry) ks.getEntry(
                    CERT_ALIAS_SIGN,
                    null // Al pasarle null usara el CallbackHandler para obtener el PIN
            );

            if(DniDialog.getInstance() != null){
                DniDialog.getInstance().dismiss();
            }

        }
        catch (final Throwable e) {
            Log.w("com.mifirma", "Error inicializando DNIe NFC en tarea asincrona: " + e);
            pke = null;
        }

        return pke;
    }

    @Override
    protected void onPostExecute(final KeyStore.PrivateKeyEntry pke) {
        super.onPostExecute(pke);

        if (pke != null) {
            try {
                // Ejecutamos la tarea de firma
                new SignTask(
                        context,
                        pke,
                        context.getExtraParams(),
                        progDailog
                ).execute(canResult.getIlp());
                return;
            }
            catch (Exception e) {
                Log.e("com.mifirma", "Error inicializando DNIe NFC: " + e);
            }
        }
        if(progDailog != null){
            progDailog.dismiss();
        }
        if(DniDialog.getInstance() != null){
            DniDialog.getInstance().dismiss();
        }
        canResult = null;
        Toast.makeText(context, "Ha sido imposible usar el DNI NFC, inténtelo de nuevo.", Toast.LENGTH_LONG).show();
    }
}
