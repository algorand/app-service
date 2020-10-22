package com.algorand.crowdfund.service;

import com.algorand.crowdfund.service.core.CrowdFundServiceImpl;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class CrowdFundService extends Application<CrowdFundServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new CrowdFundService().run(args);
    }

    public void initialize(Bootstrap<CrowdFundServiceConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor())
        );
    }

    @Override
    public void run(CrowdFundServiceConfiguration config, Environment environment) {
//        CrowdFundServiceImpl crowdFundService = new CrowdFundServiceImpl(config.algodNetworkHost, config.algodNetworkPort, config.algodApiToken);
//        environment.jersey().register(crowdFundService);
    }
}
