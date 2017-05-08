/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.core.ui;

import java.io.IOException;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.keystores.KeyStoreManager;
import es.gob.afirma.core.keystores.NameCertificateBean;

/** Interfaz que implementan los di&aacute;logos de selecci&oacute;n de certificados. */
public interface KeyStoreDialogManager {

	/**
	 * Manda recargar al almac&eacute;n asociado actualmente al di&aacute;logo de
	 * selecci&oacute;n.
	 * @throws IOException En caso de errores de entrada / salida
	 */
	void refresh() throws IOException;

	/**
	 * Obtiene el listado de certificados con alias que deben mostrarse en el
	 * di&aacute;logo de selecci&oacute;n.
	 * @return Listado de certificados con alias.
	 */
	NameCertificateBean[] getNameCertificates();

	/**
	 * Cambia el almacen que gestiona internamente el di&aacute;logo.
	 * @param ksm Almac&eacute;n de certificados.
	 */
	void setKeyStoreManager(KeyStoreManager ksm);

	/**
	 * Devuelve la clave asociada a un alias.
	 * @param alias Alias de la clave que se desea recuperar.
	 * @throws AOException Cuando no se puede extraer la clave del almac&eacute;n.
	 * @return Clave.
	 */
	Object getKeyEntry(String alias) throws AOException;

	/** Muestra el di&aacute;logo con el listado de certificados que se ajusta a los criterios establecidos
	 * para que el usuario seleccione uno de ellos.
	 * @return Alias del certifciado seleccionado.
	 * @throws es.gob.afirma.core.AOCancelledOperationException Cuando no se selecciona ning&uacute;n certificado.
	 * @throws RuntimeException Cuando se produce un error en la extracci&oacute;n de la clave del almac&eacute;n.
	 * @throws AOException Cuando no hay certificados en el almac&eacute;n acordes a los criterios establecidos. */
	String show() throws AOException;

	/**
	 * Recupera el alias del certificado seleccionado;
	 * @return Alias de certificado.
	 */
	String getSelectedAlias();
}
