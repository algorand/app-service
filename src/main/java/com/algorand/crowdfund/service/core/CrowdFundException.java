package com.algorand.crowdfund.service.core;

public class CrowdFundException extends Exception {
    public CrowdFundException(String errorMessage) {
        super(errorMessage);
    }
}
