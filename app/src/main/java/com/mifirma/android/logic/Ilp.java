package com.mifirma.android.logic;

import com.mifirma.android.util.Utils;

import java.util.logging.Logger;

/** Descripci&oacute;n de la ILP que hay que firmar (en su forma XML).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class Ilp {

    private static final Logger LOGGER = Logger.getLogger("com.mifirma.android"); //$NON-NLS-1$
	/** Firmante de la ILP.
	 * Su representaci&oacute;n en XML es la siguiente:
	 * <pre>
	 * &lt;ilp&gt;
	 *  &lt;firmante&gt;
	 *   &lt;nomb&gt;Javier&lt;/nomb&gt;
	 *   &lt;ape1&gt;Pena&lt;/ape1&gt;
	 *   &lt;ape2&gt;Martinez&lt;/ape2&gt;
	 *   &lt;fnac&gt;19740920&lt;/fnac&gt;
	 *   &lt;tipoid&gt;1&lt;/tipoid&gt;
	 *   &lt;id&gt;07504500Z&lt;/id&gt;
	 *  &lt;/firmante&gt;
	 *  &lt;datosilp&gt;
	 *   &lt;tituloilp&gt;ILP de Prueba 33212&lt;/tituloilp&gt;
	 *   &lt;codigoilp&gt;ILP2012002&lt;/codigoilp&gt;
	 *  &lt;/datosilp&gt;
	 * &lt;/ilp&gt;
s	 * </pre> */
	public static class IlpSigner {

		/** Tipo de documento de identificaci&oacute;n. */
		public enum IdType {
			/** DNI (debe incluirse la letra). */
			DNI(1);

			private final int type;

			IdType(final int t) {
				this.type = t;
			}

			private int getType() {
				return this.type;
			}

			@Override
			public String toString() {
				return Integer.toString(getType());
			}
		}

		private String name;
		private int birthYear;
		private int birthMonth;
		private int birthDay;
		private IdType idType;
		private String id;

		private String surname1;
		private String surname2;

		private final String email;
        private final boolean terms;
        private final String province;
        private final String location;

		/** Obtiene el nombre del firmante.
		 * @return Nombre del firmante. */
		public String getName() {
			return this.name;
		}

		/** Obtiene el primer apellido del firmante.
		 * @return Primer apellido del firmante. */
		public String getFirstSurname() {
			return this.surname1;
		}

		/** Obtiene el segundo apellido del firmante.
		 * @return Segundo apellido del firmante. */
		public String getSecondSurname() {
			return this.surname2;
		}

		/** Obtiene la fecha de nacimiento del firmante en formato YYYYMMDD.
		 * @return Fecha de nacimiento del firmante en formato YYYYMMDD. */
		public String getBirthDate() {
			return Integer.toString(this.birthYear) + "-" + String.format("%02d",this.birthMonth) + "-" + String.format("%02d",this.birthDay);
		}

		/** Obtiene el tipo de documento de identificaci&oacute;n del firmante.
		 * @return Tipo de documento de identificaci&oacute;n del firmante. */
		public IdType getIdType() {
			return this.idType;
		}

		/** Obtiene el n&uacute;mero de identificaci&oacute;n del firmante.
		 * @return N&uacute;mero de identificaci&oacute;n del firmante. */
		public String getId() {
			return this.id;
		}

        public String getEmail() {
            return this.email;
        }

        public boolean getTerms() {
            return this.terms;
        }


		public void setName(String name) {
			this.name = name;
		}

		public void setSurname1(final String sn1) { this.surname1 = sn1; }

		public void setSurname2(final String sn2) { this.surname2 = sn2; }

		public void setBirthYear(int birthYear) {
			this.birthYear = birthYear;
		}

		public void setBirthMonth(int birthMonth) {
			this.birthMonth = birthMonth;
		}

		public void setBirthDay(int birthDay) {
			this.birthDay = birthDay;
		}

		public void setIdType(IdType idType) {
			this.idType = idType;
		}

		public void setId(String id) {
			this.id = id;
		}



		/** Construye la informaci6oacute;n del firmante de la ILP.
		 * @param n Nombre.
		 * @param s1 Primer apellido.
		 * @param s2 Segundo apellido.
		 * @param bYear A&ntilde;o de nacimiento.
		 * @param bMonth Mes de nacimiento.
		 * @param bDay D&iacute;a de nacimiento.
		 * @param idTpy Tipo de documento de identidad.
		 * @param idNumber N&uacute;mero del documento de identidad.
		 *                 Si es un DNI o un NIE debe contener la letra, sin guiones ni espacios. */
		public IlpSigner(final String n,
				         final String s1,
				         final String s2,
				         final int bYear,
				         final int bMonth,
				         final int bDay,
				         final IdType idTpy,
				         final String idNumber,
                         final String email,
                         final boolean terms,
                         final String province,
                         final String location) {
			this.name = n != null ? n.trim() : ""; //$NON-NLS-1$
			this.surname1 = s1 != null ? s1.trim() : ""; //$NON-NLS-1$
			this.surname2 = s2 != null ? s2.trim() : ""; //$NON-NLS-1$
			this.birthYear = bYear;
			this.birthMonth = bMonth;
			this.birthDay = bDay;
			this.idType = idTpy;
			this.id = idNumber != null ? idNumber.replace("-", "").replace(" ", "").replace(".",  " ") : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
            this.email = email;
            this.terms = terms;
            this.province = province;
            this.location = location;
		}


        /*
        <ilp-signature>
	<date-of-birth type="date">1974-09-20</date-of-birth>
	<dni>07504500Z</dni>
	<email>jpenamar@gmail.com</email>
	<name>Javier</name>
	<proposal-id type="integer">3</proposal-id>
	<surname>Peña</surname>
	<surname2>Martínez</surname2>
	<terms type="boolean">true</terms>
</ilp-signature>

         */

		@Override
		public String toString() {
			String res =
			 " <date-of-birth type=\"date\">" + this.getBirthDate() +"</date-of-birth>\n" + //$NON-NLS-1$
			 " <dni>" + this.id + "</dni>\n" +  //$NON-NLS-1$//$NON-NLS-2$
			 " <email>" + this.email + "</email>\n" + //$NON-NLS-1$ //$NON-NLS-2$
             " <name>" + this.name + "</name>\n" + //$NON-NLS-1$ //$NON-NLS-2$
             " <surname>" + this.surname1 + "</surname>\n" + //$NON-NLS-1$ //$NON-NLS-2$
			 " <surname2>" + this.surname2 + "</surname2>\n" + //$NON-NLS-1$ //$NON-NLS-2$
             " <terms type=\"boolean\">" + this.terms + "</terms>\n" //$NON-NLS-1$ //$NON-NLS-2$
            ;
            if(!Utils.isBlank(this.province) && !Utils.isBlank(this.location)){
                res += " <province_id>" + this.province + "</province_id>\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        " <municipality_id>" + this.location + "</municipality_id>" //$NON-NLS-1$ //$NON-NLS-2$
                ;
            }
            LOGGER.severe("XML envio: " + res); //$NON-NLS-1$
            return res;
		}

	}

	private final IlpSigner signer;
	private final String title;
	private final String id;

	/** Construye los datos de la IPL a ser firmados.
	 * @param ilpSigner Firmante de la ILP.
	 * @param ilpTitle T&iacute;tulo de la ILP.
	 * @param ilpId Identificador de la ILP. */
	public Ilp(final IlpSigner ilpSigner,
			   final String ilpTitle,
			   final String ilpId) {
		if (ilpSigner == null) {
			throw new IllegalArgumentException(
				"El firmante de la ILP no puede ser nulo" //$NON-NLS-1$
			);
		}
		if (ilpTitle == null) {
			throw new IllegalArgumentException(
				"El titulo de la ILP no puede ser nulo" //$NON-NLS-1$
			);
		}
		if (ilpId == null) {
			throw new IllegalArgumentException(
				"El codigo de la ILP no puede ser nulo" //$NON-NLS-1$
			);
		}
		this.signer = ilpSigner;
		this.title = ilpTitle.trim();
		this.id = ilpId.trim();
	}

	/** Obtiene el firmante de la ILP.
	 * @return Firmante de la ILP. */
	public IlpSigner getSigner() {
		return this.signer;
	}
    /*
     <ilp-signature>
 <date-of-birth type="date">1974-09-20</date-of-birth>
 <dni>07504500Z</dni>
 <email>jpenamar@gmail.com</email>
 <name>Javier</name>
 <proposal-id type="integer">3</proposal-id>
 <surname>Peña</surname>
 <surname2>Martínez</surname2>
 <terms type="boolean">true</terms>
</ilp-signature>

      */
	@Override
	public String toString() {
		return
		 "<ilp-signature>\n" + //$NON-NLS-1$
		 this.signer +
		 " <proposal-id type=\"integer\">" + this.id + "</proposal-id>\n" + //$NON-NLS-1$ //$NON-NLS-2$
		 "</ilp-signature>" //$NON-NLS-1$
		;
	}

}
