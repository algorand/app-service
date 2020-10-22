package com.algorand.app.service.core;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.algod.client.AlgodClient;
import com.algorand.algosdk.algod.client.ApiException;
import com.algorand.algosdk.algod.client.api.AlgodApi;
import com.algorand.algosdk.algod.client.auth.ApiKeyAuth;
import com.algorand.algosdk.algod.client.model.*;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.crypto.Digest;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.transaction.TxGroup;
import com.algorand.algosdk.util.Encoder;

import com.algorand.algosdk.v2.client.algod.PendingTransactionInformation;
import com.algorand.algosdk.v2.client.algod.RawTransaction;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.app.service.resources.AlgodResourceInterface;
import com.algorand.app.service.resources.TransactionEnvelopeResourceInterface;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;


public class AppServiceImpl implements TransactionEnvelopeResourceInterface, AlgodResourceInterface {

    private final String algodNetworkHost;
    private final int algodNetworkPort;
    private final String ipfsNetworkHost;
    private final int ipfsNetworkPort;
    private final String apiToken;
    private final String srcAccount;
    private final String destAddr;

    private Logger log = LoggerFactory.getLogger("AppServiceImpl");
    private Map<String, TransactionEnvelope> transactionEnvelopeMap = new HashMap<>();
    private List<TransactionGroup> transactionGroupList = new ArrayList<>();
    private List<UserAccountRegistration> userAccountRegistrationList = new ArrayList<>();

    private final IPFS ipfs;

    public AppServiceImpl(final String algodNetworkHost, final int algodNetworkPort, final String ipfsNetworkHost, final int ipfsNetworkPort, final String apiToken, final String srcAccount, final String destAddr) {
        this.algodNetworkHost = algodNetworkHost;
        this.algodNetworkPort = algodNetworkPort;
        this.ipfsNetworkHost = ipfsNetworkHost;
        this.ipfsNetworkPort = ipfsNetworkPort;
        this.apiToken = apiToken;
        this.srcAccount = srcAccount;
        this.destAddr = destAddr;

        String ipfsAddress = "http://" + this.ipfsNetworkHost + ":" + this.ipfsNetworkPort;
        log.info("connecting to ipfs node at " + ipfsAddress);
        ipfs = new IPFS(this.ipfsNetworkHost, this.ipfsNetworkPort);
    }

    public void processSignedTransaction(TransactionEnvelope transactionEnvelope) {

    }

    private AlgodApi getAlgodApiInstance() {

        String apiAddress = "http://" + algodNetworkHost + ":" + algodNetworkPort;
        log.info("connecting to algod node at " + apiAddress);
        //Create an instance of the algod API client
        AlgodClient client = (AlgodClient) new AlgodClient().setBasePath(apiAddress);
        ApiKeyAuth api_key = (ApiKeyAuth) client.getAuthentication("api_key");
        log.info("api key: " + api_key);
        api_key.setApiKey(apiToken);
        return new AlgodApi(client);
    }

    private com.algorand.algosdk.v2.client.common.AlgodClient getAlgodClientV2() {

        String apiAddress = "http://" + algodNetworkHost + ":" + algodNetworkPort;
        log.info("connecting to algod node at " + apiAddress);
        //Create an instance of the algod API client
        com.algorand.algosdk.v2.client.common.AlgodClient client = (com.algorand.algosdk.v2.client.common.AlgodClient) new com.algorand.algosdk.v2.client.common.AlgodClient(algodNetworkHost,algodNetworkPort,apiToken );
        return client;
    }

    public NodeStatus getNodeStatus() {
        NodeStatus status = null;
        try {
            AlgodApi algodApiInstance = getAlgodApiInstance();
            status = algodApiInstance.getStatus();
        } catch (ApiException e) {
            log.error("Exception when calling algod#getStatus or getBlock");
            log.error(e.getMessage());
        }
        return status;
    }

    /**
     * Get Block
     * @return
     */
    public Block getBlock() throws Exception {
        return getBlock(-1);
    }

    public Block getBlock(long blockNumber) throws Exception {
        Block block = null;
        try {
            BigInteger blockNumberBI;

            AlgodApi algodApiInstance = getAlgodApiInstance();
            if (blockNumber == -1) {
                // Get the latest Block
                NodeStatus status = algodApiInstance.getStatus();
                System.out.println("Algorand network status: " + status);
                blockNumberBI = status.getLastRound();
            } else {
                blockNumberBI = BigInteger.valueOf(blockNumber);
            }
            log.info("fetching block number: " + blockNumberBI.toString());

            //Get block for the latest round
            block = algodApiInstance.getBlock(blockNumberBI);
            log.info(block.toString());
        } catch (ApiException e) {
            log.error("Exception when calling algod#getStatus or getBlock");
            log.error(e.getMessage());
        }
        return block;
    }

    /**
     * Generate a Transaction JSON file.
     *
     * @return
     */
    public boolean generateTransactionJSON() {

        try {

            String outputFile = "/tmp/transaction.json";
            File transactionFile = new File(outputFile);

            BufferedWriter transactionFileWriter = new BufferedWriter(new FileWriter(transactionFile));

            log.info("generating transaction file: " + outputFile);

            ObjectMapper objectMapper = new ObjectMapper();

            AlgodApi algodApiInstance = getAlgodApiInstance();

            boolean isMore = true;
            NodeStatus status = algodApiInstance.getStatus();
            BigInteger lastBlockNumberBI = status.getLastRound();
            BigInteger currentBlockNumber = lastBlockNumberBI.subtract(BigInteger.valueOf(1000));
            transactionFileWriter.write("[");
            boolean isFirst = true;
            int count = 0;
            boolean isFirstTransaction = true;
            while (isMore) {

                lastBlockNumberBI = status.getLastRound();
                log.info("processing block: " + currentBlockNumber);

                if (currentBlockNumber.compareTo(lastBlockNumberBI) >= 0) {
                    log.info("sleeping 5 seconds");
                    sleep(5000);
                    isMore = false;
                } else {
                    Block block = algodApiInstance.getBlock(currentBlockNumber);
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        transactionFileWriter.write(",\n");
                    }
                    List<String> transactionStringList = block.getTxns().getTransactions().stream().map((transaction) -> print(objectMapper, transaction)).collect(Collectors.toList());
                    for (String transactionString : transactionStringList) {
                        if (isFirstTransaction) {
                            isFirstTransaction = false;
                        } else {
                            transactionFileWriter.write(",\n");
                        }
                        transactionFileWriter.write(transactionString);
                    }
                    transactionFileWriter.flush();
                    currentBlockNumber = currentBlockNumber.add(BigInteger.ONE);
                }
            }
            transactionFileWriter.write("]");
            transactionFileWriter.close();
        } catch (Exception e) {
            log.error("Error retrieving transactions: " + e.getMessage());
        }
        return true;
    }


    /**
     * Generate a Transaction JSON file.
     *
     * @return
     */
    public boolean generateBlockJSON() {

        try {

            String outputFile = "/tmp/block.json";
            File transactionFile = new File(outputFile);

            BufferedWriter blockFileWriter = new BufferedWriter(new FileWriter(transactionFile));

            log.info("generating transaction file: " + outputFile);

            ObjectMapper objectMapper = new ObjectMapper();

            AlgodApi algodApiInstance = getAlgodApiInstance();

            boolean isMore = true;
            NodeStatus status = algodApiInstance.getStatus();
            BigInteger lastBlockNumberBI = status.getLastRound();
            BigInteger currentBlockNumber = lastBlockNumberBI.subtract(BigInteger.valueOf(1000));
            blockFileWriter.write("[");
            boolean isFirst = true;
            int count = 0;
            boolean isFirstTransaction = true;
            while (isMore) {

                lastBlockNumberBI = status.getLastRound();
                log.info("processing block: " + currentBlockNumber);

                if (currentBlockNumber.compareTo(lastBlockNumberBI) >= 0) {
                    isMore = false;
                } else {
                    Block block = algodApiInstance.getBlock(currentBlockNumber);
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        blockFileWriter.write(",\n");
                    }
                    String blockString = objectMapper.writeValueAsString(block);

                    blockFileWriter.write(blockString);

                    blockFileWriter.flush();
                    currentBlockNumber = currentBlockNumber.add(BigInteger.ONE);
                }
            }
            blockFileWriter.write("]");
            blockFileWriter.close();
        } catch (Exception e) {
            log.error("Error retrieving blocks: " + e.getMessage());
        }
        return true;
    }

    private String print(ObjectMapper objectMapper, com.algorand.algosdk.algod.client.model.Transaction transaction) {
        try {
            return objectMapper.writeValueAsString(transaction);
        } catch (JsonProcessingException e) {
            log.error("Error serializing transactions: " + e.getMessage());
        }
        return null;
    }

    @Override
    public com.algorand.algosdk.algod.client.model.Account getAccount(String accountAddress) throws Exception {
        return getAlgodApiInstance().accountInformation(accountAddress);
    }

    /**
     * Get transactions for an account
     */
    public List<com.algorand.algosdk.algod.client.model.Transaction> GetAccountTransactions(String accountAddress) throws Exception {

        AlgodApi algodApiInstance = getAlgodApiInstance();
        List<com.algorand.algosdk.algod.client.model.Transaction> trxList = new ArrayList<>();

        // First, get network status
        try {
            NodeStatus status = algodApiInstance.getStatus();
            BigInteger lastRound = status.getLastRound();
            BigInteger maxtx = BigInteger.valueOf(30);
            BigInteger firstRound = lastRound.subtract(BigInteger.valueOf(1000)); // 1000

            //Get the transactions for the address in the last 1k rounds
            //Note that this call requires that the node is an archival node as we are going back 1k rounds
            org.threeten.bp.LocalDate today = org.threeten.bp.LocalDate.now();
            org.threeten.bp.LocalDate yesterday = today.minusDays(1);

            TransactionList tList = algodApiInstance.transactions(accountAddress, firstRound, lastRound, yesterday, today, maxtx);
            for (com.algorand.algosdk.algod.client.model.Transaction tx : tList.getTransactions()) {
                System.out.println(tx.toString());
                trxList.add(tx);
            }
            System.out.println("Finished");
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return trxList;
    }

    @Override
    public TransactionEnvelope getTransactionEnvelope(String transactionEnvelopIdentifier) {
        log.info("trasactionEnvelopeIdentifier " + transactionEnvelopIdentifier);
        return transactionEnvelopeMap.get(transactionEnvelopIdentifier);
    }

    @Override
    public List<TransactionEnvelope> getTransactionEnvelopeList() {
        return new ArrayList<>(transactionEnvelopeMap.values());
    }

    @Override
    public List<TransactionEnvelope> getTransactionEnvelopeListForPayer(String accountAddress) {

        log.info("getTransactionEnvelopeListForAccount " + transactionEnvelopeMap);
        return transactionEnvelopeMap.values().stream().filter(te -> te.getTransactionPrototype().getPayer().equals(accountAddress) && te.getState().equals("unsigned")).collect(Collectors.toList());
    }

    @Override
    public List<TransactionEnvelope> getAllTransactionEnvelopeListForPayer(String accountAddress) {
        log.info("getTransactionEnvelopeListForAccount " + transactionEnvelopeMap);
        return transactionEnvelopeMap.values().stream().filter(te -> te.getTransactionPrototype().getPayer().equals(accountAddress)).collect(Collectors.toList());
    }

    @Override
    public void submitTransactionGroup(TransactionGroup transactionGroup) {
        transactionGroupList.add(transactionGroup);
        processTransactionGroup(transactionGroup, true);
    }

    public void submitTransactionGroupWithAlgorandTransactions(TransactionGroup transactionGroup) {
        transactionGroupList.add(transactionGroup);
        processTransactionGroup(transactionGroup, false);
    }

    private void processTransactionGroup(TransactionGroup transactionGroup, boolean createAlgorandTransactions ) {
        // transaction Group processing

         // create algorand transactions
        for (TransactionEnvelope transactionEnvelope : transactionGroup.getTransactionEnvelopeList()) {
            if (createAlgorandTransactions) {
                submitUnsignedTransactionEnvelope(transactionEnvelope);
            } else {
                log.info("adding transactionEnvelope to transactionEnvelopeMap: " + transactionEnvelope);
                transactionEnvelopeMap.put(transactionEnvelope.getId(), transactionEnvelope);
            }
        }

        // assign transaction group
        List<Transaction> transactionList = new ArrayList<>();
        for (TransactionEnvelope transactionEnvelope : transactionGroup.getTransactionEnvelopeList()) {
            Transaction transaction = transactionEnvelope.getAlgorandTransaction();
            transactionList.add(transaction);
        }
        Transaction[] transactionArray = Iterables.toArray(transactionList, Transaction.class);
        try {
            Digest transactionGroupDigest = TxGroup.computeGroupID(transactionArray);
            String digestString = new String(transactionGroupDigest.getBytes());
            log.info("group digest '" + digestString + "'");
            transactionGroup.setGroupId(digestString);
            for (TransactionEnvelope transactionEnvelope : transactionGroup.getTransactionEnvelopeList()) {
                transactionEnvelope.getAlgorandTransaction().assignGroupID(transactionGroupDigest);
                transactionEnvelope.setGroupdId(digestString);
            }
            log.info("successfully created transaction group digest '" + transactionGroupDigest + "' for group " + transactionGroup);
        } catch (IOException e) {
            log.error("Error constructing transaction group", e);
        }
    }

    @Override
    public void submitUnsignedTransactionEnvelope(TransactionEnvelope transactionEnvelope) {

        log.info("submitUnsignedTransactionEnvelope: " + transactionEnvelope);

        try {

            switch (transactionEnvelope.getTransactionPrototype().getTransactionType()) {
                case STANDARD:
                    transactionEnvelope = createTransactionForEnvelope(transactionEnvelope);
                    break;
                case CREATE_ASSET:
                    transactionEnvelope = createAssetCreateTransactionForEnvelope(transactionEnvelope);
                    break;
                case TRANSFER_ASSET:
                    transactionEnvelope = createAssetTransferTransactionForEnvelope(transactionEnvelope);
                    break;
                case OPTIN_ASSET:
                    transactionEnvelope = createAssetOptinTransactionForEnvelope(transactionEnvelope);
                    break;
                case OPTIN_APP:
                    break;
                default:
                    log.error("unhandled transaction type: " + transactionEnvelope.getTransactionPrototype().getTransactionType() + " for transactionEnvelope: " + transactionEnvelope);
            }

            saveContentsToIPFS( transactionEnvelope );

            sendMessageToSignerTopic(transactionEnvelope);

            log.info("transaction agreement signature: " + new String(transactionEnvelope.getAgreement().getSignature()  ));
            log.info("algorand transaction note: " + new String (transactionEnvelope.getAlgorandTransaction().note) );

            log.info("adding transactionEnvelope to transactionEnvelopeMap: " + transactionEnvelope);

            transactionEnvelopeMap.put(transactionEnvelope.getId(), transactionEnvelope);

        } catch (ApiException e) {
            log.error("Exception when creating transaction: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            log.error("Exception when creating transaction: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception when creating transaction: " + e.getMessage());
        }
    }

    private void sendMessageToSignerTopic(TransactionEnvelope transactionEnvelope) {

    }

    private void saveContentsToIPFS(TransactionEnvelope transactionEnvelope ) throws Exception {

        log.info("saving transaction contents to ipfs");
        Agreement agreement = transactionEnvelope.getAgreement();
        String contents = agreement.getContents();
        String contentHash = this.storeContents(contents);
        String ipfsURL = "/ipfs/" + contentHash;
        agreement.setSignature(ipfsURL.getBytes());

        Transaction transaction = transactionEnvelope.getAlgorandTransaction();
        transaction.note = ipfsURL.getBytes();
        log.info("agreement url is: " + ipfsURL);

        String topic ="algo/appservice/account/" + transactionEnvelope.getTransactionPrototype().getPayer();
        ObjectMapper objectMapper = new ObjectMapper();
        String transactionEnvelopeAsString = objectMapper.writeValueAsString(transactionEnvelope);
//        this.writeToTopic(topic, message);
        String transactionEnvelopeHash = this.storeContents(transactionEnvelopeAsString);
        log.info("transactionEnvelopeHash url is: /ipfs/" + transactionEnvelopeHash);

    }

    private TransactionEnvelope createAssetOptinTransactionForEnvelope(TransactionEnvelope transactionEnvelope) {
        ChangingBlockParms cp = null;
        AlgodApi algodApiInstance = getAlgodApiInstance();
        try {
            cp = getChangingParms(algodApiInstance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Transaction tx = null;
        try {
            tx = Transaction.createAssetAcceptTransaction(new Address(transactionEnvelope.getTransactionPrototype().getPayer()),
                    BigInteger.valueOf(1000), cp.firstRound,
                    cp.lastRound, null, cp.genID, cp.genHash, new BigInteger(transactionEnvelope.getTransactionPrototype().getAssetId()));
            // Update the fee based on the network suggested fee
            Account.setFeeByFeePerByte(tx, cp.fee);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        transactionEnvelope.setAlgorandTransaction(tx);
        return transactionEnvelope;

    }

    private TransactionEnvelope createAssetTransferTransactionForEnvelope(TransactionEnvelope transactionEnvelope) throws NoSuchAlgorithmException, ApiException {

        // Next we set asset xfer specific parameters
        // We set the assetCloseTo to null so we do not close the asset out
        Address assetCloseTo = new Address();

        AssetPrototype assetPrototype = transactionEnvelope.getTransactionPrototype().getAssetPrototype();

        TransactionPrototype transactionPrototype = transactionEnvelope.getTransactionPrototype();


        log.info("from Address: " + transactionPrototype.getPayer());
        log.info("to address: " + transactionPrototype.getReceiver());
        Address fromAddress = new Address(transactionPrototype.getPayer());
        Address toAddress = new Address(transactionPrototype.getReceiver());

        AlgodApi algodApiInstance = getAlgodApiInstance();
        TransactionParams params = algodApiInstance.transactionParams();
        BigInteger suggestedFeePerByte = params.getFee();
        BigInteger firstRound = params.getLastRound().subtract(BigInteger.ONE);
        log.info("Suggested Fee: " + suggestedFeePerByte);
        String genId = params.getGenesisID();
        Digest genesisHash = new Digest(params.getGenesishashb64());

        BigInteger lastRound = firstRound.add(BigInteger.valueOf(1000));

        BigInteger fee = BigInteger.valueOf(transactionPrototype.getFee());
        String noteField = (new String(transactionPrototype.getNoteField())) + " hash:<" + transactionPrototype.hashCode() + ">";
        transactionPrototype.setNoteField(noteField.getBytes());

        BigInteger assetId = new BigInteger(transactionEnvelope.getAlgorandAssetId());
        log.info("create transfer assetId: " + assetId);

        Transaction transaction = Transaction.createAssetTransferTransaction(fromAddress,
                toAddress, assetCloseTo, BigInteger.valueOf(transactionPrototype.getAmount()), BigInteger.valueOf(transactionPrototype.getFee()),
                firstRound, lastRound, null, genId, genesisHash, assetId);

        transactionEnvelope.setAlgorandTransaction(transaction);

        return transactionEnvelope;

    }

    private TransactionEnvelope createAssetCreateTransactionForEnvelope(TransactionEnvelope transactionEnvelope) throws NoSuchAlgorithmException, ApiException {

        log.info("createAssetCreateTransactionForEnvelope: " + transactionEnvelope);
        AssetPrototype assetPrototype = transactionEnvelope.getTransactionPrototype().getAssetPrototype();

        BigInteger assetTotal = assetPrototype.getAssetTotal();
        // Whether user accounts will need to be unfrozen before transacting
        boolean defaultFrozen = assetPrototype.isDefaultFrozen();
        // Used to display asset units to user
        String unitName = assetPrototype.getUnitName(); // = "LATINUM";
        // Friendly name of the asset
        String assetName = assetPrototype.getAssetName(); // = "latinum";
        // Optional string pointing to a URL relating to the asset
        String url = assetPrototype.getUrl(); // = "http://this.test.com";
        // Optional hash commitment of some sort relating to the asset. 32 character length.
        String assetMetadataHash = "16efaa3924a6fd9d3a4824799a4ac65d";
        // The following parameters are the only ones
        // that can be changed, and they have to be changed
        // by the current manager
        // Specified address can change reserve, freeze, clawback, and manager
        Address creatorAccount = new Address(assetPrototype.getCreatorAccount());
        Address managerAccount = new Address(assetPrototype.getManagerAccount());
        // Specified address is considered the asset reserve
        Address reserveAccount = new Address(assetPrototype.getReserveAccount());
        // Specified address can freeze or unfreeze user asset holdings
        Address freezeAccount = assetPrototype.getFreezeAccount() == null ? null : new Address(assetPrototype.getFreezeAccount());
        // Specified address can revoke user asset holdings and send
        // them to other addresses
        Address clawbackAccount = assetPrototype.getClawbackAccount() == null ? null : new Address(assetPrototype.getClawbackAccount());
        AlgodApi algodApiInstance = getAlgodApiInstance();
        // Get suggested parameters from the node
        TransactionParams params = algodApiInstance.transactionParams();
        BigInteger suggestedFeePerByte = params.getFee();
        BigInteger firstRound = params.getLastRound().subtract(BigInteger.ONE);
        log.info("Suggested Fee: " + suggestedFeePerByte);
        String genId = params.getGenesisID();
        Digest genesisHash = new Digest(params.getGenesishashb64());

        BigInteger lastRound = firstRound.add(BigInteger.valueOf(1000));
        TransactionPrototype transactionPrototype = transactionEnvelope.getTransactionPrototype();
        BigInteger fee = BigInteger.valueOf(transactionPrototype.getFee());
        String noteField = (new String(transactionPrototype.getNoteField())) + " hash:<" + transactionPrototype.hashCode() + ">";
        transactionPrototype.setNoteField(noteField.getBytes());

        BigInteger amount = BigInteger.valueOf(transactionPrototype.getAmount());

        byte[] noteFieldBytes = noteField.getBytes();
        int assetDecimals = 3;

        Transaction createAssetTransaction = Transaction.createAssetCreateTransaction(creatorAccount,
                fee, firstRound, lastRound, noteFieldBytes, genId,
                genesisHash, assetTotal, assetDecimals, defaultFrozen, unitName, assetName, url,
                assetMetadataHash.getBytes(), managerAccount, reserveAccount, freezeAccount, clawbackAccount);

        Account.setFeeByFeePerByte(createAssetTransaction, suggestedFeePerByte);

        transactionEnvelope.setAlgorandTransaction(createAssetTransaction);

        transactionEnvelope.setSignedTransaction(null);


        return transactionEnvelope;
    }


    private TransactionEnvelope createTransactionForEnvelope(TransactionEnvelope transactionEnvelope) throws NoSuchAlgorithmException, ApiException {

        log.info("createTransactionForEnvelope: " + transactionEnvelope);

        AlgodApi algodApiInstance = getAlgodApiInstance();
        // Get suggested parameters from the node
        TransactionParams params = algodApiInstance.transactionParams();
        BigInteger suggestedFeePerByte = params.getFee();
        BigInteger firstRound = params.getLastRound().subtract(BigInteger.ONE);
        log.info("Suggested Fee: " + suggestedFeePerByte);
        String genId = params.getGenesisID();
        Digest genesisHash = new Digest(params.getGenesishashb64());

        BigInteger lastRound = firstRound.add(BigInteger.valueOf(1000));
        TransactionPrototype transactionPrototype = transactionEnvelope.getTransactionPrototype();
        BigInteger fee = BigInteger.valueOf(transactionPrototype.getFee());
        String noteField = (new String(transactionPrototype.getNoteField())) + " hash:<" + transactionPrototype.hashCode() + ">";
        transactionPrototype.setNoteField(noteField.getBytes());

        BigInteger amount = BigInteger.valueOf(transactionPrototype.getAmount());

        log.info("from Address: " + transactionPrototype.getPayer());
        log.info("to address: " + transactionPrototype.getReceiver());
        Address fromAddress = new Address(transactionPrototype.getPayer());
        Address toAddress = new Address(transactionPrototype.getReceiver());

        //Setup Transaction
        Transaction algorandTransaction = new Transaction(fromAddress, fee, firstRound, lastRound, noteField.getBytes(), amount, toAddress, genId, genesisHash);
        transactionEnvelope.setAlgorandTransaction(algorandTransaction);

        transactionEnvelope.setSignedTransaction(null);

        return transactionEnvelope;
    }


//    public TransactionEnvelope signTransaction(TransactionEnvelope transactionEnvelope) throws GeneralSecurityException {
//
//        Account src = new Account(srcAccount);
//
//        Transaction alogorandTransaction = transactionEnvelope.getAlgorandTransaction();
//        // Sign the Transaction
//        SignedTransaction signedTransaction = src.signTransaction(alogorandTransaction);
//
//        log.info("signed transaction id: " + signedTransaction.transactionID);
//
////        transactionEnvelope.setSignedAlgorandTransaction(signedTransaction);
//
//        return transactionEnvelope;
//    }

    public TransactionEnvelope signTransaction(TransactionEnvelope transactionEnvelope) throws GeneralSecurityException {

        Account src = new Account(srcAccount);

        Transaction alogorandTransaction = transactionEnvelope.getAlgorandTransaction();

        log.info("algorand transaction: " + alogorandTransaction);
        // Sign the Transaction
        SignedTransaction signedTransaction = src.signTransaction(alogorandTransaction);

        log.info("signed transaction id: " + signedTransaction.transactionID);

        transactionEnvelope.setSignedTransaction(signedTransaction);
        transactionEnvelope.setState("signed");
        return transactionEnvelope;
    }

    public void submitTransactionEnvelopeToAlgod(TransactionEnvelope transactionEnvelope) throws GeneralSecurityException, ApiException {

//        AlgodApi algodApiInstance = getAlgodApiInstance();

        com.algorand.algosdk.v2.client.common.AlgodClient algodClient = getAlgodClientV2();

        ObjectMapper objectMapper = new ObjectMapper();

        SignedTransaction signedTransaction = transactionEnvelope.getSignedTransaction();

        TransactionPrototype transactionPrototype = transactionEnvelope.getTransactionPrototype();
        PendingTransactionResponse transactionInfo = null;
        try {
            String transactionId = submitTransactionV2(algodClient, signedTransaction);
            System.out.println("Transaction ID: " + transactionId);
            // wait for the transaction to be confirmed
            transactionInfo = waitForTransactionToCompleteV2(algodClient, transactionId);
            log.info("Successfully sent tx with id: " + transactionId);
            transactionEnvelope.setAlgorandTransactionId(transactionId);
            transactionEnvelope.setAPPID( transactionInfo.applicationIndex );
        } catch (Exception e) {
            log.error("error submitting transaction", e);
            return;
        }
        if (transactionPrototype.getTransactionType() == TransactionTypeEnum.CREATE_ASSET) {
            if (transactionInfo!= null) {
                Long assetId = transactionInfo.assetIndex;
                log.info("assetId " + assetId);
                transactionEnvelope.setAlgorandAssetId(String.valueOf(assetId));
            }
        }
    }

    @Override
    public void submitSignedTransactionEnvelope(TransactionEnvelope signedTransactionEnvelop) throws GeneralSecurityException {
        log.info("submitSignedTransaction " + signedTransactionEnvelop);
        try {
            // check for group transaction
            if (signedTransactionEnvelop.getGroupdId() != null) {
                transactionEnvelopeMap.put(signedTransactionEnvelop.getId(), signedTransactionEnvelop);
                processSignedGroupTransaction(signedTransactionEnvelop);
            } else {
                submitTransactionEnvelopeToAlgod(signedTransactionEnvelop);
                transactionEnvelopeMap.put(signedTransactionEnvelop.getId(), signedTransactionEnvelop);
            }
        } catch (ApiException e) {
            log.error("Error submitting signed transaction", e);
        }
    }

    private void processSignedGroupTransaction(TransactionEnvelope signedTransactionEnvelop) {
        String transactionGroupId = signedTransactionEnvelop.getGroupdId();
        for (TransactionGroup transactionGroup : transactionGroupList) {
            if (transactionGroup.getGroupId().equals(transactionGroupId)) {
                boolean allSigned = true;
                for (TransactionEnvelope groupTransactionEnvelope : transactionGroup.getTransactionEnvelopeList()) {
                    String groupTransactionId = groupTransactionEnvelope.getId();
                    TransactionEnvelope transactionEnvelope = transactionEnvelopeMap.get(groupTransactionId);
                    allSigned = transactionEnvelope.getState().equals("signed");
                    if (!allSigned) break;
                }
                if (allSigned) {
                    String transactionId = submitSignedTransactionGroup(transactionGroup);
                    log.info("submitted group transaction with id " + transactionId);
                }
            }
        }
    }

    private String submitSignedTransactionGroup(TransactionGroup transactionGroup) {
        try {

            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream( );
            for (TransactionEnvelope transactionEnvelope : transactionGroup.getTransactionEnvelopeList()) {
                String teId = transactionEnvelope.getId();
                TransactionEnvelope actual = this.transactionEnvelopeMap.get(teId);
                byte[] transactionBytes = Encoder.encodeToMsgPack(actual.getSignedTransaction());
                byteOutputStream.write( transactionBytes );
                log.info("Submitting transaction: " + actual.getSignedTransaction().transactionID);
            }

            byte groupTransactionBytes[] = byteOutputStream.toByteArray();

            com.algorand.algosdk.v2.client.common.AlgodClient algodv2Client = getAlgodClientV2();
            RawTransaction rawTransaction = algodv2Client.RawTransaction();
            rawTransaction.rawtxn(groupTransactionBytes);
            Response<PostTransactionsResponse> response = rawTransaction.execute();
            PostTransactionsResponse id = response.body();
            transactionGroup.setAlgorandTransactionId(id.txId);
            log.info("successfully committed group transaction with transaction id " + id.txId + " " + transactionGroup);
            waitForIndividualGroupTransactions( transactionGroup);
            return id.txId;

        } catch (JsonProcessingException e) {
            log.error("error submitting signed transaction group" ,e);
        } catch (IOException e) {
            log.error("error submitting signed transaction group" ,e);
        } catch (ApiException e) {
            log.error("error submitting signed transaction group" ,e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void waitForIndividualGroupTransactions( TransactionGroup transactionGroup) throws Exception {
        AlgodApi algodApiInstance = getAlgodApiInstance();
        for (TransactionEnvelope transactionEnvelope : transactionGroup.getTransactionEnvelopeList()) {
            String teId = transactionEnvelope.getId();
            TransactionEnvelope actual = this.transactionEnvelopeMap.get(teId);
            String transactionId = actual.getAlgorandTransaction().txID();
            com.algorand.algosdk.algod.client.model.Transaction transactionInfo = waitForTransactionToComplete(algodApiInstance, transactionId);

            log.info("Successfully retrieved group tx with id: " + transactionInfo);
            actual.setAlgorandTransactionId(transactionId);

            if (actual.getTransactionPrototype().getTransactionType() == TransactionTypeEnum.CREATE_ASSET) {
                if (transactionInfo != null) {
                    BigInteger assetId = transactionInfo.getTxresults().getCreatedasset();
                    log.info("assetId " + assetId);
                    actual.setAlgorandAssetId(String.valueOf(assetId));
                }
            }
        }
    }

    @Override
    public TransactionEnvelope generateTransactionEnvelope(String transactionEnvelopeIdentifier) {
        String terms = "agreement terms go here " + System.nanoTime();

        final int fee = 1000;
        final int amount = 10;
        final String transactionId = String.valueOf(System.nanoTime());
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Account payerAccount = new Account(srcAccount);
            log.info("Payer Account: " + objectMapper.writeValueAsString(payerAccount));
            log.info("Payer Address: " + payerAccount.getAddress().toString());

            TransactionPrototype trx = new TransactionPrototype();
            trx.setAmount(amount);
            trx.setFee(fee);
            trx.setNoteField((terms).getBytes());
            trx.setPayer(payerAccount.getAddress().toString());
            trx.setReceiver(destAddr);
            trx.setApplicationTransactionId(transactionId);

            Agreement agreement = new Agreement();
            agreement.setIdentifier("agreement-" + transactionId);
            agreement.setTime(new Date());
            agreement.setType("receipt");
            agreement.setContents("you agree to pay " + amount + " algos, with fee of " + fee + " microAlogs");
            agreement.setSignature("signature of agreement by the application: ".getBytes());

            TransactionEnvelope te = new TransactionEnvelope();
            te.setDate(new Date());
            te.setName("transaction-envelope-" + transactionId);
            te.setDescription("Sample transaction envelope");
            te.setApplicationId("PoS App");
            te.setTransactionPrototype(trx);
            te.setId(transactionEnvelopeIdentifier);
            te.setAgreement(agreement);

            submitUnsignedTransactionEnvelope(te);

            transactionEnvelopeMap.put(transactionEnvelopeIdentifier, te);

            return te;

        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String storeContents(String contents) throws Exception {
        log.info("storing contents to ipfs: " + contents);
        String hash = null;
        try {
            NamedStreamable.ByteArrayWrapper bytearray = new NamedStreamable.ByteArrayWrapper(contents.getBytes());
            MerkleNode response = ipfs.add(bytearray).get(0);
            hash = response.hash.toBase58();
            log.info("Hash (base 58): " + hash);
        } catch ( Exception ex) {
            log.error("error storing contents to IPSF", ex);
            throw new  Exception("Error while communicating with the IPFS node", ex);
        }
        return hash;
    }

    @Override
    public String getContents(String hash) {
        String contents = null;
        try {
            Multihash multihash = Multihash.fromBase58(hash);
            byte[] byteContent = ipfs.cat(multihash);
            contents = new String(byteContent);
            log.info("Content of " + hash + ": " + contents);
        } catch (IOException ex) {
            log.error("error retrieving contents from IPSF", ex);
            throw new RuntimeException("Error while communicating with the IPFS node", ex);
        }
        return contents;
    }

    @Override
    public List<Map> readFromTopic(String topic) throws Exception {
        Stream<Map<String, Object>> sub = ipfs.pubsub.sub(topic);
        List<Map> results = sub.limit(2).collect(Collectors.toList());
        return results;
    }

    @Override
    public void writeToTopic(String topic, String data) throws Exception {
        Object pub = ipfs.pubsub.pub(topic, data);
    }

    // Utility function to wait on a transaction to be confirmed
    private static com.algorand.algosdk.algod.client.model.Transaction waitForTransactionToComplete(AlgodApi algodApiInstance, String txID) throws Exception {
        com.algorand.algosdk.algod.client.model.Transaction transactionInfo = null;

        while (true) {
            try {
                //Check the pending transactions
                transactionInfo = algodApiInstance.pendingTransactionInformation(txID);
                if (transactionInfo.getRound() != null && transactionInfo.getRound().longValue() > 0) {
                    //Got the completed Transaction
                    System.out.println("Transaction " + transactionInfo.getTx() + " confirmed in round " + transactionInfo.getRound().longValue());
                    break;
                }
            } catch (Exception e) {
                throw (e);
            }
        }
        return transactionInfo;
    }

    static class ChangingBlockParms {
        public BigInteger fee;
        public BigInteger firstRound;
        public BigInteger lastRound;
        public String genID;
        public Digest genHash;

        public ChangingBlockParms() {
            this.fee = BigInteger.valueOf(0);
            this.firstRound = BigInteger.valueOf(0);
            this.lastRound = BigInteger.valueOf(0);
            this.genID = "";
            this.genHash = null;
        }
    }

    // Utility function to update changing block parameters
    public static ChangingBlockParms getChangingParms(AlgodApi algodApiInstance) throws Exception {
        ChangingBlockParms cp = new ChangingBlockParms();
        try {
            TransactionParams params = algodApiInstance.transactionParams();
            cp.fee = params.getFee();
            cp.firstRound = params.getLastRound();
            cp.lastRound = cp.firstRound.add(BigInteger.valueOf(1000));
            cp.genID = params.getGenesisID();
            cp.genHash = new Digest(params.getGenesishashb64());

        } catch (ApiException e) {
            throw (e);
        }
        return (cp);
    }

    // Utility function for sending a raw signed transaction to the network
    public TransactionID submitTransaction(AlgodApi algodApiInstance, SignedTransaction signedTx) throws Exception {
        try {
            // Msgpack encode the signed transaction
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            TransactionID id = algodApiInstance.rawTransaction(encodedTxBytes);
            return (id);
        } catch (ApiException e) {
            throw (e);
        }
    }

    // Utility function for sending a raw signed transaction to the network
    public  String submitTransactionV2(
            com.algorand.algosdk.v2.client.common.AlgodClient algodClient
            , SignedTransaction signedTx) throws Exception {
        try {
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
            RawTransaction rawTransaction = algodClient.RawTransaction();
            rawTransaction = rawTransaction.rawtxn(encodedTxBytes);
            Response<PostTransactionsResponse> postTransactionsResponse = rawTransaction.execute();
            if (!postTransactionsResponse.isSuccessful() ) {
                String message = "error sending transaction " + postTransactionsResponse.message();
                log.error(message);
                throw new Exception(message);
            }
            PostTransactionsResponse postTransactionsBody = postTransactionsResponse.body();
            if (postTransactionsBody != null) {
                return postTransactionsBody.txId;
            }

        } catch (ApiException e) {
            throw (e);
        }
        return null;
    }

    // Utility function to wait on a transaction to be confirmed
    private static PendingTransactionResponse waitForTransactionToCompleteV2(
            com.algorand.algosdk.v2.client.common.AlgodClient algodClient,
            String txID) throws Exception {
        PendingTransactionResponse transactionInfo = null;

        while (true) {
            try {
                //Check the pending transactions
                PendingTransactionInformation pendingTransactionInformation = algodClient.PendingTransactionInformation(txID);
                final Response<PendingTransactionResponse> pendingTransactionResponse = pendingTransactionInformation.execute();
                transactionInfo = pendingTransactionResponse.body();

                if (transactionInfo.confirmedRound != null && transactionInfo.confirmedRound  > 0) {
                    //Got the completed Transaction
                    System.out.println("Transaction " + transactionInfo.txn.tx.txID() + " confirmed in round " + transactionInfo.confirmedRound);
                    break;
                }
            } catch (Exception e) {
                throw (e);
            }
        }
        return transactionInfo;
    }


}
