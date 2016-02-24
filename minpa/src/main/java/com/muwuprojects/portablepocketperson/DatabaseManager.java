package com.muwuprojects.portablepocketperson;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

public class DatabaseManager extends SQLiteOpenHelper{
	 // All Static variables
	   // Database Version
	   private static final int DATABASE_VERSION = 10;

	   // Database Name
	   private static final String DATABASE_NAME = "minpa_db";

	   private static final String TABLE_DATE = "lastdate";

	   private static final String KEY_ID = "id";
	   private static final String KEY_LONG_DATE = "the_date";

	   private Context theContext;
	   
	   public DatabaseManager(Context context) {
	       super(context, DATABASE_NAME, null, DATABASE_VERSION);
	       
	       theContext = context;
	   }
	   
		// Creating Tables
	   @Override
	   public void onCreate(SQLiteDatabase db) {
	       String CREATE_DATE_TABLE = "CREATE TABLE " + TABLE_DATE + "("
	               + KEY_ID + " INTEGER PRIMARY KEY," 
	       		+ KEY_LONG_DATE + " INT" 
	       		+ ")";

	       db.execSQL(CREATE_DATE_TABLE);
	       
	       //ContentValues valuesStats = new ContentValues();
	       //valuesStats.put(KEY_LONG_DATE, System.currentTimeMillis());

	       //String dateString = "04/02/2016";
	       //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
	       //try {
		//		Date date = sdf.parse(dateString);
		//		long fakeDate = date.getTime();
		//	    ContentValues valuesStats = new ContentValues();
		//	    valuesStats.put(KEY_LONG_DATE, fakeDate);
		//	    db.insert(TABLE_DATE, null, valuesStats);
	     //  } catch (ParseException e) {
		//	// TODO Auto-generated catch block
		//	//e.printStackTrace();
	     //  }

	   }

		// Upgrading database
	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	       // Drop older table if existed
	       db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATE);
	       // Create tables again
	       onCreate(db);

	   }
	   
		public Long getLastDate() {
			
		       SQLiteDatabase db = this.getReadableDatabase();
		       
		       Cursor cursor = db.query(TABLE_DATE, new String[] { KEY_LONG_DATE}, KEY_ID + "=?",
		               new String[] { Long.toString(1) }, null, null, null, null);

		       if (cursor != null)
		       {
		       	if(!cursor.isAfterLast())
		       	{
			    		cursor.moveToFirst();
			    		return cursor.getLong(0);
		       	}
		       	
		       }
				return (long) 0;

		       //SQLiteDatabase db = this.getReadableDatabase();
		       //String dateString = (String) DateFormat.format("dd/MM/yyyy", System.currentTimeMillis());
		       //return dateString;
		}

		public void updateDate() {
		       
			SQLiteDatabase db = this.getWritableDatabase();
		    
	        ContentValues args = new ContentValues();
	        args.put(KEY_LONG_DATE, System.currentTimeMillis());
	        db.update(TABLE_DATE, args, KEY_ID + "=1", null);

			
		}

		public void createDate() {
			// TODO Auto-generated method stub
			SQLiteDatabase db = this.getWritableDatabase();
		    ContentValues valuesStats = new ContentValues();
		    valuesStats.put(KEY_LONG_DATE, System.currentTimeMillis());
		    db.insert(TABLE_DATE, null, valuesStats);
			
		}

		public long getNumDaysSinceLastVisit() {
			// TODO Auto-generated method stub
			long lastVisitLong = getLastDate();
			long nowLong = System.currentTimeMillis();
			long oneDay = 1000 * 60 * 60 * 24;
			long diff = (nowLong - lastVisitLong) / oneDay;
			return diff;
		}

		public long getNumHoursSinceLastVisit() {
			// TODO Auto-generated method stub
			long lastVisitLong = getLastDate();
			long nowLong = System.currentTimeMillis();
			long oneDay = 1000 * 60 * 60;
			long diff = (nowLong - lastVisitLong) / oneDay;
			return diff;
		}


}
