package com.algorand.crowdfund.service;

import com.algorand.app.service.AppServiceConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;


public class CrowdFundServiceConfiguration extends AppServiceConfiguration {
    @NotEmpty(message = "algod host")
    public String algodNetworkHost;
    @NotNull(message = "algod port number")
    public int algodNetworkPort;
    @NotEmpty(message = "algod api token")
    public String algodApiToken;
}
