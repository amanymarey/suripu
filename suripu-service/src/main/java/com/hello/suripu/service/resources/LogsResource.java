package com.hello.suripu.service.resources;

import com.flaptor.indextank.apiclient.IndexDoesNotExistException;
import com.flaptor.indextank.apiclient.IndexTankClient;
import com.google.common.base.Optional;
import com.hello.dropwizard.mikkusu.helpers.AdditionalMediaTypes;
import com.hello.suripu.api.logging.LogProtos;
import com.hello.suripu.service.SignedMessage;
import com.yammer.metrics.annotation.Timed;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("/logs")
public class LogsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveResource.class);
    private final IndexTankClient.Index index;

    public LogsResource(final String privateSearchUrl, final String indexName) {
        final IndexTankClient client = new IndexTankClient(privateSearchUrl);
        index = client.getIndex(indexName);
    }

    @Timed
    @POST
    @Consumes(AdditionalMediaTypes.APPLICATION_PROTOBUF)
    public void saveLogs(byte[] body) {

        final SignedMessage signedMessage = SignedMessage.parse(body);
        LogProtos.sense_log log;

        try {
            log = LogProtos.sense_log.parseFrom(signedMessage.body);
        } catch (IOException exception) {
            final String errorMessage = String.format("Failed parsing protobuf: %s", exception.getMessage());
            LOGGER.error(errorMessage);

            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("bad request")
                    .type(MediaType.TEXT_PLAIN_TYPE).build()
            );
        }

        // get MAC address of morpheus

        if(!log.hasDeviceId()){
            LOGGER.error("Cannot get morpheus id");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("bad request")
                    .type(MediaType.TEXT_PLAIN_TYPE).build()
            );
        }

        // TODO: Fetch key from Datastore
        final byte[] keyBytes = "1234567891234567".getBytes();
        final Optional<SignedMessage.Error> error = signedMessage.validateWithKey(keyBytes);

        if(error.isPresent()) {
            LOGGER.error(error.get().message);
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("bad request")
                    .type(MediaType.TEXT_PLAIN_TYPE).build()
            );
        }

        final Map<String, String> fields = new HashMap<>();
        final Map<String, String> categories = new HashMap<>();

        final String documentId = String.format("%s-%d", log.getDeviceId(), DateTime.now().getMillis());

        fields.put("device_id", log.getDeviceId());
        fields.put("text", log.getText());
        fields.put("ts", String.valueOf(log.getUnixTime()));

        categories.put("device_id", log.getDeviceId());

        try {
            index.addDocument(documentId, fields, null, categories);
        } catch (IndexDoesNotExistException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
