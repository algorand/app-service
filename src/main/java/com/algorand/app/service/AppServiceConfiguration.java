package com.algorand.app.service;


import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class AppServiceConfiguration extends Configuration {

    @NotEmpty(message = "algod host")
    public String algodNetworkHost;
    @NotNull(message = "algod port number")
    public int algodNetworkPort;
    @NotEmpty(message = "algod api token")
    public String algodApiToken;

    @NotEmpty(message = "IPFS host")
    public String ipfsNetworkHost;
    @NotNull(message = "IPFS port number")
    public int ipfsNetworkPort;



    @NotEmpty
    public String sourceAccount;

    @NotEmpty
    public String destAccount;
}
