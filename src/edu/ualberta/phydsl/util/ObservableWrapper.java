package edu.ualberta.phydsl.util;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

// Helper, passes parameter between activities to avoid tight coupling between
// them, and deal with impossibility of multiple inheritance with activities bridge
// This should be refactored at some point w/ something more elegant.
public class ObservableWrapper<T> extends Observable{

	public void notifyChange(ArrayList<T> notification){
		setChanged();
		notifyObservers(notification);
	}

	public void registerObserver(Observer observer) {
		registerObserver(observer);
	}
}
