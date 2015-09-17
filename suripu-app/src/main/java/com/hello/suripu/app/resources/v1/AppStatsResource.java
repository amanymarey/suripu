package com.hello.suripu.app.resources.v1;

import com.google.common.base.Optional;
import com.hello.suripu.core.db.AppStatsDAO;
import com.hello.suripu.core.db.InsightsDAODynamoDB;
import com.hello.suripu.core.models.AppStats;
import com.hello.suripu.core.models.AppUnreadStats;
import com.hello.suripu.core.oauth.AccessToken;
import com.hello.suripu.core.oauth.OAuthScope;
import com.hello.suripu.core.oauth.Scope;
import com.hello.suripu.core.util.JsonError;
import com.yammer.metrics.annotation.Timed;
import org.joda.time.DateTime;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/app/stats")
public class AppStatsResource {
    private final AppStatsDAO appStatsDAO;
    private final InsightsDAODynamoDB insightsDAO;

    public AppStatsResource(final AppStatsDAO appStatsDAO, final InsightsDAODynamoDB insightsDAO) {
        this.appStatsDAO = appStatsDAO;
        this.insightsDAO = insightsDAO;
    }


    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AppStats getLastViewed(@Scope(OAuthScope.INSIGHTS_READ) final AccessToken accessToken) {
        final Optional<DateTime> insightsLastViewed = appStatsDAO.getInsightsLastViewed(accessToken.accountId);
        return new AppStats(insightsLastViewed);
    }

    @Timed
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLastViewed(@Scope(OAuthScope.INSIGHTS_READ) final AccessToken accessToken,
                                     @Valid final AppStats appStats) {
        if (appStats.insightsLastViewed.isPresent()) {
            final DateTime insightsLastViewed = appStats.insightsLastViewed.get();
            appStatsDAO.putInsightsLastViewed(accessToken.accountId, insightsLastViewed);
            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE).entity(new JsonError(406, "Not acceptable")).build());
        }
    }

    @Timed
    @GET
    @Path("/unread")
    @Produces(MediaType.APPLICATION_JSON)
    public AppUnreadStats unread(@Scope(OAuthScope.INSIGHTS_READ) final AccessToken accessToken) {
        final Long accountId = accessToken.accountId;
        final Optional<DateTime> insightsLastViewed = appStatsDAO.getInsightsLastViewed(accountId);
        if (insightsLastViewed.isPresent()) {
            final int count = insightsDAO.getInsightCountByDate(accountId, insightsLastViewed.get(), 1);
            return new AppUnreadStats(count > 0);
        }

        return new AppUnreadStats(false);
    }
}
