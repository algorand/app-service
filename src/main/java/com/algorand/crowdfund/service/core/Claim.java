package com.algorand.crowdfund.service.core;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Claim {
	private String id;
	private String fundId;
	private String receiverAddress;
	private Long receivedAmount;
	private Date receiveDate;
	private ClaimState claimState;
}
