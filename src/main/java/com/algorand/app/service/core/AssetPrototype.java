package com.algorand.app.service.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class AssetPrototype {

    // The following parameters are asset specific
    // and will be re-used throughout the example.

    // Create the Asset
    // Total number of this asset available for circulation
    BigInteger assetTotal = BigInteger.valueOf(10000);
    // Whether user accounts will need to be unfrozen before transacting
    boolean defaultFrozen = false;
    // Used to display asset units to user
    String unitName; // = "LATINUM";
    // Friendly name of the asset
    String assetName; // = "latinum";
    // Optional string pointing to a URL relating to the asset
    String url; // = "http://this.test.com";
    // Optional hash commitment of some sort relating to the asset. 32 character length.
    String assetMetadataHash; // = "16efaa3924a6fd9d3a4824799a4ac65d";
    // The following parameters are the only ones
    // that can be changed, and they have to be changed
    // by the current manager
    // Specified address can change reserve, freeze, clawback, and manager
//    Address manager  = acct2.getAddress();

    String creatorAccount;

    String managerAccount;
    // Specified address is considered the asset reserve
    // (it has no special privileges, this is only informational)
//    Address reserve = acct2.getAddress();
    String reserveAccount;
    // Specified address can freeze or unfreeze user asset holdings
//    Address freeze = acct2.getAddress();
    String freezeAccount;
    // Specified address can revoke user asset holdings and send
    // them to other addresses
//    Address clawback = acct2.getAddress();
    String clawbackAccount;

//    Transaction tx = Transaction.createAssetCreateTransaction(acct1.getAddress(),
//            BigInteger.valueOf( 1000 ), cp.firstRound, cp.lastRound, null, cp.genID,
//            cp.genHash, assetTotal, defaultFrozen, unitName, assetName, url,
//            assetMetadataHash.getBytes(), manager, reserve, freeze, clawback);
//    // Update the fee as per what the BlockChain is suggesting
//        Account.setFeeByFeePerByte(tx, cp.fee);
//
//    // Sign the Transaction
//    SignedTransaction signedTx = acct1.signTransaction(tx);
//    // send the transaction to the network and
//    // wait for the transaction to be confirmed
//        try{
//        TransactionID id = submitTransaction( algodApiInstance, signedTx);
//        System.out.println( "Transaction ID: " + id );
//        waitForTransactionToComplete( algodApiInstance, signedTx.transactionID);
//    } catch (Exception e){
//        e.printStackTrace();
//        return;
//    }
//    // Now that the transaction is confirmed we can get the assetID
//    com.algorand.algosdk.algod.client.model.Account act = algodApiInstance.accountInformation(acct1.getAddress().toString());
//    Map treeMap = new TreeMap(act.getThisassettotal());
//    List> entryList =
//            new ArrayList>(treeMap.entrySet());
//    Entry lastEntry =
//            entryList.get(entryList.size()-1);
//    BigInteger assetID = lastEntry.getKey();
//        System.out.println( "AssetID = " +  assetID);


    public BigInteger getAssetTotal() {
        return assetTotal;
    }

    public void setAssetTotal(BigInteger assetTotal) {
        this.assetTotal = assetTotal;
    }

    public boolean isDefaultFrozen() {
        return defaultFrozen;
    }

    public void setDefaultFrozen(boolean defaultFrozen) {
        this.defaultFrozen = defaultFrozen;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAssetMetadataHash() {
        return assetMetadataHash;
    }

    public void setAssetMetadataHash(String assetMetadataHash) {
        this.assetMetadataHash = assetMetadataHash;
    }

    public String getManagerAccount() {
        return managerAccount;
    }

    public void setManagerAccount(String managerAccount) {
        this.managerAccount = managerAccount;
    }

    public String getReserveAccount() {
        return reserveAccount;
    }

    public void setReserveAccount(String reserveAccount) {
        this.reserveAccount = reserveAccount;
    }

    public String getFreezeAccount() {
        return freezeAccount;
    }

    public void setFreezeAccount(String freezeAccount) {
        this.freezeAccount = freezeAccount;
    }

    public String getClawbackAccount() {
        return clawbackAccount;
    }

    public void setClawbackAccount(String clawbackAccount) {
        this.clawbackAccount = clawbackAccount;
    }

    public String getCreatorAccount() {
        return creatorAccount;
    }

    public void setCreatorAccount(String creatorAccount) {
        this.creatorAccount = creatorAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetPrototype that = (AssetPrototype) o;
        return defaultFrozen == that.defaultFrozen &&
                Objects.equals(assetTotal, that.assetTotal) &&
                Objects.equals(unitName, that.unitName) &&
                Objects.equals(assetName, that.assetName) &&
                Objects.equals(url, that.url) &&
                Objects.equals(creatorAccount, that.creatorAccount) &&
                Objects.equals(managerAccount, that.managerAccount) &&
                Objects.equals(reserveAccount, that.reserveAccount) &&
                Objects.equals(freezeAccount, that.freezeAccount) &&
                Objects.equals(clawbackAccount, that.clawbackAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetTotal, defaultFrozen, unitName, assetName, url, creatorAccount, managerAccount, reserveAccount, freezeAccount, clawbackAccount);
    }

    @Override
    public String toString() {
        return "AssetPrototype{" +
                "assetTotal=" + assetTotal +
                ", defaultFrozen=" + defaultFrozen +
                ", unitName='" + unitName + '\'' +
                ", assetName='" + assetName + '\'' +
                ", url='" + url + '\'' +
                ", assetMetadataHash='" + assetMetadataHash + '\'' +
                ", creatorAccount='" + creatorAccount + '\'' +
                ", managerAccount='" + managerAccount + '\'' +
                ", reserveAccount='" + reserveAccount + '\'' +
                ", freezeAccount='" + freezeAccount + '\'' +
                ", clawbackAccount='" + clawbackAccount + '\'' +
                '}';
    }
}
