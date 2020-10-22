package com.algorand.prediction.service.core;

import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.app.service.AppServiceConfiguration;
import com.algorand.app.service.core.*;
import com.algorand.prediction.service.PredictionServiceConfiguration;
import com.google.common.collect.Iterables;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import com.algorand.algosdk.account.Account;


public class PredictionServiceImpl implements PredictionServiceInterface {

	private Logger log = LoggerFactory.getLogger("PredictionServiceImpl");
	private PredictionServiceConfiguration predictionServiceConfiguration;
	private Collection<Question> questionList;
	private Collection<Bid> bidList;
	private Collection<Exchange> exchangeList;
	private static final String APP_ID = "PredictionService";
	private Collection<OptInToQuestion> optInToQuestionList;

	private static String serviceHost = "http://localhost:8090";

	public PredictionServiceImpl(PredictionServiceConfiguration config) {
		questionList = new ArrayList<>();
		bidList = new ArrayList<>();
		exchangeList = new ArrayList<>();
		optInToQuestionList = new HashSet<>();
		predictionServiceConfiguration = config;
		initSampleData();
	}

	private void initSampleData() {
//		createSampleQuestions();
	}

	private static String TEST_ACCOUNT_1 = "KV2XGKMXGYJ6PWYQA5374BYIQBL3ONRMSIARPCFCJEAMAHQEVYPB7PL3KU";
	private static String TEST_ACCOUNT_2 = "KV2XGKMXGYJ6PWYQA5374BYIQBL3ONRMSIARPCFCJEAMAHQEVYPB7PL3KU";

	private void createSampleQuestions() {
		Question question = new Question();
		question.setAuthorAddress("NFL");
		question.setName("NewEngland Wins Super Bowl");
		question.setCategory("Sports");
		question.setId("1000");
		question.setOracleAddress("NFL");
		question.setQuestionState(QuestionState.CREATED);
		question.setText("Will the New Englad Patriots win the 2020 supper bowl?");

		Answer answer_true = new Answer();
		answer_true.setQuestionId(question.getId());
		answer_true.setAssetID("10000010");
		answer_true.setId("SuperBowl_2020_NewEngland_win_true");
		answer_true.setValue(true);

		Answer answer_false = new Answer();
		answer_false.setQuestionId(question.getId());
		answer_false.setAssetID("10000011");
		answer_false.setId("SuperBowl_2020_NewEngland_win_false");
		answer_false.setValue(false);

		question.setAnswerTrue(answer_true);
		question.setAnswerFalse(answer_false);

		questionList.add(question);

		Bid bid1 = new Bid();
		bid1.setAccountAddress(TEST_ACCOUNT_1);
		bid1.setAmount(51);
		bid1.setAnswer(answer_false);
		bid1.setId("1000");
		bid1.setShares(20);
		bid1.setState(BidState.OFFERED);
		bidList.add(bid1);

		Bid bid2 = new Bid();
		bid2.setAccountAddress(TEST_ACCOUNT_2);
		bid2.setAmount(49);
		bid2.setAnswer(answer_true);
		bid2.setId("1001");
		bid2.setShares(20);
		bid2.setState(BidState.OFFERED);
		bidList.add(bid2);

	}

	@Override
	public Collection<Question> getQuestionList() {
		try {
			updateQuestionAssetIds();
		} catch (GeneralSecurityException e) {
			log.error("error updating Question Asset ids", e);
		}
		return questionList;
	}

	@Override
	public Question getQuestion(String questionId) {
		List<Question> tempList = questionList.stream().filter(question -> questionId.equals(question.getId())).collect(Collectors.toList());
		return Iterables.getOnlyElement(tempList);
	}

	@Override
	public Collection<Bid> getBidList(String questionId) {
		return bidList.stream().filter(bid -> bid.getAnswer().getQuestionId().equals(questionId)).collect(Collectors.toList());
	}

	/**
	 *
	 */
	@Override
	public Question createQuestion(Question question) {
		question.getAnswerFalse().setAssetID("Asset ID false " + String.valueOf(System.nanoTime()));
		question.getAnswerTrue().setAssetID("Asset ID true " + String.valueOf(System.nanoTime()));
		questionList.add(question);
		// submit transactions to create assets for question true and false answers.
		try {
			TransactionGroup transactionGroup = createAssetsForQuestion(question);
			submitTransactionGroup(transactionGroup);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		return question;
	}

	@Override
	public Question updateQuestion(Question question) {
		return null;
	}

	@Override
	public void publishQuestion(Question question) {

	}

	@Override
	public Bid acceptBid(Bid bid) {
		log.info("starting acceptBid " + bid);
		bid.setId(String.valueOf(System.nanoTime()));
		bidList.add(bid);
		matchBids();
		log.info("completed acceptBid ");
		return bid;
	}

	@Override
	public void distributeWinnings(Question question) {

	}

	@Override
	public void optInToQuestion(OptInToQuestion optInToQuestion) {
		optInToQuestionList.add(optInToQuestion);

		// submit transactions to create assets for question true and false answers.
		try {
			TransactionGroup transactionGroup = createOptInForQuestion(optInToQuestion);
			submitTransactionGroup(transactionGroup);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	private TransactionGroup createOptInForQuestion(OptInToQuestion optInToQuestion) throws GeneralSecurityException {
		log.info("createOptInForQuestion: " + optInToQuestion);

		// create the transaction group
		TransactionGroup transactionGroup = new TransactionGroup();
		transactionGroup.setName("transaction group for optin: " + optInToQuestion.getQuestion().getId());
		transactionGroup.setApplicationId(APP_ID);
		transactionGroup.setDate(DateTime.now().toDate());
		transactionGroup.setDescription("transaction group for question " + optInToQuestion.getQuestion().getName());
		transactionGroup.setId(String.valueOf(System.nanoTime()));
		transactionGroup.setState("created");

		TransactionEnvelope transactionEnvelopeOptIn1 = createTransactionOptInForAsset(optInToQuestion, true);
		TransactionEnvelope transactionEnvelopeOptIn2 = createTransactionOptInForAsset(optInToQuestion, false);

		transactionGroup.getTransactionEnvelopeList().add(transactionEnvelopeOptIn1);
		transactionGroup.getTransactionEnvelopeList().add(transactionEnvelopeOptIn2);

		log.info("createOptInForQuestion generated transaction group: " + transactionGroup);

		return transactionGroup;

	}

	@Override
	public Collection<OptInToQuestion> optInToQuestionList(String account) {
		return optInToQuestionList.stream().filter(optInToQuestion -> optInToQuestion.getAccountAddress().equals(account)).collect(Collectors.toList());
	}

	private void updateQuestionAssetIds() throws GeneralSecurityException {
		List<TransactionEnvelope> transactionEnvelopes = fetchTransactions();
		updateBidState(transactionEnvelopes);
		updateQuestions(transactionEnvelopes);
	}
	private void updateQuestions(List<TransactionEnvelope> transactionEnvelopes ) throws GeneralSecurityException {
		for(TransactionEnvelope transactionEnvelope: transactionEnvelopes) {
			if (transactionEnvelope.getTransactionPrototype().getTransactionType().equals(TransactionTypeEnum.CREATE_ASSET)) {
				String assetId = transactionEnvelope.getAlgorandAssetId();
				if (assetId != null) {
					String transactionEnvelopeId = transactionEnvelope.getId();
					if (transactionEnvelopeId != null) {
						for (Question question : questionList) {
							if (transactionEnvelopeId.equals(question.getAnswerFalse().getTransactionEnvelopeId() )) {
								question.getAnswerFalse().setAssetID(assetId);
								log.info("updated answer with assigned asset id " + assetId);
								updateQuestionState(question);
								break;
							} else if (transactionEnvelopeId.equals(question.getAnswerTrue().getTransactionEnvelopeId())) {
								question.getAnswerTrue().setAssetID(assetId);
								log.info("updated answer with assigned asset id " + assetId);
								updateQuestionState(question);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void updateBidState(List<TransactionEnvelope> transactionEnvelopes ) throws GeneralSecurityException {

		for(TransactionEnvelope transactionEnvelope: transactionEnvelopes) {
			if (transactionEnvelope.getTransactionPrototype().getTransactionType().equals(TransactionTypeEnum.TRANSFER_ASSET)) {
				String assetId = transactionEnvelope.getTransactionPrototype().getAssetId();
				if (assetId != null) {
					String transactionEnvelopeId = transactionEnvelope.getId();
					if (transactionEnvelopeId != null) {
						for (Bid bid : bidList) {
							if (transactionEnvelopeId.equals(bid.getAnswer().getTransactionEnvelopeId())) {
								if (transactionEnvelope.getSignedTransaction() != null && transactionEnvelope.getSignedTransaction().sig != null) {
									bid.setState(BidState.SIGNED);
									log.info("updated bid with state  " + bid.getState());
									if (transactionEnvelope.getAlgorandTransactionId() != null) {
										bid.setState(BidState.ACCEPTED);
									}
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	private void updateQuestionState(Question question) {
		if (question.getAnswerFalse().getAssetID() != null && question.getAnswerTrue().getAssetID() != null) {
			question.setQuestionState(QuestionState.PUBLISHED);
			log.info("updated question " + question.getName() + " with assigned asset id " + question.getQuestionState());
		}
	}

	private void matchBids() {
		log.info("starting matchBids");
		for (Bid bid1 : bidList) {
			if (bid1.getState().equals(BidState.OFFERED)) {
				for (Bid bid2 : bidList) {
					if (bid2.getState().equals(BidState.OFFERED)) {
						if (!bid1.getId().equals(bid2.getId())) {
							if (bid1.getAnswer().getQuestionId().equals(bid2.getAnswer().getQuestionId()) ) {
								try {
									if (attemptExchange(bid1, bid2)) {
										break;
									}
								} catch (GeneralSecurityException e) {
									log.error("error processing bid: ", e);
								}
							}
						}
					}
				}
			}
		}
		log.info("completed matchBids");
	}

	private boolean attemptExchange(Bid bid1, Bid bid2) throws GeneralSecurityException {
		log.info("attemptExchange, bid1: " + bid1 + ", bid2: " + bid2);

		if (bid1.getAnswer().getQuestionId().equals(bid2.getAnswer().getQuestionId())) {
			log.info("attemptExchange, bid1 question: " + bid1.getAnswer().getQuestionId() + ", bid2 question: " + bid2.getAnswer().getQuestionId());
			if (bid1.getAnswer().getValue() != bid2.getAnswer().getValue()) {
				log.info("bid1.getAnswer().getValue() != bid2.getAnswer().getValue(): " + bid1.getAnswer().getValue() + ", bid2.getAnswer().getQuestionId(): " + bid2.getAnswer().getQuestionId());

				if (bid1.getAmount() + bid2.getAmount() >= 100) {
					log.info("bid1.getAmount() + bid2.getAmount() >= 100: " + bid1.getAmount() + ", bid2.getAmount(): " + bid2.getAmount());

					if (bid1.getShares() == bid2.getShares()) {
						log.info("bid1.getShares() == bid2.getShares()" + bid1.getShares() + ", bid2.getShares(): " + bid2.getShares());

						return createExchange(bid1, bid2);
					}
				}
			}
		}
		log.info("attemptExchange, bid1: " + bid1 + ", bid2: " + bid2);

		return false;
	}

	private boolean createExchange(Bid bid1, Bid bid2) throws GeneralSecurityException {
		log.info("createExchange, bid1: " + bid1 + ", bid2: " + bid2);

		String questionId = bid1.getAnswer().getQuestionId();

		Exchange exchange = new Exchange();
		if (bid1.getAnswer().getValue()) {
			exchange.setBidTrue(bid1);
			exchange.setBidFalse(bid2);
		}
		else {
			exchange.setBidTrue(bid2);
			exchange.setBidFalse(bid1);
		}
		exchange.setId(String.valueOf(System.nanoTime()));
		exchange.setQuestion(getQuestion(questionId));

		this.exchangeList.add(exchange);
		bid1.setExchangeId(exchange.getId());

		bid2.setExchangeId(exchange.getId());

		TransactionGroup transactionGroup = createTransactionGroupForExchange(exchange);

		boolean groupSubmitStatus = submitTransactionGroup(transactionGroup);

		BidState bidState1 = groupSubmitStatus ? BidState.MATCHED : BidState.FAILED;
		BidState bidState2 = groupSubmitStatus ? BidState.MATCHED : BidState.FAILED;
		bid1.setState(bidState1);
		bid2.setState(bidState2);

		log.info("createExchange return status: " + groupSubmitStatus );

		return groupSubmitStatus;
	}

	private TransactionGroup createTransactionGroupForExchange(Exchange exchange) throws GeneralSecurityException {
		log.info("createTransactionGroupForExchange: " + exchange);

		// create the transaction group
		TransactionGroup transactionGroup = new TransactionGroup();
		transactionGroup.setName("transaction group for exchange: " + exchange.getId());
		transactionGroup.setApplicationId(APP_ID);
		transactionGroup.setDate(DateTime.now().toDate());
		transactionGroup.setDescription("transaction group for exchange " + exchange.getId());
		transactionGroup.setId(String.valueOf(System.nanoTime()));
		transactionGroup.setState("created");

		TransactionEnvelope transactionEnvelopeBid1 = createTransactionEnvelopeForExchangePayment(exchange, exchange.getBidTrue());
		TransactionEnvelope transactionEnvelopeBid2 = createTransactionEnvelopeForExchangePayment(exchange, exchange.getBidFalse());

		transactionGroup.getTransactionEnvelopeList().add(transactionEnvelopeBid1);
		transactionGroup.getTransactionEnvelopeList().add(transactionEnvelopeBid2);

		// add enable asset transaction

		TransactionEnvelope transactionEnvelopeDistributeAsset1 = createTransactionEnvelopeForExchangeAssetDistribution( exchange,  exchange.getBidTrue());
		TransactionEnvelope transactionEnvelopeDistributeAsset2 = createTransactionEnvelopeForExchangeAssetDistribution( exchange,  exchange.getBidFalse());

		transactionGroup.getTransactionEnvelopeList().add(transactionEnvelopeDistributeAsset1);
		transactionGroup.getTransactionEnvelopeList().add(transactionEnvelopeDistributeAsset2);



		log.info("createTransactionGroupForExchange generated transaction group: " + transactionGroup);

		return transactionGroup;
	}

	private TransactionEnvelope createTransactionEnvelopeForExchangePayment(Exchange exchange, Bid bid) throws GeneralSecurityException {

		log.info("createTransactionFromExchange: " + exchange);
		int fee = 1000;

		Account sponserAccount = getSponserAccount();

		long cost = bid.getShares() * bid.getAmount();

		TransactionPrototype transactionPrototype = new TransactionPrototype();
		transactionPrototype.setNoteField(("exchange " + exchange.getId()).getBytes());
		transactionPrototype.setApplicationTransactionId(String.valueOf(System.nanoTime()));
		transactionPrototype.setAmount(cost);
		transactionPrototype.setFee(fee);
		transactionPrototype.setPayer(bid.getAccountAddress());
		transactionPrototype.setReceiver(sponserAccount.getAddress().encodeAsString());
		transactionPrototype.setTransactionType(TransactionTypeEnum.STANDARD);

		Agreement agreement = new Agreement();
		agreement.setContents("Algorand Insights\n" +
				"Decentralized Prediction Marketplace\n-----------------------------------------------\n\n"
				+ "You agree to pay " + cost + " Algos, with fee of " + fee + " uAlgos"
				+ "\nfor " + bid.getShares() + " '" + bid.getAnswer().getValue() + "' shares, at price per share of " + bid.getAmount() + " Algos, for Question:\n\n" + exchange.getQuestion().getText()
		        + "\n\n-----------------------------------------------\n Transaction Processing provided by Algorand Payment Services");
		agreement.setType("Algorand Insights Asset Purchase");
		agreement.setIdentifier(String.valueOf(System.nanoTime()));
		agreement.setTime(DateTime.now().toDate());

		TransactionEnvelope te = new TransactionEnvelope();
		te.setTransactionPrototype(transactionPrototype);
		te.setAgreement(agreement);
		te.setName("Prediction Service Exchange " + exchange.getId() );
		te.setDescription(exchange.toString());
		te.setDate(DateTime.now().toDate());
		te.setApplicationId(APP_ID);
		te.setId(String.valueOf(System.nanoTime()));
		te.setState("unsigned");

		log.info("returning from createTransactionFromExchange: " + te);

		return te;
	}

	private TransactionEnvelope createTransactionOptInForAsset(OptInToQuestion optin, boolean value) {
		log.info("createTransactionOptInForAsset for account: " + optin.getAccountAddress() + " and assetId Id");
		int fee = 1000;

		String assetId = optin.getQuestion().getAnswer(value).getAssetID();
		TransactionPrototype transactionPrototype = new TransactionPrototype();
		transactionPrototype.setNoteField(("opt in for Asset " + assetId).getBytes());
		transactionPrototype.setApplicationTransactionId(String.valueOf(System.nanoTime()));
		transactionPrototype.setAmount(0);
		transactionPrototype.setFee(fee);
		transactionPrototype.setPayer(optin.getAccountAddress());
		transactionPrototype.setReceiver(optin.getAccountAddress());
		transactionPrototype.setTransactionType(TransactionTypeEnum.OPTIN_ASSET);
		transactionPrototype.setAssetId(assetId);

		Agreement agreement = new Agreement();
		agreement.setContents("Algorand Insights\n" +
				"Decentralized Prediction Marketplace\n-----------------------------------------------\n\n"
				+ "You agree to opt in to question " + optin.getQuestion().getName() + " and asset " + assetId + " "
				+ ", with fee of " + fee + " uAlgos"
				+ "\nfor 0 Algos"
				+ "\n\n-----------------------------------------------\n Transaction Processing provided by Algorand Payment Services");
		agreement.setType("Algorand Insights Question-Asset OptIn");
		agreement.setIdentifier(String.valueOf(System.nanoTime()));
		agreement.setTime(DateTime.now().toDate());

		TransactionEnvelope te = new TransactionEnvelope();
		te.setTransactionPrototype(transactionPrototype);
		te.setAgreement(agreement);
		te.setName("Prediction Service OptIn for asset" + assetId );
		te.setDescription("Prediction Service OptIn for asset" + assetId);
		te.setDate(DateTime.now().toDate());
		te.setApplicationId(APP_ID);
		te.setId(String.valueOf(System.nanoTime()));
		te.setState("unsigned");
		te.setAlgorandAssetId(assetId);
		//

		log.info("returning from createTransactionOptInForAsset: " + te);
		return te;
	}

	private TransactionEnvelope createTransactionEnvelopeForExchangeAssetDistribution(Exchange exchange, Bid bid) throws GeneralSecurityException {

		log.info("createTransactionFromExchange: " + exchange);
		int fee = 1000;

		Account sponserAccount = getSponserAccount();

		long cost = bid.getShares() * bid.getAmount();

		TransactionPrototype transactionPrototype = new TransactionPrototype();
		transactionPrototype.setNoteField(("exchange " + exchange.getId()).getBytes());
		transactionPrototype.setApplicationTransactionId(String.valueOf(System.nanoTime()));
		transactionPrototype.setAmount(bid.getShares());
		transactionPrototype.setFee(fee);
		transactionPrototype.setPayer(sponserAccount.getAddress().encodeAsString());
		transactionPrototype.setReceiver(bid.getAccountAddress());
		transactionPrototype.setTransactionType(TransactionTypeEnum.TRANSFER_ASSET);
		transactionPrototype.setAssetId(exchange.getQuestion().getAnswer(bid.getAnswer().getValue()).getAssetID());

		Agreement agreement = new Agreement();
		agreement.setContents("Algorand Insights\n" +
				"Decentralized Prediction Marketplace\n-----------------------------------------------\n\nYou agree to distribute " + bid.getShares() + " " + bid.getAnswer().getValue() +", with fee of " + fee + " uAlgos"
				+ "\nFor " + cost + " '" + "' Algos, at price per share of " + bid.getAmount() + " Algos, for Question:\n\n" + exchange.getQuestion().getText()
				+ "\n\n-----------------------------------------------\n Transaction Processing provided by Algorand Payment Services");
		agreement.setType("Algorand Insights Asset Purchase");
		agreement.setIdentifier(String.valueOf(System.nanoTime()));
		agreement.setTime(DateTime.now().toDate());

		TransactionEnvelope te = new TransactionEnvelope();
		te.setTransactionPrototype(transactionPrototype);
		te.setAgreement(agreement);
		te.setName("Prediction Service Exchange " + exchange.getId() );
		te.setDescription(exchange.toString());
		te.setDate(DateTime.now().toDate());
		te.setApplicationId(APP_ID);
		te.setId(String.valueOf(System.nanoTime()));
		te.setState("unsigned");
		te.setAlgorandAssetId(bid.getAnswer().getAssetID());
		//
		bid.getAnswer().setTransactionEnvelopeId(te.getId());


		log.info("returning from createTransactionFromExchange: " + te);

		return te;
	}

	private TransactionGroup createAssetsForQuestion(Question question) throws GeneralSecurityException {
		log.info("createAssetsForQuestion: " + question);

		// create the transaction group
		TransactionGroup transactionGroup = new TransactionGroup();
		transactionGroup.setName("transaction group for question: " + question.getName());
		transactionGroup.setApplicationId(APP_ID);
		transactionGroup.setDate(DateTime.now().toDate());
		transactionGroup.setDescription("transaction group for question text " + question.getText());
		transactionGroup.setId(String.valueOf(System.nanoTime()));
		transactionGroup.setState("created");

		transactionGroup.getTransactionEnvelopeList().add(createAssetForQuestion(  question,  true));
		transactionGroup.getTransactionEnvelopeList().add(createAssetForQuestion(  question,  false));

		log.info("createAssetsForQuestion generated transaction group: " + transactionGroup);

		return transactionGroup;
	}

	private TransactionEnvelope createAssetForQuestion(Question question, boolean value) throws GeneralSecurityException {

		log.info("createAssetForQuestion: " + question + " with value " + value);

		// create the first "true" asset
		Account srcAccount = getSponserAccount();

		TransactionPrototype transactionPrototype = new TransactionPrototype();
		transactionPrototype.setNoteField(("asset answer:'" + value + "' question" + question.getId() + ":'" +  question.getName() + "'" ).getBytes());
		transactionPrototype.setApplicationTransactionId(String.valueOf(System.nanoTime()));
		transactionPrototype.setAmount(0);
		transactionPrototype.setFee(1000);
		transactionPrototype.setTransactionType(TransactionTypeEnum.CREATE_ASSET);
		transactionPrototype.setPayer(srcAccount.getAddress().encodeAsString());
		transactionPrototype.setReceiver(srcAccount.getAddress().encodeAsString());

		String assetName = question.getAnswerTrue().getValue() + ":" + question.getName();
		if (assetName.length() > 32) {
			assetName = assetName.substring(0, 31);
		}
		AssetPrototype assetPrototype = new AssetPrototype();
		assetPrototype.setAssetName(assetName);
		assetPrototype.setAssetTotal(BigInteger.valueOf(question.getTotalShares()));
		assetPrototype.setCreatorAccount(srcAccount.getAddress().encodeAsString());
		assetPrototype.setClawbackAccount(null);
		assetPrototype.setDefaultFrozen(false);
		assetPrototype.setFreezeAccount(null);
		assetPrototype.setManagerAccount(srcAccount.getAddress().encodeAsString());
		assetPrototype.setReserveAccount(srcAccount.getAddress().encodeAsString());
		assetPrototype.setUnitName("VOTE");
		assetPrototype.setUrl("https://www.algorand.com/");
		assetPrototype.setAssetMetadataHash(String.valueOf(assetPrototype.hashCode()));

		transactionPrototype.setAssetPrototype(assetPrototype);

		Agreement agreement = new Agreement();
		agreement.setContents("transaction to create asset for " + question.getId() + ":" + question.getName() + " with value " + question.getAnswer(value).getValue());
		agreement.setType("prediction asset purchase");
		agreement.setIdentifier(String.valueOf(System.nanoTime()));
		agreement.setTime(DateTime.now().toDate());

		TransactionEnvelope te = new TransactionEnvelope();
		te.setTransactionPrototype(transactionPrototype);
		te.setAgreement(agreement);
		te.setName("Prediction Service Question Asset " + question.getName() + " " + question.getAnswer(value).getValue() );
		te.setDescription("Prediction Service Question Asset " + question.getName() + " " + question.getAnswer(value).getValue());
		te.setDate(DateTime.now().toDate());
		te.setApplicationId(APP_ID);
		te.setId(String.valueOf(System.nanoTime()));
		te.setState("unsigned");

		question.getAnswer(value).setTransactionEnvelopeId(te.getId());

		log.info("returning from createAssetForQuestion: " + te);

		return te;
	}



	private static final String TRANSACTION_ENVELOPE_URL = "http://localhost:8090/transaction-envelope";
	private static final String SUBMIT_UNSIGNED_TRANSACTION_URL = TRANSACTION_ENVELOPE_URL + "/submit-unsigned-transaction";
	private static final String SUBMIT_SIGNED_TRANSACTION_URL = TRANSACTION_ENVELOPE_URL + "/submit-signed-transaction";
	private static final String SUBMIT_TRANSACTION_GROUP_URL = TRANSACTION_ENVELOPE_URL + "/submit-transaction-group";

	private boolean submitUnsignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {
		log.info("creating unsigned transaction: " + transactionEnvelope);
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(SUBMIT_UNSIGNED_TRANSACTION_URL);

		Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(transactionEnvelope, MediaType.APPLICATION_JSON));

		Response response = invocation.invoke();
		log.info("submit unsigned transaction response: " + response);

		return (response.getStatus() == HttpStatus.SC_NO_CONTENT);
	}

	private boolean submitTransactionGroup(TransactionGroup transactionGroup) {
		log.info("submit Transaction Group: " + transactionGroup);
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(SUBMIT_TRANSACTION_GROUP_URL);

		Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(transactionGroup, MediaType.APPLICATION_JSON));

		Response response = invocation.invoke();
		log.info("submit Transaction Group response: " + response);

		return (response.getStatus() == HttpStatus.SC_NO_CONTENT);
	}


	private boolean submitSignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {

		log.info("submitting signed transaction: " + transactionEnvelope);

		Client client = ClientBuilder.newClient();

		try {
			transactionEnvelope = signTransaction(transactionEnvelope);
			WebTarget webTarget = client.target(SUBMIT_SIGNED_TRANSACTION_URL);

			Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(transactionEnvelope, MediaType.APPLICATION_JSON));

			Response response = invocation.invoke();
			log.info("submit signed transaction response: " + response);
		} catch (GeneralSecurityException ex) {
			log.info("submit signed transaction error: ", ex);
			return false;
		}
		return true;

	}

	private Account sponserAccount = null;
	private Account getSponserAccount()  throws GeneralSecurityException{
		if (sponserAccount == null ) {
			sponserAccount = new Account(predictionServiceConfiguration.sourceAccount);
		}
		return sponserAccount;
	}

	public TransactionEnvelope signTransaction(TransactionEnvelope transactionEnvelope) throws GeneralSecurityException {

		Account srcAccount = getSponserAccount();

		Transaction alogorandTransaction = transactionEnvelope.getAlgorandTransaction();

		log.info("algorand transaction: " + alogorandTransaction);
		// Sign the Transaction
		SignedTransaction signedTransaction = srcAccount.signTransaction(alogorandTransaction);

		log.info("signed transaction id: " + signedTransaction.transactionID);

		transactionEnvelope.setSignedTransaction(signedTransaction);
		transactionEnvelope.setState("signed");
		return transactionEnvelope;
	}

	private List<TransactionEnvelope> fetchTransactions()  throws GeneralSecurityException {

		log.info("fetching transactions");
		Account srcAccount = getSponserAccount();

		Client client = ClientBuilder.newClient();


		String accountAddress = srcAccount.getAddress().toString();

		WebTarget webTarget = client.target(serviceHost + "/transaction-envelope/account-all/" + accountAddress);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

		List<TransactionEnvelope> transactionEnvelopeList = invocationBuilder.get(new GenericType<List<TransactionEnvelope>>() {
		});

		log.info("fetched transactions " + transactionEnvelopeList);
		return transactionEnvelopeList;

	}

}
