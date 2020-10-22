package com.algorand.app.client;

import com.algorand.app.service.core.TransactionEnvelope;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.app.service.core.UserAccountRegistration;

import java.security.GeneralSecurityException;

import java.util.List;

import com.algorand.app.service.core.Agreement;

import com.algorand.app.service.core.TransactionPrototype;
import com.algorand.app.service.core.TransactionTypeEnum;

import java.util.Date;

import javax.ws.rs.core.GenericType;

public class TransactionGatewayClient {

    private Logger log = LoggerFactory.getLogger("TransactionGatewayClient");
    private static final String APP_SERVICE_HOST = "app-service-toronto-hackathon-1811157952.us-east-2.elb.amazonaws.com";
    private static final int APP_SERVICE_PORT = 80;

    private Client client = ClientBuilder.newClient();

    private static String serviceHost = "http://" + APP_SERVICE_HOST + ":" + APP_SERVICE_PORT;

    private static final String SUBMIT_UNSIGNED_TRANSACTION_URL = serviceHost + "/transaction-envelope/submit-unsigned-transaction";


    public String fetchAgreement(String ipfsURL) {

        String ipfsHash = ipfsURL.substring(6);
        WebTarget webTarget = client.target(serviceHost + "/transaction-envelope/get-contents/" + ipfsHash);

        log.info("web target: " + webTarget);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        String content = invocationBuilder.get(new GenericType<String>() {
        });

        log.info("Received transaction count " + content);
        return content;
    }

    public List<TransactionEnvelope> fetchTransactions(String accountAddress) {

        WebTarget webTarget = client.target(serviceHost + "/transaction-envelope/account/" + accountAddress);

        log.info("web target: " + webTarget);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        List<TransactionEnvelope> transactionEnvelopes = invocationBuilder.get(new GenericType<List<TransactionEnvelope>>() {
        });

        log.info("Received transaction count " + transactionEnvelopes.size());

        return transactionEnvelopes;

    }

    private com.algorand.algosdk.algod.client.model.Account getAccount(String accountAddress) {
        log.info("getAccountStatus: " + accountAddress);
        WebTarget webTarget = client.target(serviceHost + "/algod/account/" + accountAddress);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        com.algorand.algosdk.algod.client.model.Account account = invocationBuilder.get(new GenericType<com.algorand.algosdk.algod.client.model.Account>() {
        });
        log.info("account: " + account);
        return account;
    }

    public void updateUserAccountRegistration(String alias, String address) {

        UserAccountRegistration userAccountRegistration = new UserAccountRegistration();
        userAccountRegistration.setAlias(alias);
        userAccountRegistration.setAddress(address);

        log.info("sending userAccountRegistration: " + userAccountRegistration);

        WebTarget webTarget = client.target(serviceHost + "/account-application-registration/user-account");

        Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(userAccountRegistration, MediaType.APPLICATION_JSON));

        Response response = invocation.invoke();
        log.info("submit userAccountRegistration response: " + response);

    }

    public TransactionEnvelope signTransaction(TransactionEnvelope transactionEnvelope, Account account) throws GeneralSecurityException {

        Transaction alogorandTransaction = transactionEnvelope.getAlgorandTransaction();
        // Sign the Transaction
        SignedTransaction signedTransaction = account.signTransaction(alogorandTransaction);

        log.info("signed transaction id: " + signedTransaction.transactionID);

        transactionEnvelope.setSignedTransaction(signedTransaction);
        transactionEnvelope.setState("signed");
        return transactionEnvelope;
    }

    public Agreement createAgreement(TransactionEnvelope transactionEnvelope, int amount, int fee, String agreementText) {
        Agreement agreement = new Agreement();
        agreement.setIdentifier("agreement-" + transactionEnvelope.getId());
        agreement.setTime(new Date());
        agreement.setType("receipt");
        StringBuilder contents = new StringBuilder();
        contents.append("receipt\n");
        contents.append("date: ").append(transactionEnvelope.getDate().toString()).append("\n");
        contents.append("you agree to pay ").append(amount).append(" algos, with fee of ").append(fee).append(" microAlogs");
        contents.append("\n ----------------- \n ").append(agreementText).append("\n -----------------");
        agreement.setContents(contents.toString());
        agreement.setSignature("signature of agreement by the application: ".getBytes());
        agreement.setType("pay");
        return agreement;
    }


    public void processCreateTransactionEnvelope(int amount, int fee, String transactionId, String payer, String receiver, String noteField) {

        TransactionEnvelope transactionEnvelope = new TransactionEnvelope();
        transactionEnvelope.setApplicationId("TEST_APP");
        transactionEnvelope.setId(transactionId);


        transactionEnvelope.setDate(new Date());
        transactionEnvelope.setName("transaction-envelope-" + transactionId);
        transactionEnvelope.setDescription("Sample transaction envelope");
        transactionEnvelope.setApplicationId("PoS App");

        transactionEnvelope.setId(transactionId);

        transactionEnvelope.setAgreement(createAgreement(transactionEnvelope, amount, fee, ""));
        TransactionPrototype tp = new TransactionPrototype();
        tp.setTransactionType(TransactionTypeEnum.STANDARD);
        tp.setAmount(amount);
        tp.setFee(fee);
        tp.setPayer(payer);
        tp.setReceiver(receiver);
        tp.setNoteField(noteField.getBytes());
        tp.setApplicationTransactionId(transactionId);
        transactionEnvelope.setTransactionPrototype(tp);
        log.info("created transaction envelope: " + transactionEnvelope);

        submitUnsignedTransactionEnvelope(transactionEnvelope);
    }

    public void submitUnsignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {
        log.info("creating unsigned transaction: " + transactionEnvelope);
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(SUBMIT_UNSIGNED_TRANSACTION_URL);

        Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(transactionEnvelope, MediaType.APPLICATION_JSON));

        Response response = invocation.invoke();
        log.info("submit unsigned transaction response: " + response);
    }

    public void submitSignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {
        log.info("submitting signed transaction: " + transactionEnvelope);

        WebTarget webTarget = client.target(serviceHost + "/transaction-envelope/submit-signed-transaction");

        Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(transactionEnvelope, MediaType.APPLICATION_JSON));

        Response response = invocation.invoke();
        log.info("submit signed transaction response: " + response);
        if (response != null && response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            String message = "Transaction submitted";
            log.info(message);
        } else {
            String message = "Error submitting transaction";
            log.info(message);
        }
    }
}
