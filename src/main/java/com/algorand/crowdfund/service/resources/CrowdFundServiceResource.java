package com.algorand.crowdfund.service.resources;

import com.algorand.crowdfund.service.core.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/crowdfund")
public class CrowdFundServiceResource implements CrowdFundServiceInterface {
    private final CrowdFundServiceInterface crowdFundServiceInterface;

    public CrowdFundServiceResource(final CrowdFundServiceInterface crowdFundServiceInterface) {
        this.crowdFundServiceInterface = crowdFundServiceInterface;
    }

    @Override
    @POST
    @Path("fund")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Fund createFund(Fund fund) throws CrowdFundException {
        return crowdFundServiceInterface.createFund(fund);
    }

    @Override
    @GET
    @Path("fund/{fund-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Fund getFund(@PathParam("fund-id") String fundId) {
        return crowdFundServiceInterface.getFund(fundId);
    }

    @Override
    @GET
    @Path("fund-list/")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Fund> getFundList() {
        return crowdFundServiceInterface.getFundList();
    }

    @Override
    @POST
    @Path("investment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Investment createInvestment(Investment investment)  throws CrowdFundException {
        return crowdFundServiceInterface.createInvestment(investment);
    }

    @Override
    @GET
    @Path("investment/{investment-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Investment getInvestment(@PathParam("investment-id") String donationId) {
        return crowdFundServiceInterface.getInvestment(donationId);
    }

    @Override
    @GET
    @Path("investment-list/{fund-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Investment> getInvestmentList(@PathParam("fund-id") String fundId) {
        return crowdFundServiceInterface.getInvestmentList(fundId);
    }

    @Override
    @PUT
    @Path("claim")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Claim claimFunds(Claim claim) throws CrowdFundException {
        return crowdFundServiceInterface.claimFunds(claim);
    }

    @Override
    @PUT
    @Path("reclaim")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Reclaim reclaimInvestment(Reclaim reclaim) throws CrowdFundException {
        return crowdFundServiceInterface.reclaimInvestment(reclaim);
    }

    @Override
    @GET
    @Path("close-out-fund/{fund-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Closeout closeOutFund(@PathParam("fund-id") String fundId) {
        return crowdFundServiceInterface.closeOutFund(fundId);
    }
}
