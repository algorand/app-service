package com.algorand.app.service.resources;

import com.algorand.app.service.core.TransactionEnvelope;
import com.algorand.app.service.core.TransactionGroup;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public interface TransactionEnvelopeResourceInterface {

    TransactionEnvelope getTransactionEnvelope( String transactionEnvelopIdentifier);

    List<TransactionEnvelope> getTransactionEnvelopeList();

    List<TransactionEnvelope> getTransactionEnvelopeListForPayer(String accountAddress);

    List<TransactionEnvelope> getAllTransactionEnvelopeListForPayer(String accountAddress);

    void submitTransactionGroup(TransactionGroup transactionGroup);

    void submitUnsignedTransactionEnvelope(TransactionEnvelope transactionEnvelope);

    void submitSignedTransactionEnvelope(TransactionEnvelope signedTransactionEnvelop) throws GeneralSecurityException;

    TransactionEnvelope generateTransactionEnvelope( String transactionEnvelopeIdentifier);

    String storeContents(String contents) throws Exception;
    String getContents(String hash);

    List<Map> readFromTopic(String topic) throws Exception;
    void writeToTopic(String topic, String message) throws Exception;
}
