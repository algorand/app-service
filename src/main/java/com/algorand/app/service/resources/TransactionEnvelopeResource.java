package com.algorand.app.service.resources;

import com.algorand.app.service.core.TransactionEnvelope;
import com.algorand.app.service.core.TransactionGroup;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.security.GeneralSecurityException;
import java.util.*;

import org.slf4j.Logger;

@Path("/transaction-envelope")
public class TransactionEnvelopeResource implements TransactionEnvelopeResourceInterface {
    private final TransactionEnvelopeResourceInterface transactionEnvelopeResourceInterface;

    public TransactionEnvelopeResource(final TransactionEnvelopeResourceInterface ter) {
        this.transactionEnvelopeResourceInterface = ter;
    }

    private Logger log = LoggerFactory.getLogger("TransactionEnvelopeResource");

    @Override
    @GET
    @Path("{transaction-envelope-identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionEnvelope getTransactionEnvelope(@PathParam("transaction-envelope-identifier") String transactionEnvelopIdentifier) {
        return transactionEnvelopeResourceInterface.getTransactionEnvelope(transactionEnvelopIdentifier);
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TransactionEnvelope> getTransactionEnvelopeList() {
       return transactionEnvelopeResourceInterface.getTransactionEnvelopeList();
    }

    @Override
    @GET
    @Path("account/{account-address}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TransactionEnvelope> getTransactionEnvelopeListForPayer(@PathParam("account-address") String accountAddress) {
        return transactionEnvelopeResourceInterface.getTransactionEnvelopeListForPayer(accountAddress);
    }

    @Override
    @GET
    @Path("account-all/{account-address}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TransactionEnvelope> getAllTransactionEnvelopeListForPayer(@PathParam("account-address") String accountAddress) {
        return transactionEnvelopeResourceInterface.getAllTransactionEnvelopeListForPayer(accountAddress);
    }

    @Override
    @POST
    @Path("submit-transaction-group")
    @Consumes(MediaType.APPLICATION_JSON)
    public void submitTransactionGroup(TransactionGroup transactionGroup) {
        transactionEnvelopeResourceInterface.submitTransactionGroup(transactionGroup);
    }

    /**
     * Submit a transaction envelope for processing.
     * @param transactionEnvelope transaction envelope to process
     */
    @Override
    @POST
    @Path("submit-unsigned-transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    public void submitUnsignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {
         transactionEnvelopeResourceInterface.submitUnsignedTransactionEnvelope(transactionEnvelope);
    }

    /**
     * Submit a signed transaction Envelope.  Submit to blockchain and notify App and Wallet.
     * @param signedTransactionEnvelop signed transaction envelope
     */
    @Override
    @POST
    @Path("submit-signed-transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    public void submitSignedTransactionEnvelope(TransactionEnvelope signedTransactionEnvelop) throws GeneralSecurityException {
        transactionEnvelopeResourceInterface.submitSignedTransactionEnvelope(signedTransactionEnvelop);
    }

    /**
     * Generate a test transaction envelope
     * @param transactionEnvelopeIdentifier transaction envelope identifier
     * @return generated transaction envelope
     */
    @Override
    @GET
    @Path("generate/{transaction-envelope-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionEnvelope generateTransactionEnvelope(@PathParam("transaction-envelope-id") String transactionEnvelopeIdentifier) {
        return transactionEnvelopeResourceInterface.generateTransactionEnvelope( transactionEnvelopeIdentifier);
    }

    @Override
    @POST
    @Path("store-contents")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String storeContents(String contents) throws Exception {
        return transactionEnvelopeResourceInterface.storeContents( contents);
    }

    @Override
    @GET
    @Path("get-contents/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getContents(@PathParam("hash") String hash) {
        return transactionEnvelopeResourceInterface.getContents( hash);
    }

    @Override
    @GET
    @Path("read-topic/{topic}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> readFromTopic(@PathParam("topic") String topic) throws Exception {
        return transactionEnvelopeResourceInterface.readFromTopic( topic);
    }

    @Override
    @POST
    @Path("put-topic/{topic}/{message}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void writeToTopic(@PathParam("topic") String topic, @PathParam("message") String message) throws Exception {
        transactionEnvelopeResourceInterface.writeToTopic( topic , message);
    }
}
