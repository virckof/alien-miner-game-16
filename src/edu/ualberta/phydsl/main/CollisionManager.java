package edu.ualberta.phydsl.main;

import java.util.ArrayList;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import edu.ualberta.phydsl.data.EventVO;
import edu.ualberta.phydsl.util.ObservableWrapper;


/**
* Detects the collision of two bodies, the event is triggered at the end
* of the contact
*/
public class CollisionManager implements ContactListener{

	private ObservableWrapper<EventVO> runway;

	public CollisionManager() {
	}

	// Beginning of contact
	public void beginContact(Contact contact) {
		//ignored
	}

	//  End of contact
	public void endContact(Contact contact) {

		Fixture colA = contact.getFixtureA();
		Body bodA = colA.getBody();

		Fixture colB = contact.getFixtureB();
		Body bodB= colB.getBody();

		ArrayList<Body> bodies = new ArrayList<Body>();
		bodies.add(bodA);
		bodies.add(bodB);

		EventVO ev = new EventVO(EventVO.EventType.COLLISION, bodies, bodA.getPosition().x, bodA.getPosition().y, System.currentTimeMillis());
		ArrayList<EventVO> notification = new ArrayList<EventVO>();
		notification.add(ev);
		runway.notifyChange(notification);
	}

	// This method enables the collision manager to notify the event runway
	// when a collision happens
	public void setRunway(ObservableWrapper<EventVO> runway) {
		this.runway = runway;
	}

	//////////////////////////////
	// Ignored
	//////////////////////////////
	public void postSolve(Contact arg0, ContactImpulse arg1) {
		// Ignored
	}

	public void preSolve(Contact arg0, Manifold arg1) {
		// Ignored
	}
}
