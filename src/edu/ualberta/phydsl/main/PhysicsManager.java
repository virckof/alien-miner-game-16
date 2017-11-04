package edu.ualberta.phydsl.main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
* Controls the physics engine of the game wrapping Box2D facilities
*/
public class PhysicsManager {

	final static float MAX_VELOCITY = 7f;

	// Bodies in the canvas (Used here only to add static bodies)
	private List<Body> bodies = new ArrayList<Body>();

	//Possible cancellation items
	private HashSet<String> staticItemsId;

	//Items that are static, but scroll
	public HashMap<Object, Vec2> scrollableItemsId;

	// Main Physics World Representation
	private World world;

	public void create(Vec2 gravity, CollisionManager collider) {

	 // Step 1: Create Physics World with Gravity
		boolean doSleep = false;
		world = new World(gravity, doSleep);
		world.setContactListener(collider);
		world.setGravity(gravity);
		staticItemsId = new HashSet<String>();

		scrollableItemsId = new HashMap<Object, Vec2>();
	}

	// Abstract object has a sprite representation, detects collision and touch
	// events but has no physics engine representation. Used for background actionable
	// items.
	public void createAbstractStatiObject(float xPos, float yPos, float xH, float yH){

		BodyDef groundBodyDef4 = new BodyDef();
		groundBodyDef4.position.set(new Vec2(xPos, yPos));
		Body groundBody4 = world.createBody(groundBodyDef4);
		PolygonShape polygonShape4 = new PolygonShape();
		polygonShape4.setAsBox(xH, yH);
		groundBody4.createFixture(polygonShape4, 1.0f);

	}

	// Creates a ball shape and uses addItem(..) to add it
	public void addBall(float x, float y, Object data, float density, float radius, float bounce, float friction, boolean dynamic, boolean isAbstract) {
		// Create Shape with Properties
		CircleShape circleShape = new CircleShape();
		circleShape.m_radius = radius;

		if(dynamic){
			addDynamicItem(x, y, circleShape, bounce, data, density, friction, isAbstract);
		}
		else{
			addStaticItem(x, y, circleShape, bounce, data, density, friction, isAbstract);
		}
	}

	// Creates a box shape and uses addItem(..) to add it
	public void addBox(float x, float y, Object data, float density, float size, float bounce, float friction, boolean dynamic, boolean isAbstract) {
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(size, size, new Vec2(size, size), 0.0f);

		if(dynamic){
			addDynamicItem(x, y, polygonShape, bounce, data, density, friction, isAbstract);
		}
		else{
			addStaticItem(x, y, polygonShape, bounce, data, density, friction, isAbstract);
		}
	}

	// Adds any dynamic item at the (x, y) position to the physics world
	private void addDynamicItem(float x, float y, Shape shape, float bounce, Object data, float density, float friction, boolean isAbstract) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.userData = data;
		bodyDef.type = BodyType.DYNAMIC;
		Body body = world.createBody(bodyDef);

		bodies.add(body);

		// Assign shape to Body
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = density;
		fixtureDef.friction = friction;
		fixtureDef.restitution = bounce;

		if (isAbstract)
		{
			fixtureDef.filter.groupIndex = -1;
		}else {
			fixtureDef.filter.groupIndex = 1;
		}
		body.createFixture(fixtureDef);
	}

	// Adds any static item at the (x, y) position to the physics world
	private void addStaticItem(float x, float y, Shape shape, float bounce, Object data, float density, float friction, boolean isAbstract) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.userData = data;
		bodyDef.type = BodyType.STATIC;
		Body body = world.createBody(bodyDef);

		bodies.add(body);

		// Assign shape to Body
		FixtureDef fixtureDef = createFixture(shape, density, friction, bounce, isAbstract);
		body.createFixture(fixtureDef);
		staticItemsId.add(x+":"+y);
	}

	// Creates a box shape and uses addEnergizedItem(..) to add it
	public void addEnergizedBox(float x, float y, Object data, float density, float bounce, float friction, float angulaVelocity, Vec2 linearVelocity, boolean isAbstract) {

		float s = 0.5f;
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(s, s, new Vec2(s, s), 0.0f);
		addEnergizedItem(x, y, polygonShape, bounce, data, density, friction, angulaVelocity, linearVelocity, isAbstract);
	}

	public void addEnergizedCircle(float x, float y, float radius, Object data, float density, float bounce, float friction, float angulaVelocity, Vec2 linearVelocity, boolean isAbstract) {

		CircleShape circleShape = new CircleShape();
		circleShape.m_radius = radius;
		addEnergizedItem(x, y, circleShape, bounce, data, density, friction, angulaVelocity, linearVelocity, isAbstract);
	}

	private void addEnergizedItem(float x, float y, Shape shape, float bounce, Object data,  float density, float friction, float angulaVelocity, Vec2 linearVelocity, boolean isAbstract) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(x, y);
		bodyDef.userData = data;
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.angularVelocity = angulaVelocity;
		bodyDef.linearVelocity = linearVelocity;
		Body body = world.createBody(bodyDef);

		bodies.add(body);

		// Assign shape to Body
		FixtureDef fixtureDef = createFixture(shape, density, friction, bounce, isAbstract);
		body.createFixture(fixtureDef);
	}

	private FixtureDef createFixture(Shape shape, float density, float friction,float bounce, boolean isAbstract) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = density;
		fixtureDef.friction = friction;
		fixtureDef.restitution = bounce;

		if (isAbstract) {
			fixtureDef.filter.groupIndex = -1;
		}
		else {
			fixtureDef.filter.groupIndex = 1;
		}

		return fixtureDef;
	}

	// Specify a scrollable object
	public void setScrollableItem(Object object, float relative_x, float relative_y) {
		scrollableItemsId.put(object, new Vec2(relative_x, relative_y));
	}

	// The solver advances bodies in time using discrete time steps.
	// Without intervention this can lead to tunneling.
	// See: http://www.box2d.org/manual.html at 2.4 Simulating the World (of Box2D)
	public void update() {
		// Update Physics World every x clock ticks
		world.step(1.0f / 60.0f, 10, 10);
	}

	//////////////////////////////
	// Utilities
	//////////////////////////////
	public World getWorld() {
		return world;
	}

	// Clears the references to the static items, and remove all the bodies.
	public void clearWorld(){
		staticItemsId.clear();
		Body body = world.getBodyList();
		while (body != null) {
			Body current = body;
			body = body.m_next;
			if (current.m_userData != null) {
				world.destroyBody(current);

			}
		}
	}

	// Deletes an specific item from the world.
	public void deleteItem(final Body toDelete){

		final Timer deleteTimer = new Timer();
		deleteTimer.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				if (!world.isLocked()) {
					Body body = world.getBodyList();
					while (body != null) {
						Body current = body;
						body = body.m_next;
						if (current.equals(toDelete)) {
							world.destroyBody(current);
						}
					}

					deleteTimer.cancel();
				}

			}
		}, 0, 1);
	}

	// finds a body by id
	public Body findBody(Object bodyId){
		if (bodyId == null) return null;

		boolean processed = false;

		while(!processed) {
			if (!world.isLocked()) {
				processed = true;
				for(Body b : bodies){
					if (b.m_userData.equals(bodyId)) {
						return b;
					}
				}
			}
		}

		return null;
	}

	// finds if there is any body in a given position (this needs to be refactored)
	public Body findBody(float x, float y){

		Body pos = null;
		for(Body b : bodies){
			if (b.getPosition().x == x && b.getPosition().y==y) {
				pos = b;
			}
		}

		return pos;

	}

	///////////////////////////////////
	//Control Object
	///////////////////////////////////

	// generic move right vector
	public void objectMoveRight(Object toMoveId){
		Body body = findBody(toMoveId);
		if(body!=null){
			Vec2 pos = body.getWorldCenter();
			body.applyLinearImpulse(new Vec2(2f, 0), pos);
		}

	}

	// generic move left vector
	public void objectMoveLeft(Object toMoveId){
		Body body = findBody(toMoveId);
		if(body!=null){
			Vec2 pos = body.getWorldCenter();
			body.applyLinearImpulse(new Vec2(-2f, 0), pos);
		}
	}

	// generic upwards force vector
	public void objectJump(Object toMoveId){
		Body body = findBody(toMoveId);
		if(body!=null){
			Vec2 pos = body.getWorldCenter();
			body.setLinearVelocity(new Vec2(body.m_linearVelocity.x, 0));
			body.setTransform(new Vec2(body.getPosition().x, body.getPosition().y +0.01f), 0);
			body.applyLinearImpulse(new Vec2(0, 5), pos);
		}

	}

	// Acceps the definition of a vector defined by the end positions of the vector
	public void objectMoveTo(Object toMoveId, float x, float y){
		Body body = findBody(toMoveId);
		if(body!=null){
			body.setTransform(new Vec2(x, y), 0);
		}
	}

	//Verifies if in the given coordinates a static element was placed.
	public boolean isStaticItem(float x, float y){
		double xR= ScoringManager.round(x, 1, BigDecimal.ROUND_HALF_DOWN);
		double yR=  ScoringManager.round(y, 1, BigDecimal.ROUND_HALF_DOWN);

		int fingerPrintRatio=7;
		double fingerPrintStep=0.1;
		double xRR = xR;
		double yRR = yR;

		for(int i=0; i<fingerPrintRatio;i++){
			for(int j=0; j<fingerPrintRatio;j++){
				if(staticItemsId.contains(xRR+":"+yRR)){
					return true;
				}
				xRR = ScoringManager.round(xRR-fingerPrintStep, 1, BigDecimal.ROUND_HALF_DOWN);
			}
			yRR = ScoringManager.round(yRR-fingerPrintStep, 1, BigDecimal.ROUND_HALF_DOWN);
		}

		xRR = xR;
		yRR = yR;

		for(int i=0; i<fingerPrintRatio;i++){
			for(int j=0; j<fingerPrintRatio;j++){
				if(staticItemsId.contains(xRR+":"+yRR)){
					return true;
				}
				xRR = ScoringManager.round(xRR+fingerPrintStep, 1, BigDecimal.ROUND_HALF_DOWN);
			}
			yRR = ScoringManager.round(yRR+fingerPrintStep, 1, BigDecimal.ROUND_HALF_DOWN);
		}

		return false;
	}
}
