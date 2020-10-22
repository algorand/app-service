package com.algorand.app.service.resources;

import com.algorand.algosdk.algod.client.model.Account;
import com.algorand.algosdk.algod.client.model.Block;
import com.algorand.algosdk.algod.client.model.NodeStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/algod")
public class AlgodResource implements AlgodResourceInterface {
    private final TransactionEnvelopeResourceInterface transactionEnvelopeResource;
    private final AlgodResourceInterface algodResourceInterface;

    public AlgodResource(final TransactionEnvelopeResourceInterface ter, final AlgodResourceInterface ar) {
        this.transactionEnvelopeResource = ter;
        this.algodResourceInterface = ar;
    }

    @Override
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public NodeStatus getNodeStatus() {
        return algodResourceInterface.getNodeStatus();
    }

    @Override
    @GET
    @Path("block")
    @Produces(MediaType.APPLICATION_JSON)
    public Block getBlock() throws Exception {
        return algodResourceInterface.getBlock();
    }

    @Override
    @GET
    @Path("block/{block-number}")
    @Produces(MediaType.APPLICATION_JSON)
    public Block getBlock(@PathParam("block-number") long blockNumber) throws Exception {
        return algodResourceInterface.getBlock(blockNumber);
    }

    @Override
    @GET
    @Path("account/{account-address}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("account-address") String accountAddress) throws Exception {
        return algodResourceInterface.getAccount(accountAddress);
    }

    @Override
    @GET
    @Path("transaction-json")
    public boolean generateTransactionJSON() {
        return algodResourceInterface.generateTransactionJSON();
    }

    @Override
    @GET
    @Path("block-json")
    public boolean generateBlockJSON() {
        return algodResourceInterface.generateBlockJSON();
    }

}
