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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mifirma.android.R;

/** Di&acute;logo para introducir el PIN.
 * Se usa en almacenes distintos al del propio sistema operativo Android.
 * @author Astrid Idoate */

public class CanDialog extends DialogFragment {

	private static final String ES_GOB_AFIRMA = "es.gob.afirma"; //$NON-NLS-1$
	private CanResult canResult;

	/** Construye un di&acute;logo para introducir el CAN. */
	public CanDialog() {
		// Vacio
	}

	private static CanDialog instance = null;

	public static CanDialog getInstance() {
		return instance;
	}

	/** Obtiene una nueva instancia de un di&acute;logo para introducir el CAN.
	 * @param canResult Objeto en el que almacenar el valor establecido en el di&oacute;logo.
	 * @return pinDialog el di&acute;logo creado. */
	public static CanDialog newInstance(final CanResult canResult) {
		if (instance == null) {
			instance = new CanDialog();
		}
		instance.setCanResult(canResult);
		instance.setArguments(new Bundle());
		return instance;
	}

	private void setCanResult(CanResult canResult) {
		this.canResult = canResult;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState){

		final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		final View view = layoutInflater.inflate(R.layout.dialog_can, null);

		final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
				.setView(view)
				.setTitle("Introduzca el número CAN de su DNIe")
				.setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
				.setNegativeButton(
						"No usar DNIe",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int id) {
								CanDialog.this.canResult.setCan(null);
								dialog.dismiss();

								new LoadKeyStoreManagerTask(CanDialog.this.getActivity()).execute(canResult.getIlp());
							}
						}
				)
				.create();

		final EditText editTextPin = (EditText) view.findViewById(R.id.etPin);

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(final DialogInterface dialog) {
				Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(
						new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								final String can = editTextPin.getText() != null ? editTextPin.getText().toString() : null;
								if(can == null || "".equals(can)) { //$NON-NLS-1$
									Log.e(ES_GOB_AFIRMA, "El CAN no puede ser vacio o nulo"); //$NON-NLS-1$
									getActivity().runOnUiThread(
											new Runnable() {
												@Override
												public void run() {
													Toast.makeText(
															getActivity(),
															"El CAN no puede estar vacío",
															Toast.LENGTH_LONG
													).show();
												}
											}
									);
								}
								else {
									dialog.dismiss();
									CanDialog.this.canResult.setCan(can);
									// Pedimos entonces el PIN
									DialogFragment pinDialog = PinDialog.newInstance(canResult);
									pinDialog.show(getActivity().getFragmentManager(), "pindialog");
								}
							}
						}
				);
			}
		});
		alertDialog.setOnKeyListener(
				new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							CanDialog.this.canResult.setCan(null);
							dialog.dismiss();
							return true;
						}
						return false;
					}
				}
		);

		return alertDialog;
	}
}