package com.algorand.crowdfund.service.core;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Investment {

	private String id;
	private String fundId;
	private String investorAddress;
	private String note;
	private long investmentAmount;
	private Date creationDate;
	private InvestmentState investmentState;

}
