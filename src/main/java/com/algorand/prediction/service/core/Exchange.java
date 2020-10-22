package com.algorand.prediction.service.core;


public class Exchange {

	private String id;
	private int round;
	private String transactionId;
	private Question question;
	private Bid bidTrue;
	private Bid bidFalse;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Bid getBidTrue() {
		return bidTrue;
	}

	public void setBidTrue(Bid bidTrue) {
		this.bidTrue = bidTrue;
	}

	public Bid getBidFalse() {
		return bidFalse;
	}

	public void setBidFalse(Bid bidFalse) {
		this.bidFalse = bidFalse;
	}

	@Override
	public String toString() {
		return "Exchange{" +
				"id='" + id + '\'' +
				", round=" + round +
				", transactionId='" + transactionId + '\'' +
				", question=" + question +
				", bidTrue=" + bidTrue +
				", bidFalse=" + bidFalse +
				'}';
	}
}
