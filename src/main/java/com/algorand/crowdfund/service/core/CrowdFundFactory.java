package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.builder.transaction.ApplicationBaseTransactionBuilder;
import com.algorand.algosdk.crypto.TEALProgram;
import com.algorand.algosdk.logic.StateSchema;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.algod.TealCompile;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.CompileResponse;
import com.algorand.app.service.core.Agreement;
import com.algorand.app.service.core.TransactionEnvelope;
import com.algorand.app.service.core.TransactionPrototype;
import com.algorand.app.service.core.TransactionTypeEnum;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;


public class CrowdFundFactory {

    private final CrowdFundServiceImpl cfsImpl;
    private final Logger log = LoggerFactory.getLogger("CrowdFundFactory");
    private final String IGNITE_APPLICATION_ID = "Ignite!";
    private final CrowdFundUtility crowdFundUtility = new CrowdFundUtility();
    private final MustacheFactory mf = new DefaultMustacheFactory();

    private final String approvalProgramFile = "crowd_fund.teal";
    private final String closeProgramFile = "crowd_fund_close.teal";
    private final String escrowFile = "crowd_fund_escrow.teal.mustache";
    private final Long globalBytes = 3L;
    private final Long globalInts = 5L;
    private final Long localBytes = 0L;
    private final Long localInts = 1L;

    public CrowdFundFactory(CrowdFundServiceImpl cfsImpl) {
        this.cfsImpl = cfsImpl;
    }

//    Create the App and then update it with the stateless teal escrow
//    APPID=$(${gcmd} app create --creator ${ACCOUNT} --approval-prog ./crowd_fund.teal --global-byteslices 3 --global-ints 5 --local-byteslices 0 --local-ints 1 --app-arg "int:"${bd} --app-arg "int:"${ed} --app-arg "int:1000000" --app-arg "addr:"${ACCOUNT} --app-arg "int:"${cd} --clear-prog ./crowd_fund_close.teal | grep Created | awk '{ print $6 }')
//    UPDATE=$(${gcmd} app update --app-id=${APPID} --from ${ACCOUNT}  --approval-prog ./crowd_fund.teal   --clear-prog ./crowd_fund_close.teal --app-arg "addr:F4HJHVIPILZN3BISEVKXL4NSASZB4LRB25H4WCSEENSPCJ5DYW6CKUVZOA" )
//
//            #optin the creator account
//    ${gcmd} app optin  --app-id ${APPID} --from $ACCOUNT
//
//    echo "App ID="$APPID
//    ${gcmd} app read --app-id $APPID --guess-format --global --from $ACCOUNT

    public Fund createCrowdFund(AlgodClient algodClient, Fund fund) throws CrowdFundException {


        appCreateFund(algodClient, fund);

        updateCrowdFund(algodClient, fund);

        optInCrowdFund(algodClient, fund);

        fund.setFundState(FundState.PUBLISHED);

        return fund;
    }

    private void createAccount() throws CrowdFundException{
        // create a new account
        try {
            com.algorand.algosdk.account.Account account = new com.algorand.algosdk.account.Account();

            log.info("created new account" + account.getAddress());
            log.info("with mnemonic" + account.getClearTextPublicKey());
        } catch (NoSuchAlgorithmException e) {
            String message = "unable to create escrow account, " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }
    // APPID=$(${gcmd} app create --creator ${ACCOUNT} --approval-prog ./crowd_fund.teal --global-byteslices 3 --global-ints 5 --local-byteslices 0 --local-ints 1 --app-arg "int:"${bd} --app-arg "int:"${ed} --app-arg "int:1000000" --app-arg "addr:"${ACCOUNT} --app-arg "int:"${cd} --clear-prog ./crowd_fund_close.teal | grep Created | awk '{ print $6 }')
    private void appCreateFund(AlgodClient algodClient, Fund fund) throws CrowdFundException {
        try {

            fund.setId(UUID.randomUUID().toString());
            TEALProgram approvalTealProgram = createTealProgram(compileTealProgram(algodClient, approvalProgramFile));
            TEALProgram closeTealProgram = createTealProgram(compileTealProgram(algodClient, closeProgramFile));
            StateSchema globalStateSchema = new StateSchema(globalInts, globalBytes);
            StateSchema localStateSchema = new StateSchema(localInts, localBytes);

            ApplicationBaseTransactionBuilder builder = Transaction.ApplicationCreateTransactionBuilder()
                    .approvalProgram(approvalTealProgram)
                    .clearStateProgram(closeTealProgram)
                    .globalStateSchema(globalStateSchema)
                    .localStateSchema(localStateSchema);

            String args = "int:" + fund.getStartDate().getTime() + ",int:" + fund.getEndDate().getTime() + ",int:" + fund.getGoalAmount() + ",addr:" + fund.getReceiverAddress() + ",int:" + fund.getCloseOutDate().getTime();
            log.info("create fund args: " + args);
            builder.args(crowdFundUtility.convertArgs(args));
            builder.sender(fund.getCreatorAddress());

            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, builder);
            Transaction transaction = builder.build();

            fund.setFundState(FundState.WAITING_FOR_APP_CREATION_TRANSACTION_COMMIT);

            TransactionEnvelope transactionEnvelope = createAppCreationTransactionEnvelope(transaction, fund);
            cfsImpl.signTransaction(transactionEnvelope);

            cfsImpl.submitSignedTransactionEnvelope(transactionEnvelope);

            fund.setAppId(transactionEnvelope.getAPPID());

        } catch (Exception e) {
            String message = "error in create App Creation Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createAppCreationTransactionEnvelope(Transaction transaction, Fund fund)
            throws CrowdFundException {

        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField("crowd funding project".getBytes());
            trx.setPayer("");
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
            te.setApplicationId("Ignite");
            te.setTransactionPrototype(trx);
            te.setId(UUID.randomUUID().toString());
            te.setAgreement(agreement);


            te.setAlgorandTransaction(transaction);
            return te;
        } catch (Exception e) {
            String message = "error in create App Creation Transaction Envelope " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }

    }

    private Escrow buildEscrow(AlgodClient algodClient, Fund fund) throws CrowdFundException, IOException {
        Escrow escrow = new Escrow();
        HashMap<String, Object> scopes = new HashMap();
        scopes.put("applicationId", fund.getAppId());

        Mustache m = mf.compile(escrowFile);
        StringWriter writer = new StringWriter();
        m.execute(writer, scopes).flush();

        String escrowFileString = writer.toString();

        CompileResponse compiledEscrow = compileTealProgram(algodClient, escrowFileString.getBytes());
        escrow.setAddress(compiledEscrow.hash);
        escrow.setTealProgram(createTealProgram(compiledEscrow));
        return escrow;
    }

    // UPDATE=$(${gcmd} app update --app-id=${APPID} --from ${ACCOUNT}  --approval-prog ./crowd_fund.teal   --clear-prog ./crowd_fund_close.teal --app-arg "addr:F4HJHVIPILZN3BISEVKXL4NSASZB4LRB25H4WCSEENSPCJ5DYW6CKUVZOA" )
    // Update CrowdFund is called after the create crowd fund transaction is complete.
    private void updateCrowdFund(AlgodClient algodClient, Fund fund) throws CrowdFundException {
        try {


            fund.setEscrow(buildEscrow(algodClient, fund));

            //update
            ApplicationBaseTransactionBuilder updateBuilder = null;
            updateBuilder = Transaction.ApplicationUpdateTransactionBuilder()
                    .approvalProgram(createTealProgram(compileTealProgram(algodClient, approvalProgramFile)))
                    .clearStateProgram(createTealProgram(compileTealProgram(algodClient, closeProgramFile)));

            updateBuilder.sender(fund.getCreatorAddress());
            updateBuilder.applicationId(fund.getAppId());
            String args = "addr:" + fund.getEscrow().getAddress();
            log.info("update app args: " + args);
            updateBuilder.args(crowdFundUtility.convertArgs(args));
            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, updateBuilder);

            Transaction updateTransaction = updateBuilder.build();

            TransactionEnvelope transactionEnvelope = createAppUpdateTransactionEnvelope(updateTransaction, fund);
            cfsImpl.signTransaction(transactionEnvelope);
            cfsImpl.submitSignedTransactionEnvelope(transactionEnvelope);

            fund.setFundState(FundState.WAITING_FOR_APP_UPDATE_TRANSACTION_COMMIT);
        } catch (Exception e) {
            String message = "error in update crowd fund " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createAppUpdateTransactionEnvelope(Transaction transaction, Fund fund)
            throws CrowdFundException {

        try {
            String transactionId = transaction.txID();

            TransactionPrototype trx = new TransactionPrototype();

            trx.setAmount(0);
            trx.setFee(1000);
            trx.setNoteField("crowd funding project".getBytes());
            trx.setPayer("");
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

    // OptIn CrowdFund
    // ${gcmd} app optin  --app-id ${APPID} --from $ACCOUNT
    private void optInCrowdFund(AlgodClient algodClient, Fund fund) throws CrowdFundException {
        try {
            ApplicationBaseTransactionBuilder optinBuilder = null;
            optinBuilder = Transaction.ApplicationOptInTransactionBuilder()
                    .applicationId(fund.getAppId())
                    .sender(fund.getCreatorAddress());


            crowdFundUtility.addTransactionParamsToAlgorandTransaction(algodClient, optinBuilder);

            Transaction optinTransaction = optinBuilder.build();

            TransactionEnvelope transactionEnvelope = createAppUpdateTransactionEnvelope(optinTransaction, fund);
            cfsImpl.signTransaction(transactionEnvelope);
            cfsImpl.submitSignedTransactionEnvelope(transactionEnvelope);

            fund.setFundState(FundState.PUBLISHED);
        } catch (Exception e) {
            String message = "error in opt In Crowd Fund " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    private TransactionEnvelope createTransactionOptIn(Transaction transaction, Fund fund) {
        log.info("createTransactionOptInForAsset for fund: " + fund.getName());
        int fee = 1000;

        TransactionPrototype transactionPrototype = new TransactionPrototype();
        transactionPrototype.setNoteField(("opt in for App Id " + fund.getAppId()).getBytes());
        transactionPrototype.setApplicationTransactionId(String.valueOf(System.nanoTime()));
        transactionPrototype.setAmount(0);
        transactionPrototype.setFee(fee);
        transactionPrototype.setReceiver(fund.getReceiverAddress());
        transactionPrototype.setTransactionType(TransactionTypeEnum.OPTIN_ASSET);

        Agreement agreement = new Agreement();
        agreement.setContents("IGNITE_APPLICATION_ID\n" +
                "Crowd Funding for project " + fund.getName() + "\n-----------------------------------------------\n\n"
                + "You agree to opt in"
                + ", with fee of " + fee + " uAlgos"
                + "\n\n-----------------------------------------------\n Transaction Processing provided by Algorand Payment Services");
        agreement.setType("Ignite OptIn");
        agreement.setIdentifier(String.valueOf(System.nanoTime()));
        agreement.setTime(DateTime.now().toDate());

        TransactionEnvelope te = new TransactionEnvelope();
        te.setTransactionPrototype(transactionPrototype);
        te.setAgreement(agreement);
        te.setName("Ignite OptIn for application " + fund.getName());
        te.setDescription("Ignite OptIn for application " + fund.getName());
        te.setDate(DateTime.now().toDate());
        te.setApplicationId(IGNITE_APPLICATION_ID);
        te.setId(String.valueOf(System.nanoTime()));
        te.setState("unsigned");

        te.setAlgorandTransaction(transaction);


        log.info("returning from createTransactionOptInForAsset: " + te);
        return te;
    }

    public CompileResponse compileTealProgram(AlgodClient algodClient, byte[] file) throws CrowdFundException {
        try {
            TealCompile tealCompiler = new TealCompile(algodClient);

            tealCompiler = tealCompiler.source(file);
            Response<CompileResponse> execute = tealCompiler.execute();
            return execute.body();
        } catch (Exception e) {
            String message = "error in load TEAL Program From File " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    public CompileResponse compileTealProgram(AlgodClient algodClient, String file) throws CrowdFundException {
        try {
            byte[] resourceBytes = loadResource(file);
            return compileTealProgram(algodClient, resourceBytes);
        } catch (Exception e) {
            String message = "error in load TEAL Program From File " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    public TEALProgram createTealProgram(CompileResponse compileResponse) throws CrowdFundException {
        byte[] compiledBytes = Encoder.decodeFromBase64(compileResponse.result);
        return new TEALProgram(compiledBytes);
    }

    public byte[] loadResource(String file) throws CrowdFundException {
        try {
            return readResource(file);
        } catch (Exception e) {
            String message = "error in load resource " + e.getMessage();
            log.error(message);
            throw new CrowdFundException(message);
        }
    }

    public byte[] readResource(String file) throws IOException {
        InputStream fis = getClass().getClassLoader().getResourceAsStream(file);
        byte[] data = new byte[fis.available()];
        fis.read(data);
        return data;
    }

}
