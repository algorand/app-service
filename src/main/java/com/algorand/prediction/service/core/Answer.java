package com.algorand.prediction.service.core;

public class Answer {

	private String id;
    private String questionId;
	private Boolean value;
	private String assetID;
	private String transactionEnvelopeId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public String getAssetID() {
		return assetID;
	}

	public void setAssetID(String assetID) {
		this.assetID = assetID;
	}

	public String getTransactionEnvelopeId() {
		return transactionEnvelopeId;
	}

	public void setTransactionEnvelopeId(String transactionEnvelopeId) {
		this.transactionEnvelopeId = transactionEnvelopeId;
	}

	@Override
	public String toString() {
		return "Answer{" +
				"id='" + id + '\'' +
				", questionId='" + questionId + '\'' +
				", value=" + value +
				", assetID='" + assetID + '\'' +
				", transactionEnvelopeId='" + transactionEnvelopeId + '\'' +
				'}';
	}
}
