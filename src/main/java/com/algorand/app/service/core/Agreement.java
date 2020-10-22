package com.algorand.app.service.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class Agreement {
    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("type")
    private String type;
    @JsonProperty("contents")
    private String contents;
    @JsonProperty("time")
    private Date time;
    @JsonProperty("signature")
    private byte[] signature;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agreement agreement = (Agreement) o;
        return Objects.equals(identifier, agreement.identifier) &&
                Objects.equals(type, agreement.type) &&
                Objects.equals(contents, agreement.contents) &&
                Objects.equals(time, agreement.time) &&
                Arrays.equals(signature, agreement.signature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(identifier, type, contents, time);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    @Override
    public String toString() {
        return "Agreement{" +
                "identifier='" + identifier + '\'' +
                ", type='" + type + '\'' +
                ", contents='" + contents + '\'' +
                ", time=" + time +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }
}
