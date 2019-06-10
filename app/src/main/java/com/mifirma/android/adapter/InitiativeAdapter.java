package com.mifirma.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mifirma.android.R;
import com.mifirma.android.model.Initiative;

import java.util.ArrayList;

/** Adapter para gestionar el listado de Iniciativas. */
public class InitiativeAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Initiative> initiativeList;

    /** Constructor de InitiativeAdapter
     * @param context Contexto en el que se encuentra el listado.
     * @param initiativeList Listado que contiene los elementos.
     */
    public InitiativeAdapter( Context context, ArrayList<Initiative> initiativeList) {
        super();
        this.context = context;
        this.initiativeList = initiativeList;
    }

    public View getView(int position, View convertView,
                        ViewGroup parent) {
        if(convertView==null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.fragment_initiative_item, parent, false);
        }
        // Obtenemos el elemento.
        Initiative objectItem = initiativeList.get(position);

        // Componemos la vista con los datos del elemento.
        TextView titleTextViewItem = convertView.findViewById(R.id.initiative_title);

        byte[] byteArray = objectItem.getBanner();
        ImageView imageView = convertView.findViewById(R.id.initiative_image);
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imageView.setImageBitmap(bmp);
        imageView.setMaxHeight(5);

        titleTextViewItem.setText(objectItem.getTitle());

        return convertView;
    }

    public int getCount() {
        return initiativeList.size();
    }

    public Object getItem(int arg0) {
        return initiativeList.get(arg0);
    }

    public long getItemId(int position) {
        return position;
    }
}