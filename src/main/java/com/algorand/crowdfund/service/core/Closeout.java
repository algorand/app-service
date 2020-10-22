package com.algorand.crowdfund.service.core;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Closeout {
	private String id;
	private String fundId;
	private String closeoutAddress;
	private long closeoutAmount;
	private Date closeoutDate;
}
