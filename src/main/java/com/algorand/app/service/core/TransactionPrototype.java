package com.algorand.app.service.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionPrototype {

    @JsonProperty("applicationTransactionId")
    private String applicationTransactionId;
    @JsonProperty("payer")
    private String payer;
    @JsonProperty("receiver")
    private String receiver;
    @JsonProperty("amount")
    private long amount;
    @JsonProperty("fee")
    private long fee;
    @JsonProperty("transactionType")
    private TransactionTypeEnum transactionType;
    @JsonProperty("tealScript")
    private String tealScript;
    @JsonProperty("assetPrototype")
    private AssetPrototype assetPrototype;
    @JsonProperty("assetId")
    private String assetId;
    @JsonProperty("noteField")
    private byte[] noteField;

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public byte[] getNoteField() {
        return noteField;
    }

    public void setNoteField(byte[] noteField) {
        this.noteField = noteField;
    }

    public String getApplicationTransactionId() { return applicationTransactionId; }

    public void setApplicationTransactionId(String applicationTransactionId) { this.applicationTransactionId = applicationTransactionId; }

    public TransactionTypeEnum getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionTypeEnum transactionType) {
        this.transactionType = transactionType;
    }

    public String getTealScript() {
        return tealScript;
    }

    public void setTealScript(String tealScript) {
        this.tealScript = tealScript;
    }

    public AssetPrototype getAssetPrototype() {
        return assetPrototype;
    }

    public void setAssetPrototype(AssetPrototype assetPrototype) {
        this.assetPrototype = assetPrototype;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionPrototype that = (TransactionPrototype) o;
        return amount == that.amount &&
                fee == that.fee &&
                Objects.equals(applicationTransactionId, that.applicationTransactionId) &&
                Objects.equals(payer, that.payer) &&
                Objects.equals(receiver, that.receiver) &&
                transactionType == that.transactionType &&
                Objects.equals(tealScript, that.tealScript) &&
                Objects.equals(assetPrototype, that.assetPrototype) &&
                Objects.equals(assetId, that.assetId) &&
                Arrays.equals(noteField, that.noteField);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(applicationTransactionId, payer, receiver, amount, fee, transactionType, tealScript, assetPrototype, assetId);
        result = 31 * result + Arrays.hashCode(noteField);
        return result;
    }

    @Override
    public String toString() {
        return "TransactionPrototype{" +
                "applicationTransactionId='" + applicationTransactionId + '\'' +
                ", payer='" + payer + '\'' +
                ", receiver='" + receiver + '\'' +
                ", amount=" + amount +
                ", fee=" + fee +
                ", transactionType=" + transactionType +
                ", tealScript='" + tealScript + '\'' +
                ", assetPrototype=" + assetPrototype +
                ", assetId='" + assetId + '\'' +
                ", noteField=" + Arrays.toString(noteField) +
                '}';
    }
}
