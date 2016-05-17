package com.hello.suripu.core.processors;

import com.google.common.collect.Lists;
import com.hello.suripu.core.db.QuestionResponseDAO;
import com.hello.suripu.core.db.QuestionResponseReadDAO;
import com.hello.suripu.core.models.AccountQuestion;
import com.hello.suripu.core.models.Question;
import com.hello.suripu.core.models.Questions.QuestionCategory;
import com.hello.suripu.core.models.Response;
import com.hello.suripu.core.util.QuestionSurveyUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by jyfan on 5/3/16.
 */
public class QuestionSurveyProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionSurveyProcessor.class);

    private final QuestionResponseReadDAO questionResponseReadDAO;
    private final QuestionResponseDAO questionResponseDAO;

    private final List<Question> surveyOneQuestions;

    public QuestionSurveyProcessor(final QuestionResponseReadDAO questionResponseReadDAO,
                                   final QuestionResponseDAO questionResponseDAO,
                                   final List<Question> surveyOneQuestions) {
        this.questionResponseReadDAO = questionResponseReadDAO;
        this.questionResponseDAO = questionResponseDAO;
        this.surveyOneQuestions = surveyOneQuestions;
    }

    /*
    Build processor
     */
    public static class Builder {
        private QuestionResponseReadDAO questionResponseReadDAO;
        private QuestionResponseDAO questionResponseDAO;
        private List<Question> surveyOneQuestions = Lists.newArrayList();

        public Builder withQuestionResponseDAO(final QuestionResponseReadDAO questionResponseReadDAO,
                                               final QuestionResponseDAO questionResponseDAO) {
            this.questionResponseReadDAO = questionResponseReadDAO;
            this.questionResponseDAO = questionResponseDAO;
            return this;
        }

        public Builder withQuestions(final QuestionResponseReadDAO questionResponseReadDAO) {
            this.surveyOneQuestions = Lists.newArrayList();

            final List<Question> allQuestions = questionResponseReadDAO.getAllQuestions();
            for (final Question question : allQuestions) {
                if (question.category != QuestionCategory.SURVEY_ONE) {
                    continue;
                }
                this.surveyOneQuestions.add(question);
            }

            return this;
        }

        public QuestionSurveyProcessor build() {
            checkNotNull(questionResponseReadDAO, "questionResponseRead can not be null");
            checkNotNull(questionResponseDAO, "questionResponse can not be null");
            checkNotNull(surveyOneQuestions, "surveyOneQuestions can not be null");

            return new QuestionSurveyProcessor(this.questionResponseReadDAO, this.questionResponseDAO, this.surveyOneQuestions);
        }
    }

    /*
    Logic for picking questions
     */
    public List<Question> getQuestions(final Long accountId, final int accountAgeInDays, final DateTime today) {

        //Get available survey one questions
        final List<Response> surveyOneResponses = questionResponseReadDAO.getAccountResponseByQuestionCategoryStr(accountId, QuestionCategory.SURVEY_ONE.toString().toLowerCase());
        final List<Question> availableQuestions = QuestionSurveyUtils.getSurveyXQuestion(surveyOneResponses, surveyOneQuestions);

        if (availableQuestions.isEmpty()) {
            return availableQuestions;
        }

        //Returns and saves 1st available question.
        final DateTime expiration = today.plusDays(1);

        //Check if database already has question with unique index on (account_id, question_id, created_local_utc_ts)
        final Boolean savedQuestion = savedAccountQuestion(accountId, availableQuestions.get(0), today);
        if (savedQuestion) {
            return availableQuestions.subList(0, 1);
        }

        saveQuestion(accountId, availableQuestions.subList(0, 1).get(0), today, expiration);
        return availableQuestions.subList(0, 1);
    }

    /*
    Insert questions
     */

    private void saveQuestion(final Long accountId, final Question question, final DateTime today, final DateTime expireDate) {
        LOGGER.debug("action=saved_question processor=question_survey account_id={} question_id={} today={} expire_date={}", accountId, question.id, today, expireDate);
        this.questionResponseDAO.insertAccountQuestion(accountId, question.id, today, expireDate);
    }

    private Boolean savedAccountQuestion(final Long accountId, final Question question, final DateTime created) {
        final List<AccountQuestion> questions = questionResponseReadDAO.getAskedQuestionByQuestionIdCreatedDate(accountId, question.id, created);
        if (questions.isEmpty()) {
            return Boolean.FALSE;
        } else {
            LOGGER.info("event=multiple_question_impression account_id={} question_id={} usr_local_utc_date={}", accountId, question.id, created);
            return Boolean.TRUE;
        }
    }
}
