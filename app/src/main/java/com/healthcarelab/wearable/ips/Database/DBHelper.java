package com.healthcarelab.wearable.ips.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper{

    public static final String TABLE_RSSI = "table_rssi";
    public static final String COL_ID = "ID";
    public static final String COL_X = "Xcoordinate ";
    public static final String COL_Y = "Ycoordinate ";
    public static final String COL_WIFI1 = "WIFI1";
    public static final String COL_WIFI2 = "WIFI2";
    public static final String COL_WIFI3 = "WIFI3";


    private static final String CREATE_BDD = " CREATE TABLE " + " TABLE_RSSI " + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_X + " INTEGER NOT NULL, " + COL_Y + " INTEGER NOT NULL, "
            + COL_WIFI1 + " INTEGER NOT NULL, " + COL_WIFI2 + " INTEGER NOT NULL, " + COL_WIFI3 + " INTEGER NOT NULL) ;";


    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BDD);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " +  TABLE_RSSI + ";");
        onCreate(db);
    }
}
