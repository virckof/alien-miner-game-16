package edu.ualberta.phydsl.activities;

import edu.ualberta.phydsl.activities.R;
import edu.ualberta.phydsl.activities.R.id;
import edu.ualberta.phydsl.activities.R.layout;
import edu.ualberta.phydsl.activities.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
* Landing activity, captures player id.
*/
public class WelcomeActivity extends Activity{

	public static final int START = 300;
	public static final String USERNAME = "USERNAME";

	private EditText usernameTxt;

	public void onCreate(Bundle savedInstanceState) {

		// Recovering game previous state
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		usernameTxt = (EditText)findViewById(R.id.nameText);

	}

	public void welcomeGameHandler(View view){
		Intent intent = this.getIntent();

		String username = usernameTxt.getText().toString();

		if(!username.equals("")){
			switch (view.getId()) {
			case R.id.startGame:
				intent.putExtra(USERNAME, username);
				setResult(RESULT_OK, intent);
				finish();
				break;
			}
		}
		else{
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, getResources().getText(R.string.No_Username), duration);
			toast.show();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
