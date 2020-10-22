package com.algorand.app.service;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;

public class AppServiceHealthCheck extends HealthCheck {
        @Override
        protected  Result check() throws Exception {
            // TODO, add actual health check
            return  Result.healthy();
        }
}
