package edu.ualberta.phydsl.activities;

import java.util.Calendar;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import edu.ualberta.phydsl.data.EventVO;
import edu.ualberta.phydsl.data.PersistenceManager;
import edu.ualberta.phydsl.data.ScoreVO;
import edu.ualberta.phydsl.main.CollisionManager;
import edu.ualberta.phydsl.main.ControlManager;
import edu.ualberta.phydsl.main.PhysicsManager;
import edu.ualberta.phydsl.main.RenderManager;
import edu.ualberta.phydsl.main.ScoringManager;
import edu.ualberta.phydsl.main.RenderManager.TimeObjectAppearActivity;
import edu.ualberta.phydsl.util.ActivitiesBridge;
import edu.ualberta.phydsl.util.ObservableWrapper;
import edu.ualberta.phydsl.activities.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
* Main Activity 
*/
public class MainActivity extends Activity {

	public int[] coordinates;

	// ////////////////////////////////////
	// Main resources
	// ////////////////////////////////////

	// The render manager (representation of the model of the physics manager)
	private RenderManager renderManager;

	// The game physics manager
	private PhysicsManager physicsManager;

	// Observable bridge for asynchronous touch notifications
	private ObservableWrapper<EventVO> runway;

	// Score Manager
	private ScoringManager scoreManager;

	// Collision Manager
	private CollisionManager collisionManager;

	// Control Manager
	private ControlManager controlManager;

	// States
	private boolean welcomed;

	// Session Information
	private String username = "";

	// Canvas update independent thread
	private Runnable r;

	// Called when the game starts
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Recovering game previous state
		super.onCreate(savedInstanceState);

		createWorld();

		scoreManager = new ScoringManager(this);
		controlManager = new ControlManager(physicsManager);

		renderManager = new RenderManager(this, this);
		renderManager.setModel(physicsManager);
		renderManager.setController(controlManager);
		setContentView(renderManager);

		setupRunway();

		if (!welcomed) {
			showWelcomeView();
			welcomed = true;
		}

	}

	private void createWorld() {
		// Creates new physics manager
		if (physicsManager == null) {
			// Initialize Gravity Vector (No Modification to 'Y' acceleration)
			Vec2 grav = new Vec2(0.0f, 9.8f);
			physicsManager = new PhysicsManager();
			collisionManager = new CollisionManager();
			physicsManager.create(grav, collisionManager);
		}
	}

	private void setupRunway() {
		runway = new ObservableWrapper<EventVO>();

		collisionManager.setRunway(runway);
		renderManager.setRunway(runway);
		registerEventObservers();
	}

	// Sets new timed activities in the gameplay, activities are not executed,
	// but added to the queue.
	private void setActivitiesToView() {
		// Setting activities
		TimeObjectAppearActivity shower  = renderManager.new TimeObjectAppearActivity();
		shower.setObjectId(RenderManager.meteoriteId);
		shower.setType(RenderManager.BODY_TYPE_BOX);
		shower.setSize(2.0f);
		shower.setX(2.0f);
		shower.setY(4.0f);
		shower.setaSpeed(10.0f);
		shower.setlSpeedX(10.0f);
		shower.setlSpeedY(10.0f);
		shower.setFrequency((int)4.0*1000);
		shower.setDensity(2.6f);
		shower.setBounce(0.5f);
		shower.setFriction(0.7f);
		renderManager.setActivity(shower);
		TimeObjectAppearActivity middle  = renderManager.new TimeObjectAppearActivity();
		middle.setObjectId(RenderManager.meteoriteId);
		middle.setType(RenderManager.BODY_TYPE_BOX);
		middle.setSize(2.0f);
		middle.setX(40.0f);
		middle.setY(4.0f);
		middle.setaSpeed(10.0f);
		middle.setlSpeedX(10.0f);
		middle.setlSpeedY(10.0f);
		middle.setFrequency((int)8.0*1000);
		middle.setDensity(2.6f);
		middle.setBounce(0.5f);
		middle.setFriction(0.7f);
		renderManager.setActivity(middle);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return physicsManager;
	}

	@Override
	protected void onResume() {

		super.onResume();

		r = new Runnable() {
			public void run() {
				physicsManager.update();
				renderManager.invalidate();
				getWindow().getDecorView().postDelayed(r, 10);
			}
		};

		getWindow().getDecorView().post(r);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getWindow().getDecorView().removeCallbacks(r);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// On menu remove action, remove all the objects from the physicsManager
	private void removeObjects() {
		physicsManager.clearWorld();
	}

	// /////////////////////////////////////////////////
	// Master Event and Activity Shifting Management
	// /////////////////////////////////////////////////
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

		// Scores Activity Events
		if (requestCode == ScoresActivity.LISTENER_CODE) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				String eventId = extras.getString(ScoresActivity.EVENT_FLAG);

				if (eventId.equals(ScoresActivity.NEW_GAME_EVENT)) {
					startGame();

				}
				else if (eventId.equals(ScoresActivity.SAVE_DEVICE_EVENT)) {
					PersistenceManager.printEvents("[" + username + "]" + " ["+ date + "] "	+ scoreManager.getScoreVO().getEvents());
					showToast(0, 10, getResources().getString(R.string.Mettrics_added_locally));
					showEndOfGameView();
				}
				else if (eventId.equals(ScoresActivity.CLOSE)) {
					renderManager.stopActivities();
					finish();
				}
			}
		}

		// Welcome Activity Events
		else if (requestCode == WelcomeActivity.START) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				username = extras.getString(WelcomeActivity.USERNAME);
			}
			startGame();

		}
	}


	// Displays the end of game view (menu)
	public void showEndOfGameView() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		renderManager.stopActivities();
		renderManager.restartCanvas();
		removeObjects();

		Intent intent = new Intent(this, ScoresActivity.class);
		ScoreVO eScore = scoreManager.getScoreVO();
		ActivitiesBridge.setObject(eScore);
		intent.putExtra("USERNAME", username);
		this.startActivityForResult(intent, ScoresActivity.LISTENER_CODE);
	}


	// Displays the welcome view
	private void showWelcomeView() {
		Intent intent = new Intent(this, WelcomeActivity.class);
		startActivityForResult(intent, WelcomeActivity.START);
	}

	// ////////////////////////////////////////////////////////////////////
	// Default Main Menu Event Handler
	// ///////////////////////////////////////////////////////////////////

	// end game, close the app
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.results: {
				showEndOfGameView();
			}

			case R.id.item_close: {
				finish();
			}

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// ////////////////////////////
	// Utilities
	// ////////////////////////////

	// Restarts the game (remove all the objects from the model, and resets the
	// collision detector
	private void startGame() {
		removeObjects();

		renderManager.restartCanvas();
		setActivitiesToView();

		scoreManager.reset();
		scoreManager.initializeTimedEvents();
		showToast(0, 365, getResources().getString(R.string.touchToStart));

	}

	// Shows a simple toast to the user
	public void showToast(int x, int y, String text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = makeCuztomizedToast(x, y, context, text, duration);
		toast.show();
	}

	// Registers a observer in the event hub (runway)
	public void registerEventObservers() {
		runway.addObserver(scoreManager);
		runway.addObserver(controlManager);
	}

	public static Toast makeCuztomizedToast(int x, int y, Context contextP,	CharSequence text, int duration) {

		Toast toast = Toast.makeText(contextP, text, duration);
		toast.setGravity(Gravity.BOTTOM, x, y);
		View rootView = toast.getView();
		LinearLayout linearLayout = null;
		TextView messageTextView = null;

		// check (expected) toast layout
		if (rootView instanceof LinearLayout) {
			linearLayout = (LinearLayout) rootView;

			if (linearLayout.getChildCount() == 1) {
				View child = linearLayout.getChildAt(0);

				if (child instanceof TextView) {
					messageTextView = (TextView) child;
				}
			}
		}

		if (linearLayout == null || messageTextView == null) {
			return toast;
		}

		messageTextView.setTextSize(25);
		messageTextView.setGravity(Gravity.CENTER);
		ViewGroup.LayoutParams textParams = messageTextView.getLayoutParams();

		((LinearLayout.LayoutParams) textParams).gravity = Gravity.CENTER_HORIZONTAL;

		return toast;
	}


	public void getTouchLocation(float x, float y) {
		coordinates = new int[]{(int) x,(int) y};
	}

	public int getX() {
		return coordinates[0];
	}

	public int getY() {
		return coordinates[1];
	}

	public void removebody(Body a) {
		renderManager.removeBody(a);
	}

	public int getScore() {
		return scoreManager.getScoreTotal();
	}

	public int getTimer() {
		return renderManager.getTimerNumber();
	}
}
