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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mifirma.android.R;

/** Di&acute;logo para introducir el PIN.
 * Se usa en almacenes distintos al del propio sistema operativo Android. */

public final class PinDialog extends DialogFragment {

    private static final String ES_GOB_AFIRMA = "es.gob.afirma"; //$NON-NLS-1$

    private CanResult canResult;
    private void setCanResult(CanResult canResult) {
        this.canResult = canResult;
    }

    private static PinDialog instance = null;

    public static PinDialog getInstance() {
        return instance;
    }

    /** Construye un di&acute;logo para introducir el PIN. */
    public PinDialog() {
        // Vacio
    }

    /** Obtiene una nueva instancia de un di&acute;logo para introducir el PIN.
     * @return pinDialog el di&acute;logo creado. */
    public static PinDialog newInstance(final CanResult canResult) {
        if (instance == null) {
            instance = new PinDialog();
        }
        instance.setCanResult(canResult);
        instance.setArguments(new Bundle());
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState){

        final Builder alertDialogBuilder = new Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.pinrequest));

        final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View view = layoutInflater.inflate(R.layout.dialog_pin, null);

        final EditText editTextPin = (EditText) view.findViewById(R.id.etPin);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setNegativeButton(
                getActivity().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        PinDialog.this.canResult.setCan(null);
                        PinDialog.this.canResult.setPin(null);
                        dialog.dismiss();
                    }
                }
        );
        alertDialogBuilder.setPositiveButton(
            R.string.ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                    if(editTextPin.getText() != null && !"".equals(editTextPin.getText().toString())) { //$NON-NLS-1$
                        dialog.dismiss();
                        canResult.setPin(editTextPin.getText().toString());

                        DialogFragment dniDialog = DniDialog.newInstance(canResult);
                        dniDialog.show(getActivity().getFragmentManager(), "dnidialog");
                    }
                    else {
                        Log.e(ES_GOB_AFIRMA, "El pin no puede ser vacio o nulo"); //$NON-NLS-1$
                        getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(
                                        getActivity(),
                                        "El PIN no puede estar vacío",
                                        Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                        );
                    }
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