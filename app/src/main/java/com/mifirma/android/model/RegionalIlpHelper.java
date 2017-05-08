package com.mifirma.android.model;

import android.content.Context;

import com.mifirma.android.data.RegionalSQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/** Utilidades para la gesti&oacute;n de ILP auton&oacute;micas.
 * @author Tom&aacute;s Garc&iacute;-Mer&aacute;s. */
public final class RegionalIlpHelper {

	/** Localidad de la provincia dentro de una ILP auton&oacute;mica. */
	public static final class Location {

        private final String id;
		private final String name;
		private final String ineCode;

		public Location(final String i, final String n, final String c) {
            if (i == null || i.isEmpty()) {
                throw new IllegalArgumentException(
                        "El identificador de la localidad no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            if (n == null || n.isEmpty()) {
                throw new IllegalArgumentException(
                        "El nombre de la localidad no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            if (c == null || c.isEmpty()) {
                throw new IllegalArgumentException(
                        "El codigo INE de la localidad no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            this.id = i;
			this.name = n;
			this.ineCode = c;
		}

		@Override
		public String toString() {
			return this.name;
		}

		/** Obtiene el c&oacute;digo de localidad.
		 * @return C&oacute;digo de localidad. */
        public String getIneCode() {
            return this.ineCode;
        }

        public String getId() {
            return this.id;
        }
	}

	/** Provincia dentro de una ILP auton&oacute;mica. */
	public static final class Province {

        private final String id;
		private final String name;
		private final String ineCode;
        private final List<Location> locations;

		public Province(final String i, final String n, final String ine, List<Location> l) {
            if (i == null || i.isEmpty()) {
                throw new IllegalArgumentException(
                        "El identificador de la provincia no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            if (n == null || n.isEmpty()) {
                throw new IllegalArgumentException(
                    "El nombre de la provincia no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            if (ine == null || ine.isEmpty()) {
                throw new IllegalArgumentException(
                    "El codigo INE de la provincia no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            if (l == null || l.isEmpty()) {
                throw new IllegalArgumentException(
                        "El listado de localizaciones no puede ser nulo ni vacio" //$NON-NLS-1$
                );
            }
            this.id = i;
			this.name = n;
			this.ineCode = ine;
            this.locations = l;
		}

		/** Obtiene el c&oacute;digo INE de la provincia.
		 * @return C&oacute;digo INE de la provincia. */
		public String getIneCode() {
			return this.ineCode;
		}

		@Override
		public String toString() {
			return this.name;
		}

		/** Obtiene las localidades de esta provincia.
		 * @return lista de localidades de esta provincia. */
		public List<Location> getLocations() {
			return locations;
		}
	}

	/** Obtiene las provincias correspondientes a los c&oacute;digos proporcionados.
     * @param context
	 * @param provCodes C&oacute;digos de provincia.
	 * @return Lista de provincias correspondientes a los c&oacute;digos proporcionados. */
 	public static List<Province> getProvinces(final Context context, final String[] provCodes) {
		List<Province> ret = new ArrayList<Province>();

        RegionalSQLiteHelper provinceDbHelper = new RegionalSQLiteHelper(context);
		ret = provinceDbHelper.getProvinces(provCodes);
		return ret;
	}
}
