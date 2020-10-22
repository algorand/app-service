package com.algorand.crowdfund.service.core;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Reclaim {
	private String id;
	private String fundId;
	private String investorAddress;
	private long reclaimAmount;
	private Date reclaimDate;
}
