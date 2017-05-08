package com.mifirma.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.mifirma.android.model.RegionalIlpHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Clase gestora de la BBDD. */
public class RegionalSQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Regions.db";

    // Ficheros que contienen los datos.
    private static final String PROVINCES_FILE = "ListadoProvincias.csv";
    private static final String LOCATION_FILE = "ListadoMunicipios.csv";

    private final Context context;

    /** Datos de la entidad Province */
    static abstract class ProvinceEntry implements BaseColumns {
        static final String TABLE_NAME ="province";

        public static final String ID = "id";
        static final String NAME = "name";
        static final String CODE_INE = "cod_ine";
    }

    /** Datos de la entidad Location */
    static abstract class LocationEntry implements BaseColumns {
        static final String TABLE_NAME ="location";

        public static final String ID = "id";
        static final String PROVINCE_ID = "prov_id";
        static final String NAME = "name";
        static final String CODE_INE = "cod_ine";
    }

    public RegionalSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //PROVINCIAS
        sqLiteDatabase.execSQL("CREATE TABLE " + ProvinceEntry.TABLE_NAME + " ("
                + ProvinceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProvinceEntry.ID + " TEXT NOT NULL,"
                + ProvinceEntry.NAME + " TEXT NOT NULL,"
                + ProvinceEntry.CODE_INE + " TEXT NOT NULL, "
                + "UNIQUE (" + ProvinceEntry.ID + ", " + ProvinceEntry.CODE_INE + "))");

        // Insertamos los datos desde el fichero.
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(PROVINCES_FILE);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;

            sqLiteDatabase.beginTransaction();
            while ((line = buffer.readLine()) != null) {

                String[] str = line.split("\t");

                ContentValues values = new ContentValues();
                values.put(ProvinceEntry.ID, str[0]);
                values.put(ProvinceEntry.CODE_INE, str[1]);
                values.put(ProvinceEntry.NAME, str[2]);

                sqLiteDatabase.insert(
                        ProvinceEntry.TABLE_NAME,
                        null,
                        values);
            }
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //LOCALIDADES
        sqLiteDatabase.execSQL("CREATE TABLE " + LocationEntry.TABLE_NAME + " ("
                + LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LocationEntry.ID + " TEXT NOT NULL,"
                + LocationEntry.NAME + " TEXT NOT NULL,"
                + LocationEntry.CODE_INE + " TEXT NOT NULL, "
                + LocationEntry.PROVINCE_ID + " TEXT NOT NULL, "
                + "UNIQUE (" + ProvinceEntry.ID + ", " + ProvinceEntry.CODE_INE + "))");

        // Insertamos los datos desde el fichero.
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(LOCATION_FILE);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;

            sqLiteDatabase.beginTransaction();
            while ((line = buffer.readLine()) != null) {

                String[] str = line.split("\t");

                ContentValues values = new ContentValues();
                values.put(LocationEntry.ID, str[0]);
                values.put(LocationEntry.CODE_INE, str[1]);
                values.put(LocationEntry.NAME, str[2]);
                values.put(LocationEntry.PROVINCE_ID, str[3]);

                sqLiteDatabase.insert(
                        LocationEntry.TABLE_NAME,
                        null,
                        values);
            }
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /** Obtener datos de la BBDD
     * @param codProvinces Codigos de las provincias
     * @return Listado de provincias con sus localidades asociadas.
     */
    public List<RegionalIlpHelper.Province> getProvinces(String[] codProvinces){
        List<RegionalIlpHelper.Province> res = new ArrayList<RegionalIlpHelper.Province>();

        // Formateo del listado de provincias para hacer la busqueda.
        String codProvincesFormated = "(";

        for (int i = 0; i < codProvinces.length; i++){
            codProvincesFormated += "'" + codProvinces[i] + "'";
            if(i < codProvinces.length -1){
                codProvincesFormated += ",";
            }
        }
        codProvincesFormated += ")";

        // Consulta provincias.
        String query = "select * from " + ProvinceEntry.TABLE_NAME + " WHERE "+ ProvinceEntry.ID+" IN " + codProvincesFormated;
        Cursor c = this.getReadableDatabase().rawQuery(query, null);
        while(c.moveToNext()){
            String id = c.getString(c.getColumnIndex(ProvinceEntry.ID));
            String name = c.getString(c.getColumnIndex(ProvinceEntry.NAME));
            String codINE = c.getString(c.getColumnIndex(ProvinceEntry.CODE_INE));

            // Obtenemos localizaciones.
            List<RegionalIlpHelper.Location> locations = new ArrayList<RegionalIlpHelper.Location>();

            String queryLocation = "select * from " + LocationEntry.TABLE_NAME + " WHERE "+ LocationEntry.PROVINCE_ID+"=?";
            Cursor cLocation = this.getReadableDatabase().rawQuery(queryLocation, new String[]{codINE});
            while(cLocation.moveToNext()){
                String idLocation = cLocation.getString(cLocation.getColumnIndex(LocationEntry.ID));
                String nameLocation = cLocation.getString(cLocation.getColumnIndex(LocationEntry.NAME));
                String codINELocation = cLocation.getString(cLocation.getColumnIndex(LocationEntry.CODE_INE));
                RegionalIlpHelper.Location location = new RegionalIlpHelper.Location(idLocation, nameLocation, codINELocation);
                locations.add(location);
            }

            RegionalIlpHelper.Province province = new RegionalIlpHelper.Province(id, name, codINE, locations);

            res.add(province);
        }

        return res;
    }
}