package com.atomrockets.continuoustobolus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_OPTIONNAME = "OptionName";
	public static final String KEY_OPTIONVALUE = "OptionValue";
	
	private static final String TAG = "DBAdapter";
	
	private static final String DATABASE_NAME = "ContinuousToBolus";
    private static final String DATABASE_TABLE = "tblOptions";
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_OPTIONNAME + " text not null,"
        + KEY_OPTIONVALUE + " text not null);";
	
	
	private final Context context;
	
	private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    
    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

	private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            
            //---Setting default options in the DB, used in db creation only---
            ContentValues initialValues = new ContentValues();
        	initialValues.put(KEY_OPTIONNAME, "duration");
        	initialValues.put(KEY_OPTIONVALUE, "180");
        	db.insert(DATABASE_TABLE, null, initialValues);
        	
        	initialValues.put(KEY_OPTIONNAME, "maxDuration");
        	initialValues.put(KEY_OPTIONVALUE, "180");
        	db.insert(DATABASE_TABLE, null, initialValues);
        	
        	initialValues.put(KEY_OPTIONNAME, "stoppageDuration");
        	initialValues.put(KEY_OPTIONVALUE, "0");
        	db.insert(DATABASE_TABLE, null, initialValues);
        	
        	initialValues.put(KEY_OPTIONNAME, "maxStoppage");
        	initialValues.put(KEY_OPTIONVALUE, "480");
        	db.insert(DATABASE_TABLE, null, initialValues);
        	
        	initialValues.put(KEY_OPTIONNAME, "neededCalPerKgDay");
        	initialValues.put(KEY_OPTIONVALUE, "100");
        	db.insert(DATABASE_TABLE, null, initialValues);
        	
        	initialValues.put(KEY_OPTIONNAME, "milkProvidesCalPerOz");
        	initialValues.put(KEY_OPTIONVALUE, "22");
        	db.insert(DATABASE_TABLE, null, initialValues);
        	
        	initialValues.put(KEY_OPTIONNAME, "units");
        	initialValues.put(KEY_OPTIONVALUE, "SI");
        	db.insert(DATABASE_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion
                  + " to "
                  + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    //---opens the database---
    public DBAdapter open() throws SQLException {
    	Log.v(TAG, "Getting the Writable Database with the DBHelper");
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close() {
		DBHelper.close();
    }
    
    //---insert option into database---
    public long insertOption(String optionName, String optionValue) {
    	if(!optionIsSet(optionName)) {
	    	ContentValues initialValues = new ContentValues();
	    	initialValues.put(KEY_OPTIONNAME, optionName);
	    	initialValues.put(KEY_OPTIONVALUE, optionValue);
	    	return db.insert(DATABASE_TABLE, null, initialValues);
    	} else {
    		ContentValues initialValues = new ContentValues();
	    	initialValues.put(KEY_OPTIONNAME, optionName);
	    	initialValues.put(KEY_OPTIONVALUE, optionValue);
	    	return db.update(DATABASE_TABLE, initialValues, KEY_OPTIONNAME + "=" + "?", new String[] {optionName});
    	}
    }
    public long insertOption(String optionName, int optionValue) {
    	String optionValueStr=Integer.toString(optionValue);
    	return insertOption(optionName,optionValueStr);
    }
    public String getOptionValue(String optionName) {
    	Cursor c = db.query(DATABASE_TABLE,new String[] {KEY_OPTIONVALUE},KEY_OPTIONNAME + "=" + "?", new String[] {optionName}, null, null, null);
    	Cursor cursor  = c;
    	c.close();
    	
    	if(cursor.getCount()>0) {
	    	if(cursor.moveToFirst()) {
				return cursor.getString(0);
			}
			return cursor.getString(0);
    	} else {
    		return "";
    	}
    }
    private boolean optionIsSet(String optionName)
    {
    	Cursor c = db.query(DATABASE_TABLE,new String[] {KEY_OPTIONVALUE},KEY_OPTIONNAME + "=" + "?", new String[] {optionName}, null, null, null);
    	Cursor cursor  = c;
    	c.close();
    	if(cursor.getCount()>0) {
            return true;
        } else {
            return false;
        }
    }
}
