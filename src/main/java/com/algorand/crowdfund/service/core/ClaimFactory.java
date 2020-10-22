package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.builder.transaction.ApplicationBaseTransactionBuilder;
import com.algorand.algosdk.builder.transaction.PaymentTransactionBuilder;
import com.algorand.algosdk.crypto.LogicsigSignature;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.app.service.core.Agreement;
import com.algorand.app.service.core.TransactionEnvelope;
import com.algorand.app.service.core.TransactionGroup;
import com.algorand.app.service.core.TransactionPrototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.UUID;

class ClaimFactory {

    private final CrowdFundServiceImpl cfsImpl;
    private final Logger log = LoggerFactory.getLogger("ClaimFactory");
    private final String IGNITE_APPLICATION_ID = "Ignite!";
    private final CrowdFundUtility crowdFundUtility = new CrowdFundUtility();


//    gcmd="goal -d ../test/Primary"
//    gcmd2="goal -d ../test/Node"
//    ACCOUNT=$(${gcmd} account list|awk '{ print $3 }'|head -n 1)
//
//    ${gcmd} app call --app-id ${APPID} --app-arg "str:claim"  --from $ACCOUNT  --out=unsginedtransaction1.tx
//    ${gcmd2} clerk send --to=$ACCOUNT --close-to=$ACCOUNT --from-program=./crowd_fund_escrow.teal --amount=0 --out=unsginedtransaction2.tx
//
//
//    cat unsginedtransaction1.tx unsginedtransaction2.tx > combinedtransactions.tx
//    ${gcmd2} clerk group -i combinedtransactions.tx -o groupedtransactions.tx
//    ${gcmd2} clerk split -i groupedtransactions.tx -o split.tx
//
//    ${gcmd} clerk sign -i split-0.tx -o signout-0.tx
//    cat signout-0.tx split-1.tx > signout.tx
//    ${gcmd} clerk rawsend -f signout.tx
//    ${gcmd} app read --app-id ${APPID} --guess-format --global --from $ACCOUNT
//    ${gcmd} app read --app-id ${APPID} --guess-format --local --from $ACCOUNT
//    rm *.tx


    public ClaimFactory(CrowdFundServiceImpl cfsImpl) {
        this.cfsImpl = cfsImpl;
    }

    public Claim createClaim(AlgodClient algodClient, Fund fund, Claim claim) throws CrowdFundException {

        claim.setId(UUID.randomUUID().toString());
        claim.setClaimState(ClaimState.CREATED);

        TransactionEnvelope appCallClaimTransactionEnvelope = appCallClaim(algodClient, fund, claim);
        TransactionEnvelope sendClaimTransactionEnvelope = this.sendClaim(algodClient,fund,claim);

        TransactionGroup transactionGroup = new TransactionGroup();
        transactionGroup.setDescription("claim group");
        transactionGroup.setState("unsigned");
        transactionGroup.setApplicationId(IGNITE_APPLICATION_ID);
        transactionGroup.setName("claim");
        transactionGroup.getTransactionEnvelopeList().add(appCallClaimTransactionEnvelope);
        transactionGroup.getTransactionEnvelopeList().add(sendClaimTransactionEnvelope);
        cfsImpl.appService.submitTransactionGroupWithAlgorandTransactions(transactionGroup);

        try {
            cfsImpl.appService.submitSignedTransactionEnvelope(sendClaimTransactionEnvelope);
        } catch (GeneralSecurityException e) {
            String message = "error submitting signed transaction envelope";
            log.error(message);
            throw new CrowdFundException(message);
        }

        claim.setReceiveDate(new Date());
        claim.setClaimState(ClaimState.COMPLETE);

        this.cfsImpl.getClaimList().add(claim);

        return claim;
    }

    //  ${gcmd} app call --app-id ${APPID} --app-arg "str:claim"  --from $ACCOUNT  --out=unsginedtransaction1.tx
    private TransactionEnvelope appCallClaim(AlgodClient algodClient, Fund fund, Claim claim) throws CrowdFundException {
        try {
            //update
            ApplicationBaseTransactionBuilder appCallBuilder = null;
            appCallBuilder = Transaction.ApplicationCallTransactionBuilder()
                    .applicationId(fund.getAppId())
                    .sender(fund.getCreatorAddress());

            appCallBuilder.applicationId(fund.getAppId());

            String args = "str:claim";
            log.info("update app args: " + args);
            appCallBuilder.args(crowdFundUtility.convertArgs(args));
            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, appCallBuilder);

            Transaction updateTransaction = appCallBuilder.build();

            TransactionEnvelope transactionEnvelope = createAppCallClaimTransactionEnvelope(updateTransaction, fund, claim);

            cfsImpl.signTransaction(transactionEnvelope);
            transactionEnvelope.setState("signed");

            return transactionEnvelope;
        } catch (Exception e) {
            String message = "error in create app call claim " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createAppCallClaimTransactionEnvelope(Transaction transaction, Fund fund, Claim claim)
            throws CrowdFundException {
        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField("claim funds funding project".getBytes());
            trx.setPayer("");
            trx.setReceiver("");
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transaction.txID());
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to claim funds from the fund '" + fund.getName() + "'");
            agreement.setSignature("signature of agreement by the application: ".getBytes());

            TransactionEnvelope te = new TransactionEnvelope();
            te.setDate(new Date());
            te.setName("transaction-envelope-" + transactionId);
            te.setDescription("Fund claim envelope");
            te.setApplicationId(IGNITE_APPLICATION_ID);
            te.setTransactionPrototype(trx);
            te.setId(UUID.randomUUID().toString());
            te.setAgreement(agreement);
            te.setState("unsigned");

            te.setAlgorandTransaction(transaction);
            return te;
        } catch (Exception e) {
            String message = "error in create App Call Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }

    }

    //    ${gcmd2} clerk send --to=$ACCOUNT --close-to=$ACCOUNT --from-program=./crowd_fund_escrow.teal --amount=0 --out=unsginedtransaction2.tx
    private TransactionEnvelope sendClaim(AlgodClient algodClient, Fund fund, Claim claim) throws CrowdFundException {
        try {

            LogicsigSignature lsig = new LogicsigSignature(fund.getEscrow().getTealProgram().getBytes(), null);

            PaymentTransactionBuilder claimTransactionBuilder = Transaction.PaymentTransactionBuilder()
                    .receiver(fund.getReceiverAddress())
                    .closeRemainderTo(fund.getReceiverAddress())
                    .sender(lsig.toAddress())
                    .amount(0);

            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, claimTransactionBuilder);

            Transaction paymentTransaction = claimTransactionBuilder.build();

            TransactionEnvelope transactionEnvelope = createSendClaimTransactionEnvelope(paymentTransaction, fund, claim);

            SignedTransaction stx = Account.signLogicsigTransaction(lsig, paymentTransaction);
            transactionEnvelope.setSignedTransaction(stx);
            transactionEnvelope.setState("signed");

            return transactionEnvelope;
        } catch (Exception e) {
            String message = "error in send investment " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createSendClaimTransactionEnvelope(Transaction transaction, Fund fund, Claim claim)
            throws CrowdFundException {

        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField(("send claim from " + fund.getName() + " to " + fund.getReceiverAddress()).getBytes());
            trx.setPayer(fund.getReceiverAddress());
            trx.setReceiver(fund.getEscrow().getAddress());
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transaction.txID());
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to claim the fund '" + fund.getName() + "'");
            agreement.setSignature("signature of agreement by the application: ".getBytes());

            TransactionEnvelope te = new TransactionEnvelope();
            te.setDate(new Date());
            te.setName("transaction-envelope-" + transactionId);
            te.setDescription("Investment transaction envelope");
            te.setApplicationId(IGNITE_APPLICATION_ID);
            te.setTransactionPrototype(trx);
            te.setId(UUID.randomUUID().toString());
            te.setAgreement(agreement);
            te.setState("unsigned");

            te.setAlgorandTransaction(transaction);
            return te;
        } catch (Exception e) {
            String message = "error in send investment Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }
}

