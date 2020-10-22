package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.builder.transaction.ApplicationBaseTransactionBuilder;
import com.algorand.algosdk.builder.transaction.ApplicationCallTransactionBuilder;
import com.algorand.algosdk.builder.transaction.PaymentTransactionBuilder;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.app.service.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

public class InvestmentFactory {

    private final CrowdFundServiceImpl cfsImpl;
    private final Logger log = LoggerFactory.getLogger("InvestmentFactory");
    private final String IGNITE_APPLICATION_ID = "Ignite!";
    private final CrowdFundUtility crowdFundUtility = new CrowdFundUtility();


//    goal app call --app-id {APPID}  --app-arg "str:donate" --from={ACCOUNT}  --out=unsignedtransaction1.tx -d ~/node/data
//    goal clerk send --from={ACCOUNT} --to="F4HJHVIPILZN3BISEVKXL4NSASZB4LRB25H4WCSEENSPCJ5DYW6CKUVZOA" --amount=500000 --out=unsignedtransaction2.tx -d ~/node/data
//    cat unsignedtransaction1.tx unsignedtransaction2.tx > combinedtransactions.tx
//    goal clerk group -i combinedtransactions.tx -o groupedtransactions.tx
//    goal clerk sign -i groupedtransactions.tx -o signout.tx
//    goal clerk rawsend -f signout.tx


    public InvestmentFactory(CrowdFundServiceImpl cfsImpl) {
        this.cfsImpl = cfsImpl;
    }

    public Investment createInvestment(AlgodClient algodClient, Fund fund, Investment investment) throws CrowdFundException {

        investment.setCreationDate(new Date());
        investment.setId(UUID.randomUUID().toString());
        investment.setInvestmentState(InvestmentState.RECEIVED);

        TransactionEnvelope appOptInEnvelope = appOptIn(algodClient, fund, investment);
        appOptInEnvelope.setState("unsigned");

        TransactionEnvelope appCallInvestTransactionEnvelope = appCallInvest(algodClient, fund, investment);

        TransactionEnvelope sendInvestmentTransactionEnvelope = sendInvestment(algodClient, fund, investment);

        cfsImpl.appService.submitUnsignedTransactionEnvelope(appOptInEnvelope);

        TransactionGroup transactionGroup = new TransactionGroup();
        transactionGroup.setDescription("investment group");
        transactionGroup.setState("unsigned");
        transactionGroup.setApplicationId(IGNITE_APPLICATION_ID);
        transactionGroup.setName("investment");
        transactionGroup.getTransactionEnvelopeList().add(appCallInvestTransactionEnvelope);
        transactionGroup.getTransactionEnvelopeList().add(sendInvestmentTransactionEnvelope);
        cfsImpl.appService.submitTransactionGroupWithAlgorandTransactions(transactionGroup);

        investment.setInvestmentState(InvestmentState.ACCEPTED);

        this.cfsImpl.getInvestmentList().add(investment);

        return investment;
    }

    private TransactionEnvelope appOptIn(AlgodClient algodClient, Fund fund, Investment investment) throws CrowdFundException {
        try {
            ApplicationBaseTransactionBuilder optinBuilder = null;
            optinBuilder = Transaction.ApplicationOptInTransactionBuilder()
                .applicationId(fund.getAppId())
                .sender(investment.getInvestorAddress());


            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, optinBuilder);

            Transaction optinTransaction = optinBuilder.build();
            return createAppUpdateTransactionEnvelope(optinTransaction, fund, investment);
        } catch (Exception e) {
            String message = "error in update crowd fund " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createAppUpdateTransactionEnvelope(Transaction transaction, Fund fund, Investment investment)
            throws CrowdFundException {

        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setTransactionType(TransactionTypeEnum.OPTIN_APP);
            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField("crowd funding project".getBytes());
            trx.setPayer(investment.getInvestorAddress());
            trx.setReceiver("");
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transaction.txID());
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to creating this fund '" + fund.getName() + "'");
            agreement.setSignature("signature of agreement by the application: ".getBytes());

            TransactionEnvelope te = new TransactionEnvelope();
            te.setDate(new Date());
            te.setName("transaction-envelope-" + transactionId);
            te.setDescription("Sample transaction envelope");
            te.setApplicationId(IGNITE_APPLICATION_ID);
            te.setTransactionPrototype(trx);
            te.setId(UUID.randomUUID().toString());
            te.setAgreement(agreement);


            te.setAlgorandTransaction(transaction);
            return te;
        } catch (Exception e) {
            String message = "error in create App Update Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }

    }

    //  goal app call --app-id {APPID}  --app-arg "str:donate" --from={ACCOUNT}  --out=unsignedtransaction1.tx -d ~/node/data
    private TransactionEnvelope appCallInvest(AlgodClient algodClient, Fund fund, Investment investment) throws CrowdFundException {
        try {
            //update
            ApplicationCallTransactionBuilder updateBuilder = null;
            updateBuilder = Transaction.ApplicationCallTransactionBuilder()
                    .applicationId(fund.getAppId())
                    .sender(investment.getInvestorAddress());

            String args = "str:donate";
            log.info("update app args: " + args);
            updateBuilder.args(crowdFundUtility.convertArgs(args));
            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, updateBuilder);

            Transaction updateTransaction = updateBuilder.build();

            TransactionEnvelope transactionEnvelope = createAppCallTransactionEnvelope(updateTransaction, fund, investment);

            return transactionEnvelope;
        } catch (Exception e) {
            String message = "error in update crowd fund " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createAppCallTransactionEnvelope(Transaction transaction, Fund fund, Investment investment)
            throws CrowdFundException {
        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setTransactionType(TransactionTypeEnum.STANDARD);
            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField("invest in crowd funding project".getBytes());
            trx.setPayer(investment.getInvestorAddress());
            trx.setReceiver("");
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transaction.txID());
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to investing " + investment.getInvestmentAmount() + " in the fund '" + fund.getName() + "'");
            agreement.setSignature("signature of agreement by the application: ".getBytes());

            TransactionEnvelope te = new TransactionEnvelope();
            te.setDate(new Date());
            te.setName("transaction-envelope-" + transactionId);
            te.setDescription("Fund investment envelope");
            te.setApplicationId("Ignite");
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

    // goal clerk send --from={ACCOUNT} --to="F4HJHVIPILZN3BISEVKXL4NSASZB4LRB25H4WCSEENSPCJ5DYW6CKUVZOA" --amount=500000 --out=unsignedtransaction2.tx -d ~/node/data
    private TransactionEnvelope sendInvestment(AlgodClient algodClient, Fund fund, Investment investment) throws CrowdFundException {
        try {
            //update
            PaymentTransactionBuilder paymentTransactionBuilder = null;
            paymentTransactionBuilder = Transaction.PaymentTransactionBuilder()
                    .sender(investment.getInvestorAddress())
                    .receiver(fund.getEscrow().getAddress())
                    .amount(investment.getInvestmentAmount());

            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, paymentTransactionBuilder);

            Transaction paymentTransaction = paymentTransactionBuilder.build();

            TransactionEnvelope transactionEnvelope = createSendInvestmentTransactionEnvelope(paymentTransaction, fund, investment);

            return transactionEnvelope;
        } catch (Exception e) {
            String message = "error in send investment " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createSendInvestmentTransactionEnvelope(Transaction transaction, Fund fund, Investment investment)
            throws CrowdFundException {

        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(investment.getInvestmentAmount());
            trx.setTransactionType(TransactionTypeEnum.TRANSFER_ASSET);
            trx.setFee(1000);
            trx.setNoteField(("send investment to " + fund.getName()).getBytes());
            trx.setPayer(investment.getInvestorAddress());
            trx.setReceiver(fund.getEscrow().getAddress());
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transaction.txID());
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to invest " + investment.getInvestmentAmount() + "in the fund '" + fund.getName() + "'");
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
