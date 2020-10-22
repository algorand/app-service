package com.algorand.prediction.service.resources;

import com.algorand.prediction.service.core.Bid;
import com.algorand.prediction.service.core.OptInToQuestion;
import com.algorand.prediction.service.core.PredictionServiceInterface;
import com.algorand.prediction.service.core.Question;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/prediction")
public class PredictionServiceResource implements PredictionServiceInterface {
    private final PredictionServiceInterface predictionServiceInterface;

    public PredictionServiceResource( final PredictionServiceInterface predictionServiceInterface) {
        this.predictionServiceInterface = predictionServiceInterface;
    }

    @Override
    @GET
    @Path("question")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Question> getQuestionList() {
        return predictionServiceInterface.getQuestionList();
    }

    @Override
    @GET
    @Path("question/{question-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Question getQuestion(@PathParam("question-id") String questionId) {
        return predictionServiceInterface.getQuestion(questionId);
    }

    @Override
    @GET
    @Path("bid/{question-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Bid> getBidList(@PathParam("question-id") String questionId) {
        return predictionServiceInterface.getBidList(questionId);
    }

    @Override
    @POST
    @Path("question")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Question createQuestion(Question question) {
        return predictionServiceInterface.createQuestion(question);
    }

    @Override
    @PUT
    @Path("question")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Question updateQuestion(Question question) {
        return predictionServiceInterface.updateQuestion(question);
    }

    @Override
    @PUT
    @Path("publish-question")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void publishQuestion(Question question) {
         predictionServiceInterface.publishQuestion(question);
    }

    @Override
    @POST
    @Path("bid")
    @Produces(MediaType.APPLICATION_JSON)
    public Bid acceptBid(Bid bid) {
        return predictionServiceInterface.acceptBid(bid);
    }

    @Override
    @PUT
    @Path("distribute-winnings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void distributeWinnings(Question question) {
        predictionServiceInterface.distributeWinnings(question);
    }

    @Override
    @POST
    @Path("optin")
    @Produces(MediaType.APPLICATION_JSON)
    public void optInToQuestion(OptInToQuestion optInToQuestion) {
        predictionServiceInterface.optInToQuestion(optInToQuestion);
    }

    @Override
    @GET
    @Path("optin/{account}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<OptInToQuestion> optInToQuestionList(@PathParam("account") String account) {
        return predictionServiceInterface.optInToQuestionList(account);
    }
}
