package edu.ualberta.phydsl.util;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import edu.ualberta.phydsl.activities.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import edu.ualberta.phydsl.main.RenderManager;
import edu.ualberta.phydsl.main.ControlManager;


/**
* Helper class the loads all the graphical assets so they can be accessed or
* modified in R, including actor sprites and background slices, as well as
* they scaling given screen resolution and size.
*/
public class RenderHelper {

	int[] screenSizes = { 1280, 800, 480, 320 };

	// Dynamic bodies images
	private HashMap<Object, Bitmap> actorBitmaps;

	private Bitmap alien;
	private Bitmap meteorite;
	private Bitmap brick;
	private Bitmap emerald;
	private Bitmap diamond;
	private Bitmap star;
	private Bitmap crate_blue;
	private Bitmap board;
	private boolean imagesInitialized;

	private static int[] backgroundResourceId320 = {
		R.drawable.alien_planet1
		,R.drawable.alien_planet2
		,R.drawable.alien_planet3
		,R.drawable.alien_planet4
		,R.drawable.alien_planet5
		,R.drawable.alien_planet6
		,R.drawable.alien_planet7
		,R.drawable.alien_planet8
		,R.drawable.alien_planet9
	};
	private static int[] backgroundResourceId480 = {
		R.drawable.alien_planet1
		,R.drawable.alien_planet2
		,R.drawable.alien_planet3
		,R.drawable.alien_planet4
		,R.drawable.alien_planet5
		,R.drawable.alien_planet6
		,R.drawable.alien_planet7
		,R.drawable.alien_planet8
		,R.drawable.alien_planet9
	};
	private static int[] backgroundResourceId800 = {
		R.drawable.alien_planet1
		,R.drawable.alien_planet2
		,R.drawable.alien_planet3
		,R.drawable.alien_planet4
		,R.drawable.alien_planet5
		,R.drawable.alien_planet6
		,R.drawable.alien_planet7
		,R.drawable.alien_planet8
		,R.drawable.alien_planet9
	};
	private static int[] backgroundResourceId1280 = {
		R.drawable.alien_planet1
		,R.drawable.alien_planet2
		,R.drawable.alien_planet3
		,R.drawable.alien_planet4
		,R.drawable.alien_planet5
		,R.drawable.alien_planet6
		,R.drawable.alien_planet7
		,R.drawable.alien_planet8
		,R.drawable.alien_planet9
	};
	private static int BACKGROUND_SLICES = backgroundResourceId320.length;

	private Bitmap[] background;
	private float[] backgroundWidth;
	private float[] accumulatedBackgroundWidth;
	private float backgroundHeight;

	private Timer backgroundTimer;

	private RenderManager renderManager;
	private Canvas viewCanvas;
	private Resources viewResources;
	private ControlManager controlManager;

	//Different layers where objects in the screen are painted (text elements, actors, and background_
	private Paint textPaintLayer = new Paint();
	private Paint actorsPaintLayer = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint backgroundPaintLayer = new Paint(Paint.ANTI_ALIAS_FLAG);

	public RenderHelper(RenderManager renderManager, Canvas canvas, ControlManager controlManager) {
		this.renderManager = renderManager;
		this.viewResources = renderManager.getResources();
		this.viewCanvas = canvas;
		this.controlManager = controlManager;

		actorBitmaps = new HashMap<Object, Bitmap>();
	}

	// ////////////////////////////
	// Image Initialization
	// ////////////////////////////
	public void initializeImages() {
		if (imagesInitialized) return;

		int[]  aliens = { R.drawable.alien, R.drawable.alien,
			R.drawable.alien, R.drawable.alien };
		int[]  meteorites = { R.drawable.asteroid_fire, R.drawable.asteroid_fire,
			R.drawable.asteroid_fire, R.drawable.asteroid_fire };
		int[]  bricks = { R.drawable.granite, R.drawable.granite,
			R.drawable.granite, R.drawable.granite };
		int[]  emeralds = { R.drawable.emerald, R.drawable.emerald,
			R.drawable.emerald, R.drawable.emerald };
		int[]  diamonds = { R.drawable.diamond, R.drawable.diamond,
			R.drawable.diamond, R.drawable.diamond };
		int[]  stars = { R.drawable.level_end, R.drawable.level_end,
			R.drawable.level_end, R.drawable.level_end };

		int[] borders = { R.drawable.crate_blue,
				R.drawable.crate_blue, R.drawable.crate_blue,
				R.drawable.crate_blue };

		int[] boards = { R.drawable.board, R.drawable.board,
				R.drawable.board, R.drawable.board };

		int size = renderManager.getWidth();

		alien = getImageFromArrayForSize(viewResources, aliens, screenSizes, size);
		actorBitmaps.put(RenderManager.alienId, alien);
		meteorite = getImageFromArrayForSize(viewResources, meteorites, screenSizes, size);
		actorBitmaps.put(RenderManager.meteoriteId, meteorite);
		brick = getImageFromArrayForSize(viewResources, bricks, screenSizes, size);
		actorBitmaps.put(RenderManager.brickId, brick);
		emerald = getImageFromArrayForSize(viewResources, emeralds, screenSizes, size);
		actorBitmaps.put(RenderManager.emeraldId, emerald);
		diamond = getImageFromArrayForSize(viewResources, diamonds, screenSizes, size);
		actorBitmaps.put(RenderManager.diamondId, diamond);
		star = getImageFromArrayForSize(viewResources, stars, screenSizes, size);
		actorBitmaps.put(RenderManager.starId, star);

		crate_blue = getImageFromArrayForSize(viewResources, borders, screenSizes, size);
		board = getImageFromArrayForSize(viewResources, boards, screenSizes, size);

		actorBitmaps.put(RenderManager.crate_blueId, crate_blue);

		for (Object control: controlManager.controlImages.keySet()) {
			Bitmap ctrlBitmap = getImageFromArrayForSize(viewResources, controlManager.controlImages.get(control), screenSizes, size);
			actorBitmaps.put(control, ctrlBitmap);
		}

		imagesInitialized = true;
	}

	public void initializeBackground() {
		if (background != null) return;

		background = new Bitmap[BACKGROUND_SLICES];
		backgroundWidth = new float[BACKGROUND_SLICES];
		accumulatedBackgroundWidth = new float[BACKGROUND_SLICES];

		for(int i = 0; i < backgroundWidth.length; i++) {
			loadBackground(i);
			backgroundWidth[i] = background[i].getScaledWidth(viewCanvas);

			if (i != 0 && i != 1) {
				background[i].recycle();
			}
		}

		backgroundHeight = background[0].getScaledHeight(viewCanvas);

		for (int i = 0; i < backgroundWidth.length; i++) {
			if (i == 0) {
				accumulatedBackgroundWidth[i] = backgroundWidth[i];
			} else {
				accumulatedBackgroundWidth[i] = accumulatedBackgroundWidth[i-1] + backgroundWidth[i];
			}
		}

		backgroundTimer = new Timer();
		if (BACKGROUND_SLICES > 1) {
			backgroundTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					premptivelyLoadBackground();
				}
			}, 25, 25);
		}
	}

	private void loadBackground(int slice) {
		if (background[slice] == null || background[slice].isRecycled()) {

			int[] backgrounds = { backgroundResourceId1280[slice], backgroundResourceId800[slice],
			backgroundResourceId480[slice], backgroundResourceId320[slice] };

			background[slice] = getImageFromArrayForSize(viewResources, backgrounds, screenSizes, renderManager.getWidth());
		}
	}

	private void premptivelyLoadBackground() {
		Vec2 offset = getViewOffset();
		int slice = getBackgroundSlice(offset.x);

		Body b = renderManager.getMainActor();
		if (b == null) return;
		float direction = Math.signum(b.getLinearVelocity().x);

		for (int i = 0; i < background.length; i++) {
			if ((i == slice) || (i == slice + 1) || (i == slice - 1)){
				loadBackground(i);
			} else if (i == slice + 2 && direction == 1f) {
				loadBackground(i);
			}
			else {
				if (background[i] != null && !background[i].isRecycled()) {
					background[i].recycle();
				}
			}
		}
	}

	// ////////////////////////////
	// Draw Canvas
	// ////////////////////////////
	public void drawBackgroundLayer() {
		Vec2 offset = getViewOffset();
		float x_start = 0f;

		int backgroundSlice = getBackgroundSlice(offset.x);

		if (backgroundSlice == 0) {
			x_start = 0;
		} else {
			x_start = accumulatedBackgroundWidth[backgroundSlice - 1];
		}

		viewCanvas.translate(-offset.x, -offset.y);

		loadBackground(backgroundSlice);
		viewCanvas.drawBitmap(background[backgroundSlice], x_start, 0, backgroundPaintLayer);

		if (backgroundSlice != BACKGROUND_SLICES - 1
				&& (backgroundWidth[backgroundSlice]  - offset.x < renderManager.getWidth())) {
			loadBackground(backgroundSlice + 1);
			viewCanvas.drawBitmap(background[backgroundSlice + 1], accumulatedBackgroundWidth[backgroundSlice], 0, backgroundPaintLayer);
		}

		viewCanvas.translate(offset.x, offset.y);
	}

	public void drawActorLayer() {
		float scale = renderManager.getDrawingScale();

		Vec2 offset = getViewOffset();
		viewCanvas.translate(-offset.x, -offset.y);

		Body body = renderManager.getBodyIterator();

		while (body != null) {
			if (body.m_userData == null) {
				body = body.getNext();
				continue;
			}

			renderManager.scrollBody(body.m_userData, offset.x/renderManager.getDrawingScale(), offset.y/renderManager.getDrawingScale());

			Vec2 position = body.getPosition();
			Fixture fixture = body.getFixtureList();
			Shape shape = fixture.getShape();

			if(!actorBitmaps.containsKey(body.m_userData)) {
				throw new RuntimeException("No bitmap loaded for " + body.m_userData);
			}
			Bitmap bitmap = actorBitmaps.get(body.m_userData);

			if (shape instanceof PolygonShape) {
				viewCanvas.save(Canvas.MATRIX_SAVE_FLAG);
				viewCanvas.rotate((float) (180 * body.getAngle() / Math.PI), scale * position.x, scale * position.y);

				viewCanvas.drawBitmap(bitmap, scale * (position.x), scale * (position.y), actorsPaintLayer);

				viewCanvas.restore();
			}
			else if (shape instanceof CircleShape) {
				CircleShape circleShape = (CircleShape) shape;
				viewCanvas.save();
				viewCanvas.rotate((float) (180 * body.getAngle() / Math.PI), scale * position.x, scale * position.y);
				viewCanvas.drawBitmap(bitmap, scale * (position.x - circleShape.m_radius), scale * (position.y - circleShape.m_radius), actorsPaintLayer);
				viewCanvas.restore();
			}

			body = body.getNext();
		}

		viewCanvas.translate(offset.x, offset.y);
	}

	public void drawTextLayer() {
		textPaintLayer.setARGB(255, 255, 255, 255);
		int fontSize = 40;
		textPaintLayer.setTextSize(fontSize);

		int width = board.getScaledWidth(viewCanvas);
		int width_margin = (int) (width * 0.15);

		int height_margin = 10;
		int height =  (int) (board.getScaledHeight(viewCanvas)/2) + height_margin/2 + fontSize/2;

		viewCanvas.drawBitmap(board, 0, height_margin, textPaintLayer);
		viewCanvas.drawText(renderManager.getRunTimeString(), width_margin, height, textPaintLayer);

		viewCanvas.drawBitmap(board, (renderManager.getWidth() - width), height_margin, textPaintLayer);
		viewCanvas.drawText(renderManager.getScoreString(),
					(renderManager.getWidth() - width + width_margin), height, textPaintLayer);
	}

	// ////////////////////////////
	// Utilities
	// ////////////////////////////
	private int getBackgroundSlice(float x) {
		if (backgroundWidth.length == 1) return 0;

		for(int i = 0; i < backgroundWidth.length; i++) {
			float width = backgroundWidth[i];

			if (i == 0) {
				if (x < width) {
					return i;
				}
				else
					continue;
			}

			// TODO: this should be refactored in the next version.
			if (i == backgroundWidth.length - 1) {
				return i;
			}

			if (accumulatedBackgroundWidth[i - 1] <= x && x < accumulatedBackgroundWidth[i]) {
				return i;
			}
		}
		return 0;
	}

	public Vec2 getViewOffset() {
		Body actor = renderManager.getMainActor();
		if (actor == null) {
			return new Vec2(0f, 0f);
		}

		Vec2 offset =  new Vec2((actor.getPosition()));

		offset.x = calculateOffset(offset.x, renderManager.getWidth(), accumulatedBackgroundWidth[BACKGROUND_SLICES - 1]);
		offset.y = calculateOffset(offset.y, renderManager.getHeight(), backgroundHeight);

		return offset;
	}

	private float calculateOffset(float value, float cameraMax, float backgroundMax) {
		float mainActorCenter = 2f;
		float offset =  (float) Math.floor(renderManager.getDrawingScale()*(value - mainActorCenter));

		if (offset < 0) {
			offset = 0f;
		}

		if (backgroundMax > 0 && (backgroundMax - offset) < cameraMax) {
			offset= backgroundMax - cameraMax;
		}

		return Math.round(offset);
	}

	// Loading the bitmaps for the representation of the dynamic bodies
	private static Bitmap getImageFromArrayForSize(Resources res, int[] resIds, int[] sizes, int size) {

		Bitmap bmp = null;

		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i] == size) {
				bmp = BitmapFactory.decodeResource(res, resIds[i]);
				break;
			}
		}

		if (bmp == null) {
			Matrix matrix = new Matrix();
			float scale = ((float) size) / sizes[0];
			matrix.postScale(scale, scale);
			bmp = BitmapFactory.decodeResource(res, resIds[0]);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
		}
		return bmp;
	}

	public float getBackgroundWidth() {
		return accumulatedBackgroundWidth[BACKGROUND_SLICES - 1];
	}

	public float getBackgroundHeight() {
		return backgroundHeight;
	}

	// ////////////////////////////
	// Called to optimize memopry paging when objects are out of camera scope.
	// ////////////////////////////
	public void cleanup() {
		imagesInitialized = false;

		if (backgroundTimer != null) backgroundTimer.cancel();

		for (Bitmap b : actorBitmaps.values()) {
			if (b != null && !b.isRecycled()) {
				b.recycle();
			}
		}

		if (background != null) {
			for (Bitmap b : background) {
				if ( b != null && !b.isRecycled()) {
					b.recycle();
				}
			}
		}
	}
}
