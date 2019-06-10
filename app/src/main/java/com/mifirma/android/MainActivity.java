package com.mifirma.android;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.mifirma.android.fragments.WelcomeFragment;
import com.mifirma.android.keystore.CanDialog;
import com.mifirma.android.keystore.CanResult;
import com.mifirma.android.keystore.DniDialog;
import com.mifirma.android.keystore.PinDialog;
import com.mifirma.android.logic.KeyStoreLoadTask;
import com.mifirma.android.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Clase principal de la aplicacion.
 * @author Javier */
public class MainActivity extends FragmentActivity {

    /** Indica si la aplicaci&oacute;n est&aacute; en modo depuraci&oacute;n. */
    public static final boolean DEBUG = true;

    /** Indica si se ha hecho la comprobacion de si el dispositivo tiene NFC. Se usa para evitar
     * detectarlo en cada carga de la actividad. */
    private boolean nfcAvailableChecked = false;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    private CanResult canResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            // Etiquetamos para poder volver a la ventana mas tarde especificando el TAG.
            getFragmentManager()
                .beginTransaction()
                .add(R.id.container, new WelcomeFragment(), Utils.HOME_TAG)
                .commit();
        }

        if (!nfcAvailableChecked) { // Si no no se ha comprobado antes...

            mAdapter = NfcAdapter.getDefaultAdapter(this);

            if (mAdapter != null) {
                // Si el NFC esta disponible, preguntamos si desea usarlo
                new ConfigNfcDialog().show(
                        getSupportFragmentManager(),
                        "enableNfcDialog"
                );
            }

            nfcAvailableChecked = true;
        }

        mPendingIntent = PendingIntent.getActivity(
            this,
            0,
            new Intent(
                this,
                getClass()
            ).addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            ),
            0
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (canResult != null) {
            if(DniDialog.getInstance() != null){
                DniDialog.getInstance().dismiss();
            }
            final Tag discoveredTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d("com.mifirma", "Tag: " + discoveredTag);

            final ProgressDialog progDailog = new ProgressDialog(MainActivity.this);
            progDailog.setMessage(this.getResources().getString(R.string.loadingNFC));
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();

            try {
                // Ejecutamos la tarea de firma
                new KeyStoreLoadTask(
                    this,
                    canResult,
                    progDailog
                ).execute(discoveredTag);
            }
            catch (final Throwable e){
                if(DniDialog.getInstance() != null){
                    DniDialog.getInstance().dismiss();
                }
                progDailog.dismiss();
                canResult = null;
                Log.w("com.mifirma", "Error inicializando DNIe NFC: " + e);
                Toast.makeText(this, "Ha sido imposible usar el DNI NFC, inténtelo de nuevo.", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(useNfc && !mAdapter.isEnabled()){
            new ActiveNfcDialog().show(
                    this.getSupportFragmentManager(),
                    "enableNfcDialog"
            );
        }
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    public void setCanResult(CanResult canResult) {
        this.canResult = canResult;
    }

    public Properties getExtraParams() {
        final Properties properties = new Properties();
        AssetManager assetManager = getAssets();
        final InputStream inputStream;
        try {
            inputStream = assetManager.open(Utils.CONFIG_FILE);
            properties.load(inputStream);
        }
        catch (final IOException e) {
            Log.e("com.mifirma", "Error leyendo los parametros de firma: " + e);
            return null;
        }
        return properties;
    }

    @Override
    public void onBackPressed() {
        if(DniDialog.getInstance() != null &&
           DniDialog.getInstance().getDialog() != null &&
           DniDialog.getInstance().getDialog().isShowing()){
            DniDialog.getInstance().dismiss();
        }
        else if(CanDialog.getInstance() != null &&
                CanDialog.getInstance().getDialog() != null &&
                CanDialog.getInstance().getDialog().isShowing()) {
            CanDialog.getInstance().dismiss();
        }
        else if(PinDialog.getInstance() != null &&
                PinDialog.getInstance().getDialog() != null &&
                PinDialog.getInstance().getDialog().isShowing()) {
            PinDialog.getInstance().dismiss();
        }
        else {
            super.onBackPressed();
        }
    }

    private static boolean useNfc = false;
    public static void setUseNfc(boolean use) {
        useNfc = use;
    }
    public static boolean getUseNfc() {
        return useNfc;
    }

}