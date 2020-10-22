package com.algorand.prediction.service;

import com.algorand.app.service.AppServiceConfiguration;

public class PredictionServiceConfiguration extends AppServiceConfiguration {
    public PredictionServiceConfiguration(AppServiceConfiguration config){
        algodApiToken = config.algodApiToken;
        algodNetworkHost = config.algodNetworkHost;
        algodNetworkPort = config.algodNetworkPort;
        sourceAccount = config.sourceAccount;
        destAccount = config.destAccount;
        ipfsNetworkHost = config.ipfsNetworkHost;
        ipfsNetworkPort = config.ipfsNetworkPort;
    }
}
