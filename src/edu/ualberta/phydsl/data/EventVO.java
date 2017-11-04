package edu.ualberta.phydsl.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.jbox2d.dynamics.Body;
import edu.ualberta.phydsl.main.ScoringManager;

/**
* Event value object
*/
public class EventVO {

	public enum EventType {
		APPEAR,
		COLLISION,
		TOUCH
	}

	private EventType type;

	private double x;

	private double y;

	private Long time;

	private float pressure;

	private float tapsize;

	private ArrayList <Body> bodies;

	public EventVO(EventType type, ArrayList <Body> bodies, double x, double y, Long time){
		this.type = type;
		this.bodies = bodies;
		this.x = x;
		this.y = y;
		this.time = time;
	}

	public EventVO(EventType type, ArrayList <Body> bodies, double x, double y, Long time, float pressure, float tapsize){
		this(type, bodies, x, y, time);

		this.pressure = pressure;
		this.tapsize = tapsize;
	}

	public ArrayList <Body> getBodies() {
		return bodies;
	}

	public void setBodies(ArrayList <Body> bodies) {
		this.bodies = bodies;
	}

	public double getX() {
		return  x;
	}

	public void setX(int x) {
		this.x = (int) x;
	}

	public double getY() {
		return y;
	}

	public void setY(int y) {
		this.y = (int) y;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public float getPressure() {
		return pressure;
	}

	public void setPressure(float pressure) {
		this.pressure = pressure;
	}

	public float getTapSize() {
		return tapsize;
	}

	public void setTapSize(float tapsize) {
		this.tapsize = tapsize;
	}

	public EventType getType() {
		return type;
	}

	public String toString(){
		return ScoringManager.round(x, 1, BigDecimal.ROUND_HALF_DOWN) +":"+ScoringManager.round(y, 1, BigDecimal.ROUND_HALF_DOWN)+":"+time;
	}

	public String toStringHuman(){
		String str = "(x: "+x+", "+y+") " +"time: "+time;

		if (pressure != 0) {
			str += " pressure: " + pressure + " tap size: " + tapsize;
		}

		return str;
	}

}
