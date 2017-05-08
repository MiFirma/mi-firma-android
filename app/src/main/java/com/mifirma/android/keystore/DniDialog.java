/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package com.mifirma.android.keystore;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.mifirma.android.MainActivity;
import com.mifirma.android.R;

/** Di&aacute;logo con instrucciones de c&oacute;mo colocar el DNIe NFC. */
public final class DniDialog extends DialogFragment {

    public static final String DNI_GIF_WEB_VIEW = "file:///android_asset/dni_nfc.gif";

    private CanResult canResult;
    private void setCanResult(CanResult canResult) {
        this.canResult = canResult;
    }

    private static DniDialog instance = null;

    public static DniDialog getInstance() {
        return instance;
    }

    public DniDialog() {
        setArguments(new Bundle());
    }

    public static DniDialog newInstance(final CanResult canResult) {
        if (instance == null) {
            instance = new DniDialog();
        }
        instance.setCanResult(canResult);
        instance.setArguments(new Bundle());
        return instance;
    }

    public static DniDialog newInstance(final CanResult canResult, final Bundle bundle) {
        if (instance == null) {
            instance = new DniDialog();
        }
        instance.setCanResult(canResult);
        instance.setArguments(bundle);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState){

        ((MainActivity) this.getActivity()).setCanResult(canResult);

        final Builder alertDialogBuilder = new Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.mobileoverdni));

        final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View view = layoutInflater.inflate(R.layout.dialog_dni, null);

        WebView wView = (WebView) view.findViewById(R.id.dni_web_view);


        wView.setInitialScale(1);
        wView.getSettings().setLoadWithOverviewMode(true);
        wView.getSettings().setUseWideViewPort(true);
        wView.loadUrl(DNI_GIF_WEB_VIEW);

        alertDialogBuilder.setView(view);

        alertDialogBuilder.setNegativeButton(
                getActivity().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        DniDialog.this.canResult.setCan(null);
                        DniDialog.this.canResult.setPin(null);
                        dialog.dismiss();
                    }
                }
        );
        alertDialogBuilder.setOnKeyListener(
            new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                }
                return false;
                }
            }
        );

        return alertDialogBuilder.create();
    }

}