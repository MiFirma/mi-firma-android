package com.mifirma.android.logic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.mifirma.android.fragments.InitiativeFragment;
import com.mifirma.android.R;
import com.mifirma.android.model.Initiative;

import java.util.ArrayList;

public class RequestInitiativesTask extends AsyncTask<ArrayList<Initiative>, ArrayList<Initiative>, ArrayList<Initiative>> {

    private  static final String TAG = "RequestInitiativesTask";
    private Activity context;
    private ProgressDialog progDailog;
    private InitiativeFragment fragment;

    public RequestInitiativesTask(Activity context){
        this.context = context;
    }
    public RequestInitiativesTask(Activity context, InitiativeFragment fragment){
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDailog = new ProgressDialog(context);
        progDailog.setMessage(context.getResources().getString(R.string.loading));
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);
        progDailog.show();
    }

    @Override
    protected ArrayList<Initiative> doInBackground(ArrayList<Initiative>... uri) {
        try {
            return InitiativesList.getInitiativesList(context).getInitiatives();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Initiative> result) {
        super.onPostExecute(result);
        progDailog.dismiss();

        boolean insideList = false;
        if(fragment != null){
            insideList = true;
        }

        if(result != null) {
            if(insideList){
                fragment.getInitiativeList().clear();
                fragment.getInitiativeList().addAll(result);
                ((BaseAdapter) fragment.getmListView().getAdapter()).notifyDataSetChanged();
            }else {
                Bundle data = new Bundle();
                data.putParcelableArrayList("result", result);

                Log.v(TAG, "result= " + result);
                Fragment newFragment = new InitiativeFragment();

                newFragment.setArguments(data);

                FragmentTransaction transaction = context.getFragmentManager().beginTransaction();

                transaction.replace(R.id.container, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        }else{
            if(insideList){
                Toast.makeText(context,
                        R.string.get_initiatives_error_list, Toast.LENGTH_LONG)
                        .show();
            }else {
                Toast.makeText(context,
                        R.string.get_initiatives_error, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }


}