package edu.ualberta.phydsl.util;

// Helper, passes parameter between activities toa void tight coupling between
// them, and deal with imposibility of multiple inheritance with Observable wrapper
// This should be refactored at some point w/ somethign more elegant.
public class ActivitiesBridge {

	private static Object object;

	/**
	 * Set object to static variable and retrieve it from another activity
	 */
	public static void setObject(Object obj) {
		object = obj;
	}

	/**
	 * Get object passed from previous activity
	 */
	public static Object getObject() {
		Object obj = object;
		// Can get only once
		object = null;
		return obj;
	}
}
