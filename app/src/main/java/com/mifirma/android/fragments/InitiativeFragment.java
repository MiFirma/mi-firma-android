package com.mifirma.android.fragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;


import com.mifirma.android.logic.RequestInitiativesTask;
import com.mifirma.android.adapter.InitiativeAdapter;
import com.mifirma.android.R;
import com.mifirma.android.model.Initiative;
import com.mifirma.android.util.Utils;

import java.util.ArrayList;

public final class InitiativeFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String RESULT = "result";
    private static final String DETAIL = "detail";
    private ArrayList<Initiative> initiativeList;

    public ArrayList<Initiative> getInitiativeList() {
        return initiativeList;
    }

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    public static InitiativeFragment newInstance(String param) {
        InitiativeFragment fragment = new InitiativeFragment();
        Bundle args = new Bundle();
        args.putString(RESULT, param);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InitiativeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            initiativeList = getArguments().getParcelableArrayList(RESULT);
        }

        mAdapter = new InitiativeAdapter(getActivity(), initiativeList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iniciative, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Bundle data = new Bundle();

            Fragment newFragment = new InitiativeDetail();
            Initiative initiat = initiativeList.get(position);
            data.putParcelable(DETAIL, initiat);

            newFragment.setArguments(data);

            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();

            transaction.replace(R.id.container, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.refresh_button).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh_button) {
            if(Utils.verificaConexion(InitiativeFragment.this.getActivity())){
                new RequestInitiativesTask(InitiativeFragment.this.getActivity(),InitiativeFragment.this).execute();
            }
            else{
                Toast.makeText(InitiativeFragment.this.getActivity().getBaseContext(),
                        R.string.connection_msg, Toast.LENGTH_SHORT)
                        .show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public AbsListView getmListView() {
        return mListView;
    }

}
