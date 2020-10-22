package com.algorand.wallet.service.resources;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.app.service.core.TransactionEnvelope;
import com.algorand.wallet.service.WalletServiceConfiguration;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Path("/client/")
public class WalletRESTClientController {
    private Client client;

    private Logger log = LoggerFactory.getLogger("WalletRESTClientController");
    private static String serviceHost = "http://localhost:8080";
    private WalletServiceConfiguration walletServiceConfiguration;

    public WalletRESTClientController(Client client, WalletServiceConfiguration config) {
        this.client = client;
        this.walletServiceConfiguration = config;
        // default timeout value for all requests
        client.property(ClientProperties.CONNECT_TIMEOUT, 20000);
        client.property(ClientProperties.READ_TIMEOUT,    20000);
    }

    private Account getAccountForPassphrase(String passphrase) throws GeneralSecurityException {
        Account account = new Account(walletServiceConfiguration.sourceAccount);
        return account;
    }

    private TransactionEnvelope signTransaction(Account account, TransactionEnvelope transactionEnvelope) throws GeneralSecurityException {

        Transaction alogorandTransaction = transactionEnvelope.getAlgorandTransaction();

        log.info("Signing transaction: " + alogorandTransaction);
        // Sign the Transaction
        SignedTransaction signedTransaction = account.signTransaction(alogorandTransaction);

        log.info("signed transaction id: " + signedTransaction.transactionID);

        transactionEnvelope.setSignedTransaction(signedTransaction);

        return transactionEnvelope;
    }

    private void submitSignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {
        WebTarget webTarget = client.target(serviceHost + "/transaction-envelope/submit-signed-transaction" );

        Invocation  invocation   = webTarget.request(MediaType.APPLICATION_JSON).buildPost(Entity.entity(transactionEnvelope, MediaType.APPLICATION_JSON) );

        Response response = invocation.invoke();
        log.info("submit signed transaction response: " + response);
    }

    @GET
    @Path("/sign-transactions/")
    public String signTransactions() {
        log.info("signing transactions");
        Account account = null;
        try {
            account = getAccountForPassphrase(walletServiceConfiguration.sourceAccount);
        } catch (GeneralSecurityException e) {
            log.error("Error creating account from pass phrase: " + e.getMessage());
        }

        String accountAddress = account.getAddress().toString();

        //Do not hard code in your application
        WebTarget webTarget = client.target(serviceHost + "/transaction-envelope/account/" + accountAddress);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        List<TransactionEnvelope> transactionEnvelopes = invocationBuilder.get(new GenericType<List<TransactionEnvelope>>() {
        });

        log.info("signing transaction envelope: " + transactionEnvelopes);
        for (TransactionEnvelope transactionEnvelope : transactionEnvelopes) {
            try {
                log.info("account: " + account);

                log.info("signing transaction envelope: " + transactionEnvelope.toString());
                transactionEnvelope = signTransaction(account, transactionEnvelope);

                log.info("submitting signed transaction envelope: " + transactionEnvelope.toString());
                submitSignedTransactionEnvelope(transactionEnvelope);
            } catch (GeneralSecurityException e) {
                log.error("Error signing transaction: " + e.getMessage());
            }
        }
        return "OK";
    }

}