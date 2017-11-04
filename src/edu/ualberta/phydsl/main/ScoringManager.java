package edu.ualberta.phydsl.main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import org.jbox2d.dynamics.Body;
import android.content.Context;
import android.os.AsyncTask;
import android.media.MediaPlayer;
import android.os.Vibrator;
import edu.ualberta.phydsl.activities.MainActivity;
import edu.ualberta.phydsl.data.EventVO;
import edu.ualberta.phydsl.data.ScoreVO;
import edu.ualberta.phydsl.activities.R;

/**
* This class manages the scoring consolidates and the time-based
* events of the game
*/
public class ScoringManager implements Observer {

	MainActivity mainActivity;
	private int scoreTotal;

	private ArrayList<EventVO> touchEvents;

	private EventVO recentHitEvent;
	private int debounceTime = 500;

	private boolean playerWins;
	protected boolean gameEnds;
	protected boolean gameEndInitiated;
	protected boolean combo = false;

	public ScoringManager() {
		scoreTotal = 0;
		playerWins = false;
		gameEnds = false;
		gameEndInitiated = false;
		touchEvents = new ArrayList<EventVO>();
	}

	public ScoringManager(MainActivity mainActivity) {
		this();
		this.mainActivity = mainActivity;
	}

	public int getScoreTotal() {
		return scoreTotal;
	}

	public void setScoreTotal(int scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	public boolean playerWins() {
		return playerWins;
	}

	public void setplayerWins(boolean playerWins) {
		this.playerWins = playerWins;
	}

	// Scoring Rule Collection
	public void update(Observable o, Object pl) {

		ArrayList<EventVO> payload = (ArrayList<EventVO>) pl;
		EventVO ev = payload.get(0);
		EventVO.EventType type = ev.getType();

		Vibrator vibrator = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

		// Simple Touching
		if (type == EventVO.EventType.TOUCH) {
			// Register the touch event for statistics
			touchEvents.add(ev);

			Body a = ev.getBodies().get(0);

			if(a.m_userData.equals(RenderManager.diamondId)){
				recentHitEvent = ev;

				vibrator.vibrate(90);

				MediaPlayer mp = MediaPlayer.create(this.mainActivity.getApplicationContext(), R.raw.glass_koenig);
				mp.start();

				scoreTotal += 20;
				playerWins = true;
				gameEnds = false;
				mainActivity.removebody(a);
			}
		}

		// Collision of two objects coming from the collision collector.
		else if (type == EventVO.EventType.COLLISION) {
			if (ev.getBodies().size() == 2 && 	!ignoreEvent(ev)) {
				Body a = ev.getBodies().get(0);
				Body b = ev.getBodies().get(1);

				if((a.m_userData.equals(RenderManager.alienId) && b.m_userData.equals(RenderManager.emeraldId)) ||
						b.m_userData.equals(RenderManager.alienId) && a.m_userData.equals(RenderManager.emeraldId)){
					recentHitEvent = ev;

					scoreTotal += 30;
					playerWins = true;
					gameEnds = false;
					if(a.m_userData.equals(RenderManager.emeraldId)) {
						mainActivity.removebody(a);
					} else if (b.m_userData.equals(RenderManager.emeraldId)) {
						mainActivity.removebody(b);
					}
				}

				if((a.m_userData.equals(RenderManager.alienId) && b.m_userData.equals(RenderManager.meteoriteId)) ||
						b.m_userData.equals(RenderManager.alienId) && a.m_userData.equals(RenderManager.meteoriteId)){
					recentHitEvent = ev;
					scoreTotal += -20;
					playerWins = true;
					gameEnds = false;
					if(a.m_userData.equals(RenderManager.meteoriteId)) {
						mainActivity.removebody(a);
					} else if (b.m_userData.equals(RenderManager.meteoriteId)) {
						mainActivity.removebody(b);
					}
				}

				if((a.m_userData.equals(RenderManager.alienId) && b.m_userData.equals(RenderManager.starId)) ||
						b.m_userData.equals(RenderManager.alienId) && a.m_userData.equals(RenderManager.starId)){
					recentHitEvent = ev;
					scoreTotal += 0;
					playerWins = true;
					gameEnds = true;
				}

			}
		}

		if (gameEnds && !gameEndInitiated) {
			gameEndInitiated = true;
			mainActivity.showEndOfGameView();
		}
	}

	public ScoreVO getScoreVO() {
		ScoreVO a = new ScoreVO();
		a.setScoreTotal(String.valueOf(scoreTotal));
		if (scoreTotal>380) { // playerWins
			a.setPlayerFinalState(mainActivity.getResources().getString(	R.string.Wins));
		} else {
			a.setPlayerFinalState(mainActivity.getResources().getString(	R.string.Lost));
		}

		a.setEvents(touchEvents.toString());

		return a;
	}

	// Rounds doubles with fine precision
	public static double round(double unrounded, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	public Object getTouchEvents() {
		return touchEvents;
	}

	public void reset() {
		scoreTotal = 0;
		playerWins = false;
		gameEnds = false;
		gameEndInitiated = false;
		touchEvents = new ArrayList<EventVO>();
	}

  // This method helps the engine to ignore double taps on controls or trigger actors
	private boolean ignoreEvent(EventVO ev)
	{
		if (recentHitEvent == null)
			return false;

		if(Math.abs(recentHitEvent.getTime() - ev.getTime()) > debounceTime)
			return false;

		if (recentHitEvent.getType() != ev.getType())
			return false;

		ArrayList<Body> recentEventBodies = recentHitEvent.getBodies();
		ArrayList<Body> eventBodies = ev.getBodies();

		Collections.sort(recentEventBodies, new Comparator<Body>() {
	        public int compare(Body b1, Body  b2)
	        {
	        	if ( b1.m_userData == b2.m_userData)
	        		return 0;
	            return  -1;
	        }
	    });


		Collections.sort(eventBodies, new Comparator<Body>() {
	        public int compare(Body b1, Body  b2)
	        {
	        	if ( b1.m_userData == b2.m_userData)
	        		return 0;
	            return  -1;
	        }
	    });

		if (recentHitEvent.getBodies().size() != ev.getBodies().size())
			return false;

		for (int i = 0; i < recentEventBodies.size(); i++) {
			if (recentEventBodies.get(i) != eventBodies.get(i)) {
				return false;
			}
		}

		return true;
	}


	/////////////////////////////////////////////////
	// Time based event management
	////////////////////////////////////////////////
	public void initializeTimedEvents() {
		TimedScoreActivity timeup = new TimedScoreActivity();
		timeup.setGameEnding(true);
		timeup.setplayerWinsState(true);
		timeup.setPoints(0 );
		timeup.setTimer(60*1000 );
		timeup.execute((Void[])null);
	}

	// Timed Rules Manager
	protected class TimedScoreActivity extends	AsyncTask<Void, Void, Void> {

		private int points;
		private boolean gameEnding;
		private boolean playerWinsState;
		private int timer;

		public int getTimer() {
			return timer;
		}

		public void setTimer(int timer) {
			this.timer = timer;
		}

		public int getPoints() {
			return points;
		}

		public void setPoints(int points) {
			this.points = points;
		}

		public boolean isGameEnding() {
			return gameEnding;
		}

		public void setGameEnding(boolean gameEnding) {
			this.gameEnding = gameEnding;
		}

		public boolean playerWinsState() {
			return playerWinsState;
		}

		public void setplayerWinsState(boolean playerWinsState) {
			this.playerWinsState = playerWinsState;
		}

		@Override
		protected Void doInBackground(Void... params) {

			Thread t = new Thread() {
				public void run() {
					while (!gameEnds) {
						try {
							Thread.sleep(getTimer());
							// Caught by onProgressUpdate below
							gameEnds = gameEnding;
							scoreTotal += points;
							playerWins = playerWinsState;
							publishProgress();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			if (gameEnds && !gameEndInitiated) {
				gameEndInitiated = true;
				mainActivity.showEndOfGameView();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
				// ignored at this point
		}
	}
}
