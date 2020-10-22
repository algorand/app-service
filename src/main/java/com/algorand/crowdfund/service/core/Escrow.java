package com.algorand.crowdfund.service.core;

import com.algorand.algosdk.crypto.TEALProgram;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Escrow {
    private String address;
    private TEALProgram tealProgram;
}
