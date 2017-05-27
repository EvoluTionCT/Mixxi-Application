package com.example.evolutionct.mixxi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by zarn_ on 18/4/2560.
 */

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "Thailand Only";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "Thailand";
    public static final String COL_NAME = "id";
    public static final String COL_ZIP = "zip";
    public static final String COL_PROVINCE = "province";
    public static final String COL_DIST = "district";
    public static final String COL_LAT= "lat";
    public static final String COL_LNG = "lng";

    Context context;
    SQLiteDatabase mdb;

    public Database(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DATA_TABLE = String.format("CREATE TABLE " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, " + COL_ZIP + " TEXT, "
                + COL_PROVINCE + " TEXT, " + COL_DIST + " TEXT, "
                + COL_LAT  + " DOUBLE, " + COL_LNG + " DOUBLE);");
        db.execSQL(CREATE_DATA_TABLE);

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(
                            "dat.csv")));
            String readLine = null;
            readLine = br.readLine();

            try {
                while ((readLine = br.readLine()) != null) {
                    String[] str = readLine.split(",");
                    db.execSQL("INSERT INTO " + TABLE_NAME
                            + " (" + COL_NAME + ", " + COL_ZIP
                            + ", " + COL_PROVINCE + ", " + COL_DIST
                            + ", " + COL_LAT + ", " + COL_LNG
                            + ") VALUES ('" + str[0]
                            + "', '" + str[1] + "', '" + str[2]
                            + "', '" + str[3] + "', " + str[4]
                            + ", " + str[5] + ");");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion
            , int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public List<CountryInfo> getList(Double startlat, Double startlng) {
        List<CountryInfo> data = new ArrayList<>();

        mdb = this.getWritableDatabase();
        mdb = this.getReadableDatabase();
        /*startlat = 12.674842 ;
        startlng = 101.365709;*/
        LatLng tempLatlng = null ;

        Cursor cursor = mdb.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        while ( !cursor.isAfterLast() ){
            CountryInfo countryInfo = new CountryInfo();
            double lat = cursor.getDouble(cursor.getColumnIndex(COL_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndex(COL_LNG));
            String city = cursor.getString(cursor.getColumnIndex(COL_DIST));
            double dist;

            //dist = Math.sqrt(Math.pow(69.1 * (lat - startlat ),2) + Math.pow(69.1 * (startlng - lng) * Math.cos(lat/ 57.3),2));
            int R = 6371; // Radius of the earth in km
            double dLat = deg2rad(lat-startlat);  // deg2rad below
            double dLon = deg2rad(lng-startlng);
            double a =
                    Math.sin(dLat/2) * Math.sin(dLat/2) +
                            Math.cos(deg2rad(startlat)) * Math.cos(deg2rad(lat)) *
                                    Math.sin(dLon/2) * Math.sin(dLon/2)
                    ;
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            dist = R * c; // Distance in km
            if(dist < 25) {
                //data.add("Distance : " + dist);
                tempLatlng = new LatLng(lat,lng);
                countryInfo.latlng = tempLatlng ;
                countryInfo.city = city ;
                countryInfo.dist = dist ;
                data.add(countryInfo);
               // data.add(tempLatlng);
            }
            cursor.moveToNext();
        }
        /*Log.i("DataTest", String.valueOf(data));
        Log.i("DataTest", String.valueOf(data.size()));*/
        mdb.close();

        return data;
    }
    public CountryInfo getNearLo(List<CountryInfo> city)
    {
        List<Double> tempDist = new ArrayList<>();
        for(int i =0;i<city.size();i++)
        {
           tempDist.add(city.get(i).dist);
        }
        int minIndex = tempDist.indexOf(Collections.min(tempDist));

        return city.get(minIndex) ;

    }


    private double deg2rad(double deg) {
        return deg * (Math.PI/180) ;
    }

}
