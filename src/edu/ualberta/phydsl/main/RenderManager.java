package edu.ualberta.phydsl.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import edu.ualberta.phydsl.activities.MainActivity;
import edu.ualberta.phydsl.data.EventVO;
import edu.ualberta.phydsl.util.RenderHelper;
import edu.ualberta.phydsl.util.ObservableWrapper;
import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
* Main Game canvas view, representation of the physical physicsManager
*/
public class RenderManager extends View {

	public static final String BODY_TYPE_CIRCLE = "TYPE_CIRCLE";
	public static final String BODY_TYPE_BOX= "TYPE_BOX";

	// Screen stretch factor
	public static final int STRETCH = 8;

	// Application Context
	private Context context;

	Random ran = new Random();

	// Score Notification
	private ObservableWrapper<EventVO> runway;

	// Bodies in the canvas (Used here only to add dynamic bodies through the
	// physics physicsManager)
	private PhysicsManager physicsManager;

	// Body Controller that supports injecting right, left, jump, or down forces
	// to body fixtures.
	private ControlManager controller;

	private Canvas canvas;

	public static Object alienId = new Object();
	public static Object meteoriteId = new Object();
	public static Object brickId = new Object();
	public static Object emeraldId = new Object();
	public static Object diamondId = new Object();
	public static Object starId = new Object();

	public static Object crate_blueId = new Object();

	public static Object mainActorId = alienId;

	// Activities
	public ArrayList<AsyncTask> activities;

	// Game started
	private boolean gameStarted;
	private boolean gameEnded;

	// Mouse Joint Matrix
	private Map<Integer, MouseJoint> mouseJoints = new HashMap<Integer, MouseJoint>();

	private MainActivity mainActivity;
	private float mX, mY;

	private int interval;
	private Timer timer;

	private boolean timerSet = false;
	private String timeString;

	private RenderHelper drawingHelper;

	// Constructor
	public RenderManager(Context context, MainActivity main) {
		super(context);
		activities = new ArrayList<AsyncTask>();
		this.context = context;
		setClickable(true);
		mainActivity = main;
	}

	// Updates and draws the physics canvas
	@Override
	protected void onDraw(Canvas canvasP) {
		canvas = canvasP;
		super.onDraw(canvas);

		if (drawingHelper == null && controller != null) {
			drawingHelper = new RenderHelper(this, canvas, controller);
		}

		if (drawingHelper == null) return;

		drawingHelper.initializeImages();
		drawingHelper.initializeBackground();

		drawingHelper.drawBackgroundLayer();
		drawingHelper.drawActorLayer();
		if (gameStarted) {
			drawingHelper.drawTextLayer();
		}

		if (!timerSet && gameStarted) {
			countup();
			timerSet = true;
		}
	}

	public void countup() {
		int delay = 5;
		int period = 1000;
		timer = new Timer();
		interval = 0;
		timer.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				timeString = "Time: " + setInterval();

			}
		}, delay, period);
	}

	public String getRunTimeString() {
		return timeString != null? timeString : "";
	}

	public String getScoreString() {
		return "Score: " + mainActivity.getScore();
	}

	private final int setInterval() {
		return interval++;
	}

	// Sets a new physics model for the simulation.
	public void setModel(PhysicsManager physicsManager) {
		this.physicsManager = physicsManager;
	}

	// Translates a coordinate from device screen to a coordinate in the physics physicsManager.
	// This is done by dividing the coordinate by the drawing scale
	// and then adding our canvas view offset
	private Vec2 translateFromScreenToPhysicsCoord(Vec2 coord) {
		Vec2 scaled = new Vec2(coord.x/getDrawingScale(), coord.y/getDrawingScale());

		Vec2 offset = drawingHelper.getViewOffset();
		scaled.x += offset.x/getDrawingScale();
		scaled.y += offset.y/getDrawingScale();

		return scaled;
	}

	// Creates Joints between the touch prints of the user and a body
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;

		final Vec2 vec = translateFromScreenToPhysicsCoord(new Vec2(event.getX(pointerIndex), event.getY(pointerIndex)));

		final float pressure = event.getPressure();
		final float tapsize = event.getSize(pointerIndex);
		final int pointerId = event.getPointerId(pointerIndex);

		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {

			mainActivity.getTouchLocation(event.getX(), getHeight() - event.getY());
			mX = vec.x;
			mY = vec.y - 0.2f;

			physicsManager.getWorld().queryAABB(

					// First Query Parameter QueryCallback
					new QueryCallback() {
						public boolean reportFixture(Fixture fixture) {
							Body body = fixture.m_body;
							MouseJointDef jointDef = new MouseJointDef();
							jointDef.bodyA = body;
							jointDef.bodyB = body;
							jointDef.target.x = vec.x;
							jointDef.target.y = vec.y;
							jointDef.maxForce = 8000.0f * body.getMass();
							MouseJoint mouseJoint = (MouseJoint) physicsManager
									.getWorld().createJoint(jointDef);
							if (mouseJoints.containsKey(pointerId)) {
								Log.w("joint existing", "pointer id: "
										+ pointerId);
								physicsManager.getWorld().destroyJoint(
										mouseJoints.get(pointerId));
							}
							mouseJoints.put(pointerId, mouseJoint);

							onElementTouch(body, body.getPosition().x, body.getPosition().y, pressure, tapsize);

							return false;
						}
					},
					// Second Query Parameter
					new AABB(vec, vec));
		}


		// This is the case where the game hasn't started and the screen is
		// waiting for the first touch event.
		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_POINTER_UP) {

			if (mouseJoints.containsKey(pointerId)) {
				physicsManager.getWorld().destroyJoint(mouseJoints.remove(pointerId));
			}
			if (!gameStarted) {
				startGame(vec.x, vec.y);
			}
		}

		return super.onTouchEvent(event);
	}

	// /////////////////////////////////////////////////
	// Game lifecycle
	// ////////////////////////////////////////////////

	// Adds the gameplay elements (actors and activities) and starts the
	// activities.
	private void startGame(float x, float y) {

		controller.addGameControls();

		addActors();

		addGameBorder();

		System.out.println("##########################################################");
		this.startActivities();
		System.out.println("##########################################################");
		gameStarted = true;
	}

	// Add the actors
	private void addActors() {

		physicsManager.addBall(1.0f, 1.0f, alienId, 1.12f, 0.45f, 0.5f, 100.0f, true, true);
		physicsManager.addBox(5.0f, 3.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(5.5f, 3.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(6.0f, 3.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(5.0f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(5.5f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(6.0f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(6.5f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(7.0f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(7.5f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(8.0f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(8.5f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(9.0f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(9.5f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(3.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(3.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(4.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(4.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(11.0f, 2.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(11.5f, 2.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(12.0f, 2.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(12.5f, 2.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(13.0f, 2.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(13.0f, 7.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(13.5f, 7.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(14.0f, 7.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(14.5f, 7.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(20.0f, 3.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(20.0f, 3.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(20.0f, 6.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(20.0f, 6.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(28.0f, 3.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(28.5f, 3.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(29.0f, 3.5f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(35.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(35.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(36.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(36.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(37.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(37.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(38.0f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(38.5f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(39.0f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(39.5f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(40.0f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(44.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(44.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(45.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(45.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(46.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(46.5f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(47.0f, 9.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(60.0f, 8.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(60.5f, 8.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(61.0f, 8.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(54.0f, 4.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(54.0f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(54.0f, 6.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(68.0f, 4.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(68.0f, 5.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBox(68.0f, 6.0f, brickId, 1.12f, 0.3f, 0.05f, 0.4f, false, false);
		physicsManager.addBall(5.0f, 2.25f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(6.0f, 5.75f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(12.0f, 1.75f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(15.0f, 2.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(30.0f, 5.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(35.0f, 8.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(45.0f, 8.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(50.0f, 5.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(60.5f, 6.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(70.0f, 5.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(70.0f, 6.0f, emeraldId, 0.0f, 0.3f, 0.0f, 0.0f, false, false);
		physicsManager.addBall(3.0f, 3.0f, diamondId, 0.0f, 0.3f, 0.0f, 0.0f, false, true);
		physicsManager.addBall(7.0f, 5.75f, diamondId, 0.0f, 0.3f, 0.0f, 0.0f, false, true);
		physicsManager.addBall(9.0f, 7.0f, diamondId, 0.0f, 0.3f, 0.0f, 0.0f, false, true);
		physicsManager.addBall(20.0f, 5.0f, diamondId, 0.0f, 0.3f, 0.0f, 0.0f, false, true);
		physicsManager.addBall(30.0f, 6.0f, diamondId, 0.0f, 0.3f, 0.0f, 0.0f, false, true);
		physicsManager.addBall(60.5f, 7.0f, diamondId, 0.0f, 0.3f, 0.0f, 0.0f, false, true);
		physicsManager.addBall(70.0f, 8.0f, starId, 0.0f, 0.4f, 0.0f, 0.0f, false, false);
	}

	// Create a border around the background
	private void addGameBorder()
	{
		float density = 1.0f;
		float bounce = 0.2f;
		float friction = 0.4f;
		boolean dynamic = false;
		boolean isAbstract = false;

		float blockSize = 0.5f;
		float pixelToCoordRatio = 1.07f/158f;

		float max_x = (float) Math.ceil(drawingHelper.getBackgroundWidth()*pixelToCoordRatio) - (blockSize);
		float max_y = (float) Math.floor(drawingHelper.getBackgroundHeight()*pixelToCoordRatio);
		max_y += (blockSize)/4;

		// top & bottom borders
		for(int i = 0; i < max_x; i++)
		{
			physicsManager.addBox(i, -1.25f, crate_blueId, density, blockSize, bounce, friction, dynamic, isAbstract);
			physicsManager.addBox(i, max_y, crate_blueId, density, blockSize, bounce, friction, dynamic, isAbstract);
		}

		// left & right borders
		for(int i = 0; i < Math.ceil(max_y); i++)
		{
			physicsManager.addBox(-1, i, crate_blueId, density, blockSize, bounce, friction, dynamic, isAbstract);
			physicsManager.addBox(max_x, i, crate_blueId, density, blockSize, bounce, friction, dynamic, isAbstract);
		}
	}

	// AppearTimedEvent, creates objects and interacts with the physics world
	// every tick according with the timer.
	public class TimeObjectAppearActivity extends AsyncTask<Void, Void, Void> {

		private Object objectId;
		private float x;
		private float y;
		private float size;
		private float aSpeed;
		private float lSpeedX;
		private float lSpeedY;
		private int frequency;
		private String type;
		private boolean isAbstract;
		private float density;
		private float bounce;
		private float friction;

		public float getSize() {
			return size;
		}

		public void setSize(float size) {
			this.size = size;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getFrequency() {
			return frequency;
		}

		public void setFrequency(int frequency) {
			this.frequency = frequency;
		}

		public Object getObjectId() {
			return objectId;
		}

		public void setObjectId(Object objectId) {
			this.objectId = objectId;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

		public float getaSpeed() {
			return aSpeed;
		}

		public void setaSpeed(float aSpeed) {
			this.aSpeed = aSpeed;
		}

		public float getlSpeedX() {
			return lSpeedX;
		}

		public void setlSpeedX(float lSpeedX) {
			this.lSpeedX = lSpeedX;
		}

		public float getlSpeedY() {
			return lSpeedY;
		}

		public void setlSpeedY(float lSpeedY) {
			this.lSpeedY = lSpeedY;
		}

		public boolean getIsAbstract() {
			return isAbstract;
		}

		public void setlSpeed(boolean isAbstract) {
			this.isAbstract = isAbstract;
		}

		public float getDensity() {
			return density;
		}

		public void setDensity(float density) {
			this.density = density;
		}

		public float getBounce() {
			return bounce;
		}

		public void setBounce(float bounce) {
			this.bounce = bounce;
		}

		public float getFriction() {
			return friction;
		}

		public void setFriction(float friction) {
			this.friction = friction;
		}

		@Override
		protected Void doInBackground(Void... params) {

			while (!gameEnded) {
				try {
					Thread.sleep(this.getFrequency());

					// Caught by onProgressUpdate below
					publishProgress();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {

			Vec2 linearVel = new Vec2(lSpeedX, lSpeedY);
			if(type.equals(BODY_TYPE_BOX)){
				physicsManager.addEnergizedBox(x, y, objectId, density, bounce, friction, aSpeed,	linearVel, false);
			}
			else if (type.equals(BODY_TYPE_CIRCLE)){
				physicsManager.addEnergizedCircle(x, y, size, objectId, density, bounce, friction, aSpeed,	linearVel, false);
			}

			Body b = physicsManager.findBody(x, y);
			onElementAppear(b, x, y);
		}

		@Override
		protected void onPostExecute(Void result) {

		}


	}

	// Adds a new activity to the activity queue
	public void setActivity(AsyncTask<Void, Void, Void> act) {
		activities.add(act);
	}

	// Stops the execution of all the activities in the activity queue
	public void stopActivities() {

		// This is the global exit condition of all the ASynchTaks
		gameEnded=true;
	}

	// Starts the execution of all the activities in the activity queue
	public void startActivities() {

		for (AsyncTask<Void, Void, Void> a : activities) {
			a.execute((Void[])null);
		}
	}


	// ////////////////////////////
	// Event Managers
	// ////////////////////////////
	public void onElementTouch(Body body, double x, double y, float pressure, float tapsize) {

		ArrayList<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		EventVO ev = new EventVO(EventVO.EventType.TOUCH, bodies, x, y, System.currentTimeMillis(), pressure, tapsize);

		if (gameStarted) {
			// An ArrayList is modeled just in case we are dealing with multi-touch devices
			ArrayList<EventVO> notification = new ArrayList<EventVO>();
			notification.add(ev);
			runway.notifyChange(notification);
		}
	}

	public void onElementAppear(Body body, double x, double y) {

		ArrayList<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		EventVO ev = new EventVO(EventVO.EventType.APPEAR, bodies, x, y, System.currentTimeMillis());

		ArrayList<EventVO> notification = new ArrayList<EventVO>();
		notification.add(ev);
		runway.notifyChange(notification);
	}
	// ////////////////////////////
	// Utilities
	// ////////////////////////////

	// Cleans the canvas and resets the game state flags.
	public void restartCanvas() {
		gameStarted = false;
		gameEnded = false;
		activities = new ArrayList<AsyncTask>();

		if (drawingHelper != null) drawingHelper.cleanup();
	}

	// Indicates if the game has already started.
	public boolean isGameStarted() {
		return gameStarted;
	}

	// Sets the sate of the game to started.
	public void setGameStarted(boolean gameStarted) {
		this.gameStarted = gameStarted;
	}

	// Shows a simple toast to the user
	public void showToast(int x, int y, String text) {
		int duration = Toast.LENGTH_SHORT;
		Toast toast = MainActivity.makeCuztomizedToast(x, y, context, text,	duration);
		toast.show();
	}

	public void setRunway(ObservableWrapper<EventVO> runway) {
		this.runway = runway;
	}

	// Removes bodies from the simulation
	public void removeBody(Body toRemove) {
		physicsManager.deleteItem(toRemove);
	}

	public void setController(ControlManager controlManager) {
		controller = controlManager;
	}

	public int getTimerNumber() {
		return interval;
	}

	public float getDrawingScale() {
		float scale = getWidth() / STRETCH;
		return scale;
	}

	public Body getMainActor() {
		return physicsManager.findBody(mainActorId);
	}

	public Body getBodyIterator() {
		return physicsManager.getWorld().getBodyList();
	}

	public void scrollBody(Object objectId, float delta_x, float delta_y) {
		if (physicsManager.scrollableItemsId.containsKey(objectId)) {
			Vec2 staticPosition = physicsManager.scrollableItemsId.get(objectId);
			physicsManager.objectMoveTo(objectId, staticPosition.x + delta_x, staticPosition.y + delta_y);
		}
	}
}
