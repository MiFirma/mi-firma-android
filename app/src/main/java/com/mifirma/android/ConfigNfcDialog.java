/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package com.mifirma.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/** Di&acute;logo para avisar al usuario de que su dispositivo dispone de NFC y preguntarle si
 * desea habilitar el uso del DNIe 3.0 por NFC. */
public final class ConfigNfcDialog extends DialogFragment {

    /** Construye un di&acute;logo para preguntar si se desea habilitar el uso de NFC. */
    public ConfigNfcDialog() {
        setArguments(new Bundle());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState){

        return new AlertDialog.Builder(getActivity())
            .setMessage("¿Desea usar NFC para conexión con DNIe 3.0?")
            .setNegativeButton(
                android.R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        MainActivity.setUseNfc(false);
                        dialog.dismiss();

                    }
                }
            )
            .setPositiveButton(
                android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        MainActivity.setUseNfc(true);
                        dialog.dismiss();
                        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(ConfigNfcDialog.this.getActivity());
                        if(mAdapter != null && !mAdapter.isEnabled()){
                            new ActiveNfcDialog().show(
                                    ConfigNfcDialog.this.getActivity().getSupportFragmentManager(),
                                    "enableNfcDialog"
                            );
                        }
                    }
                }
            )
            .create();
    }


}