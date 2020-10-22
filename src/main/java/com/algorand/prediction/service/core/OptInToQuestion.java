package com.algorand.prediction.service.core;

import java.util.Objects;

public class OptInToQuestion {

    private Question question;
    private String accountAddress;


    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptInToQuestion that = (OptInToQuestion) o;
        return Objects.equals(question, that.question) &&
                Objects.equals(accountAddress, that.accountAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, accountAddress);
    }

    @Override
    public String toString() {
        return "OptInToQuestion{" +
                "question=" + question +
                ", accountAddress='" + accountAddress + '\'' +
                '}';
    }
}
