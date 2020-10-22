package com.algorand.app.service.resources;

import com.algorand.algosdk.algod.client.model.Account;
import com.algorand.algosdk.algod.client.model.Block;
import com.algorand.algosdk.algod.client.model.NodeStatus;

public interface AlgodResourceInterface {

    public NodeStatus getNodeStatus();

    Block getBlock() throws Exception;

    Block getBlock(long blockNumber) throws Exception;

    Account getAccount(String accountAddress) throws Exception;

    boolean generateTransactionJSON();

    boolean generateBlockJSON();
}
