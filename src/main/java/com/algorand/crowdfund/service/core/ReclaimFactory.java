package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.builder.transaction.ApplicationBaseTransactionBuilder;
import com.algorand.algosdk.builder.transaction.PaymentTransactionBuilder;
import com.algorand.algosdk.crypto.Address;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

class ReclaimFactory {

    private final CrowdFundServiceImpl cfsImpl;
    private final Logger log = LoggerFactory.getLogger("ReclaimFactory");
    private final String IGNITE_APPLICATION_ID = "Ignite!";
    private final CrowdFundUtility crowdFundUtility = new CrowdFundUtility();


//#app account is the escrow
//    ${gcmd2} app call --app-id ${APPID} --app-account=F4HJHVIPILZN3BISEVKXL4NSASZB4LRB25H4WCSEENSPCJ5DYW6CKUVZOA --app-arg "str:reclaim" --from $ACCOUNT  --out=unsginedtransaction1.tx
//# uncomment this line to be the last reclaimer the fee has to be accounted for
//            # note that the reclaim has to account for the tx fee hence why the amount does not match the donation
//    ${gcmd2} clerk send --to=$ACCOUNT --close-to=$ACCOUNT --from-program=./crowd_fund_escrow.teal --amount=499000 --out=unsginedtransaction2.tx
//#${gcmd2} clerk send --to=$ACCOUNT --from-program=./crowd_fund_escrow.teal --amount=499000 --out=unsginedtransaction2.tx
//
//
//    cat unsginedtransaction1.tx unsginedtransaction2.tx > combinedtransactions.tx
//    ${gcmd2} clerk group -i combinedtransactions.tx -o groupedtransactions.tx



    public ReclaimFactory(CrowdFundServiceImpl cfsImpl) {
        this.cfsImpl = cfsImpl;
    }

    public Reclaim createReclaim(AlgodClient algodClient, Fund fund, Reclaim reclaim) throws CrowdFundException {

        reclaim.setId(UUID.randomUUID().toString());

        TransactionEnvelope appCallClaimTransactionEnvelope = appCallReclaim(algodClient, fund, reclaim);
        TransactionEnvelope sendClaimTransactionEnvelope = sendReclaim(algodClient,fund,reclaim);

        TransactionGroup transactionGroup = new TransactionGroup();
        transactionGroup.setDescription("claim group");
        transactionGroup.setState("unsigned");
        transactionGroup.setApplicationId(IGNITE_APPLICATION_ID);
        transactionGroup.setName("claim");
        transactionGroup.getTransactionEnvelopeList().add(appCallClaimTransactionEnvelope);
        transactionGroup.getTransactionEnvelopeList().add(sendClaimTransactionEnvelope);
        cfsImpl.appService.submitTransactionGroupWithAlgorandTransactions(transactionGroup);

        reclaim.setReclaimDate(new Date());

        this.cfsImpl.getReclaimList().add(reclaim);

        return reclaim;
    }

    //    ${gcmd2} app call --app-id ${APPID} --app-account=F4HJHVIPILZN3BISEVKXL4NSASZB4LRB25H4WCSEENSPCJ5DYW6CKUVZOA --app-arg "str:reclaim" --from $ACCOUNT  --out=unsginedtransaction1.tx
    private TransactionEnvelope appCallReclaim(AlgodClient algodClient, Fund fund, Reclaim reclaim) throws CrowdFundException {
        try {
            //update
            List<Address> accountList = new ArrayList<>();
            accountList.add(new Address(fund.getEscrow().getAddress()));

            ApplicationBaseTransactionBuilder appCallBuilder = null;
            appCallBuilder = Transaction.ApplicationCallTransactionBuilder()
                    .applicationId(fund.getAppId())
                    .sender(fund.getEscrow().getAddress())
                    .accounts(accountList);

            String args = "str:reclaim";
            log.info("update app args: " + args);
            appCallBuilder.args(crowdFundUtility.convertArgs(args));
            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, appCallBuilder);

            Transaction updateTransaction = appCallBuilder.build();

            TransactionEnvelope transactionEnvelope = createAppCallReclaimTransactionEnvelope(updateTransaction, fund, reclaim);

            return transactionEnvelope;
        } catch (Exception e) {
            String message = "error in reclaim app call claim " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createAppCallReclaimTransactionEnvelope(Transaction transaction, Fund fund, Reclaim reclaim)
            throws CrowdFundException {
        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField("reclaim funds funding project".getBytes());
            trx.setPayer("");
            trx.setReceiver("");
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transaction.txID());
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to reclaim funds from the fund '" + fund.getName() + "'");
            agreement.setSignature("signature of agreement by the application: ".getBytes());

            TransactionEnvelope te = new TransactionEnvelope();
            te.setDate(new Date());
            te.setName("transaction-envelope-" + transactionId);
            te.setDescription("Fund reclaim envelope");
            te.setApplicationId(IGNITE_APPLICATION_ID);
            te.setTransactionPrototype(trx);
            te.setId(UUID.randomUUID().toString());
            te.setAgreement(agreement);
            te.setState("unsigned");

            te.setAlgorandTransaction(transaction);
            return te;
        } catch (Exception e) {
            String message = "error in reclaim App Call Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }

    }

    //#${gcmd2} clerk send --to=$ACCOUNT --from-program=./crowd_fund_escrow.teal --amount=499000 --out=unsginedtransaction2.tx
    private TransactionEnvelope sendReclaim(AlgodClient algodClient, Fund fund, Reclaim reclaim) throws CrowdFundException {
        try {

            LogicsigSignature lsig = new LogicsigSignature(fund.getEscrow().getTealProgram().getBytes(), null);

            PaymentTransactionBuilder reclaimTransactionBuilder = Transaction.PaymentTransactionBuilder()
                    .receiver(reclaim.getInvestorAddress())
                    .sender(lsig.toAddress())
                    .amount(reclaim.getReclaimAmount());

            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, reclaimTransactionBuilder);

            Transaction paymentTransaction = reclaimTransactionBuilder.build();

            TransactionEnvelope transactionEnvelope = createSendReclaimTransactionEnvelope(paymentTransaction, fund, reclaim);

            SignedTransaction stx = Account.signLogicsigTransaction(lsig, paymentTransaction);
            transactionEnvelope.setSignedTransaction(stx);
            transactionEnvelope.setState("signed");

            return transactionEnvelope;
        } catch (Exception e) {
            String message = "error in send reclaim investment " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createSendReclaimTransactionEnvelope(Transaction transaction, Fund fund, Reclaim reclaim)
            throws CrowdFundException {

        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField(("send reclaim from " + fund.getName() + " to " + reclaim.getInvestorAddress()).getBytes());
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
            String message = "error in send reclaim Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }
}

