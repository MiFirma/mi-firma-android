package com.mifirma.android.logic;

import android.content.Context;
import android.content.res.AssetManager;

import com.mifirma.android.model.Initiative;
import com.mifirma.android.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.misc.http.UrlHttpManagerFactory;
import es.gob.afirma.core.misc.http.UrlHttpMethod;

/** Lista de iniciativas.
 * @author Juliana Marulanda
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
final class InitiativesList {

    private static final Logger LOGGER = Logger.getLogger("com.mifirma.android"); //$NON-NLS-1$

    private final ArrayList<Initiative> initiatives = new ArrayList<Initiative>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Listado de iniciativas"); //$NON-NLS-1$
        if (this.initiatives.isEmpty()) {
            sb.append(" vacio"); //$NON-NLS-1$
            return sb.toString();
        }
        sb.append(":\n"); //$NON-NLS-1$
        for (int i=0;i<this.initiatives.size();i++) {
            sb.append("Iniciativa "); //$NON-NLS-1$
            sb.append(i+1);
            sb.append('\n');
            sb.append(this.initiatives.get(i));
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Obtiene la lista de iniciativas actuales.
     * @return Lista de iniciativas actuales.
     * @throws java.io.IOException Si hay problemas en la obtenci&oacute;n.
     * @throws java.text.ParseException Si uno de los campos del XML de defibici&oacute;n de iniciativas es de tipo fecha pero no contiene un texto de fecha
     *                        en el formato ISO-8601 adecuado (por ejemplo '2015-01-21T19:27:36Z').
     * @throws org.w3c.dom.DOMException Si hay problemas analizando el XML de defibici&oacute;n de iniciativas.
     * @throws NumberFormatException Si uno de los campos del XML de defibici&oacute;n de iniciativas es de tipo entero pero no contiene un
     *                               n&uacute;mero entero. */
    static InitiativesList getInitiativesList(Context context) throws IOException, DOMException, ParseException {
        final Properties p = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(Utils.CONFIG_FILE);
        p.load(inputStream);

        final String urlStr = p.getProperty(Utils.CONFIG_FILE_KEY_PROPOSALS);
        if (urlStr == null) {
            throw new IOException(
                    "El fichero de configuracion no contiene la URL de obtencion de iniciativas" //$NON-NLS-1$
            );
        }


        LOGGER.info("Solicitando la lista de iniciativas a " + urlStr); //$NON-NLS-1$
        final byte[] xmlBytes = UrlHttpManagerFactory.getInstalledManager().readUrl(urlStr, -1, "application/xml", "text/xml", UrlHttpMethod.GET);
        /*("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ilp-proposals type=\"array\">" +
                "<ilp-proposal>\n" +
                "  <attestor-template-code></attestor-template-code>\n" +
                "  <banner-content-type nil=\"true\"></banner-content-type>\n" +
                "  <banner-file-name nil=\"true\"></banner-file-name>\n" +
                "  <banner-file-size type=\"integer\" nil=\"true\"></banner-file-size>\n" +
                "  <banner-updated-at type=\"datetime\" nil=\"true\"></banner-updated-at>\n" +
                "  <created-at type=\"datetime\">2014-05-20T18:00:48Z</created-at>\n" +
                "  <election-id type=\"integer\" nil=\"true\"></election-id>\n" +
                "  <election-type nil=\"true\"></election-type>\n" +
                "  <handwritten-signatures type=\"integer\">0</handwritten-signatures>\n" +
                "  <howto-solve>&lt;p&gt; &lt;b&gt;&#191;Sab&#237;as que...&lt;/b&gt; En Castilla La Mancha (CLM) y en Castilla y Le&#243;n (CyL), en &lt;b&gt;TODOS LOS CUERPOS&lt;/b&gt; docentes, los funcionarios/as definitivos, si no obtienen un mejor destino en el concurso de traslados, pueden optar a estar como provisionales en una plaza que responda mejor a sus preferencias en un procedimiento conocido como &lt;b&gt;&quot;CONCURSILLO&quot;?&lt;/b&gt;&lt;/p&gt;\n" +
                " \n" +
                "&lt;p&gt;&lt;b&gt;&#191;Sab&#237;as que...&lt;/b&gt; En las dos Castillas, los &lt;b&gt;MAESTROS&lt;/b&gt; provisionales o en pr&#225;cticas que nunca han obtenido destino definitivo no est&#225;n obligados a ordenar todas las provincias de la Comunidad sino que pueden pedir libremente &lt;b&gt;UNA SOLA PROVINCIA&lt;/b&gt; --o m&#225;s-- para destino definitivo y de no obtener destino ni en sus 300 peticiones ni en esa provincia siguen como provisionales?&lt;/p&gt;\n" +
                " \n" +
                "&lt;p&gt;&lt;b&gt;&#191;Sab&#237;as que...&lt;/b&gt; de las cinco Comunidades Aut&#243;nomas espa&#241;olas de mayor extensi&#243;n territorial, Andaluc&#237;a, segunda en extensi&#243;n y n&#250;mero de provincias, es la &#250;nica que no contempla ninguna medida que flexibilice la gesti&#243;n de su personal docente en el concurso general de traslados a pesar de tener cedidas las competencias en Educaci&#243;n diecis&#233;is a&#241;os antes que las otras cuatro?&lt;/p&gt; \n" +
                "\n" +
                "&lt;p&gt;&#191;Sab&#237;as que... seg&#250;n el apartado 7 del art&#237;culo 23 de la &lt;a href=&quot;http://www.juntadeandalucia.es/educacion/portal/com/bin/Contenidos/TemasFuerza/nuevosTF/290108_Ley_Educacion_Andalucia/LEA/1201696918777_lea.pdf&quot;&gt;Ley de Educaci&#243;n de Andaluc&#237;a&lt;/a&gt;, las y los docentes andaluces tenemos &lt;b&gt;DERECHO&lt;/b&gt; a la &lt;b&gt;CONCILIACI&#211;N DE LA VIDA FAMILIAR Y LABORAL&lt;/b&gt; y la Administraci&#243;n educativa la &lt;b&gt;OBLIGACI&#211;N&lt;/b&gt; de promover acciones para favorecer ese derecho?&lt;/p&gt;\n" +
                "\n" +
                "&lt;p&gt;&lt;b&gt;&#191;Sab&#237;as que...&lt;/b&gt; ese mismo derecho a la CONCILIACI&#211;N DE LA VIDA LABORAL Y FAMILIAR lo tenemos reconocido como andaluces y andaluzas en el &lt;a href=&quot;http://noticias.juridicas.com/base_datos/Admin/lo2-2007.t6.html#a168&quot;&gt;art. 168 del Estatuto de Autonom&#237;a de Andaluc&#237;a&lt;/a&gt; y como funcionarios del Estado en el &lt;a href=&quot;http://noticias.juridicas.com/base_datos/Admin/l7-2007.t3.html#a14&quot;&gt;apartado j) del art. 14 del Estatuto B&#225;sico del Empleado P&#250;blico&lt;/a&gt;, expresado como conciliaci&#243;n de la vida personal, familiar y laboral?\n" +
                "\n" +
                "&lt;p&gt;&lt;b&gt;&#191;Sab&#237;as que...&lt;/b&gt; en el &lt;a href=&quot;http://www.parlamentodeandalucia.es/opencms/export/portal-web-parlamento/contenidos/pdf/PublicacionesNOoficiales/Novedades/serietrabajosparlamentarios_11.pdf&quot;&gt;paquete de medidas para la convergencia de la Educaci&#243;n en Andaluc&#237;a&lt;/a&gt; , aprobado por el Parlamento andaluz en febrero de 2010, la primera medida que aparece respecto al profesorado, la n&#250;mero 26, contempla la &lt;b&gt;&quot;ATENCI&#211;N PRIORITARIA A LA MEJORA DE LAS CONDICIONES EN QUE EL PROFESORADO REALIZA SU TRABAJO&quot;&lt;/b&gt;? y que dicha desiderata no es sino la transcripci&#243;n del &lt;a href=&quot;http://noticias.juridicas.com/base_datos/Admin/lo2-2006.t3.html#a104&quot;&gt;art&#237;culo 104.2 de la LOE&lt;/a&gt;?&lt;/p&gt;\n" +
                "\n" +
                "&lt;p&gt;Pues, a pesar de todo lo anterior, la Consejer&#237;a de Educaci&#243;n de Andaluc&#237;a no est&#225; forzada a mejorar la gesti&#243;n del profesorado en la adjudicaci&#243;n de puestos definitivos en concurso de traslados, por lo que la INICIATIVA LEGISLATIVA POPULAR es necesaria para obligar, por ley, a la Administraci&#243;n educativa andaluza, a tomar medidas que nos concedan, al profesorado andaluz, todas las garant&#237;as posibles para la conciliaci&#243;n personal, familiar y laboral all&#225; donde m&#225;s comprometida puede quedar &#233;sta: en los concursoss de traslados o a causa de &#233;stos.&lt;/p&gt;</howto-solve>\n" +
                "  <id type=\"integer\">32</id>\n" +
                "  <ilp-code>ILP0114001</ilp-code>\n" +
                "  <name>ILP para la modificacion de la Ley 17/2007 de Educaci&#243;n de Andaluc&#237;a</name>\n" +
                "  <num-required-signatures type=\"integer\">40000</num-required-signatures>\n" +
                "  <pdf-content-type>application/pdf</pdf-content-type>\n" +
                "  <pdf-file-name>ILP-EDU3.pdf</pdf-file-name>\n" +
                "  <pdf-file-size type=\"integer\">123696</pdf-file-size>\n" +
                "  <pdf-updated-at type=\"datetime\">2014-05-20T18:00:48Z</pdf-updated-at>\n" +
                "  <position type=\"integer\">17</position>\n" +
                "  <problem>&lt;p&gt;Tras m&#225;s de 30 a&#241;os de competencias en materia de Educaci&#243;n no universitaria, Andaluc&#237;a no ha tomado medidas que garanticen, todo lo posible, la conciliaci&#243;n de la vida personal, familiar y laboral de su profesorado en los concursos de traslados, como s&#237; hacen las dos Castillas, por lo que la modificaci&#243;n de la LEA es la v&#237;a m&#225;s fuerte para comprometer a la Consejer&#237;a de Educaci&#243;n.&lt;/p&gt;</problem>\n" +
                "  <promoter-name>educacionandalucia</promoter-name>\n" +
                "  <promoter-short-name>ILP Educaci&#243;n Andaluc&#237;a</promoter-short-name>\n" +
                "  <promoter-url>www.mifirma.com</promoter-url>\n" +
                "  <signatures-end-date type=\"date\">2017-05-20</signatures-end-date>\n" +
                "  <subtype>AUTONOMICA</subtype>\n" +
                "  <subtype-provinces>56,66,70,73,75,81,63,93</subtype-provinces>\n" +
                "  <tractis-template-code></tractis-template-code>\n" +
                "  <updated-at type=\"datetime\">2016-06-05T19:39:25Z</updated-at>\n" +
                "  <user-id type=\"integer\">19</user-id>\n" +
                "</ilp-proposal>" +
                "</ilp-proposals>").getBytes();*/
        if (xmlBytes == null || xmlBytes.length < 1) {
            throw new IOException("Se ha obtenido una lista de iniciativas vacia"); //$NON-NLS-1$
        }

        LOGGER.info("Obtenido el siguiente XML de lista de iniciativas:\n" + new String(xmlBytes)); //$NON-NLS-1$

        return getInitiativesListFromXml(
                xmlBytes,
                p.getProperty(Utils.CONFIG_FILE_KEY_IMAGES_BASE_URL)
        );

    }

    private InitiativesList() {
        // Constructor vacio
    }

    private void addInitiative(final Initiative i) {
        if (i != null) {
            this.initiatives.add(i);
        }
    }

    ArrayList<Initiative> getInitiatives() {
        return this.initiatives;
    }

    private static InitiativesList getInitiativesListFromXml(final byte[] xml,
                                                     final String baseUrl) throws IOException,
            DOMException,
            ParseException {
        final InputStream is = new ByteArrayInputStream(xml);
        final Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        }
        catch (final Exception e) {
            Logger.getLogger("es.gob.afirma").severe( //$NON-NLS-1$
                    "Error al cargar el fichero XML de lista de iniciativas: " + e + "\n" + new String(xml) //$NON-NLS-1$ //$NON-NLS-2$
            );
            throw new IOException(
                    "Error al cargar el fichero XML de lista de iniciativas: " + e, e //$NON-NLS-1$
            );
        }
        is.close();

        final Node ilpProposalsNode = doc.getDocumentElement();
        if (!"ilp-proposals".equalsIgnoreCase(ilpProposalsNode.getNodeName())) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "No se encontro el nodo 'ilp-proposals' en el XML proporcionado" //$NON-NLS-1$
            );
        }
        return parseInitiatives(ilpProposalsNode, baseUrl);
    }

    private static InitiativesList parseInitiatives(final Node n,
                                                    final String baseUrl) throws DOMException,
            ParseException,
            IOException {
        final NodeList childNodes = n.getChildNodes();
        int idx = nextNodeElementIndex(childNodes, 0);
        final InitiativesList il = new InitiativesList();
        while (idx != -1) {
            il.addInitiative(new Initiative(childNodes.item(idx), baseUrl));
            idx = nextNodeElementIndex(childNodes, idx + 1);
        }
        return il;
    }

    /** Recupera el &iacute;ndice del siguiente nodo de la lista de tipo
     * <code>Element</code>. Empieza a comprobar los nodos a partir del
     * &iacute;ndice marcado. Si no encuentra un nodo de tipo <i>elemento</i>
     * devuelve -1.
     * @param nodes Listado de nodos.
     * @param currentIndex &Iacute;ndice del listado a partir del cual se empieza la
     *                     comprobaci&oacute;n.
     * @return &Iacute;ndice del siguiente node de tipo Element o -1 si no se
     *         encontr&oacute;. */
    private static int nextNodeElementIndex(final NodeList nodes, final int currentIndex) {
        Node node;
        int i = currentIndex;
        while (i < nodes.getLength()) {
            node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
