package com.algorand.prediction.service;

import com.algorand.prediction.service.core.PredictionServiceImpl;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class PredictionService extends Application<PredictionServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new PredictionService().run(args);
    }

    public void initialize(Bootstrap<PredictionServiceConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor())
        );
    }

    @Override
    public void run(PredictionServiceConfiguration config, Environment environment) throws Exception {
        PredictionServiceImpl predictionService = new PredictionServiceImpl(config);
        environment.jersey().register(predictionService);
    }
}
