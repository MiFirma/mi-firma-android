package com.mifirma.android.keystore;

import android.app.Activity;

/** Facrtor&iacute;a de gestores de contrase&ntilde;as y claves para Android. */
final class KeyStoreManagerFactory {

	private KeyStoreManagerFactory() {
		// Se prohibe crear instancias
	}

	/** Obtiene el gestor de contrase&ntilde;as y claves.
	 * @param activity Actividad padre. */
	static MobileKeyStoreManager getKeyStoreManager(final Activity activity) {
		return new Android4KeyStoreManager(activity);
	}


}