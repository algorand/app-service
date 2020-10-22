package com.algorand.prediction.service.core;


public class Bid {

	private String id;
	private int amount;
	private int shares;
	public BidState state;
	private Answer answer;
	private String accountAddress;
	private String exchangeId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getShares() {
		return shares;
	}

	public void setShares(int shares) {
		this.shares = shares;
	}

	public BidState getState() {
		return state;
	}

	public void setState(BidState state) {
		this.state = state;
	}

	public Answer getAnswer() {
		return answer;
	}

	public void setAnswer(Answer answer) {
		this.answer = answer;
	}

	public String getAccountAddress() {
		return accountAddress;
	}

	public void setAccountAddress(String accountAddress) {
		this.accountAddress = accountAddress;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	@Override
	public String toString() {
		return "Bid{" +
				"id='" + id + '\'' +
				", amount=" + amount +
				", shares=" + shares +
				", state=" + state +
				", answer=" + answer +
				", accountAddress='" + accountAddress + '\'' +
				", exchangeId='" + exchangeId + '\'' +
				'}';
	}
}
