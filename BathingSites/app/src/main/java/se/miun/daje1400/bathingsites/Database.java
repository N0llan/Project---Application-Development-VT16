package se.miun.daje1400.bathingsites;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class Database{

    private SQLiteDatabase database;
    private MySQLiteClass dbHelper;

    //Construct
    public Database(Context context){
        dbHelper = new MySQLiteClass(context);
    }

    //Function returns a list of all bathingsites in database
    public List<String[]> getAllBathingSites(){
        List<String[]> bathingSites = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + MySQLiteClass.TABLE_NAME, null);   //Sends query to database
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){  //Loop them
            String [] tmp = new String[8];
            tmp[0] = cursor.getString(1);
            tmp[1] = cursor.getString(2);
            tmp[2] = cursor.getString(3);
            tmp[3] = cursor.getString(4);
            tmp[4] = cursor.getString(5);
            tmp[5] = cursor.getString(6);
            tmp[6] = cursor.getString(7);
            tmp[7] = cursor.getString(8);
            bathingSites.add(tmp);
            cursor.moveToNext();
        }
        cursor.close();
        return bathingSites; //Return vector with bathingsites
    }

    //Function saves one bathsite to database
    public void saveBathingSite(List<String> bathing_values){
        ContentValues values = new ContentValues();
        values.put(MySQLiteClass.NAME, bathing_values.get(0));
        values.put(MySQLiteClass.DESCRIPTION, bathing_values.get(1));
        values.put(MySQLiteClass.ADDRESS, bathing_values.get(2));
        values.put(MySQLiteClass.LONGITUDE, bathing_values.get(3));
        values.put(MySQLiteClass.LATITUDE, bathing_values.get(4));
        values.put(MySQLiteClass.STARRATING, bathing_values.get(5));
        values.put(MySQLiteClass.WATER_TEMP, bathing_values.get(6));
        values.put(MySQLiteClass.WATER_TEMP_DATE, bathing_values.get(7));
        database.insert(MySQLiteClass.TABLE_NAME,null, values);
    }

    //Checks if bathsite already exists (long and lat) in database
    public boolean exists(String longitude, String latitude){
        Cursor cursor = database.rawQuery("SELECT "+MySQLiteClass.LATITUDE + "," + MySQLiteClass.LONGITUDE + " FROM " + MySQLiteClass.TABLE_NAME,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            if ((cursor.getString(1).equals(longitude) && cursor.getString(0).equals(latitude) && (!longitude.equals("") && !latitude.equals("")))){
                cursor.close();
                return true;
            }
            cursor.moveToNext();
        }
        cursor.close();
        return false;
    }

    //Returns the number of bathingsites in database
    public int getNumOfBathsites(){
        Cursor cursor = database.rawQuery("SELECT * FROM " + MySQLiteClass.TABLE_NAME, null);
        int numBathSites = cursor.getCount();
        cursor.close();
        return numBathSites;
    }

    //Opens the database
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    //Closes the database
    public void close(){
        dbHelper.close();
    }

    //Klassen MySQLiteClass
    public class MySQLiteClass extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "badplatser.db";
        public static final String TABLE_NAME = "badplatser";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String STARRATING = "starrating";
        public static final String DESCRIPTION = "description";
        public static final String WATER_TEMP = "water_temp";
        public static final String WATER_TEMP_DATE = "water_temp_date";

        private static final int DATABASE_VERSION = 1;
        public static final String COLUMN_ID = "_id";
        private static final String DATABASE_CREATE=
                "create table " + TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, " + NAME + " text, " +
                DESCRIPTION + " text, " + ADDRESS + " text, " + LONGITUDE + " text, " + LATITUDE + " text, " + STARRATING + " text, " +
                WATER_TEMP + " text, " + WATER_TEMP_DATE +" text);";

        public MySQLiteClass(Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

    }
}

