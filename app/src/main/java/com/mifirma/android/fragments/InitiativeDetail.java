package com.mifirma.android.fragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.mifirma.android.R;
import com.mifirma.android.model.Initiative;

/** Detalle de la iniciativa. */
public class InitiativeDetail extends Fragment {

    private static final String DETAIL = "detail";

    /** Iniciativa que va a ser mostrada. */
    private Initiative initiative;

    /** Boton para realizar la firma. */
    private Button button;

    public static InitiativeDetail newInstance(String param) {
        InitiativeDetail fragment = new InitiativeDetail();
        Bundle args = new Bundle();
        args.putString(DETAIL, param);
        fragment.setArguments(args);
        return fragment;
    }

    public InitiativeDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initiative = getArguments().getParcelable(DETAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_initiative_detail, container, false);
        final WebView content = rootView.findViewById(R.id.initiative_web_view);
        button = rootView.findViewById(R.id.sign_button);

        // Cargamos el contenido en el WebView.
        content.loadDataWithBaseURL("", initiative.toHtmlString(), "text/html", "UTF-8", "");

        addListenerOnButton();

        return rootView;
    }

    public void addListenerOnButton() {

        button.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    // Creamos el fragment Formulario con los datos de la iniciativa.
                    Bundle data = new Bundle();
                    data.putParcelable(InitiativeFormulary.INITIATIVE, initiative);
                    Fragment newFragment = new InitiativeFormulary();
                    newFragment.setArguments(data);

                    FragmentTransaction transaction = InitiativeDetail.this.getActivity().getFragmentManager()
                        .beginTransaction();

                    transaction.replace(R.id.container, newFragment);
                    transaction.addToBackStack(null);

                    transaction.commit();
                }
            }
        );

    }
}