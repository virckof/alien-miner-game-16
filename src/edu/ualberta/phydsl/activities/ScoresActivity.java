package edu.ualberta.phydsl.activities;

import edu.ualberta.phydsl.data.ScoreVO;
import edu.ualberta.phydsl.util.ActivitiesBridge;
import edu.ualberta.phydsl.activities.R;
import edu.ualberta.phydsl.activities.R.id;
import edu.ualberta.phydsl.activities.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
* Exit Activity, report the total score obtained by the player.
*/
public class ScoresActivity extends Activity{

	// Event IDs
	public static final int LISTENER_CODE = 200;
	public static String EVENT_FLAG="EVENT_FLAG";
	public static String NEW_GAME_EVENT="NEW_GAME_EVENT";
	public static String SEND_SERVICE_EVENT="SEND_SERVICE_EVENT";
	public static String SEND_REST_EVENT="SEND_REST_EVENT";
	public static String SAVE_DEVICE_EVENT="SAVE_DEVICE_EVENT";
	public static String VIEW_SCORES="VIEW_SCORES";
	public static String CLOSE="CLOSE";

	private TextView totalScore;
	private ScoreVO eScore;

	public void onCreate(Bundle savedInstanceState) {

		// Recovering game previous state
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_of_game);

		totalScore=(TextView)findViewById(R.id.totalScoreText);

		eScore = (ScoreVO) ActivitiesBridge.getObject();
		totalScore.setText("Total Score: " + eScore.getScoreTotal());

	}

	// All the buttons of the view define this method as their listener
	public void endOfGameHandler(View view){
		Intent intent = this.getIntent();
		switch (view.getId()) {
			case R.id.saveDevice:
				intent.putExtra(EVENT_FLAG, SAVE_DEVICE_EVENT);
				setResult(RESULT_OK, intent);
				finish();
				break;
			case R.id.newGame:
				Intent i = new Intent(this, MainActivity.class);
				i.putExtra(EVENT_FLAG, NEW_GAME_EVENT);
				setResult(RESULT_OK, i);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivityForResult(i, LISTENER_CODE);
				finish();
				break;
			case R.id.close:
				intent.putExtra(EVENT_FLAG, CLOSE);
				setResult(RESULT_OK, intent);
				finish();
		}

	}

	// Filter and Ignore the back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	     if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	 //Preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
	    	 return true;
	     }
	     return super.onKeyDown(keyCode, event);
	}
}
