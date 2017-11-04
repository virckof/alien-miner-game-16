package edu.ualberta.phydsl.data;

/**
* Score value object
*/
public class ScoreVO {

	private long id;

	private String username;

	private String scoreTotal;

	private String playerFinalState;

	private String events;

	public String getEvents() {
		return events;
	}

	public void setEvents(String events) {
		this.events = events;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getScoreTotal() {
		return scoreTotal;
	}

	public void setScoreTotal(String scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	public String getPlayerFinalState() {
		return playerFinalState;
	}

	public void setPlayerFinalState(String playerFinalSate) {
		this.playerFinalState = playerFinalSate;
	}

	public String toString(){
		return "U:"+username+"/ ST:"+scoreTotal;
	}

}
