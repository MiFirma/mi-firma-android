package com.mifirma.android.adapter;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mifirma.android.R;

/** Elemento del adapter InitiativeAdapter. */
public class InitiativeItem extends Fragment {

    public static InitiativeItem newInstance() {
        InitiativeItem fragment = new InitiativeItem();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public InitiativeItem() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_initiative_item, container, false);
    }

}
