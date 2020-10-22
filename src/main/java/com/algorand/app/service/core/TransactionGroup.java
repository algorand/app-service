package com.algorand.app.service.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionGroup {
    @JsonProperty("state")
    private String state = "unsigned";
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("applicationId")
    private String applicationId;
    @JsonProperty("date")
    private Date date;
    @JsonProperty("transactionPrototype")
    private List<TransactionEnvelope> transactionEnvelopeList;
    // TODO, move agreement to Transaction Prototype
    @JsonProperty("agreement")
    private Agreement agreement;
    @JsonProperty("algorandTransactionId")
    private String algorandTransactionId;

    private String groupId;

    public TransactionGroup() {
        transactionEnvelopeList = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Agreement getAgreement() {
        return agreement;
    }

    public void setAgreement(Agreement agreement) {
        this.agreement = agreement;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<TransactionEnvelope> getTransactionEnvelopeList() {
        return transactionEnvelopeList;
    }

    public void setTransactionEnvelopeList(List<TransactionEnvelope> transactionEnvelopeList) {
        this.transactionEnvelopeList = transactionEnvelopeList;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAlgorandTransactionId() {
        return algorandTransactionId;
    }

    public void setAlgorandTransactionId(String algorandTransactionId) {
        this.algorandTransactionId = algorandTransactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionGroup that = (TransactionGroup) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(transactionEnvelopeList, that.transactionEnvelopeList) &&
                Objects.equals(agreement, that.agreement) &&
                Objects.equals(algorandTransactionId, that.algorandTransactionId) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, id, name, description, applicationId, date, transactionEnvelopeList, agreement, algorandTransactionId, groupId);
    }

    @Override
    public String toString() {
        return "TransactionGroup{" +
                "state='" + state + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", date=" + date +
                ", transactionEnvelopeList=" + transactionEnvelopeList +
                ", agreement=" + agreement +
                ", algorandTransactionId='" + algorandTransactionId + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
