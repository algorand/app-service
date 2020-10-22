package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.app.service.core.AppServiceImpl;
import com.algorand.app.service.core.TransactionEnvelope;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class CrowdFundServiceImpl implements CrowdFundServiceInterface  {

	private Logger log = LoggerFactory.getLogger("CrowdFundServiceImpl");
	private Collection<Fund> fundList;
	private Collection<Investment> investmentList;
	private Collection<Claim> claimList;
	private Collection<Reclaim> reclaimList;
	private final String algodNetworkHost;
	private final int algodNetworkPort;
	private final String apiToken;
	final AppServiceImpl appService;

	private static final String APP_ID = "CrowdFundService";

	Collection<Investment> getInvestmentList() { return investmentList; }
	Collection<Claim> getClaimList() { return claimList; }
	Collection<Reclaim> getReclaimList() { return reclaimList; }

	public CrowdFundServiceImpl(AppServiceImpl appService, final String algodNetworkHost, final int algodNetworkPort, final String apiToken) {
		this.appService = appService;
		this.algodNetworkHost = algodNetworkHost;
		this.algodNetworkPort = algodNetworkPort;
		this.apiToken = apiToken;
		fundList = new ArrayList<>();
		investmentList = new ArrayList<>();
		claimList = new ArrayList<>();
		reclaimList = new ArrayList<>();

		initSampleData();
	}

	private void initSampleData() {

	}

	private AlgodClient getAlgodClient() {

		String apiAddress = "http://" + algodNetworkHost + ":" + algodNetworkPort;
		log.info("connecting to algod node at " + apiAddress);
		//Create an instance of the algod API client
		AlgodClient client = (AlgodClient) new AlgodClient(algodNetworkHost,algodNetworkPort,apiToken );
		return client;
	}

	@Override
	public Collection<Fund> getFundList() {
		return fundList;
	}

	@Override
	public Fund getFund(String fundId) {
		List<Fund> tempList = fundList.stream().filter(fund -> fundId.equals(fund.getId())).collect(Collectors.toList());
		return Iterables.getOnlyElement(tempList);
	}

	@Override
	public Collection<Investment> getInvestmentList(String fundId) {
		return investmentList.stream().filter(investment -> investment.getFundId().equals(fundId)).collect(Collectors.toList());
	}

	@Override
	public Claim claimFunds(Claim claim) throws CrowdFundException {
		AlgodClient algodClient = getAlgodClient();
		try {
			Fund fund = getFund(claim.getFundId());
			if (fund == null) {
				String message = "fund not found '" + claim.getFundId() + "'";
				log.error(message);
				throw new CrowdFundException(message);
			}
			(new ClaimFactory(this)).createClaim( algodClient, fund, claim);
		} catch (Exception e) {
			String message = "error creating fund, " + e.getMessage();
			log.error(message);
			throw new CrowdFundException(message);
		}
		claimList.add(claim);
		return claim;
	}

	@Override
	public Reclaim reclaimInvestment(Reclaim reclaim) throws CrowdFundException {
		AlgodClient algodClient = getAlgodClient();
		try {
			Fund fund = getFund(reclaim.getFundId());
			if (fund == null) {
				String message = "fund not found '" + reclaim.getFundId() + "'";
				log.error(message);
				throw new CrowdFundException(message);
			}
			(new ReclaimFactory(this)).createReclaim( algodClient, fund, reclaim);
		} catch (CrowdFundException e) {
			throw e;
		} catch (Exception e) {
			String message = "error creating fund, " + e.getMessage();
			log.error(message);
			throw new CrowdFundException(message);
		}
		reclaimList.add(reclaim);
		return reclaim;
	}

	@Override
	public Closeout closeOutFund(String fundId) {
		return null;
	}

	@Override
	public Investment getInvestment(String donationId) {
		Collection<Investment> investmentList1 = investmentList.stream().filter(investment -> investment.getId().equals(donationId)).collect(Collectors.toList());
		if (investmentList1.size() > 0) {
			return investmentList1.iterator().next();
		}
		return null;
	}

	@Override
	public Fund createFund(Fund fund) throws CrowdFundException {

		AlgodClient algodClient = getAlgodClient();
		try {
			(new CrowdFundFactory(this)).createCrowdFund(  algodClient, fund);
		} catch (Exception e) {
			String message = "error creating fund, " + e.getMessage();
			log.error(message);
			throw new CrowdFundException(message);
		}
		fundList.add(fund);
		return fund;
	}

	@Override
	public Investment createInvestment(Investment investment) throws CrowdFundException {
		investmentList.add(investment);
		Fund fund = getFund(investment.getFundId());
		if (fund != null) {
            try {
                (new InvestmentFactory(this)).createInvestment(getAlgodClient(), fund, investment);
            } catch (CrowdFundException e) {
                e.printStackTrace();
            }
        } else {
		    throw new CrowdFundException("error, no matching fund " + investment.getFundId() + " for investment ");
        }
		return investment;
	}

	// package private
	boolean submitSignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) throws CrowdFundException {

		log.info("submitting signed transaction: " + transactionEnvelope);

		try {
			appService.submitSignedTransactionEnvelope(transactionEnvelope);
		} catch (GeneralSecurityException e) {
			String message = "error submitting signed transaction envelope " + e.getMessage();
			log.error(message);
			throw new CrowdFundException(message);
		}

		return true;
	}

	public TransactionEnvelope signTransaction(TransactionEnvelope transactionEnvelope) throws CrowdFundException {
		try {
			appService.signTransaction(transactionEnvelope);
		} catch (GeneralSecurityException e) {
			String message = "error signing transaction envelope " + e.getMessage();
			log.error(message);
			throw new CrowdFundException(message);
		}
		return transactionEnvelope;
	}

}
