package com.algorand.app.service;

import com.algorand.app.service.core.AppServiceImpl;
import com.algorand.app.service.resources.AccountApplicationRegistrationResource;
import com.algorand.app.service.resources.AlgodResource;
import com.algorand.app.service.resources.TransactionEnvelopeResource;
import com.algorand.crowdfund.service.core.CrowdFundServiceImpl;
import com.algorand.crowdfund.service.core.CrowdFundServiceInterface;
import com.algorand.crowdfund.service.resources.CrowdFundServiceResource;
import com.algorand.prediction.service.PredictionServiceConfiguration;
import com.algorand.prediction.service.core.PredictionServiceImpl;
import com.algorand.prediction.service.core.PredictionServiceInterface;
import com.algorand.prediction.service.resources.PredictionServiceResource;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppService extends Application<AppServiceConfiguration> {

    private Logger log = LoggerFactory.getLogger("AppService");

    public static void main(String[] args) throws Exception {
        new AppService().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppServiceConfiguration> bootstrap) {

        log.info("initializing application");
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

    }

    @Override
    public void run(AppServiceConfiguration config, Environment environment) {
        AppServiceImpl appService = new AppServiceImpl(config.algodNetworkHost, config.algodNetworkPort,  config.ipfsNetworkHost, config.ipfsNetworkPort, config.algodApiToken, config.sourceAccount, config.destAccount);
        environment.healthChecks().register("healthCheck", new AppServiceHealthCheck());
        environment.jersey().register(new AlgodResource(appService, appService));
        environment.jersey().register(new TransactionEnvelopeResource(appService));
        environment.jersey().register(new AccountApplicationRegistrationResource());
        PredictionServiceConfiguration predictionServiceConfiguration = new PredictionServiceConfiguration(config);
        PredictionServiceInterface predictionService = new PredictionServiceImpl(predictionServiceConfiguration);
        PredictionServiceResource predictionServiceResource = new PredictionServiceResource(predictionService);
        environment.jersey().register(predictionServiceResource);
        CrowdFundServiceInterface crowdFundService = new CrowdFundServiceImpl(appService, config.algodNetworkHost, config.algodNetworkPort, config.algodApiToken);
        CrowdFundServiceResource crowdFundServiceResource = new CrowdFundServiceResource(crowdFundService);
        environment.jersey().register(crowdFundServiceResource);
    }
}
