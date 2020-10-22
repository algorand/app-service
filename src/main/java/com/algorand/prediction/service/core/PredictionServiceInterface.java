package com.algorand.prediction.service.core;

import java.util.Collection;

public interface PredictionServiceInterface {

    Collection<Question> getQuestionList();

    Question getQuestion(String questionId);

    Collection<Bid> getBidList(String questionId);

    Question createQuestion(Question question);

    Question updateQuestion(Question question);

    void publishQuestion(Question question);

    Bid acceptBid(Bid bid);

    void distributeWinnings(Question question);

    void optInToQuestion(OptInToQuestion optInToQuestion);

    Collection<OptInToQuestion> optInToQuestionList(String account);
}
