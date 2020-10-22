package com.algorand.app.service.core;

import java.util.Date;

/**
 * ApplicationAccountRegistration provides association between a Wallet, Account and Application
 */
public class AccountApplicationRegistration {

    private String applicationId;
    private String accountAddress;
    private String walletIdentifier;
    private Date createDate;

    public String getId() {
        return accountAddress + ":" + applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public String getWalletIdentifier() {
        return walletIdentifier;
    }

    public void setWalletIdentifier(String walletIdentifier) {
        this.walletIdentifier = walletIdentifier;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

}
