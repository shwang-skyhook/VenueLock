package com.skyhookwireless.venuelock;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by steveh on 2/18/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper
{
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    private String tableName;
    private static final String DATABASE_NAME = "venue_mac_bosphlsfo.db";
    private static final String BOSTON = "venue_mac_boston";
    private static final String PHILLY = "venue_mac_philly";
    private static final String SANFRAN = "venue_mac_sanfran";
    private static final String MAINE = "venue_mac_maine";
    public final static String DATABASE_PATH ="/data/data/com.skyhookwireless.venuelock/databases/";
    public static final int DATABASE_VERSION = 1;
    //public static final int DATABASE_VERSION_old = 1;

    //Constructor
    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        tableName = BOSTON;
    }


    //Create a empty database on the system
    public void createDataBase() throws IOException
    {
        boolean dbExist = checkDataBase();

        if(dbExist)
        {
            Log.v("DB Exists", "db exists");
            // By calling this method here onUpgrade will be called on a
            // writeable database, but only if the version number has been
            // bumped
            //onUpgrade(myDataBase, DATABASE_VERSION_old, DATABASE_VERSION);
        }

        boolean dbExist1 = checkDataBase();
        if(!dbExist1)
        {
            this.getReadableDatabase();
            try
            {
                this.close();
                copyDataBase();
            }
            catch (IOException e)
            {
                throw new Error(e.getMessage());
            }
        }
    }

    //Check database already exist or not
    private boolean checkDataBase()
    {
        boolean checkDB = false;
        try
        {
            String myPath = DATABASE_PATH + DATABASE_NAME;
            File dbfile = new File(myPath);
            checkDB = dbfile.exists();
        }
        catch(SQLiteException e)
        {
        }
        return checkDB;
    }

    //Copies your database from your local assets-folder to the just created empty database in the system folder
    private void copyDataBase() throws IOException
    {
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0)
        {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();
    }

    //delete database
    public void db_delete()
    {
        File file = new File(DATABASE_PATH + DATABASE_NAME);
        if(file.exists())
        {
            file.delete();
            System.out.println("delete database file.");
        }
    }

    //Open database
    public void openDataBase() throws SQLException
    {
        String myPath = DATABASE_PATH + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void selectTable(String city) {
        if (city != null) {
            switch (city) {
                case "Boston":
                    tableName = BOSTON;
                    break;
                case "Philly":
                    tableName = PHILLY;
                    break;
                case "SanFran":
                    tableName = SANFRAN;
                    break;
                case "Maine":
                    tableName = MAINE;
                    break;
                default:
                    tableName = BOSTON;
                    break;
            }
        }
        else {
            tableName = BOSTON;
        }

    }
    public synchronized void closeDataBase()throws SQLException
    {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    public void onCreate(SQLiteDatabase db)
    {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (newVersion > oldVersion)
        {
            Log.v("VenueLock Database", "Database version higher than old.");
            db_delete();
        }
    }

    //add your public methods for insert, get, delete and update data in database.

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.


    public String getVidForMac(String mac) {
        String s = "";
        try {
            Log.d("VenueLock Database", "getVidForMac: " + mac);
            String getVidQuery = "select vid from " + tableName + " where _id=\"" + mac + "\"" ;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(getVidQuery, null);
            cursor.moveToFirst();
            if (cursor.getCount() >= 1)
            {
                s = cursor.getString(0);
            }
            cursor.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return s;
        }

    }


    public ScannedVenue getScannedVenue(String mac) {
        try {
            Log.d("VenueLock Database", "getScannedVenue: " + mac);
            String getVidQuery = "select vid, vname, vlatitude, vlongitude from " + tableName + " where _id=\"" + mac + "\"" ;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(getVidQuery, null);
            String s = "";
            ScannedVenue scannedVenue = new ScannedVenue();

            if (cursor.moveToFirst()) {
                do {
                    scannedVenue.setVID(cursor.getString(0));
                    scannedVenue.setName(cursor.getString(1));
                    scannedVenue.setvLatLng(cursor.getString(2), cursor.getString(3));
                } while (cursor.moveToNext());
            };

            return scannedVenue;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}