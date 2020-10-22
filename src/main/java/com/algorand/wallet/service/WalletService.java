package com.algorand.wallet.service;

import com.algorand.wallet.service.resources.WalletRESTClientController;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.client.Client;

public class WalletService extends Application<WalletServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new WalletService().run(args);
    }

    public void initialize(Bootstrap<WalletServiceConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor())
        );
    }

    @Override
    public void run(WalletServiceConfiguration config, Environment environment) throws Exception {

        final Client client = new JerseyClientBuilder(environment).build("WalletRESTClient");
        environment.jersey().register(new WalletRESTClientController(client, config));
    }
}
