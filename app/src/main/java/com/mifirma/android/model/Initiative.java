package com.mifirma.android.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Base64;

import com.mifirma.android.MainActivity;

import es.gob.afirma.core.misc.http.UrlHttpManagerFactory;
import es.gob.afirma.core.misc.http.UrlHttpMethod;

//import es.gob.afirma.core.misc.http.UrlHttpManagerFactory;

/** Resumen de una iniciativa.
 * @author Juliana Marulanda
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class Initiative implements Parcelable {


    private static final Logger LOGGER = Logger.getLogger("com.mifirma.android"); //$NON-NLS-1$

    private static final String XML_ELEMENT_NAME = "name"; //$NON-NLS-1$
    private static final String XML_ELEMENT_PROMOTER = "promoter-name"; //$NON-NLS-1$
    private static final String XML_ELEMENT_PROMOTER_URL = "promoter-url"; //$NON-NLS-1$
    private static final String XML_ELEMENT_BANNER_FILENAME = "banner-file-name"; //$NON-NLS-1$
    private static final String XML_ELEMENT_REQUIRED_SIGNATURES = "num-required-signatures"; //$NON-NLS-1$
    private static final String XML_ELEMENT_ACTUAL_SIGNATURES = "handwritten-signatures"; //$NON-NLS-1$
    private static final String XML_ELEMENT_ID = "id"; //$NON-NLS-1$
    private static final String XML_ELEMENT_HOWTOSOLVE = "howto-solve"; //$NON-NLS-1$
    private static final String XML_ELEMENT_CREATED_AT = "created-at"; //$NON-NLS-1$
    private static final String XML_ELEMENT_CODE = "ilp-code"; //$NON-NLS-1$
    private static final String XML_ELEMENT_SUBTYPE = "subtype";
    private static final String XML_ELEMENT_SUBTYPE_PROVINCES = "subtype-provinces";

    private String title;
    private String promoter;
    private URL promoterUrl;
    private byte[] banner;
    private Date initDate;
    private Date endDate;
    private int numRequiredSignatures;
    private int numActualSignatures;
    private int id;
    private String howtoSolveAsHtml;
    private String code;
    private boolean autonomic = false;
    private List<RegionalIlpHelper.Province> provinces;
    private String[] provincesCodes;

    @Override
    public String toString() {
        return
            " Identificador: " + this.id + '\n' + //$NON-NLS-1$
                " Titulo: " + this.title + '\n' + //$NON-NLS-1$
                " Promotor: " + this.promoter + '\n' + //$NON-NLS-1$
                " Sitio Web del promotor: " + this.promoterUrl  + '\n' + //$NON-NLS-1$
                " Fecha de inicio: " + this.initDate + '\n' + //$NON-NLS-1$
                " Fecha de fin: " + this.endDate + '\n' + //$NON-NLS-1$
                " Firmas necesarias: " + this.numRequiredSignatures + '\n' + //$NON-NLS-1$
                " Firmas recogidas: " + this.numActualSignatures + '\n' + //$NON-NLS-1$
                " Codigo: " + this.code //$NON-NLS-1$
            ;
    }

    /** Crea una iniciativa a partir del nodo de su definici&oacute;n XML.
     * @param node Nodo XML de definici&oacute;n de la iniciativa.
     * @param imagesBaseUrl URL base para la descarga de im&aacute;genes.
     * @throws org.w3c.dom.DOMException Si hay problemas analizando el XML.
     * @throws java.text.ParseException Si uno de los campos del nodo de tipo fecha no contiene un texto de fecha
     *                        en el formato ISO-8601 adecuado (por ejemplo '2015-01-21T19:27:36Z').
     * @throws java.io.IOException Si no se pueden descargar las im&aacute;genes indicadas en los campos.
     * @throws NumberFormatException Si uno de los campos del nodo de tipo entero no contiene un
     *                               n&uacute;mero entero. */
     public Initiative(final Node node, final String imagesBaseUrl) throws DOMException, ParseException, IOException {
        if (!(node instanceof Element)) {
            throw new IllegalArgumentException(
                    "El nodo de definicion de iniciativa debe ser un Elemento DOM" //$NON-NLS-1$
            );
        }
        final Element el = (Element)node;

        this.title = el.getElementsByTagName(XML_ELEMENT_NAME).item(0).getTextContent().trim();
        this.promoter = el.getElementsByTagName(XML_ELEMENT_PROMOTER).item(0).getTextContent().trim();
        try {
            this.promoterUrl = new URL(el.getElementsByTagName(XML_ELEMENT_PROMOTER_URL).item(0).getTextContent().trim());
        }
        catch(final Exception e) {
            this.promoterUrl = new URL("http://" + el.getElementsByTagName(XML_ELEMENT_PROMOTER_URL).item(0).getTextContent().trim());
        }
        this.id = Integer.parseInt(el.getElementsByTagName(XML_ELEMENT_ID).item(0).getTextContent().trim());
        this.banner = getImage(
                el.getElementsByTagName(XML_ELEMENT_BANNER_FILENAME).item(0).getTextContent().trim(),
                imagesBaseUrl,
                this.id
        );
        this.initDate = getDate(el.getElementsByTagName(XML_ELEMENT_CREATED_AT).item(0).getTextContent().trim());
        this.endDate = getDate(el.getElementsByTagName(XML_ELEMENT_CREATED_AT).item(0).getTextContent().trim());
        this.numRequiredSignatures = Integer.parseInt(el.getElementsByTagName(XML_ELEMENT_REQUIRED_SIGNATURES).item(0).getTextContent().trim());
        this.numActualSignatures = Integer.parseInt(el.getElementsByTagName(XML_ELEMENT_ACTUAL_SIGNATURES).item(0).getTextContent().trim());
        this.howtoSolveAsHtml = el.getElementsByTagName(XML_ELEMENT_HOWTOSOLVE).item(0).getTextContent().trim();

        this.code = el.getElementsByTagName(XML_ELEMENT_CODE).item(0).getTextContent().trim();

        NodeList nl = el.getElementsByTagName(XML_ELEMENT_SUBTYPE);
        if (nl != null && nl.getLength() > 0) {
            final String nc = nl.item(0).getTextContent();
            if (nc != null && "AUTONOMICA".equalsIgnoreCase(nc.trim())) {
                autonomic = true;
            }
        }

        if (autonomic) {
            nl = el.getElementsByTagName(XML_ELEMENT_SUBTYPE_PROVINCES);
            if (nl != null && nl.getLength() > 0) {
                final String nc = nl.item(0).getTextContent();
                if (nc != null) {
                    provincesCodes = nc.trim().split(",");
                }
                provinces = new ArrayList<>(0);
            }
        }
    }

    public void generateProvinces(Context context){

        if(provinces == null || provinces.isEmpty()){
            provinces = RegionalIlpHelper.getProvinces(context, provincesCodes);
        }
    }

    /** Convierte una cadena de texto en formato <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a> (por ejemplo '2015-01-21T19:27:36Z')
     * en un <code>Date</code>.
     * @param date Fecha en formato <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a>.
     * @return <code>Date</code> equivalente al texto.
     * @throws java.text.ParseException Si el texto no est&aacute; en el formato ISO-8601 adecuado (por ejemplo '2015-01-21T19:27:36Z'). */
    private static Date getDate(final String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date); //$NON-NLS-1$
    }

    private static byte[] getImage(final String bannerFileName,
                                   final String baseUrl,
                                   final int id) throws IOException {
        if (baseUrl != null && bannerFileName != null) {
            final String url = (baseUrl + (baseUrl.endsWith("/") ? "" : "/") + bannerFileName).replace(" ", "%20").replace("$$ID$$", Integer.toString(id)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            LOGGER.info("Descargando imagen de la iniciativa desde " + url); //$NON-NLS-1$

            if (!MainActivity.DEBUG) {
                return UrlHttpManagerFactory.getInstalledManager().readUrl(url, -1, "application/xml", "text/xml", UrlHttpMethod.GET);
            }
            else {
                return UrlHttpManagerFactory.getInstalledManager().readUrl("https://www.google.es/images/branding/googleg/1x/googleg_standard_color_128dp.png", -1, "application/xml", "text/xml", UrlHttpMethod.GET);
            }

        }
        return null;
    }

    public String toHtmlString(){
        String stringToStore = new String(Base64.encode(this.getBanner(),Base64.DEFAULT));
        //return "<html><img src=\"data:image/png;base64, "+stringToStore+"\" style=\"width: 10%;\"/><h3>"+this.getTitle()+"</h3>"+this.getHowtoSolveAsHtml()+"</html>";
        return "<html><body>\n" +
                "\n" +
                "<div style=\"\n" +
                "    overflow:hidden;\n" +
                "    margin:0 auto;\">\n" +
                "    <div style=\"\n" +
                "    display:inline-block;\n" +
                "    vertical-align:middle;\n" +
                "    width:20%;" +
                "    margin:1% \">\n" +
                "        <div style=\"\n" +
                "    float:left;\">\n" +
                "            <img src=\"data:image/png;base64,"+stringToStore+"\" style=\"width:100%\">\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <div style=\"\n" +
                "    display:inline-block;\n" +
                "    vertical-align:middle;\n" +
                "    width:76%;\">\n" +
                "        <div style=\"\n" +
                "    float:right;\">\n" +
                "            <p style=\"width:100%; font-size: 1.3em;\">"+this.getTitle()+"</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                this.getHowtoSolveAsHtml() +
                "</body></html>";
    }

    Initiative(final String tit,
               final String prom,
               final URL promUrl,
               final byte[] bnnr,
               final Date initDt,
               final Date endDt,
               final int requiredSigs,
               final int actualSigs,
               final int identifier,
               final String howtoSolve,
               final String cod) {

        if (tit == null || tit.isEmpty()) {
            throw new IllegalArgumentException("El titulo no puede ser nulo ni vacio"); //$NON-NLS-1$
        }
        if (prom == null || prom.isEmpty()) {
            throw new IllegalArgumentException("El promotor no puede ser nulo ni vacio"); //$NON-NLS-1$
        }
        if (promUrl == null || promUrl.getPath().isEmpty()) {
            throw new IllegalArgumentException("La url del promotor no puede ser nula ni vacia"); //$NON-NLS-1$
        }
        if (initDt == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser nula"); //$NON-NLS-1$
        }
        if (endDt == null) {
            throw new IllegalArgumentException("La fecha de finalizacion no puede ser nula"); //$NON-NLS-1$
        }
        if (howtoSolve == null || howtoSolve.isEmpty()) {
            throw new IllegalArgumentException("El parametro 'howToSolve' no puede ser nulo ni vacio"); //$NON-NLS-1$
        }
        if (cod == null || cod.isEmpty()) {
            throw new IllegalArgumentException("El codigo no puede ser nulo ni vacio"); //$NON-NLS-1$
        }

        this.title = tit;
        this.promoter = prom;
        this.banner = bnnr;
        this.initDate = initDt;
        this.endDate = endDt;
        this.numRequiredSignatures = requiredSigs;
        this.id = identifier;
        this.promoterUrl = promUrl;
        this.numActualSignatures = actualSigs;
        this.howtoSolveAsHtml = howtoSolve;
        this.code = cod;
    }

    public String getTitle() {
        return this.title;
    }

    String getPromoter() {
        return this.promoter;
    }

    URL getPromoterUrl() {
        return  this.promoterUrl;
    }

    public byte[] getBanner() {
        return this.banner;
    }

    Date getInitDate() {
        return this.initDate;
    }

    Date getEndDate() {
        return this.endDate;
    }

    int getNumRequiredSignatures() {
        return this.numRequiredSignatures;
    }

    int getNumActualSignatures() {
        return this.numActualSignatures;
    }

    public int getId() {
        return this.id;
    }

    public String getHowtoSolveAsHtml() {
        return this.howtoSolveAsHtml;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isAutonomic(){
        return autonomic;
    }

    public List<RegionalIlpHelper.Province> getProvinces(){
        return provinces;
    }

    public List<String> getProvincesNames() {
        List<String> res = new ArrayList<>(provinces.size());
        for(final RegionalIlpHelper.Province p : provinces){
            res.add(p.toString());
        }
        return res;
    }

    public RegionalIlpHelper.Province getProvince(final String name) {
        if (name == null) {
            return null;
        }
        for(final RegionalIlpHelper.Province p : provinces){
            if (p.toString().equals(name.trim())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(this.title);
        parcel.writeString(this.promoter);
        parcel.writeInt(this.banner.length);
        parcel.writeByteArray(this.banner);
        parcel.writeSerializable(this.initDate);
        parcel.writeSerializable(this.endDate);
        parcel.writeInt(this.numRequiredSignatures);
        parcel.writeInt(this.id);
        parcel.writeString(this.promoterUrl.toString());
        parcel.writeInt(this.numActualSignatures);
        parcel.writeString(this.howtoSolveAsHtml);
    }

    public void readFromParcel(Parcel in){

        String tit = in.readString();
        if (tit == null || tit.isEmpty()) {
            throw new IllegalArgumentException("El titulo no puede ser nulo ni vacio"); //$NON-NLS-1$
        }
        this.title = tit;

        this.promoter = in.readString();
        this.banner = new byte[in.readInt()];
        in.readByteArray(this.banner);

        this.initDate = (Date) in.readSerializable();
        this.endDate = (Date) in.readSerializable();

        this.numRequiredSignatures = in.readInt();
        this.id = in.readInt();
        try {
            this.promoterUrl = new URL(in.readString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.numActualSignatures = in.readInt();
        this.howtoSolveAsHtml = in.readString();

    }

    public String getCode() {
        return code;
    }
}