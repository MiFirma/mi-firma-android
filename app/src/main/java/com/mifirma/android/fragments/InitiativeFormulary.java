package com.mifirma.android.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.mifirma.android.MainActivity;
import com.mifirma.android.R;
import com.mifirma.android.keystore.CanDialog;
import com.mifirma.android.keystore.CanResult;
import com.mifirma.android.keystore.LoadKeyStoreManagerTask;
import com.mifirma.android.logic.Ilp;
import com.mifirma.android.model.Initiative;
import com.mifirma.android.model.RegionalIlpHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Formulario de los datos del usuario para firmar la iniciativa. */
public final class InitiativeFormulary extends Fragment {

    public static final String INITIATIVE = "initiative";
    public static final String DD_MM_YYYY = "dd/MM/yyyy";

    /** Iniciativa a firmar. */
    private Initiative initiative;
    /** Combo de las provincias. */
    private Spinner spinnerProvince;
    /** Texto asociado al combo de las provincias. */
    private TextView provinceText;
    /** Combo de las localidades. */
    private Spinner spinnerLocation;
    /** Texto asociado al combo de las localidades. */
    private TextView locationText;
    /** Nombre del usuario. */
    private EditText name;
    /** Primer apellido del usuario. */
    private EditText surnameFirst;
    /** Segundo apellido del usuario. */
    private EditText surnameSecond;
    /** DNI del usuario. */
    private EditText dni;
    /** Fecha de nacimiento del usuario. */
    private EditText birthday;
    /** Email del usuario. */
    private EditText email;
    /** Checkbox para confirmar que se han leidos las condiciones de uso. */
    private CheckBox checkBox;
    /** Boton para iniciar la firma. */
    private Button button;
    /** Calendario auxiliar para establecer la fechas de nacimiento del usuario. */
    private Calendar myCalendar;

    public static InitiativeFormulary newInstance(String param) {
        final InitiativeFormulary fragment = new InitiativeFormulary();
        final Bundle args = new Bundle();
        args.putString(INITIATIVE, param);
        fragment.setArguments(args);
        return fragment;
    }

    public InitiativeFormulary() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initiative = getArguments().getParcelable(INITIATIVE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_initiative_formulary, container, false);
        spinnerProvince = (Spinner) rootView.findViewById(R.id.formulary_province);
        provinceText = (TextView) rootView.findViewById(R.id.formulary_province_text);
        spinnerLocation = (Spinner) rootView.findViewById(R.id.formulary_location);
        locationText = (TextView) rootView.findViewById(R.id.formulary_location_text);
        name = (EditText) rootView.findViewById(R.id.name);
        surnameFirst = (EditText) rootView.findViewById(R.id.surname_first);
        surnameSecond = (EditText) rootView.findViewById(R.id.surname_second);
        dni = (EditText) rootView.findViewById(R.id.dni);
        birthday = (EditText) rootView.findViewById(R.id.birthday);
        email = (EditText) rootView.findViewById(R.id.email);
        checkBox = (CheckBox) rootView.findViewById(R.id.checkBox);
        button = (Button) rootView.findViewById(R.id.electronic_sign_button);

        if(MainActivity.getUseNfc()){
            name.setHint(R.string.hint_nfc);
            surnameFirst.setHint(R.string.hint_nfc);
            surnameSecond.setHint(R.string.hint_nfc);
            dni.setHint(R.string.hint_nfc);
            birthday.setHint(R.string.hint_nfc);
        }


        addListenerOnButton();

        // Se inicializan los combos Provincias y Localidades en caso de que se trate de una iniciativa autonomica.
        if(initiative.isAutonomic()){

            initiative.generateProvinces(this.getActivity());
            //Provincias
            final ArrayAdapter<RegionalIlpHelper.Province> adapterProvinces =
                new ArrayAdapter<RegionalIlpHelper.Province>(
                    this.getActivity(),
                    android.R.layout.simple_spinner_item,
                    android.R.id.text1,
                    initiative.getProvinces()
                );

            adapterProvinces.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerProvince.setAdapter(adapterProvinces);

            //Localidades
            spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    final ArrayAdapter<RegionalIlpHelper.Location> adapterLocation =
                        new ArrayAdapter<RegionalIlpHelper.Location>(view.getContext(),
                            android.R.layout.simple_spinner_item, android.R.id.text1, ((RegionalIlpHelper.Province) adapterView.getItemAtPosition(i)).getLocations());

                    adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerLocation.setAdapter(adapterLocation);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spinnerProvince.setVisibility(View.VISIBLE);
            provinceText.setVisibility(View.VISIBLE);

            spinnerLocation.setVisibility(View.VISIBLE);
            locationText.setVisibility(View.VISIBLE);
        }

        // CALENDARIO.

        TextWatcher tw = new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMAAAA";
            private Calendar cal = Calendar.getInstance();
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanC) && (sel == 6 || sel == 3)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }
                    else{
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format(Locale.US, "%02d%02d%02d",day, mon, year);
                        myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, mon);
                        myCalendar.set(Calendar.DAY_OF_MONTH, day);
                    }

                    clean = String.format(
                        "%s/%s/%s",
                        clean.substring(0, 2),
                        clean.substring(2, 4),
                        clean.substring(4, 8)
                    );

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    birthday.setText(current);
                    birthday.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        };

        birthday.addTextChangedListener(tw);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( isChecked ) {
                    // Eliminamos un posible error anterior.
                    checkBox.setError(null);
                }
                else {
                    checkBox.setError(getString(R.string.error_msg_terms));
                }
            }
        });

        return rootView;
    }

    /** Asociacion de eventos de botones. */
    public void addListenerOnButton() {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean validation = validateFields();
                if(validation){

                    final String provinceCode = spinnerProvince.getSelectedItem() != null ? ((RegionalIlpHelper.Province)spinnerProvince.getSelectedItem()).getIneCode() : "";
                    final String locationCode = spinnerLocation.getSelectedItem() != null ? ((RegionalIlpHelper.Location)spinnerLocation.getSelectedItem()).getIneCode() : "";
                    final Ilp ilp = new Ilp(
                        new Ilp.IlpSigner(
                            name.getText().toString(),
                            surnameFirst.getText().toString(),
                            surnameSecond.getText().toString(),
                            myCalendar != null ? myCalendar.get(Calendar.YEAR) : 0,
                            myCalendar != null ? myCalendar.get(Calendar.MONTH) : 0,
                            myCalendar != null ? myCalendar.get(Calendar.DAY_OF_MONTH) : 0,
                            Ilp.IlpSigner.IdType.DNI,
                            dni.getText().toString(),
                            email.getText().toString(),
                            checkBox.isChecked(),
                            provinceCode,
                            locationCode
                        ),
                        initiative.getTitle(),
                        Integer.toString(initiative.getId())
                    );

                    // Si no queremos usar NFC, almacen normal
                    if(!MainActivity.getUseNfc()){
                        new LoadKeyStoreManagerTask(InitiativeFormulary.this.getActivity()).execute(ilp);
                    }
                    // Si hemos indiado que si queremos usar NFC, pedimos CAN y PIN
                    else {
                        final CanResult canResult = new CanResult(ilp);
                        DialogFragment canDialog = CanDialog.newInstance(canResult);
                        canDialog.show(getActivity().getFragmentManager(), "dialog");
                    }
                }
            }
        });

    }

    /** Validacion del email. */
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /** Validacion de los campos del formulario. */
    private boolean validateFields() {
        boolean res = true;
        if(!MainActivity.getUseNfc() && !validateString(name.getText().toString(),true,true,0,0)){
            name.setError(getString(R.string.error_msg_name));
            res = false;
        }
        if(!MainActivity.getUseNfc() && !validateString(surnameFirst.getText().toString(),true,true,0,0)){
            surnameFirst.setError(getString(R.string.error_msg_surname_first));
            res = false;
        }
        if(!MainActivity.getUseNfc() && !validateString(surnameSecond.getText().toString(),true,true,0,0)){
            surnameSecond.setError(getString(R.string.error_msg_surname_second));
            res = false;
        }
        String dniAux = dni.getText().toString();
        if(!MainActivity.getUseNfc() && (!validateString(dniAux,true,true,0,0) || !comprobar(dniAux))){
            dni.setError(getString(R.string.error_msg_nif));
            res = false;
        }
        if(!MainActivity.getUseNfc() && (!validateString(birthday.getText().toString(),true,true,0,0) || !validateDate(birthday.getText().toString()))){
            birthday.setError(getString(R.string.error_msg_birthday));
            res = false;
        }
        String emailAux =  email.getText().toString();
        if(!validateString(emailAux,true,true,0,0) || !isValidEmail(emailAux)){
            email.setError(getString(R.string.error_msg_email));
            res = false;
        }
        if(!checkBox.isChecked()){
            checkBox.setError(getString(R.string.error_msg_terms));
            res = false;
        }

        return res;
    }

    /** Validador de formato de fecha. */
    public static boolean validateDate(String date) {
        final Date d;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DD_MM_YYYY, Locale.getDefault());
        try {
            dateFormat.setLenient(false);
            d = dateFormat.parse(date);
        }
        catch (final ParseException e) {
            return false;
        }
        return !(d == null || !date.equals(dateFormat.format(d)));
    }

    /** Validar cadenas de texto.
     * @param value valor a validar
     * @param n validar null
     * @param v validar vacio
     * @param minSize longitud minima
     * @param maxSize longitud maxima
     * @return true: validado. false: no valida
     */
    private boolean validateString(String value, boolean n, boolean v, int minSize, int maxSize){
        boolean res = true;
        if(n){
            res = value != null;
        }
        if(res && v){
            res = !value.equals("");
        }
        if(res && minSize > 0){
            res = value.length()>= minSize;
        }
        if(res && maxSize > 0){
            res = value.length() <= maxSize;
        }
        return res;
    }

    /** Comprueba que un n&uacute;mero de DNI es correcto. */
    private static boolean comprobar(String dniAComprobar){

        // Array con las letras posibles del dni en su posición
        char[] letraDni = {
                'T', 'R', 'W', 'A', 'G', 'M', 'Y', 'F', 'P', 'D',  'X',  'B', 'N', 'J', 'Z', 'S', 'Q', 'V', 'H', 'L', 'C', 'K', 'E'
        };

        String num= "";

        // existen dnis con 7 digitos numericos, si fuese el caso
        // le añado un cero al principio
        if(dniAComprobar.length() == 8) {
            dniAComprobar = "0" + dniAComprobar;
        }

        if(dniAComprobar.length() != 9){
            return false;
        }

        // compruebo que el 9º digito es una letra
        if (!Character.isLetter(dniAComprobar.charAt(8))) {
            return false;
        }

        // compruebo su longitud que sea 9
        if (dniAComprobar.length() != 9){
            return false;
        }

        // Compruebo que lo 8 primeros digitos sean numeros
        for (int i=0; i<8; i++) {

            if(!Character.isDigit(dniAComprobar.charAt(i))){
                return false;
            }
            // si es numero, lo recojo en un String
            num += dniAComprobar.charAt(i);
        }

        // paso a int el string donde tengo el numero del dni
        int ind = Integer.parseInt(num);

        // calculo la posición de la letra en el array que corresponde a este dni
        ind %= 23;

        // verifico que la letra del dni corresponde con la del array
        if ((Character.toUpperCase(dniAComprobar.charAt(8))) != letraDni[ind]){
            return false;
        }
        // si el flujo de la funcion llega aquí, es que el dni es correcto
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
