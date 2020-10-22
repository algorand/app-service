package com.algorand.prediction.service.core;

import java.util.ArrayList;
import java.util.Collection;

public class Question {

	private String id;
	private String name;
	private String text;
	private String category;
	private String authorAddress;
	private QuestionState questionState;
	private Answer answerTrue;
	private Answer answerFalse;
	private String oracleAddress;
	private long totalShares;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAuthorAddress() {
		return authorAddress;
	}

	public void setAuthorAddress(String authorAddress) {
		this.authorAddress = authorAddress;
	}

	public QuestionState getQuestionState() {
		return questionState;
	}

	public void setQuestionState(QuestionState questionState) {
		this.questionState = questionState;
	}


	public String getOracleAddress() {
		return oracleAddress;
	}

	public void setOracleAddress(String oracleAddress) {
		this.oracleAddress = oracleAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Answer getAnswerTrue() {
		return answerTrue;
	}

	public void setAnswerTrue(Answer answerTrue) {
		this.answerTrue = answerTrue;
	}

	public Answer getAnswerFalse() {
		return answerFalse;
	}

	public Answer getAnswer(boolean value) {
	    return value ? getAnswerTrue(): getAnswerFalse();
    }

	public void setAnswerFalse(Answer answerFalse) {
		this.answerFalse = answerFalse;
	}

	public long getTotalShares() {
		return totalShares;
	}

	public void setTotalShares(long totalShares) {
		this.totalShares = totalShares;
	}

	@Override
	public String toString() {
		return "Question{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", text='" + text + '\'' +
				", category='" + category + '\'' +
				", authorAddress='" + authorAddress + '\'' +
				", questionState=" + questionState +
				", answerTrue=" + answerTrue +
				", answerFalse=" + answerFalse +
				", oracleAddress='" + oracleAddress + '\'' +
				", totalShares=" + totalShares +
				'}';
	}
}
