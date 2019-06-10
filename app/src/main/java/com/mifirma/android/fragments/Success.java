package com.mifirma.android.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mifirma.android.R;
import com.mifirma.android.util.Utils;

/** Ventana que muestra si ha ido correctamente la firma. */
public class Success extends Fragment{

    private Button button;

    public static Success newInstance(String param1, String param2) {
        Success fragment = new Success();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public Success() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_success, container, false);

        button = rootView.findViewById(R.id.success_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Volvemos al inicio.
                Success.this.getFragmentManager().popBackStack(Utils.HOME_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Fragment newFragment = new WelcomeFragment();
                FragmentTransaction transaction = Success.this.getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, newFragment);
                transaction.commit();
            }
        });

        return rootView;
    }


}
