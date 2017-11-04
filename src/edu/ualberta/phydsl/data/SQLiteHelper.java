package edu.ualberta.phydsl.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
* Optional SQLite persistence implementation
*/
public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String DBPACKAGE = "/data/edu.ualberta.ssrg.mda.physics.template.main/databases/scores.db";
	public static final String TABLE_SCORES = "scores";
	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_SCORE_TOTAL = "score_total";
	public static final String COLUMN_SCORE_EXPECTED = "score_expected";
	public static final String COLUMN_SCORE_ZONES = "score_zones";
	public static final String COLUMN_PERSEVERATION = "perseveration";
	public static final String COLUMN_TOTAL_TIME = "total_time";
	public static final String COLUMN_LATENCY_AVERAGE = "latency_avg";
	public static final String COLUMN_LATENCY_DEVIATION = "latency_SD";
	public static final String COLUMN_EVENTS = "events";

	private static final String DATABASE_NAME = "scores.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_SCORES + "( " + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_USERNAME + " text not null, "
			+ COLUMN_SCORE_TOTAL + " text not null, "
			+ COLUMN_SCORE_EXPECTED + " text not null,"
			+ COLUMN_SCORE_ZONES + " text not null,"
			+ COLUMN_PERSEVERATION + " text not null, "
			+ COLUMN_TOTAL_TIME +" text not null, "
			+ COLUMN_LATENCY_AVERAGE + " text not null, "
			+ COLUMN_LATENCY_DEVIATION + " text not null, "
			+ COLUMN_EVENTS + " text not null);";

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
		onCreate(db);
	}

}
