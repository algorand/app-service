package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.builder.transaction.ApplicationBaseTransactionBuilder;
import com.algorand.algosdk.builder.transaction.TransactionBuilder;
import com.algorand.algosdk.v2.client.algod.TransactionParams;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CrowdFundUtility {

    private Logger log = LoggerFactory.getLogger("CrowdFundFactory");

    void addTransactionParamsToAlgorandTransaction(AlgodClient algodClient,  TransactionBuilder builder) throws Exception {
        TransactionParams transactionParams = algodClient.TransactionParams();
        final Response<TransactionParametersResponse> response = transactionParams.execute();
        builder.firstValid(response.body().lastRound);
        builder.lastValid(response.body().lastRound + 1000);
        builder.fee(response.body().fee);
        builder.genesisHash(response.body().genesisHash);
    }

    List<byte[]> convertArgs(String args) throws CrowdFundException {
        return Arrays.stream(Strings.split(args, ','))
                .map(s -> {
                    String[] parts = Strings.split(s, ':');
                    byte[] converted = null;
                    switch (parts[0]) {
                        case "str":
                        case "addr":
                            converted = parts[1].getBytes();
                            break;
                        case "int":
                            converted = BigInteger.valueOf(Integer.parseInt(parts[1])).toByteArray();
                            break;
                        default:
                            String message = "Non supported field type '" + parts[0] + "'";
                            log.error(message);
                            break;
                    }
                    return converted;
                })
                .collect(Collectors.toList());
    }
}
