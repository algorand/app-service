package com.algorand.crowdfund.service.core;

import java.util.Collection;

public interface CrowdFundServiceInterface {

    Fund createFund(Fund fund) throws CrowdFundException;

    Fund getFund(String fundId);

    Collection<Fund> getFundList();

    Investment createInvestment(Investment investment) throws CrowdFundException;

    Investment getInvestment(String donationId);

    Collection<Investment> getInvestmentList(String fundId);

    Claim claimFunds(Claim claim) throws CrowdFundException;

    Reclaim reclaimInvestment(Reclaim reclaim) throws CrowdFundException;

    Closeout closeOutFund(String fundId);

}
