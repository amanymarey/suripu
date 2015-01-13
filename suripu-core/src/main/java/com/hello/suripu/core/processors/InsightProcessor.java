package com.hello.suripu.core.processors;

import com.google.common.base.Optional;
import com.hello.suripu.core.db.AggregateSleepScoreDAODynamoDB;
import com.hello.suripu.core.db.DeviceDAO;
import com.hello.suripu.core.db.DeviceDataDAO;
import com.hello.suripu.core.db.InsightsDAODynamoDB;
import com.hello.suripu.core.db.TrackerMotionDAO;
import com.hello.suripu.core.db.TrendsDAO;
import com.hello.suripu.core.models.Insights.InsightCard;
import com.hello.suripu.core.processors.insights.LightData;
import com.hello.suripu.core.processors.insights.Lights;
import com.hello.suripu.core.processors.insights.TemperatureHumidity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kingshy on 10/24/14.
 */
public class InsightProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsightProcessor.class);

    private static final int RECENT_DAYS = 10; // last 10 days

    private final DeviceDataDAO deviceDataDAO;
    private final DeviceDAO deviceDAO;
    private final TrendsDAO trendsDAO;
    private final TrackerMotionDAO trackerMotionDAO;
    private final AggregateSleepScoreDAODynamoDB scoreDAODynamoDB;
    private final InsightsDAODynamoDB insightsDAODynamoDB;
    private final LightData lightData;
    private final AccountInfoProcessor accountInfoProcessor;

    public InsightProcessor(final DeviceDataDAO deviceDataDAO,
                            final DeviceDAO deviceDAO,
                            final TrendsDAO trendsDAO,
                            final TrackerMotionDAO trackerMotionDAO,
                            final AggregateSleepScoreDAODynamoDB scoreDAODynamoDB,
                            final InsightsDAODynamoDB insightsDAODynamoDB,
                            final AccountInfoProcessor accountInfoProcessor,
                            final LightData lightData
                            ) {
        this.deviceDataDAO = deviceDataDAO;
        this.deviceDAO = deviceDAO;
        this.trendsDAO = trendsDAO;
        this.trackerMotionDAO = trackerMotionDAO;
        this.scoreDAODynamoDB = scoreDAODynamoDB;
        this.insightsDAODynamoDB = insightsDAODynamoDB;
        this.lightData = lightData;
        this.accountInfoProcessor = accountInfoProcessor;
    }

    public void generateInsights(final Long accountId, final DateTime accountCreated) {
        final int accountAge = this.getAccountAgeInDays(accountCreated);
        if (accountAge < 1) {
            return; // not slept one night yet
        }

        final Optional<Long> deviceIdOptional = deviceDAO.getMostRecentSenseByAccountId(accountId);
        if (!deviceIdOptional.isPresent()) {
            return;
        }

        final Long deviceId = deviceIdOptional.get();

        if (accountAge <= 10) {
            generateNewUserInsights(accountId, deviceId, accountAge);
        } else {
            generateGeneralInsights(accountId, deviceId);
        }
    }

    /**
     * for new users, first 10 days
     * @param accountId
     * @param accountAge
     */
    private void generateNewUserInsights(final Long accountId, final Long deviceId, final int accountAge) {

        Optional<InsightCard> insightCardOptional = Optional.absent();
        switch (accountAge) {
            case 1:
                insightCardOptional = Lights.getInsights(accountId, deviceId, deviceDataDAO, lightData);
                break;
            case 2:
                final String tempPref = this.accountInfoProcessor.checkTemperaturePreference(accountId);
                insightCardOptional = TemperatureHumidity.getInsights(accountId, deviceId, deviceDataDAO, tempPref);
                break;
            default:
                insightCardOptional = Lights.getInsights(accountId, deviceId, deviceDataDAO, lightData);
                break; // TODO: rm debug, lights insight is all we have
        }

        if (insightCardOptional.isPresent()) {
            // save to dynamo
            this.insightsDAODynamoDB.insertInsight(insightCardOptional.get());
        }
    }

    /**
     * logic to determine what kind of insights to generate
     * @param accountId
     */
    private void generateGeneralInsights(final Long accountId, final Long deviceId) {

        final Set<InsightCard.Category> recentCategories = this.getRecentInsightsCategories(accountId);

        // TODO - randomly select a card that hasn't been generated recently
        if (recentCategories.contains(InsightCard.Category.LIGHT)) {
            LOGGER.debug("No new insights generated: {}", accountId);
            return;
        }

        this.generateInsightsByCategory(accountId, deviceId, InsightCard.Category.LIGHT);

    }

    public void generateInsightsByCategory(final Long accountId, final Long deviceId, final InsightCard.Category category) {

        Optional<InsightCard> insightCardOptional = Optional.absent();

        if (category == InsightCard.Category.LIGHT) {
            insightCardOptional = Lights.getInsights(accountId, deviceId, deviceDataDAO, lightData);
        } else if (category == InsightCard.Category.TEMPERATURE) {
            final String tempPref = this.accountInfoProcessor.checkTemperaturePreference(accountId);
            insightCardOptional = TemperatureHumidity.getInsights(accountId, deviceId, deviceDataDAO, tempPref);
        }

        if (insightCardOptional.isPresent()) {
            // save to dynamo
            this.insightsDAODynamoDB.insertInsight(insightCardOptional.get());
        }
    }

    private Set<InsightCard.Category> getRecentInsightsCategories(final Long accountId) {
        // get all insights from the past week
        final DateTime aWeekAgo = DateTime.now(DateTimeZone.UTC).minus(7);
        final Boolean chronological = false;

        final List<InsightCard> cards = this.insightsDAODynamoDB.getInsightsByDate(accountId, aWeekAgo, chronological, RECENT_DAYS);

        final Set<InsightCard.Category> seenCategories = new HashSet<>();
        for (InsightCard card : cards) {
            seenCategories.add(card.category);
        }
        return seenCategories;
    }

    private int getAccountAgeInDays(final DateTime accountCreated) {
        final DateTime now = DateTime.now(DateTimeZone.UTC);
        final Duration duration = new Duration(accountCreated, now);
        return duration.toStandardDays().getDays();
    }
}
