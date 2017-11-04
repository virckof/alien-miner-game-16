package edu.ualberta.phydsl.data;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
* Optional SQLite persistence data access object
*/
public class ScoresDAO {

	// Database fields
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_USERNAME, SQLiteHelper.COLUMN_SCORE_TOTAL, SQLiteHelper.COLUMN_SCORE_EXPECTED, SQLiteHelper.COLUMN_SCORE_ZONES, SQLiteHelper.COLUMN_PERSEVERATION, SQLiteHelper.COLUMN_TOTAL_TIME, SQLiteHelper.COLUMN_LATENCY_AVERAGE, SQLiteHelper.COLUMN_LATENCY_AVERAGE, SQLiteHelper.COLUMN_EVENTS};

	public ScoresDAO(Context context) {
		dbHelper = new SQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ScoreVO registerScore(String username, String scoreTotal, String events) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_USERNAME, username);
		values.put(SQLiteHelper.COLUMN_SCORE_TOTAL, scoreTotal);
		values.put(SQLiteHelper.COLUMN_EVENTS, events);
		long insertId = database.insert(SQLiteHelper.TABLE_SCORES, null, values);

		Cursor cursor = database.query(SQLiteHelper.TABLE_SCORES, allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null, 	null, null, null, null);
		cursor.moveToFirst();
		ScoreVO newScore = cursorToScoreVO(cursor);
		cursor.close();
		return newScore;
	}

	public void deleteScore(ScoreVO score) {
		long id = score.getId();
		database.delete(SQLiteHelper.TABLE_SCORES, SQLiteHelper.COLUMN_ID	+ " = " + id, null);
	}

	public List<ScoreVO> getAllScores() {
		List<ScoreVO> scores = new ArrayList<ScoreVO>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_SCORES, allColumns, null, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ScoreVO comment = cursorToScoreVO(cursor);
			scores.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return scores;
	}

	private ScoreVO cursorToScoreVO(Cursor cursor) {
		ScoreVO score = new ScoreVO();
		score.setId(cursor.getLong(0));
		score.setUsername(cursor.getString(1));
		score.setScoreTotal(cursor.getString(2));
		score.setEvents(cursor.getString(9));

		return score;
	}

}
