package com.code.kawakuti.phonepharmacy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.code.kawakuti.phonepharmacy.models.Alarm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Russelius on 26/01/16.
 */
public class DataBaseAlarmsHandler extends SQLiteOpenHelper {

	public static final String ALARM_TABLE = "alarm";
	public static final String COLUMN_ALARM_ID = "_id";
	public static final String COLUMN_ALARM_ACTIVE = "alarm_active";
	public static final String COLUMN_ALARM_TIME = "alarm_time";
	public static final String COLUMN_ALARM_DAYS = "alarm_days";
	public static final String COLUMN_ALARM_TONE = "alarm_tone";
	public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
	public static final String COLUMN_ALARM_NAME = "alarm_name";
	static final String DATABASE_NAME = "AlarmsDataBase";
	static final int DATABASE_VERSION = 1;
	static DataBaseAlarmsHandler instance = null;
	static SQLiteDatabase database = null;


	public DataBaseAlarmsHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static void init(Context context) {
		if (null == instance) {
			instance = new DataBaseAlarmsHandler(context);
		}
	}

	public static SQLiteDatabase getDatabase() {
		if (null == database) {
			database = instance.getWritableDatabase();
		}
		return database;
	}

	public static void deactivate() {
		if (null != database && database.isOpen()) {
			database.close();
		}
		database = null;
		instance = null;
	}

	public static long create(Alarm alarm) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
		cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(alarm.getDays());
			byte[] buff = bos.toByteArray();
		    
		    cv.put(COLUMN_ALARM_DAYS, buff);
		    
		} catch (Exception e){
			e.printStackTrace();
		}
		cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
		cv.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
		cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
		return getDatabase().insert(ALARM_TABLE, null, cv);
	}

	public static int update(Alarm alarm) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
		cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());
		
		try {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(alarm.getDays());
		    byte[] buff = bos.toByteArray();
		    cv.put(COLUMN_ALARM_DAYS, buff);
		    
		} catch (Exception e){

			e.printStackTrace();
		}

		cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
		cv.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
		cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
					
		return getDatabase().update(ALARM_TABLE, cv, "_id=" + alarm.getId(), null);
	}
	public static int deleteEntry(Alarm alarm){
		return deleteEntry(alarm.getId());
	}
	
	public static int deleteEntry(int id){
		return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
	}
	
	public static int deleteAll(){
		return getDatabase().delete(ALARM_TABLE, "1", null);
	}
	
	public static Alarm getAlarm(int id) {
		// TODO Auto-generated method stub
		String[] columns = new String[] { 
				COLUMN_ALARM_ID, 
				COLUMN_ALARM_ACTIVE,
				COLUMN_ALARM_TIME,
				COLUMN_ALARM_DAYS,
				COLUMN_ALARM_TONE,
				COLUMN_ALARM_VIBRATE,
				COLUMN_ALARM_NAME
				};
		Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID+"="+id, null, null, null,
				null);
		Alarm alarm = null;
		
		if(c.moveToFirst()){
			alarm =  new Alarm();
			alarm.setId(c.getInt(1));
			alarm.setAlarmActive(c.getInt(2)==1);
			alarm.setAlarmTime(c.getString(3));
			byte[] repeatDaysBytes = c.getBlob(4);
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				Alarm.Day[] repeatDays;
				Object object = objectInputStream.readObject();
				if(object instanceof Alarm.Day[]){
					repeatDays = (Alarm.Day[]) object;
					alarm.setDays(repeatDays);
				}								
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			alarm.setAlarmTonePath(c.getString(5));
			alarm.setVibrate(c.getInt(6)==1);
			alarm.setAlarmName(c.getString(7));
		}
		c.close();
		return alarm;
	}
	
	public static Cursor getCursor() {
		// TODO Auto-generated method stub
		String[] columns = new String[] { 
				COLUMN_ALARM_ID, 
				COLUMN_ALARM_ACTIVE,
				COLUMN_ALARM_TIME,
				COLUMN_ALARM_DAYS,
				COLUMN_ALARM_TONE,
				COLUMN_ALARM_VIBRATE,
				COLUMN_ALARM_NAME
				};
		return getDatabase().query(ALARM_TABLE, columns, null, null, null, null,
				null);
	}

	public static List <Alarm> getAllAlarms() {
		List<Alarm> alarms = new ArrayList<Alarm>();
		Cursor cursor = DataBaseAlarmsHandler.getCursor();
		if (cursor.moveToFirst()) {

			do {

				Alarm alarm = new Alarm();
				alarm.setId(cursor.getInt(0));
				alarm.setAlarmActive(cursor.getInt(1) == 1);
				alarm.setAlarmTime(cursor.getString(2));
				byte[] repeatDaysBytes = cursor.getBlob(3);

				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
						repeatDaysBytes);
				try {
					ObjectInputStream objectInputStream = new ObjectInputStream(
							byteArrayInputStream);
					Alarm.Day[] repeatDays;
					Object object = objectInputStream.readObject();
					if (object instanceof Alarm.Day[]) {
						repeatDays = (Alarm.Day[]) object;
						alarm.setDays(repeatDays);
					}
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				alarm.setAlarmTonePath(cursor.getString(4));
				alarm.setVibrate(cursor.getInt(5) == 1);
				alarm.setAlarmName(cursor.getString(6));
				alarms.add(alarm);

			} while (cursor.moveToNext());
		}
		cursor.close();
		return alarms;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
				+ COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
				+ COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
				+ COLUMN_ALARM_TIME + " TEXT NOT NULL, "
				+ COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
				+ COLUMN_ALARM_TONE + " TEXT NOT NULL, "
				+ COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
				+ COLUMN_ALARM_NAME + " TEXT NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
		onCreate(db);
	}

	public enum AlarmColumns {
		TABLE_ALARM,
		ALARM_ID,
		ALARM_ACTIVE,
		ALARM_TIME,
		ALARM_DAYS,
		ALARM_TONE,
		ALARM_VIBRATE,
		ALARM_NAME;

		@Override
		public String toString() {
			switch (this) {
				case TABLE_ALARM:
					return "alarm";
				case ALARM_ID:
					return "_id";
				case ALARM_ACTIVE:
					return "alarm_active";
				case ALARM_TIME:
					return "alarm_time";
				case ALARM_DAYS:
					return "alarm_days";
				case ALARM_TONE:
					return "alarm_tone";
				case ALARM_VIBRATE:
					return "alarm_vibrate";
				case ALARM_NAME:
					return "alarm_name";
			}
			return super.toString();
		}
	}
}