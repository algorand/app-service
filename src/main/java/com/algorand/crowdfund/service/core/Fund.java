package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.crypto.TEALProgram;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Fund {

	private String id;
	private Long appId;
	private String name;
	private String description;
	private Date startDate;
	private Date endDate;
	private Date closeOutDate;
	private long goalAmount;
	private long goalAssetId;  // Algo, USDC, USDT, ...
	private FundState fundState;
	private String creatorAddress;
	private String receiverAddress;
	private Escrow escrow;
}
