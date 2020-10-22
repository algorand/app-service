package com.algorand.crowdfund.service.core;

public enum FundState {
    CREATED,
    WAITING_FOR_APP_CREATION_TRANSACTION_COMMIT,
    WAITING_FOR_APP_UPDATE_TRANSACTION_COMMIT,
    PUBLISHED,
    STARTED,
    ENDED,
    FUNDS_RECEIVED,
    FUNDS_RETURNED,
    CLOSED;
}
