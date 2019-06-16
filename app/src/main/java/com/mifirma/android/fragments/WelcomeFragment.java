package com.mifirma.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mifirma.android.BuildConfig;
import com.mifirma.android.R;
import com.mifirma.android.logic.RequestInitiativesTask;
import com.mifirma.android.util.Utils;

/** Ventana de inicio de la app. */
public class WelcomeFragment extends Fragment {

    /** Boton para entrar en la aplicacion. */
    private Button button;

    public static WelcomeFragment newInstance() {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        button = rootView.findViewById(R.id.welcome_button);
        addListenerOnButton();

        // Pintamos el nombre de la version.
        String versionName = BuildConfig.VERSION_NAME;
        TextView version = rootView.findViewById(R.id.version);
        TextView welcome = rootView.findViewById(R.id.welcome);
        welcome.setMovementMethod(LinkMovementMethod.getInstance());
        version.setText(versionName);

        return rootView;
    }

    public void addListenerOnButton() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(Utils.verificaConexion(WelcomeFragment.this.getActivity())){
                    new RequestInitiativesTask(WelcomeFragment.this.getActivity()).execute();
                }
                else{
                    Toast.makeText(
                        WelcomeFragment.this.getActivity().getBaseContext(),
                        R.string.connection_msg,
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
}
