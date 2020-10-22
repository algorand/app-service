package com.algorand.app.service.core;

import java.util.Date;
import java.util.Objects;

import com.algorand.algosdk.algod.client.model.TransactionID;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEnvelope {
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
    private TransactionPrototype transactionPrototype;
    // TODO, move agreement to Transaction Prototype
    @JsonProperty("agreement")
    private Agreement agreement;
    @JsonProperty("algorandTransaction")
    private Transaction algorandTransaction;
    @JsonProperty("signedTransaction")
    private SignedTransaction signedTransaction;
    @JsonProperty("algorandTransactionId")
    private String algorandTransactionId;
    @JsonProperty("algorandAssetId")
    private String algorandAssetId;
    @JsonProperty("groupdId")
    private String groupdId;
    @JsonProperty("APPID")
    private Long APPID;

    public Long getAPPID() {
        return APPID;
    }

    public void setAPPID(Long APPID) {
        this.APPID = APPID;
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

    public TransactionPrototype getTransactionPrototype() {
        return transactionPrototype;
    }

    public void setTransactionPrototype(TransactionPrototype transactionPrototype) {
        this.transactionPrototype = transactionPrototype;
    }

    public Agreement getAgreement() {
        return agreement;
    }

    public void setAgreement(Agreement agreement) {
        this.agreement = agreement;
    }

    public Transaction getAlgorandTransaction() {
        return algorandTransaction;
    }

    public void setAlgorandTransaction(Transaction algorandTransaction) {
        this.algorandTransaction = algorandTransaction;
    }

    public SignedTransaction getSignedTransaction() {
        return signedTransaction;
    }

    public void setSignedTransaction(SignedTransaction signedTransaction) {
        this.signedTransaction = signedTransaction;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAlgorandTransactionId() {
        return algorandTransactionId;
    }

    public void setAlgorandTransactionId(String algorandTransactionId) {
        this.algorandTransactionId = algorandTransactionId;
    }

    public String getAlgorandAssetId() {
        return algorandAssetId;
    }

    public void setAlgorandAssetId(String algorandAssetId) {
        this.algorandAssetId = algorandAssetId;
    }

    public String getGroupdId() {
        return groupdId;
    }

    public void setGroupdId(String groupdId) {
        this.groupdId = groupdId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEnvelope that = (TransactionEnvelope) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(transactionPrototype, that.transactionPrototype) &&
                Objects.equals(agreement, that.agreement) &&
                Objects.equals(algorandTransaction, that.algorandTransaction) &&
                Objects.equals(signedTransaction, that.signedTransaction) &&
                Objects.equals(algorandTransactionId, that.algorandTransactionId) &&
                Objects.equals(algorandAssetId, that.algorandAssetId) &&
                Objects.equals(groupdId, that.groupdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, id, name, description, applicationId, date, transactionPrototype, agreement, algorandTransaction, signedTransaction, algorandTransactionId, algorandAssetId, groupdId);
    }

    @Override
    public String toString() {
        return "TransactionEnvelope{" +
                "state='" + state + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", date=" + date +
                ", transactionPrototype=" + transactionPrototype +
                ", agreement=" + agreement +
                ", algorandTransaction=" + algorandTransaction +
                ", signedTransaction=" + signedTransaction +
                ", algorandTransactionId='" + algorandTransactionId + '\'' +
                ", algorandAssetId='" + algorandAssetId + '\'' +
                ", groupdId='" + groupdId + '\'' +
                '}';
    }
}
