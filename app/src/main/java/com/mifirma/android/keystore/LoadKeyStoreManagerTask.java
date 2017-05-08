package com.mifirma.android.keystore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.mifirma.android.R;
import com.mifirma.android.logic.Ilp;
import com.mifirma.android.logic.SignTask;
import com.mifirma.android.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Tarea de carga e inicializaci&oacute;n del gestor de claves y certificados en Android. */
public final class LoadKeyStoreManagerTask extends AsyncTask<Ilp, Void, byte[]> {

	private final Activity activity;

    private ProgressDialog progDialog;
	/** Crea una tarea de carga e inicializaci&oacute;n del gestor de claves y certificados en
     * Android.
	 * @param act Actividad padre. */
	public LoadKeyStoreManagerTask(final Activity act) {
        this.activity = act;
	}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDialog = new ProgressDialog(activity);
        progDialog.setMessage(activity.getResources().getString(R.string.loading));
        progDialog.setIndeterminate(false);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setCancelable(false);
        progDialog.show();
    }

	@Override
	protected byte[] doInBackground(final Ilp... params) {
        MobileKeyStoreManager res = KeyStoreManagerFactory.getKeyStoreManager(this.activity);
        final Properties properties = new Properties();
        AssetManager assetManager = activity.getAssets();
        final InputStream inputStream;
        try {
            inputStream = assetManager.open(Utils.CONFIG_FILE);
            properties.load(inputStream);
        }
        catch (final IOException e) {
            Log.e("com.mifirma", "Error leyendo los parametros de firma: " + e);
            return null;
        }
        res.getPrivateKeyEntryAsynchronously(
            new MobileKeyStoreManager.PrivateKeySelectionListener() {
                public void keySelected(MobileKeyStoreManager.KeySelectedEvent kse) {
                try {
                    if (params[0] != null) {
                        new SignTask(
                            activity,
                            kse.getPrivateKeyEntry(),
                            properties,
                            progDialog
                        ).execute(params[0]);
                    }
                }
                catch (final Throwable e) {
                    e.printStackTrace();
                    progDialog.dismiss();
                }
                }
            }
        );
        return null;
	}

    @Override
    protected void onPostExecute(final byte[] result) {
        super.onPostExecute(result);
    }

}
