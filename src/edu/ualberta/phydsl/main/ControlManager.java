package edu.ualberta.phydsl.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import edu.ualberta.phydsl.activities.R;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import edu.ualberta.phydsl.data.EventVO;

/**
* This class encapsulates the on-screen controls of the game and managers
* their events translating them into forces to be interpreted by the physics
* manager.
*/
public class ControlManager implements Observer {

	public static Object jumpId = new Object();
	public static Object rightId = new Object();
	public static Object leftId = new Object();

	private float MAX_VELOCITY_X = 4f;

	public HashMap<Object, int[]> controlImages = new HashMap<Object, int[]>();

	private PhysicsManager physicsManager;

	public ControlManager(PhysicsManager physicsManager) {
		this.physicsManager = physicsManager;

		controlImages.put(jumpId, new int[] { R.drawable.a_button, R.drawable.a_button,
			R.drawable.a_button, R.drawable.a_button });
		controlImages.put(rightId, new int[] { R.drawable.arrow_right, R.drawable.arrow_right,
			R.drawable.arrow_right, R.drawable.arrow_right });
		controlImages.put(leftId, new int[] { R.drawable.arrow_left, R.drawable.arrow_left,
			R.drawable.arrow_left, R.drawable.arrow_left });
	}

	public void objectMove(Object toMoveId, float forceX, float forceY){
		moveBody(toMoveId, new Vec2(forceX, forceY));
	}

	public void objectRemove(Object toMoveId){
		Body body = physicsManager.findBody(toMoveId);
		if(body!=null){
			physicsManager.deleteItem(body);
		}
	}

	// updayes the event manager to register control events for
	// analytics purposes
	public void update(Observable o, Object pl) {
		ArrayList<EventVO> payload = (ArrayList<EventVO>) pl;
		EventVO ev = payload.get(0);
		Body a = ev.getBodies().get(0);

		if (ev.getType() != EventVO.EventType.TOUCH)
			return;

		if (a.m_userData == jumpId) {
			objectMove(RenderManager.mainActorId, 0.0f, -5.0f);
		}
		if (a.m_userData == rightId) {
			objectMove(RenderManager.mainActorId, 2.0f, 0.0f);
		}
		if (a.m_userData == leftId) {
			objectMove(RenderManager.mainActorId, -2.0f, 0.0f);
		}
	}

	// Moves a body applying force via the physics manager given a vector
	private void moveBody(Object toMoveId, Vec2 impulse) {
		Body body = physicsManager.findBody(toMoveId);
		if(body!=null){
			Vec2 pos = body.getWorldCenter();
			body.setLinearVelocity(capVelocity(body.getLinearVelocity()));
			body.applyLinearImpulse(impulse, pos);
		}
	}

	// Controls velocity cap to avoid objects to have unlimited acceleration.
	private Vec2 capVelocity(Vec2 currentVelocity) {
		Vec2 capVelocity = currentVelocity;

		if (Math.abs(currentVelocity.x) > MAX_VELOCITY_X) {
			capVelocity.x = MAX_VELOCITY_X * Math.signum(currentVelocity.x);
		}

		return capVelocity;
	}

	//  Control layout 
	public void addGameControls() {
		physicsManager.addBox(6.75f, 3.6f, jumpId, 0, 0.5f, 0, 0, false, true);
		physicsManager.setScrollableItem(jumpId, 6.75f, 3.6f);

		physicsManager.addBox(1.0f, 3.6f, rightId, 0, 0.5f, 0, 0, false, true);
		physicsManager.setScrollableItem(rightId, 1.0f, 3.6f);

		physicsManager.addBox(0.1f, 3.6f, leftId, 0, 0.5f, 0, 0, false, true);
		physicsManager.setScrollableItem(leftId, 0.1f, 3.6f);

	}
}
